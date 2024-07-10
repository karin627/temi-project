package com.example.temiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.TtsRequest;

public class Activity4 extends AppCompatActivity implements OnRobotReadyListener, OnGoToLocationStatusChangedListener{
    private static final String TAG = Activity4.class.getSimpleName();
    Button enterBtn, noChatBtn;
    Robot mRobot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4);
        mRobot = Robot.getInstance();
        enterBtn = (Button) findViewById(R.id.button_fourth);
        noChatBtn = (Button) findViewById(R.id.button_nochat);
        Bundle bundle = this.getIntent().getExtras();
        String name = (String)bundle.getString("name");

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
            if(destination.equals("骨科診間")){
                ip = "192.168.50.230";
            }
            else{
                ip = "192.168.50.230";
            }
            String message = name+" has arrived.";
            new SendMessageTask().execute(ip, message);
            mRobot.speak(TtsRequest.create("已經到達診間，請在外面稍坐等候醫護人員叫號。現在還有一些時間，需不需要陪你聊天呢?需要的話可以按下開始聊天按鈕開始聊天", false, TtsRequest.Language.ZH_TW, false));
        }
    }

}