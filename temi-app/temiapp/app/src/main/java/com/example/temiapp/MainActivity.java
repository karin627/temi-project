package com.example.temiapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.os.Bundle;
import java.util.Map;
import java.util.HashMap;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.TtsRequest;

public class MainActivity extends AppCompatActivity implements OnRobotReadyListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    EditText inputText;
    Button enterBtn, backBtn;
    Robot mRobot;

    public String getName(String id){
        Map<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("110511223", "齊心");
        nameMap.put("110511130", "果琳");
        return nameMap.get(id);
    }

    public String getSymptom(String id){
        Map<String, String> sympMap = new HashMap<String, String>();
        sympMap.put("110511223", "None");
        sympMap.put("110511130", "骨折");
        return sympMap.get(id);
    }

    public String getDestination(String symp){
        Map<String, String> destMap = new HashMap<String, String>();
        destMap.put("拉肚子", "腸胃科");
        destMap.put("骨折", "骨科");
        return destMap.get(symp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRobot = Robot.getInstance();
        mRobot.speak(TtsRequest.create("請輸入您的健保卡號", false, TtsRequest.Language.ZH_TW, false));
        inputText = (EditText) findViewById(R.id.studentID);
        enterBtn = (Button) findViewById(R.id.enterBtn_main);
        backBtn = (Button) findViewById(R.id.backBtn_main);

        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String id = inputText.getText().toString();
                String name = getName(id);
                String symp = getSymptom(id);
                if(symp != "None"){
                    Intent intent = new Intent(MainActivity.this, Activity3.class); ///已掛號
                    Bundle bundle = new Bundle();
                    bundle.putString("id", id);
                    bundle.putString("name", name);
                    bundle.putString("symptom", symp);
                    bundle.putString("destination", getDestination(symp));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

                else{
                    Intent intent = new Intent(MainActivity.this, EnterSymptomWhisper.class); ///未掛號
                    Bundle bundle = new Bundle();
                    bundle.putString("id", id);
                    bundle.putString("name", name);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Intro.class);
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