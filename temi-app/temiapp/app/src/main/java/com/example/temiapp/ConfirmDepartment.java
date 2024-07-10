package com.example.temiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import java.lang.String;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.TtsRequest;

public class ConfirmDepartment extends AppCompatActivity implements OnRobotReadyListener{
    private static final String TAG = ConfirmDepartment.class.getSimpleName();
    Robot mRobot;
    TextView department1,department2,department3,department4,ragtext;
    Button confirmBtn, backBtn,rag1Btn,rag2Btn,rag3Btn,rag4Btn;
    String rag_ans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_department);

        confirmBtn = (Button) findViewById(R.id.confirmBtn);
        backBtn = (Button) findViewById(R.id.backBtn);
        rag1Btn = (Button) findViewById(R.id.rag1Btn);
        rag2Btn = (Button) findViewById(R.id.rag2Btn);
        rag3Btn = (Button) findViewById(R.id.rag3Btn);
        rag4Btn = (Button) findViewById(R.id.rag4Btn);
        department1 = (TextView) findViewById(R.id.department1);
        department2 = (TextView) findViewById(R.id.department2);
        department3 = (TextView) findViewById(R.id.department3);
        department4 = (TextView) findViewById(R.id.department4);
        ragtext = (TextView) findViewById(R.id.ragtext);
        Bundle bundle = this.getIntent().getExtras();

        String destination = (String)bundle.getString("destination");
        String[] rag_separated = new String[4];
        rag_separated = destination.split("、");
        for(int i = 0;i < rag_separated.length;i++) {
            if(i == 0) department1.setText((rag_separated[0].trim()));
            if(i == 1) department2.setText((rag_separated[1]));
            if(i == 2) department3.setText((rag_separated[2]));
            if(i == 3) department4.setText((rag_separated[3]));
        }
//

        mRobot = Robot.getInstance();
        mRobot.speak(TtsRequest.create("請按下想要掛號的科別", false, TtsRequest.Language.ZH_TW, false));
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(ConfirmDepartment.this, Activity3.class);
//                Bundle bundle = new Bundle();
                bundle.putString("destination",rag_ans);
//                bundle.putString("symptom","拉肚子");
//                bundle.putString("symptom",rag_ans);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(ConfirmDepartment.this, EnterSymptomWhisper.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        rag1Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                rag_ans = department1.getText().toString();
                ragtext.setText("已選擇 : " + department1.getText().toString());
            }
        });
        rag2Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                rag_ans = department2.getText().toString();
                ragtext.setText("已選擇 : " + department2.getText().toString());
            }
        });
        rag3Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                rag_ans = department3.getText().toString();
                ragtext.setText("已選擇 : " + department3.getText().toString());
            }
        });
        rag4Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                rag_ans = department4.getText().toString();
                ragtext.setText("已選擇 : " + department4.getText().toString());
            }
        });
//        ragtext.setText(rag_ans);

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