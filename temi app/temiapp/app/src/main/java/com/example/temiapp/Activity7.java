package com.example.temiapp;
// choose the next department
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.TtsRequest;
public class Activity7 extends AppCompatActivity implements OnRobotReadyListener {

    private static final String TAG = Activity7.class.getSimpleName();
    Robot mRobot;
    Button bedroomBtn,kitchenBtn,entranceBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_7);
        bedroomBtn = (Button) findViewById(R.id.bedroomBtn);
        kitchenBtn = (Button) findViewById(R.id.kitchenBtn);
        entranceBtn = (Button) findViewById(R.id.entranceBtn);

        Bundle bundle = this.getIntent().getExtras();
        bundle.remove("destination");

        mRobot = Robot.getInstance();
        mRobot.speak(TtsRequest.create("請設定下一個目的地", false, TtsRequest.Language.ZH_TW, false));

        bedroomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity7.this, Activity8.class);
                bundle.putString("destination", "骨科診間");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        kitchenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity7.this, Activity8.class);
                bundle.putString("destination", "腸胃科診間");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        entranceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity7.this, Activity8.class);
                bundle.putString("destination", "領藥處");
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