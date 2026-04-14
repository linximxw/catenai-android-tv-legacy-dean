package com.catenai.hotelos.legacy;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.catenai.hotelos.legacy.bind.BindActivity;
import com.catenai.hotelos.legacy.data.local.PrefsStore;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new PrefsStore(this).hasToken()) {
            startActivity(new Intent(this, BindActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_splash);
    }
}
