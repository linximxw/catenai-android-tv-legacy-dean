package com.catenai.hotelos.legacy.data.net;

import okhttp3.Request;

public final class ApiHeaders {
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String HEADER_DEVICE_TOKEN = "x-device-token";
    private static final String HEADER_DEVICE_TYPE = "x-device-type";
    private static final String HEADER_DEVICE_VENDOR = "x-device-vendor";
    private static final String HEADER_DEVICE_MODEL = "x-device-model";
    private static final String HEADER_APP_VERSION = "x-app-version";

    private ApiHeaders() {
    }

    public static Request.Builder applyCommonHeaders(
            Request.Builder builder,
            String deviceToken,
            String deviceType,
            String deviceVendor,
            String deviceModel,
            String appVersion
    ) {
        builder.header(CONTENT_TYPE, JSON_CONTENT_TYPE);
        putIfNotBlank(builder, HEADER_DEVICE_TYPE, deviceType);
        putIfNotBlank(builder, HEADER_DEVICE_VENDOR, deviceVendor);
        putIfNotBlank(builder, HEADER_DEVICE_MODEL, deviceModel);
        putIfNotBlank(builder, HEADER_APP_VERSION, appVersion);
        putIfNotBlank(builder, HEADER_DEVICE_TOKEN, deviceToken);
        return builder;
    }

    private static void putIfNotBlank(Request.Builder builder, String headerName, String value) {
        String normalizedValue = normalize(value);
        if (!normalizedValue.isEmpty()) {
            builder.header(headerName, normalizedValue);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
