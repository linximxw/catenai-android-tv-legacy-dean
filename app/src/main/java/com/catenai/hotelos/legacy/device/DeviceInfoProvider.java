package com.catenai.hotelos.legacy.device;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;

import com.catenai.hotelos.legacy.BuildConfig;
import com.catenai.hotelos.legacy.data.model.DeviceInfo;

public final class DeviceInfoProvider {
    private static final String DEVICE_TYPE_ANDROID_TV = "ANDROID_TV";
    private static final String UNKNOWN_VALUE = "unknown";
    private static final String INVALID_MAC = "02:00:00:00:00:00";

    private final Context appContext;

    public DeviceInfoProvider(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public String getDeviceType() {
        return DEVICE_TYPE_ANDROID_TV;
    }

    public String getDeviceVendor() {
        return defaultIfBlank(BuildConfig.DEVICE_VENDOR, UNKNOWN_VALUE);
    }

    public String getDeviceModel() {
        return defaultIfBlank(Build.MODEL, UNKNOWN_VALUE);
    }

    public String getAppVersion() {
        String versionName = readVersionName();
        return versionName.isEmpty() ? BuildConfig.VERSION_NAME : versionName;
    }

    public String getAndroidVersion() {
        return defaultIfBlank(Build.VERSION.RELEASE, String.valueOf(Build.VERSION.SDK_INT));
    }

    public int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    public String getMacAddress() {
        try {
            WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                return "";
            }

            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo == null) {
                return "";
            }

            String macAddress = normalize(wifiInfo.getMacAddress());
            return INVALID_MAC.equals(macAddress) ? "" : macAddress;
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    public String getSerial() {
        try {
            return normalize(Build.SERIAL);
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    public String getBoard() {
        return normalize(Build.BOARD);
    }

    public String getAndroidId() {
        return normalize(Settings.Secure.getString(appContext.getContentResolver(), Settings.Secure.ANDROID_ID));
    }

    public String getScreenSize() {
        DisplayMetrics displayMetrics = appContext.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels + "x" + displayMetrics.heightPixels;
    }

    public String getDeviceFingerprint() {
        return DeviceFingerprint.generateFromParts(getMacAddress(), getSerial(), getBoard());
    }

    public DeviceInfo getBindDeviceInfo() {
        return new DeviceInfo(
                getMacAddress(),
                getDeviceModel(),
                getDeviceVendor(),
                getAndroidVersion(),
                getSdkVersion()
        );
    }

    private String readVersionName() {
        try {
            PackageInfo packageInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            return normalize(packageInfo.versionName);
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String defaultIfBlank(String value, String fallbackValue) {
        String normalizedValue = normalize(value);
        return normalizedValue.isEmpty() ? fallbackValue : normalizedValue;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
