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
import com.robotemi.sdk.listeners.OnMovementStatusChangedListener;
import com.robotemi.sdk.TtsRequest;

public class Activity4 extends AppCompatActivity implements OnRobotReadyListener, OnGoToLocationStatusChangedListener, OnMovementStatusChangedListener{
    private static final String TAG = Activity4.class.getSimpleName();
    Button enterBtn, noChatBtn;
    Robot mRobot;
    private Handler handler;
    private int obstacleCount = 0;
    private boolean isTimerRunning = false;
    private final int TIME_PERIOD = 30000; // in 3 second
    private final int OBSTACLE_THRESHOLD = 3; // 3 times

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4);
        mRobot = Robot.getInstance();
        enterBtn = (Button) findViewById(R.id.button_fourth);
        noChatBtn = (Button) findViewById(R.id.button_nochat);
        Bundle bundle = this.getIntent().getExtras();
        String name = (String)bundle.getString("name");
        handler = new Handler();

        mRobot.addOnMovementStatusChangedListener(this::onMovementStatusChanged);

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
    public void onMovementStatusChanged(String type, String status){
        if(status.equals("obstacle detected")){
            obstacleCount++;

            if(!isTimerRunning){
                startTimer();
            }
            if(obstacleCount >= OBSTACLE_THRESHOLD){
                mRobot.speak(TtsRequest.create("有人要通過，請讓路，謝謝", false, TtsRequest.Language.ZH_TW));
                resetObstacleCount();
            }
        }
    }

    private void startTimer(){
        isTimerRunning = true;

        handler.postDelayed(new Runnable(){
            public void run(){
                resetObstacleCount();
            }
        }, TIME_PERIOD);
    }

    private void resetObstacleCount(){
        obstacleCount = 0;
        isTimerRunning = false;
    }

    protected void onDestroy(){
        super.onDestroy();
        mRobot.removeOnMovementStatusChangedListener(this::onMovementStatusChanged);
        handler.removeCallbacksAndMessages(null);
    }

}