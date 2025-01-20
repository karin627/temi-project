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

public class askChat extends AppCompatActivity implements OnRobotReadyListener, Robot.TtsListener {
    private static final String TAG = Activity4.class.getSimpleName();
    Robot mRobot;
    Button chatBtn, nochatBtn;
    private boolean asked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_chat);
        mRobot = Robot.getInstance();
        chatBtn = (Button) findViewById(R.id.button_chat);
        nochatBtn = (Button) findViewById(R.id.button_nochat);
        Bundle bundle = this.getIntent().getExtras();

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // String message = name+" has arrived.";
                // new SendMessageTask().execute(message);
                Intent intent = new Intent(askChat.this, Chat.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        nochatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(askChat.this, Activity6.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        mRobot.addOnRobotReadyListener(this);
        mRobot.addTtsListener(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
        mRobot.removeOnRobotReadyListener(this);
        mRobot.removeTtsListener(this);
    }

    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            Log.i(TAG, "Robot is ready");
            mRobot.hideTopBar();
        }
    }

    @Override
    public void onTtsStatusChanged(TtsRequest ttsRequest) {
        if(ttsRequest.getStatus() == TtsRequest.Status.COMPLETED && !asked){
            Log.i(TAG, "ttsRequest completed.");
            mRobot.speak(TtsRequest.create("現在還有一些時間，需不需要陪你聊天呢?需要的話可以按下開始聊天按鈕開始聊天", false, TtsRequest.Language.ZH_TW, false));
            asked = true;
        }
    }
}