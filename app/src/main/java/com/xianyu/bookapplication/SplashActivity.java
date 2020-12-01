package com.xianyu.bookapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

public class SplashActivity extends Activity {
    protected Handler mHandler = new Handler(Looper.myLooper());

    TextView timeText;
    TextView skipText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        timeText = findViewById(R.id.timeText);
        skipText = findViewById(R.id.skipText);
        SkipLoading();
        TimeLoading();
    }

    private void SkipLoading() {
        skipText.setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, BookListActivity.class));
            this.finish();
            mHandler.removeCallbacksAndMessages(null);
        });
    }

    private void TimeLoading() {
        timeText.postDelayed(() -> timeText.setText("2秒后进入主页"), 1000);
        timeText.postDelayed(() -> timeText.setText("1秒后进入主页"), 2000);
        timeText.postDelayed(() -> timeText.setText("0秒后进入主页"), 3000);
        mHandler.postDelayed(() -> startActivity(new Intent(SplashActivity.this, BookListActivity.class)), 3100);
        mHandler.postDelayed(SplashActivity.this::finish, 3100);
    }
}