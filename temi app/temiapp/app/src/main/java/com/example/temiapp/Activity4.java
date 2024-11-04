package com.example.temiapp;
// reach comforting room
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.AsyncTask;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

import com.robotemi.sdk.NlpResult;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.SttLanguage;
import com.robotemi.sdk.constants.SdkConstants;
import com.robotemi.sdk.listeners.*;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.navigation.model.SpeedLevel;

public class Activity4 extends AppCompatActivity implements OnRobotReadyListener, OnGoToLocationStatusChangedListener, Robot.AsrListener {
    private static final String TAG = Activity4.class.getSimpleName();
    Button enterBtn, noChatBtn;
    Robot mRobot;
    private Handler handler;
    private String gotoStatus = "";
    private boolean isTimerRunning = false;
    private final int DETECT_OBSTACLES_PERIOD = 2000; // in 2 second
    private final int SPEED_PERIOD = 5000;
    private String destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4);

        mRobot = Robot.getInstance();
        setAsrLanguages();

        enterBtn = (Button) findViewById(R.id.button_fourth);
        noChatBtn = (Button) findViewById(R.id.button_nochat);
        Bundle bundle = this.getIntent().getExtras();
        String name = (String)bundle.getString("name");
        destination = (String)bundle.getString("destination");
        handler = new Handler();

        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // String message = name+" has arrived.";
                // new SendMessageTask().execute(message);
                Intent intent = new Intent(Activity4.this, ChatRecord.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        noChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(Activity4.this, Activity6.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
    private class SendMessageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String ip = params[0];
            String message = params[1];
            return sendMessageToServer(ip, message);
        }
    }

    private String sendMessageToServer(String ip, String message) {
        try (Socket socket = new Socket(ip, 5031);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send message to the server
            out.println(message);

            // Receive response from the server
            return "Success";

        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();

        // Add robot event listeners
        Log.i(TAG, "Adding robot event listeners");
        mRobot.addOnRobotReadyListener(this);
        mRobot.addOnGoToLocationStatusChangedListener(this);
        mRobot.addAsrListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove robot event listeners
        mRobot.removeOnRobotReadyListener(this);
        mRobot.removeOnGoToLocationStatusChangedListener(this);
        mRobot.removeAsrListener(this);
    }

    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            Log.i(TAG, "Robot is ready");
            mRobot.hideTopBar(); // hide temi's ActionBar when skill is active
        }
    }

    @Override
    public void onGoToLocationStatusChanged(String location, String status, int descriptionId, String description) {
        Log.i(TAG, "location status: "+status);
        gotoStatus = status;
        if(status.equals("obstacle detected")){
            if(!isTimerRunning){
                startTimer();
            }
        }
        if (descriptionId == 500) {
            Bundle bundle = this.getIntent().getExtras();
            String name = (String)bundle.getString("name");
            String destination = (String)bundle.getString("destination");
            String ip = "";
            ip = "192.168.56.1";
//            if(destination.equals("骨科診間")){
//                ip = "192.168.56.1";
//            }
//            else{
//                ip = "192.168.56.1";
//            }
            String message = name+" has arrived.";
            new SendMessageTask().execute(ip, message);
            mRobot.speak(TtsRequest.create("已經到達診間，請在外面稍坐等候醫護人員叫號。現在還有一些時間，需不需要陪你聊天呢?需要的話可以按下開始聊天按鈕開始聊天", false, TtsRequest.Language.ZH_TW, false));
        }
    }

    @Override
    public void onAsrResult(String asrResult, SttLanguage sttLanguage){
        Log.i(TAG, "asrResult = " + asrResult + ", sttLanguage = " + sttLanguage);
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData == null){
                Log.i(TAG, "appInfo.metaData == null");
                return;
            }
            if (!mRobot.isSelectedKioskApp()){
                Log.i(TAG, "not in kiosk mode");
                return;
            }
            if (!appInfo.metaData.getBoolean(SdkConstants.METADATA_OVERRIDE_NLU)){
                Log.i(TAG, "!appInfo.metaData.getBoolean(SdkConstants.METADATA_OVERRIDE_NLU)");
                return;
            }
        } catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return;
        }

        if(asrResult.contains("快")){
            Log.i(TAG, "speedup");
            mRobot.stopMovement();
            mRobot.goTo(destination, false, null, SpeedLevel.HIGH);
            mRobot.speak(TtsRequest.create("加速中", false, TtsRequest.Language.ZH_TW));
        }
        else
            mRobot.speak(TtsRequest.create("維持原速", false, TtsRequest.Language.ZH_TW));
    }


    private void startTimer(){
        isTimerRunning = true;
        Log.i(TAG, "Start 5-second-timer");
        handler.postDelayed(new Runnable(){
            public void run(){
                isObstacleStillDetected();
            }
        }, DETECT_OBSTACLES_PERIOD);
    }

    private void isObstacleStillDetected(){
        if(gotoStatus.equals("obstacle detected")){
            Log.i(TAG, "obstacle still detected after 5 seconds");
            mRobot.speak(TtsRequest.create("有人要通過，請讓路，謝謝", false, TtsRequest.Language.ZH_TW));
            startTimer();
        }
        else{
            Log.i(TAG, "obstacle removed");
            isTimerRunning = false;
        }
    }

    private void setAsrLanguages(){
        if(!mRobot.isSelectedKioskApp()){
            return;
        }
        int result = mRobot.setAsrLanguages(
                Arrays.asList(SttLanguage.SYSTEM, SttLanguage.ZH_TW)
        );
        Log.i(TAG, "Asr languages: "+result);
    }

    protected void onDestroy(){
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

}