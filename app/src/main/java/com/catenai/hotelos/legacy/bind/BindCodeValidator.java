package com.catenai.hotelos.legacy.bind;

public final class BindCodeValidator {
    private BindCodeValidator() {
    }

    public static boolean isValid(String bindCode) {
        String normalized = bindCode == null ? "" : bindCode.trim();
        return normalized.matches("\\d{6}");
    }
}
