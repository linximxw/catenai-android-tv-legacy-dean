package com.catenai.hotelos.legacy.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsStore {
    private static final String PREFS_NAME = "legacy_device_session";
    private static final String KEY_DEVICE_TOKEN = "device_token";
    private static final String KEY_HOTEL_ID = "hotel_id";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_DEVICE_NAME = "device_name";
    private static final String KEY_CONFIG_HASH = "config_hash";

    private final StringStore stringStore;

    public PrefsStore(Context context) {
        this(new SharedPreferencesStringStore(
                context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        ));
    }

    public PrefsStore(StringStore stringStore) {
        this.stringStore = stringStore;
    }

    public StoredSession readSession() {
        return new StoredSession(
                read(KEY_DEVICE_TOKEN),
                read(KEY_HOTEL_ID),
                read(KEY_DEVICE_ID),
                read(KEY_DEVICE_NAME),
                read(KEY_CONFIG_HASH)
        );
    }

    public void saveAuthorizedSession(String deviceToken, String hotelId, String deviceId, String deviceName) {
        stringStore.putString(KEY_DEVICE_TOKEN, normalize(deviceToken));
        stringStore.putString(KEY_HOTEL_ID, normalize(hotelId));
        stringStore.putString(KEY_DEVICE_ID, normalize(deviceId));
        stringStore.putString(KEY_DEVICE_NAME, normalize(deviceName));
    }

    public void saveConfigHash(String configHash) {
        stringStore.putString(KEY_CONFIG_HASH, normalize(configHash));
    }

    public void clearSession() {
        stringStore.remove(KEY_DEVICE_TOKEN);
        stringStore.remove(KEY_HOTEL_ID);
        stringStore.remove(KEY_DEVICE_ID);
        stringStore.remove(KEY_DEVICE_NAME);
        stringStore.remove(KEY_CONFIG_HASH);
    }

    public boolean hasToken() {
        return !read(KEY_DEVICE_TOKEN).isEmpty();
    }

    private String read(String key) {
        return normalize(stringStore.getString(key));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public interface StringStore {
        String getString(String key);

        void putString(String key, String value);

        void remove(String key);
    }

    private static final class SharedPreferencesStringStore implements StringStore {
        private final SharedPreferences sharedPreferences;

        private SharedPreferencesStringStore(SharedPreferences sharedPreferences) {
            this.sharedPreferences = sharedPreferences;
        }

        @Override
        public String getString(String key) {
            return sharedPreferences.getString(key, "");
        }

        @Override
        public void putString(String key, String value) {
            sharedPreferences.edit().putString(key, value).apply();
        }

        @Override
        public void remove(String key) {
            sharedPreferences.edit().remove(key).apply();
        }
    }

    public static final class StoredSession {
        private final String deviceToken;
        private final String hotelId;
        private final String deviceId;
        private final String deviceName;
        private final String configHash;

        private StoredSession(
                String deviceToken,
                String hotelId,
                String deviceId,
                String deviceName,
                String configHash
        ) {
            this.deviceToken = deviceToken;
            this.hotelId = hotelId;
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.configHash = configHash;
        }

        public String getDeviceToken() {
            return deviceToken;
        }

        public String getHotelId() {
            return hotelId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getConfigHash() {
            return configHash;
        }
    }
}
