package com.example.temiapp;
// reach dispensary
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;

public class Activity10 extends AppCompatActivity implements OnRobotReadyListener, OnGoToLocationStatusChangedListener {
    private static final String TAG = Activity10.class.getSimpleName();
    Button enterBtn;
    Robot mRobot;
    private Handler handler;
    private String gotoStatus = "";
    private boolean isTimerRunning = false;
    private final int TIME_PERIOD = 2000; // in 2 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_10);
        mRobot = Robot.getInstance();
        enterBtn = (Button) findViewById(R.id.button_tenth);
        handler = new Handler();
        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity10.this, Activity5.class);
                startActivity(intent);
            }
        });

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
        Log.i(TAG, "location status: "+status);
        gotoStatus = status;
        if(status.equals("obstacle detected")){
            if(!isTimerRunning){
                startTimer();
            }
        }
        if (descriptionId == 500) {
            mRobot.speak(TtsRequest.create("已到達領藥處，請抽號碼牌領藥", false, TtsRequest.Language.ZH_TW, false));
        }
    }
    private void startTimer(){
        isTimerRunning = true;
        Log.i(TAG, "Start 5-second-timer");
        handler.postDelayed(new Runnable(){
            public void run(){
                isObstacleStillDetected();
            }
        }, TIME_PERIOD);
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
}