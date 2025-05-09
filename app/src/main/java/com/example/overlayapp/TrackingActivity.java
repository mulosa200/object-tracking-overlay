package com.example.overlayapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class TrackingActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(new TrackingView(this));
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
