package com.catenai.hotelos.legacy.device;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeviceFingerprintTest {

    @Test
    public void generateFromParts_returnsStableSha256Hex() {
        String fingerprint = DeviceFingerprint.generateFromParts(
                "AA:BB:CC:DD:EE:FF",
                "SER123",
                "BOARDX"
        );

        assertEquals(64, fingerprint.length());
        assertTrue(fingerprint.matches("[0-9a-f]{64}"));
        assertEquals("71f32fa3d4f536aa43935e2f62b0394f46f29090cea92a9b2906ac533bebf816", fingerprint);
    }
}
