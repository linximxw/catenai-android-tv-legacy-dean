package com.catenai.hotelos.legacy.data.repo;

import com.catenai.hotelos.legacy.data.local.PrefsStore;
import com.catenai.hotelos.legacy.data.model.BindActivateRequest;
import com.catenai.hotelos.legacy.data.model.BindActivateResponse;
import com.catenai.hotelos.legacy.data.model.DeviceInfo;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AuthRepositoryTest {

    @Test
    public void bindActivate_persistsSessionWhenStatusIsBindActivated() throws Exception {
        PrefsStore prefsStore = new PrefsStore(new InMemoryStringStore());
        FakeBindActivationService bindService = new FakeBindActivationService(
                new BindActivateResponse(
                        200,
                        "BIND_ACTIVATED",
                        new BindActivateResponse.BindData(
                                "token-123",
                                "hotel-01",
                                "测试大酒店",
                                "device-88",
                                "TV-AABBCC"
                        )
                )
        );
        FakeDeviceProfile deviceProfile = new FakeDeviceProfile(
                "fingerprint-xyz",
                new DeviceInfo("AA:BB:CC:DD:EE:FF", "RK3128", "daqian", "4.4.4", 19)
        );
        AuthRepository repository = new AuthRepository(bindService, deviceProfile, prefsStore);

        BindActivateResponse response = repository.bindActivate("123456");

        assertEquals("BIND_ACTIVATED", response.getStatus());
        assertEquals("123456", bindService.lastRequest.getBindCode());
        assertEquals("fingerprint-xyz", bindService.lastRequest.getDeviceFingerprint());
        assertEquals("AA:BB:CC:DD:EE:FF", bindService.lastRequest.getDeviceInfo().getMacAddress());
        assertEquals("token-123", prefsStore.readSession().getDeviceToken());
        assertEquals("hotel-01", prefsStore.readSession().getHotelId());
        assertEquals("device-88", prefsStore.readSession().getDeviceId());
        assertEquals("TV-AABBCC", prefsStore.readSession().getDeviceName());
    }

    @Test
    public void bindActivate_treatsAlreadyActivatedAsSuccess() throws Exception {
        PrefsStore prefsStore = new PrefsStore(new InMemoryStringStore());
        FakeBindActivationService bindService = new FakeBindActivationService(
                new BindActivateResponse(
                        200,
                        "ALREADY_ACTIVATED",
                        new BindActivateResponse.BindData(
                                "token-reused",
                                "hotel-02",
                                "演示酒店",
                                "device-99",
                                "TV-REUSED"
                        )
                )
        );
        FakeDeviceProfile deviceProfile = new FakeDeviceProfile(
                "fingerprint-reused",
                new DeviceInfo("11:22:33:44:55:66", "RK3128", "daqian", "4.4.4", 19)
        );
        AuthRepository repository = new AuthRepository(bindService, deviceProfile, prefsStore);

        BindActivateResponse response = repository.bindActivate("654321");

        assertEquals("ALREADY_ACTIVATED", response.getStatus());
        assertEquals("token-reused", prefsStore.readSession().getDeviceToken());
        assertEquals("hotel-02", prefsStore.readSession().getHotelId());
        assertEquals("device-99", prefsStore.readSession().getDeviceId());
        assertEquals("TV-REUSED", prefsStore.readSession().getDeviceName());
    }

    private static final class FakeBindActivationService implements AuthRepository.BindActivationService {
        private final BindActivateResponse response;
        private BindActivateRequest lastRequest;

        private FakeBindActivationService(BindActivateResponse response) {
            this.response = response;
        }

        @Override
        public BindActivateResponse activate(BindActivateRequest request) {
            this.lastRequest = request;
            return response;
        }
    }

    private static final class FakeDeviceProfile implements AuthRepository.DeviceProfile {
        private final String deviceFingerprint;
        private final DeviceInfo deviceInfo;

        private FakeDeviceProfile(String deviceFingerprint, DeviceInfo deviceInfo) {
            this.deviceFingerprint = deviceFingerprint;
            this.deviceInfo = deviceInfo;
        }

        @Override
        public String getDeviceFingerprint() {
            return deviceFingerprint;
        }

        @Override
        public DeviceInfo getBindDeviceInfo() {
            return deviceInfo;
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
