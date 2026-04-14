package com.catenai.hotelos.legacy.data.local;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PrefsStoreTest {

    @Test
    public void saveAuthorizedSession_persistsIdentityFields() {
        PrefsStore store = new PrefsStore(new InMemoryStringStore());

        store.saveAuthorizedSession("token-123", "hotel-01", "device-88", "TV-AABBCC");

        PrefsStore.StoredSession session = store.readSession();
        assertEquals("token-123", session.getDeviceToken());
        assertEquals("hotel-01", session.getHotelId());
        assertEquals("device-88", session.getDeviceId());
        assertEquals("TV-AABBCC", session.getDeviceName());
        assertTrue(store.hasToken());
    }

    @Test
    public void saveConfigHash_persistsHashValue() {
        PrefsStore store = new PrefsStore(new InMemoryStringStore());

        store.saveConfigHash("hash-001");

        assertEquals("hash-001", store.readSession().getConfigHash());
    }

    @Test
    public void clearSession_removesAllStoredSessionFields() {
        PrefsStore store = new PrefsStore(new InMemoryStringStore());
        store.saveAuthorizedSession("token-123", "hotel-01", "device-88", "TV-AABBCC");
        store.saveConfigHash("hash-001");

        store.clearSession();

        PrefsStore.StoredSession session = store.readSession();
        assertEquals("", session.getDeviceToken());
        assertEquals("", session.getHotelId());
        assertEquals("", session.getDeviceId());
        assertEquals("", session.getDeviceName());
        assertEquals("", session.getConfigHash());
        assertFalse(store.hasToken());
    }

    private static final class InMemoryStringStore implements PrefsStore.StringStore {
        private final Map<String, String> values = new HashMap<String, String>();

        @Override
        public String getString(String key) {
            String value = values.get(key);
            return value == null ? "" : value;
        }

        @Override
        public void putString(String key, String value) {
            values.put(key, value == null ? "" : value);
        }

        @Override
        public void remove(String key) {
            values.remove(key);
        }
    }
}
