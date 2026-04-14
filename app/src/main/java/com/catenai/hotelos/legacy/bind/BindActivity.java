package com.catenai.hotelos.legacy.bind;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.catenai.hotelos.legacy.App;
import com.catenai.hotelos.legacy.R;
import com.catenai.hotelos.legacy.SplashActivity;
import com.catenai.hotelos.legacy.data.local.PrefsStore;
import com.catenai.hotelos.legacy.data.model.BindActivateResponse;
import com.catenai.hotelos.legacy.data.net.ApiException;
import com.catenai.hotelos.legacy.data.repo.AuthRepository;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BindActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private EditText etBindCode;
    private Button btnSubmit;
    private TextView tvStatus;

    private AuthRepository authRepository;
    private PrefsStore prefsStore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind);

        App app = (App) getApplication();
        prefsStore = new PrefsStore(this);
        authRepository = new AuthRepository(app.getApiClient(), app.getDeviceInfoProvider(), prefsStore);

        etBindCode = findViewById(R.id.etBindCode);
        btnSubmit = findViewById(R.id.btnSubmitBind);
        tvStatus = findViewById(R.id.tvBindStatus);

        btnSubmit.setOnClickListener(v -> submitBindCode());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }

    private void submitBindCode() {
        final String bindCode = etBindCode.getText() == null ? "" : etBindCode.getText().toString().trim();
        if (!BindCodeValidator.isValid(bindCode)) {
            showStatus(getString(R.string.bind_error_invalid_code));
            return;
        }

        setSubmitting(true);
        showStatus(getString(R.string.bind_status_submitting));

        executorService.execute(() -> {
            try {
                BindActivateResponse response = authRepository.bindActivate(bindCode);
                runOnUiThread(() -> {
                    showStatus(resolveSuccessMessage(response.getStatus()));
                    startActivity(new Intent(BindActivity.this, SplashActivity.class));
                    finish();
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    showStatus(resolveErrorMessage(e));
                    setSubmitting(false);
                });
            }
        });
    }

    private void setSubmitting(boolean submitting) {
        btnSubmit.setEnabled(!submitting);
        etBindCode.setEnabled(!submitting);
    }

    private void showStatus(String message) {
        tvStatus.setText(message);
    }

    private String resolveSuccessMessage(String status) {
        if ("ALREADY_ACTIVATED".equals(status)) {
            return getString(R.string.bind_status_already_activated);
        }
        return getString(R.string.bind_status_success);
    }

    private String resolveErrorMessage(IOException error) {
        if (error instanceof ApiException) {
            ApiException apiException = (ApiException) error;
            if (apiException.getStatusCode() == 400) {
                return getString(R.string.bind_error_expired_code);
            }
            if (apiException.getStatusCode() == 409) {
                return getString(R.string.bind_error_conflict);
            }
        }
        return getString(R.string.bind_error_generic);
    }
}
