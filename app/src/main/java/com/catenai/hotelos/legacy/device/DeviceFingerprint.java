package com.catenai.hotelos.legacy.device;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class DeviceFingerprint {

    private DeviceFingerprint() {
    }

    public static String generateFromParts(String macAddress, String serial, String board) {
        String raw = safe(macAddress) + "|" + safe(serial) + "|" + safe(board);
        return sha256(raw);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate device fingerprint", e);
        }
    }
}
