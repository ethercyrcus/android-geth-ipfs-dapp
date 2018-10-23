package com.example.cameron.ethereumtest1.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.cameron.ethereumtest1.R;
import com.example.cameron.ethereumtest1.util.PrefUtils;

public class IntroductionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_introduction);
        PrefUtils.updateHasUserSeenIntroScreen(this);
    }

    public void proceed(View view) {
        finish();
    }
}
