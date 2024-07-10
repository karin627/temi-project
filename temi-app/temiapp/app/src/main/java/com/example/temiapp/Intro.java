package com.example.temiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.TtsRequest;


public class Intro extends AppCompatActivity  implements OnRobotReadyListener {
    private static final String TAG = Intro.class.getSimpleName();
    Button startBtn, backBtn;
    Robot mRobot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        mRobot = Robot.getInstance();

        startBtn = (Button) findViewById(R.id.startBtn);
        backBtn = (Button) findViewById(R.id.backBtn_intro);

        mRobot.speak(TtsRequest.create("嗨我是temi今天負責引導您的看診流程", false, TtsRequest.Language.ZH_TW, false));

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                Intent intent = new Intent(Intro.this, MainActivity.class);
                startActivity(intent);

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(Intro.this, FirstPage.class);
                startActivity(intent);
            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();

        // Add robot event listeners
        mRobot.addOnRobotReadyListener(this);
//        mRobot.addOnCurrentPositionChangedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove robot event listeners
        mRobot.removeOnRobotReadyListener(this);
//        mRobot.removeOnCurrentPositionChangedListener(this);
    }

    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            Log.i(TAG, "Robot is ready");
            mRobot.hideTopBar(); // hide temi's ActionBar when skill is active
        }
    }

}