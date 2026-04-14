package com.catenai.hotelos.legacy.data.model;

public class BindActivateRequest {
    private String bindCode;
    private String deviceFingerprint;
    private DeviceInfo deviceInfo;

    public BindActivateRequest() {
    }

    public BindActivateRequest(String bindCode, String deviceFingerprint, DeviceInfo deviceInfo) {
        this.bindCode = bindCode;
        this.deviceFingerprint = deviceFingerprint;
        this.deviceInfo = deviceInfo;
    }

    public String getBindCode() {
        return bindCode;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }
}
