package com.catenai.hotelos.legacy.data.repo;

import com.catenai.hotelos.legacy.data.local.PrefsStore;
import com.catenai.hotelos.legacy.data.model.DeviceSessionResponse;
import com.catenai.hotelos.legacy.data.net.ApiClient;
import com.catenai.hotelos.legacy.data.net.ApiException;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Response;

public class SessionRepository {
    private static final String BIND_STATUS_ACTIVE = "ACTIVE";
    private static final String BIND_STATUS_ERROR = "ERROR";

    private final SessionService sessionService;
    private final PrefsStore prefsStore;

    public SessionRepository(ApiClient apiClient, PrefsStore prefsStore) {
        this(new HttpSessionService(apiClient), prefsStore);
    }

    public SessionRepository(SessionService sessionService, PrefsStore prefsStore) {
        this.sessionService = sessionService;
        this.prefsStore = prefsStore;
    }

    public SessionValidationResult validateCurrentSession() {
        PrefsStore.StoredSession storedSession = prefsStore.readSession();
        String deviceToken = storedSession.getDeviceToken();
        if (deviceToken.isEmpty()) {
            return unauthorized(BIND_STATUS_ERROR);
        }

        try {
            DeviceSessionResponse response = sessionService.fetch(deviceToken);
            if (response == null || !response.isSuccess() || response.getData() == null) {
                return unauthorized(BIND_STATUS_ERROR);
            }

            String bindStatus = normalize(response.getData().getBindStatus());
            if (!BIND_STATUS_ACTIVE.equals(bindStatus)) {
                return unauthorized(bindStatus);
            }

            prefsStore.saveAuthorizedSession(
                    deviceToken,
                    response.getData().getHotelId(),
                    response.getData().getDeviceId(),
                    response.getData().getName()
            );
            return new SessionValidationResult(true, bindStatus);
        } catch (IOException ignored) {
            return unauthorized(BIND_STATUS_ERROR);
        }
    }

    private SessionValidationResult unauthorized(String bindStatus) {
        prefsStore.clearSession();
        return new SessionValidationResult(false, bindStatus.isEmpty() ? BIND_STATUS_ERROR : bindStatus);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public interface SessionService {
        DeviceSessionResponse fetch(String deviceToken) throws IOException;
    }

    public static final class SessionValidationResult {
        private final boolean authorized;
        private final String bindStatus;

        public SessionValidationResult(boolean authorized, String bindStatus) {
            this.authorized = authorized;
            this.bindStatus = bindStatus;
        }

        public boolean isAuthorized() {
            return authorized;
        }

        public String getBindStatus() {
            return bindStatus;
        }
    }

    private static final class HttpSessionService implements SessionService {
        private final ApiClient apiClient;
        private final Gson gson = new Gson();

        private HttpSessionService(ApiClient apiClient) {
            this.apiClient = apiClient;
        }

        @Override
        public DeviceSessionResponse fetch(String deviceToken) throws IOException {
            Response response = apiClient.getHttpClient()
                    .newCall(apiClient.buildGet("/api/device/session", deviceToken))
                    .execute();

            try {
                String body = response.body() == null ? "" : response.body().string();
                if (!response.isSuccessful()) {
                    throw new ApiException(response.code(), "Session request failed", body);
                }
                DeviceSessionResponse parsed = gson.fromJson(body, DeviceSessionResponse.class);
                if (parsed == null) {
                    throw new IOException("Unable to parse device session response");
                }
                return parsed;
            } finally {
                response.close();
            }
        }
    }
}
