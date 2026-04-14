package com.catenai.hotelos.legacy.data.model;

public class DeviceInfo {
    private String macAddress;
    private String model;
    private String vendor;
    private String androidVersion;
    private int sdkVersion;

    public DeviceInfo() {
    }

    public DeviceInfo(String macAddress, String model, String vendor, String androidVersion, int sdkVersion) {
        this.macAddress = macAddress;
        this.model = model;
        this.vendor = vendor;
        this.androidVersion = androidVersion;
        this.sdkVersion = sdkVersion;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getModel() {
        return model;
    }

    public String getVendor() {
        return vendor;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public int getSdkVersion() {
        return sdkVersion;
    }
}
