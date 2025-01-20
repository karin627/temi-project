package com.example.temiapp;
// reach comforting room
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.navigation.model.SpeedLevel;

public class Activity4 extends AppCompatActivity implements OnRobotReadyListener, OnGoToLocationStatusChangedListener{
    private static final String TAG = Activity4.class.getSimpleName();
    Button tooFastBtn, moderateBtn, tooSlowBtn;
    Robot mRobot;
    private Handler handler;
    private String gotoStatus = "";
    private boolean isTimerRunning = false;
    private final int TIME_PERIOD = 2000; // in 2 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4);
        mRobot = Robot.getInstance();
        tooFastBtn = (Button) findViewById(R.id.button_tooFast);
        moderateBtn = (Button) findViewById(R.id.button_moderate);
        tooSlowBtn = (Button) findViewById(R.id.button_tooSlow);
        Bundle bundle = this.getIntent().getExtras();
        String name = (String)bundle.getString("name");
        handler = new Handler();

        tooFastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // String message = name+" has arrived.";
                // new SendMessageTask().execute(message);
                Log.i(TAG, "the speed is too fast.");
                SpeedLevel currentSpeed = GlobalVariable.getInstance().getGlobalSpeedLevel();
                SpeedLevel newSpeed;
                if(currentSpeed == SpeedLevel.HIGH){
                    newSpeed = SpeedLevel.MEDIUM;
                    mRobot.speak(TtsRequest.create("好的，等一下將會幫你放慢速度。", false, TtsRequest.Language.ZH_TW, false));
                }
                else if(currentSpeed == SpeedLevel.MEDIUM){
                    newSpeed = SpeedLevel.SLOW;
                    mRobot.speak(TtsRequest.create("好的，等一下將會幫你放慢速度。", false, TtsRequest.Language.ZH_TW, false));
                }
                else{
                    newSpeed = SpeedLevel.SLOW;
                    mRobot.speak(TtsRequest.create("很抱歉，現在速度已經是最慢速了，等一下將會維持原速。", false, TtsRequest.Language.ZH_TW, false));
                }
                Log.i(TAG, "current speed is " + currentSpeed.toString());
                GlobalVariable.getInstance().setGlobalSpeedLevel(newSpeed);
                newSpeed = GlobalVariable.getInstance().getGlobalSpeedLevel();
                Log.i(TAG, "new speed is " + newSpeed.toString());
                Intent intent = new Intent(Activity4.this, askChat.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        moderateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // String message = name+" has arrived.";
                // new SendMessageTask().execute(message);
                Log.i(TAG, "the speed is moderate.");
                mRobot.speak(TtsRequest.create("好的，等一下將會持續用現在的速度為你引導。", false, TtsRequest.Language.ZH_TW, false));
                Intent intent = new Intent(Activity4.this, askChat.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        tooSlowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // String message = name+" has arrived.";
                // new SendMessageTask().execute(message);
                Log.i(TAG, "the speed is too slow.");
                SpeedLevel currentSpeed = GlobalVariable.getInstance().getGlobalSpeedLevel();
                SpeedLevel newSpeed;
                if(currentSpeed == SpeedLevel.SLOW){
                    Log.i(TAG, "current speed is SLOW.");
                    newSpeed = SpeedLevel.MEDIUM;
                    mRobot.speak(TtsRequest.create("好的，等一下將會幫你加快速度。", false, TtsRequest.Language.ZH_TW, false));
                }
                else if(currentSpeed == SpeedLevel.MEDIUM){
                    Log.i(TAG, "current speed is MEDIUM.");
                    newSpeed = SpeedLevel.HIGH;
                    mRobot.speak(TtsRequest.create("好的，等一下將會幫你加快速度。", false, TtsRequest.Language.ZH_TW, false));
                }
                else{
                    Log.i(TAG, "current speed is HIGH.");
                    newSpeed = SpeedLevel.HIGH;
                    mRobot.speak(TtsRequest.create("很抱歉，現在已經是最快速了，等一夏將會維持原速。", false, TtsRequest.Language.ZH_TW, false));
                }
                GlobalVariable.getInstance().setGlobalSpeedLevel(newSpeed);
                Intent intent = new Intent(Activity4.this, askChat.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

//        enterBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v){
//                // String message = name+" has arrived.";
//                // new SendMessageTask().execute(message);
//                Intent intent = new Intent(Activity4.this, Chat.class);
//                intent.putExtras(bundle);
//                startActivity(intent);
//            }
//        });
//        noChatBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v){
//                Intent intent = new Intent(Activity4.this, Activity6.class);
//                intent.putExtras(bundle);
//                startActivity(intent);
//            }
//        });
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
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove robot event listeners
        mRobot.removeOnRobotReadyListener(this);
        mRobot.removeOnGoToLocationStatusChangedListener(this);
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
            mRobot.speak(TtsRequest.create("已經到達診間，請在外面稍坐等候醫護人員叫號。另外，你覺得剛剛的引導速度如何呢?", false, TtsRequest.Language.ZH_TW, false));
//            mRobot.speak(TtsRequest.create("已經到達診間，請在外面稍坐等候醫護人員叫號。現在還有一些時間，需不需要陪你聊天呢?需要的話可以按下開始聊天按鈕開始聊天", false, TtsRequest.Language.ZH_TW, false));
        }
    }

    private void startTimer(){
        isTimerRunning = true;
        Log.i(TAG, "Start 2-second-timer");
        handler.postDelayed(new Runnable(){
            public void run(){
                isObstacleStillDetected();
            }
        }, TIME_PERIOD);
    }

    private void isObstacleStillDetected(){
            if(gotoStatus.equals("obstacle detected")){
            Log.i(TAG, "obstacle still detected after 2 seconds");
            mRobot.speak(TtsRequest.create("有人要通過，請讓路，謝謝", false, TtsRequest.Language.ZH_TW));
            startTimer();
        }
        else{
            Log.i(TAG, "obstacle removed");
            isTimerRunning = false;
        }
    }

    protected void onDestroy(){
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

}