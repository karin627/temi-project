package com.example.temiapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.temiapp.recorder.RecorderFSM
import com.example.temiapp.recorder.RecorderManager
import com.github.liuyueyi.quick.transfer.ChineseUtils
import android.util.Log
import android.inputmethodservice.InputMethodService
import android.net.Uri
import com.google.gson.internal.bind.TreeTypeAdapter
import com.robotemi.sdk.Robot
import com.robotemi.sdk.listeners.OnRobotReadyListener
import com.robotemi.sdk.TtsRequest
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

// 200 and 201 are an arbitrary values, as long as they do not conflict with each other
private const val MICROPHONE_PERMISSION_REQUEST_CODE = 200
private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 201
private const val RECORDED_AUDIO_FILENAME = "recorded.m4a"
private const val AUDIO_MEDIA_TYPE = "audio/mp4"
private const val IME_SWITCH_OPTION_AVAILABILITY_API_LEVEL = 28
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class InputMethodService : InputMethodService() {

    fun insertText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }
}

class EnterSymptomWhisper : AppCompatActivity(), OnRobotReadyListener {
    private enum class KeyboardStatus {
        Idle,             // Ready to start recording
        Recording,       // Currently recording
        Transcribing,    // Waiting for transcription results
    }

    // variables
    private var keyboardStatus: KeyboardStatus = KeyboardStatus.Idle
    private var whisperTranscriber: WhisperTranscriber = WhisperTranscriber()
    private var recorderManager: RecorderManager? = null
    private var recorderFsm: RecorderFSM? = null
    private var recordedAudioFilename: String = ""
    private val inputMethodService = InputMethodService()
    private val TAG = EnterSymptomWhisper::class.simpleName
    private lateinit var labelStatus: TextView
    private lateinit var micButton: Button
    private lateinit var enterBtn: Button
    private lateinit var mRobot: Robot
    private lateinit var backBtn: Button
    private lateinit var symptom: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_symptom_whisper)
        enterBtn = findViewById(R.id.enterBtn)
        backBtn = findViewById(R.id.backBtn)
        var b: Bundle? = intent.extras
        mRobot = Robot.getInstance()
        mRobot.speak(TtsRequest.create("您尚未掛號，請按下錄音鍵並敘述症狀", false, TtsRequest.Language.ZH_TW, false))

        // Keyboard Status
        keyboardStatus = KeyboardStatus.Idle
        micButton = findViewById(R.id.button2)
        labelStatus = findViewById(R.id.textView)
        labelStatus.text = "按下錄音鍵，並講述您的症狀"

        checkPermissions()

        recorderManager = RecorderManager(this)
        recorderFsm = RecorderFSM(this)
        recordedAudioFilename = "${externalCacheDir?.absolutePath}/${RECORDED_AUDIO_FILENAME}"

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
                    setKeyboardStatus(KeyboardStatus.Transcribing)
                    onStartTranscription()
                }
                KeyboardStatus.Transcribing -> {
                    setKeyboardStatus(KeyboardStatus.Idle)
                }
            }
        }
        //////////////////////////////////////////////////

        enterBtn.setOnClickListener {

            // send message to python(rag system)
            if(symptom == "") {
                labelStatus.setText("請先按下錄音鍵錄下症狀")
            }else{
                Thread{
                    val serverAddress = "192.168.238.31"
                    val serverPort = 5000
                    try{
                        val socket = Socket(serverAddress, serverPort)
                        val inputStream = BufferedReader(InputStreamReader(socket.getInputStream()))
                        val outputStream = PrintWriter(socket.getOutputStream(), true)
                        outputStream.println(symptom)

                        val department = inputStream.readLine()


//                        labelStatus.setText(department)
//
                        outputStream.close()
                        inputStream.close()
                        socket.close()

                        runOnUiThread{
                            labelStatus.setText("診斷中，請稍等...")
                            val intent = Intent(this@EnterSymptomWhisper, ConfirmDepartment::class.java)
                            b?.putString("symptom", symptom)
                            b?.putString("destination", department!!)
                            intent.putExtras(b!!)
                            startActivity(intent)
                        }
                    } catch(e: Exception){
                        e.printStackTrace()
                    }
                }.start()
            }

        }
        backBtn.setOnClickListener {
            val intent = Intent(this@EnterSymptomWhisper, MainActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onStart() {
        super.onStart()
        mRobot.addOnRobotReadyListener(this)
    }

    override fun onStop() {
        super.onStop()
        mRobot.removeOnRobotReadyListener(this)
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
                labelStatus!!.setText("請按下錄音鍵，並描述您的症狀")
                micButton.setText("開始錄音")
            }
            KeyboardStatus.Recording -> {
                labelStatus!!.setText("聆聽中...")
                micButton.setText("結束錄音")
            }
            KeyboardStatus.Transcribing -> {
                labelStatus!!.setText("請稍等...")
                micButton.setText("重新錄音")
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
                labelStatus.text = transcribedText
                symptom = transcribedText!!
            },
            { exceptionMessage ->
                // Handle exception
                Log.e(TAG, exceptionMessage)
            }
        )
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
}