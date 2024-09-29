package com.example.temiapp;
// press start button to start the guide
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.navigation.model.SpeedLevel;
import com.robotemi.sdk.TtsRequest;
public class Activity8 extends AppCompatActivity implements OnRobotReadyListener{
    private static final String TAG = Activity8.class.getSimpleName();
    Robot mRobot;
    Button startBtn, returnBtn;
    String destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_8);
        mRobot = Robot.getInstance();
        startBtn = (Button) findViewById(R.id.startBtn);
        returnBtn = (Button) findViewById(R.id.returnBtn8);

        Bundle bundle = this.getIntent().getExtras();

        destination = (String)bundle.getString("destination");

        mRobot.speak(TtsRequest.create("請按下開始引導鍵前往目的地", false, TtsRequest.Language.ZH_TW, false));
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mRobot.goTo("骨科診間", false, null, SpeedLevel.SLOW);
                if(destination.equals("領藥處")){
                    mRobot.goTo("領藥處", false, null, SpeedLevel.SLOW);
                    Intent intent = new Intent(Activity8.this, Activity10.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                else{
                    mRobot.goTo("診間", false, null, SpeedLevel.SLOW);
                    Intent intent = new Intent(Activity8.this, Activity4.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        returnBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(Activity8.this, Activity3.class);
                intent.putExtras(bundle);
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