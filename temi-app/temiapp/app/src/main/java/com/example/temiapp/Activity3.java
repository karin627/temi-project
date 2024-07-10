package com.example.temiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.TtsRequest;

import java.util.HashMap;
import java.util.Map;

public class Activity3 extends AppCompatActivity implements OnRobotReadyListener{
    private static final String TAG = Activity3.class.getSimpleName();
    TextView destinationPlace, symptomPlace, namePlace;
    Button guideBtn, backBtn;
    Robot mRobot;
    public String getDepartment(String symp){
        Map<String, String> departMap = new HashMap<String, String>();
        departMap.put("拉肚子", "腸胃科");
        departMap.put("骨折", "骨科");
        return departMap.get(symp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);
        mRobot = Robot.getInstance();
        guideBtn = (Button) findViewById(R.id.enterBtn3);
        backBtn = (Button) findViewById(R.id.backBtn3);

        destinationPlace = (TextView) findViewById(R.id.destination);
        symptomPlace = (TextView) findViewById(R.id.symptom);
        namePlace = (TextView) findViewById(R.id.name) ;

        Bundle bundle = this.getIntent().getExtras();
        String symptom = (String)bundle.getString("symptom");
        String name = (String)bundle.getString("name");
        String destination = (String)bundle.getString("destination");
        mRobot.speak(TtsRequest.create("您已完成報到請跟隨我至"+destination+"診間", false, TtsRequest.Language.ZH_TW, false));

        if(destination != null){
            destinationPlace.setText("您已完成報到請跟隨我至"+destination+"診間");
            symptomPlace.setText("科別: " + destination);
//            symptomPlace.setText(getDepartment(symptom));
        }
        if(name != null){
            namePlace.setText("姓名: " + name);
        }

        guideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(Activity3.this, Activity8.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(Activity3.this, MainActivity.class);
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