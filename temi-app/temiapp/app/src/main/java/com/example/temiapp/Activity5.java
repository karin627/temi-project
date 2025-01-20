package com.example.temiapp;
// the end
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.navigation.model.SpeedLevel;
import com.robotemi.sdk.TtsRequest;

public class Activity5 extends AppCompatActivity implements OnRobotReadyListener {
    private static final String TAG = Activity5.class.getSimpleName();
    Robot mRobot;
    Button enterBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_5);
        mRobot = Robot.getInstance();
        enterBtn = (Button) findViewById(R.id.button_fifth);
        mRobot.speak(TtsRequest.create("您已看診結束", false, TtsRequest.Language.ZH_TW, false));
        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                mRobot.goTo("充電座", false, false, SpeedLevel.SLOW);
                Intent intent = new Intent(Activity5.this, FirstPage.class);
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