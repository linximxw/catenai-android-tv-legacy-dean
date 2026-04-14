package com.catenai.hotelos.legacy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.catenai.hotelos.legacy.bind.BindActivity;
import com.catenai.hotelos.legacy.data.local.PrefsStore;
import com.catenai.hotelos.legacy.data.repo.SessionRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrefsStore prefsStore = new PrefsStore(this);
        if (!prefsStore.hasToken()) {
            startActivity(new Intent(this, BindActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_splash);
        updateSubtitle(getString(R.string.splash_status_checking));

        SessionRepository sessionRepository = new SessionRepository(((App) getApplication()).getApiClient(), prefsStore);
        executorService.execute(() -> {
            SessionRepository.SessionValidationResult result = sessionRepository.validateCurrentSession();
            runOnUiThread(() -> {
                if (!result.isAuthorized()) {
                    updateSubtitle(getString(R.string.splash_status_rebind));
                    startActivity(new Intent(SplashActivity.this, BindActivity.class));
                    finish();
                    return;
                }
                updateSubtitle(getString(R.string.splash_status_ready));
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }

    private void updateSubtitle(String message) {
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        if (tvSubtitle != null) {
            tvSubtitle.setText(message);
        }
    }
}
