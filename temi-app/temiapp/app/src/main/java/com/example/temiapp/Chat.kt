package com.example.temiapp
// start chatting
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.core.content.ContextCompat

import com.example.temiapp.recorder.RecorderFSM
import com.example.temiapp.recorder.RecorderManager
import com.github.liuyueyi.quick.transfer.ChineseUtils
import android.util.Log
import android.inputmethodservice.InputMethodService
import android.net.Uri
import com.example.temiapp.recorder.RecorderStateOutput

import com.robotemi.sdk.Robot
import com.robotemi.sdk.listeners.OnRobotReadyListener
import com.robotemi.sdk.TtsRequest

import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

import okhttp3.MediaType.Companion.toMediaTypeOrNull

val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

// 200 and 201 are an arbitrary values, as long as they do not conflict with each other
private const val MICROPHONE_PERMISSION_REQUEST_CODE = 200
private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 201
private const val RECORDED_AUDIO_FILENAME = "recorded2.m4a"
private const val AUDIO_MEDIA_TYPE = "audio/mp4"
private const val IME_SWITCH_OPTION_AVAILABILITY_API_LEVEL = 28
//val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "setting")

class InputMethodService2 : InputMethodService() {

    fun insertText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }
}

class Chat : AppCompatActivity(), OnRobotReadyListener, Robot.TtsListener {
    private enum class KeyboardStatus {
        Idle,             // Ready to start recording
        Recording,       // Currently recording
    }

    // variables
    private var keyboardStatus: KeyboardStatus = KeyboardStatus.Idle
    private var whisperTranscriber: WhisperTranscriber = WhisperTranscriber()
    private var recorderManager: RecorderManager? = null
    private var recorderFsm: RecorderFSM? = null
    private var recordedAudioFilename: String = ""
    private val inputMethodService = InputMethodService()
    private val TAG = Chat::class.simpleName
    private lateinit var labelStatus: TextView
    private lateinit var micButton: Button
    private lateinit var mRobot: Robot
    private lateinit var backBtn: Button
    private lateinit var question: String
    private lateinit var gpt_a: TextView
    private lateinit var stopchat_btn: Button
    private lateinit var messageList: MutableList<Message>
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_record)
        backBtn = findViewById(R.id.backBtn)
        var b: Bundle? = intent.extras
        messageList = mutableListOf()
        mRobot = Robot.getInstance()
        mRobot.speak(TtsRequest.create("我是temi，我們來聊天吧!今天心情如何阿?", false, TtsRequest.Language.ZH_TW, false))

        // Keyboard Status
        keyboardStatus = KeyboardStatus.Idle
        micButton = findViewById(R.id.button2)
        labelStatus = findViewById(R.id.textStatus)
        stopchat_btn = findViewById(R.id.stopBtn)
        gpt_a = findViewById(R.id.textView)
        labelStatus.text = "按下錄音鍵，開始聊天吧"

        checkPermissions()

        recorderManager = RecorderManager(this)
        recorderFsm = RecorderFSM(this)
        recordedAudioFilename = "${externalCacheDir?.absolutePath}/${RECORDED_AUDIO_FILENAME}"

        gpt_a.text = "嗨，我是temi，今天過得如何?"
//        setKeyboardStatus(KeyboardStatus.Recording)
//        onStartRecording()

        setupAmplitudeListener()

        micButton.setOnClickListener {
            // Upon button mic click...
            // Idle -> Start Recording
            // Recording -> Finish Recording (without a newline)
            // Transcribing -> Nothing (to avoid double-clicking by mistake, which starts transcribing and then immediately cancels it)
            when(keyboardStatus){
                KeyboardStatus.Idle -> {
                    setKeyboardStatus(KeyboardStatus.Recording)
                    onStartRecording()
                }
                KeyboardStatus.Recording -> {
                    setKeyboardStatus(KeyboardStatus.Idle)
                    onStartTranscription()
                }
            }
        }

        stopchat_btn.setOnClickListener {
            val intent = Intent(this@Chat, Activity6::class.java)
            b?.putString("question", question)
            intent.putExtras(b!!)
            startActivity(intent)
        }

        backBtn.setOnClickListener {
            val intent = Intent(this@Chat, Activity4::class.java)
            intent.putExtras(b!!)
            startActivity(intent)
        }

    }

    override fun onStart() {
        super.onStart()
        mRobot.addOnRobotReadyListener(this)
        mRobot.addTtsListener(this)
    }

    override fun onStop() {
        super.onStop()
        mRobot.removeOnRobotReadyListener(this)
        mRobot.removeTtsListener(this)
    }

    override fun onRobotReady(isReady: Boolean) {
        if (isReady) {
            Log.i(TAG, "Robot is ready")
            mRobot.hideTopBar()
        }
    }

    fun reset() {
        setKeyboardStatus(KeyboardStatus.Idle)
    }

    private fun transcriptionCallback(text: String?) {
        if (!text.isNullOrEmpty()) {
            inputMethodService.currentInputConnection?.commitText(ChineseUtils.s2tw(text), 1)
        }
        reset()
    }

    private fun transcriptionExceptionCallback(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // Set keyboard status
    private fun setKeyboardStatus(newStatus: KeyboardStatus){
        if(keyboardStatus == newStatus){
            return
        }
        when(newStatus) {
            KeyboardStatus.Idle -> {
                labelStatus!!.setText("")
                micButton.setText("開始錄音")
            }
            KeyboardStatus.Recording -> {
                labelStatus!!.setText("聆聽中...")
                micButton.setText("結束錄音")
            }
        }
        keyboardStatus = newStatus
    }

    private fun onStartRecording(){
        if (!recorderManager!!.allPermissionsGranted(this)) {
            labelStatus!!.setText("Permission Error")
            return
        }

        recorderManager!!.start(this, recordedAudioFilename)
        recorderFsm!!.reset()
    }

    private fun onStartTranscription(){
        recorderManager!!.stop()
        whisperTranscriber.startAsync(this,
            recordedAudioFilename,
            AUDIO_MEDIA_TYPE,
            { transcribedText ->
                // Update the TextView with the transcribed text
                question = transcribedText!!
                if(question == "") {
                    labelStatus.text = ""
                }
                else {
                    addToChat(question, Message.SENT_BY_BOT)
                    callAPI(question)
                }
            },
            { exceptionMessage ->
                // Handle exception
                Log.e(TAG, exceptionMessage)
            }
        )
    }

    private fun setupAmplitudeListener(){
        recorderManager?.onUpdateMicrophoneAmplitude = {amplitude ->
            Log.d("AmplitudeLogger", "Amplitude: $amplitude")
            when(recorderFsm?.reportAmplitude(amplitude)){
                RecorderStateOutput.Normal -> {

                }
                RecorderStateOutput.CancelRecording -> {
                    setKeyboardStatus(KeyboardStatus.Idle)
                    labelStatus.text = ""
                }
                RecorderStateOutput.FinishRecording -> {
                    setKeyboardStatus(KeyboardStatus.Idle)
                    onStartTranscription()
                }

                else -> {}
            }
        }
    }

    // The onClick event of the grant permission button.
    // Opens up the app settings panel to manually configure permissions.
    fun onRequestMicrophonePermission(view: View) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        with(intent) {
            data = Uri.fromParts("package", packageName, null)
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }

        startActivity(intent)
    }

    // Checks whether permissions are granted. If not, automatically make a request.
    private fun checkPermissions() {
        val permission_and_code = arrayOf(
            Pair(Manifest.permission.RECORD_AUDIO, MICROPHONE_PERMISSION_REQUEST_CODE),
            Pair(Manifest.permission.POST_NOTIFICATIONS, NOTIFICATION_PERMISSION_REQUEST_CODE),
        )
        for ((permission, code) in permission_and_code) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                // Shows a popup for permission request.
                // If the permission has been previously (hard-)denied, the popup will not show.
                // onRequestPermissionsResult will be called in either case.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    code
                )
            }
        }
    }
    // Handles the results of permission requests.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var msg: String;

        // Only handles requests marked with the unique code.
        if (requestCode == MICROPHONE_PERMISSION_REQUEST_CODE) {
            msg = getString(R.string.mic_permission_required)
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            msg = getString(R.string.notification_permission_required)
        } else {
            return
        }

        // All permissions should be granted.
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                return
            }
        }
    }

    private fun addToChat(message: String, sentBy: String) {
        runOnUiThread {
            messageList.add(Message(message, sentBy))
        }
    }

    override fun onTtsStatusChanged(ttsRequest: TtsRequest) {
        if(ttsRequest.status == TtsRequest.Status.COMPLETED){
            setKeyboardStatus(KeyboardStatus.Recording)
            onStartRecording()
        }
    }

    private fun addResponse(response: String) {
        runOnUiThread {
            addToChat(response, Message.SENT_BY_BOT)
            gpt_a.text = response
            mRobot.speak(TtsRequest.create(response, false, TtsRequest.Language.ZH_TW, false))
        }
    }

    private fun callAPI(question: String) {
        // okhttp
        val jsonBody = JSONObject()
        val messagesArray = JSONArray()
        val systemMessage = JSONObject()
        try {
            systemMessage.put("role", "system")
            systemMessage.put("content", "你是一位無法行動的醫院志工，負責關心一位正在等待看診的老年患者，性別不一定。請用繁體中文陪他聊天，給予心靈上的安慰。讓患者多說話，你只要說最多兩句話就好了。")
            messagesArray.put(systemMessage)

            val userMessage = JSONObject()
            userMessage.put("role", "user")
            userMessage.put("content", question)
            messagesArray.put(userMessage)

            jsonBody.put("model", "gpt-3.5-turbo")
            jsonBody.put("messages", messagesArray)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
        val body = RequestBody.create(JSON, jsonBody.toString())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer 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")
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                addResponse("Failed to load response due to " + e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        val jsonObject = JSONObject(response.body!!.string())
                        val jsonArray = jsonObject.getJSONArray("choices")
                        val choicesObject = jsonArray.getJSONObject(0)
                        val messageObject = choicesObject.getJSONObject("message")
                        val result = messageObject.getString("content")
                        addResponse(result.trim())
                    } catch (e: JSONException) {
                        throw RuntimeException(e)
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body!!.toString())
                }
            }
        })
    }
}