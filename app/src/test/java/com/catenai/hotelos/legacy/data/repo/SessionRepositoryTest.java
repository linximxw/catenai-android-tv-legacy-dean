package com.catenai.hotelos.legacy.data.repo;

import com.catenai.hotelos.legacy.data.local.PrefsStore;
import com.catenai.hotelos.legacy.data.model.DeviceSessionResponse;

import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SessionRepositoryTest {

    @Test
    public void validateCurrentSession_keepsTokenWhenBindStatusIsActive() throws Exception {
        PrefsStore prefsStore = new PrefsStore(new InMemoryStringStore());
        prefsStore.saveAuthorizedSession("token-123", "hotel-old", "device-old", "TV-OLD");
        SessionRepository repository = new SessionRepository(
                new FakeSessionService(
                        new DeviceSessionResponse(
                                true,
                                new DeviceSessionResponse.SessionData(
                                        "device-88",
                                        "TV-AABBCC",
                                        "hotel-01",
                                        "测试大酒店",
                                        "ACTIVE",
                                        "2027-04-13T00:00:00.000Z"
                                )
                        )
                ),
                prefsStore
        );

        SessionRepository.SessionValidationResult result = repository.validateCurrentSession();

        assertTrue(result.isAuthorized());
        assertEquals("ACTIVE", result.getBindStatus());
        assertEquals("token-123", prefsStore.readSession().getDeviceToken());
        assertEquals("hotel-01", prefsStore.readSession().getHotelId());
        assertEquals("device-88", prefsStore.readSession().getDeviceId());
        assertEquals("TV-AABBCC", prefsStore.readSession().getDeviceName());
    }

    @Test
    public void validateCurrentSession_clearsTokenWhenBindStatusIsInactive() throws Exception {
        PrefsStore prefsStore = new PrefsStore(new InMemoryStringStore());
        prefsStore.saveAuthorizedSession("token-123", "hotel-old", "device-old", "TV-OLD");
        SessionRepository repository = new SessionRepository(
                new FakeSessionService(
                        new DeviceSessionResponse(
                                true,
                                new DeviceSessionResponse.SessionData(
                                        "device-88",
                                        "TV-AABBCC",
                                        "hotel-01",
                                        "测试大酒店",
                                        "UNBOUND",
                                        "2027-04-13T00:00:00.000Z"
                                )
                        )
                ),
                prefsStore
        );

        SessionRepository.SessionValidationResult result = repository.validateCurrentSession();

        assertFalse(result.isAuthorized());
        assertEquals("UNBOUND", result.getBindStatus());
        assertFalse(prefsStore.hasToken());
    }

    @Test
    public void validateCurrentSession_clearsTokenWhenRequestFails() {
        PrefsStore prefsStore = new PrefsStore(new InMemoryStringStore());
        prefsStore.saveAuthorizedSession("token-123", "hotel-old", "device-old", "TV-OLD");
        SessionRepository repository = new SessionRepository(
                new ThrowingSessionService(new IOException("network down")),
                prefsStore
        );

        SessionRepository.SessionValidationResult result = repository.validateCurrentSession();

        assertFalse(result.isAuthorized());
        assertEquals("ERROR", result.getBindStatus());
        assertFalse(prefsStore.hasToken());
    }

    private static final class FakeSessionService implements SessionRepository.SessionService {
        private final DeviceSessionResponse response;

        private FakeSessionService(DeviceSessionResponse response) {
            this.response = response;
        }

        @Override
        public DeviceSessionResponse fetch(String deviceToken) {
            return response;
        }
    }

    private static final class ThrowingSessionService implements SessionRepository.SessionService {
        private final IOException exception;

        private ThrowingSessionService(IOException exception) {
            this.exception = exception;
        }

        @Override
        public DeviceSessionResponse fetch(String deviceToken) throws IOException {
            throw exception;
        }
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
