package com.catenai.hotelos.legacy.data.repo;

import com.catenai.hotelos.legacy.data.local.PrefsStore;
import com.catenai.hotelos.legacy.data.model.BindActivateRequest;
import com.catenai.hotelos.legacy.data.model.BindActivateResponse;
import com.catenai.hotelos.legacy.data.model.DeviceInfo;
import com.catenai.hotelos.legacy.data.net.ApiClient;
import com.catenai.hotelos.legacy.data.net.ApiException;
import com.catenai.hotelos.legacy.device.DeviceInfoProvider;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Response;

public class AuthRepository {
    private static final String STATUS_BIND_ACTIVATED = "BIND_ACTIVATED";
    private static final String STATUS_ALREADY_ACTIVATED = "ALREADY_ACTIVATED";

    private final BindActivationService bindActivationService;
    private final DeviceProfile deviceProfile;
    private final PrefsStore prefsStore;

    public AuthRepository(ApiClient apiClient, DeviceInfoProvider deviceInfoProvider, PrefsStore prefsStore) {
        this(new HttpBindActivationService(apiClient), new DeviceInfoProfile(deviceInfoProvider), prefsStore);
    }

    public AuthRepository(BindActivationService bindActivationService, DeviceProfile deviceProfile, PrefsStore prefsStore) {
        this.bindActivationService = bindActivationService;
        this.deviceProfile = deviceProfile;
        this.prefsStore = prefsStore;
    }

    public BindActivateResponse bindActivate(String bindCode) throws IOException {
        BindActivateRequest request = new BindActivateRequest(
                normalize(bindCode),
                deviceProfile.getDeviceFingerprint(),
                deviceProfile.getBindDeviceInfo()
        );

        BindActivateResponse response = bindActivationService.activate(request);
        if (response == null) {
            throw new IOException("Bind activate response is empty");
        }

        if (!isSuccessful(response)) {
            throw new ApiException(response.getCode(), "Bind activate failed: " + response.getStatus(), "");
        }

        persistAuthorizedSession(response.getData());
        return response;
    }

    private boolean isSuccessful(BindActivateResponse response) {
        if (response.getCode() != 200) {
            return false;
        }
        String status = normalize(response.getStatus());
        return STATUS_BIND_ACTIVATED.equals(status) || STATUS_ALREADY_ACTIVATED.equals(status);
    }

    private void persistAuthorizedSession(BindActivateResponse.BindData data) throws IOException {
        if (data == null) {
            throw new IOException("Bind activate data is empty");
        }
        prefsStore.saveAuthorizedSession(data.getToken(), data.getHotelId(), data.getDeviceId(), data.getName());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public interface BindActivationService {
        BindActivateResponse activate(BindActivateRequest request) throws IOException;
    }

    public interface DeviceProfile {
        String getDeviceFingerprint();

        DeviceInfo getBindDeviceInfo();
    }

    private static final class DeviceInfoProfile implements DeviceProfile {
        private final DeviceInfoProvider deviceInfoProvider;

        private DeviceInfoProfile(DeviceInfoProvider deviceInfoProvider) {
            this.deviceInfoProvider = deviceInfoProvider;
        }

        @Override
        public String getDeviceFingerprint() {
            return deviceInfoProvider.getDeviceFingerprint();
        }

        @Override
        public DeviceInfo getBindDeviceInfo() {
            return deviceInfoProvider.getBindDeviceInfo();
        }
    }

    private static final class HttpBindActivationService implements BindActivationService {
        private final ApiClient apiClient;
        private final Gson gson = new Gson();

        private HttpBindActivationService(ApiClient apiClient) {
            this.apiClient = apiClient;
        }

        @Override
        public BindActivateResponse activate(BindActivateRequest request) throws IOException {
            Response response = apiClient.getHttpClient()
                    .newCall(apiClient.buildPostJson("/api/device/bind/activate", "", request))
                    .execute();

            try {
                String body = response.body() == null ? "" : response.body().string();
                if (!response.isSuccessful()) {
                    throw new ApiException(response.code(), "Bind activate http failure", body);
                }
                BindActivateResponse parsed = gson.fromJson(body, BindActivateResponse.class);
                if (parsed == null) {
                    throw new IOException("Unable to parse bind activate response");
                }
                return parsed;
            } finally {
                response.close();
            }
        }
    }
}
