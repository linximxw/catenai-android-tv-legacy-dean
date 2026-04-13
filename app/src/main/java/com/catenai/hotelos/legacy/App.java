package com.catenai.hotelos.legacy;

import android.app.Application;

import com.catenai.hotelos.legacy.data.net.ApiClient;
import com.catenai.hotelos.legacy.device.DeviceInfoProvider;

public class App extends Application {
    private DeviceInfoProvider deviceInfoProvider;
    private ApiClient apiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        deviceInfoProvider = new DeviceInfoProvider(this);
        apiClient = new ApiClient(this, deviceInfoProvider);
    }

    public DeviceInfoProvider getDeviceInfoProvider() {
        return deviceInfoProvider;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }
}
