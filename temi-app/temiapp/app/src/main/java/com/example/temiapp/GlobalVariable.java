package com.example.temiapp;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.navigation.model.SpeedLevel;

public class GlobalVariable extends AppCompatActivity {
    private static final String TAG = Activity4.class.getSimpleName();
    private static GlobalVariable instance;
    private SpeedLevel temiSpeed = SpeedLevel.SLOW;
    private GlobalVariable(){}

    public static synchronized  GlobalVariable getInstance(){
        if(instance == null){
            instance = new GlobalVariable();
        }
        return instance;
    }

    public SpeedLevel getGlobalSpeedLevel(){
        return temiSpeed;
    }

    public void setGlobalSpeedLevel(SpeedLevel newSpeed){
        Log.i(TAG, "set speed to " + newSpeed.toString());
        this.temiSpeed = newSpeed;
    }
}
