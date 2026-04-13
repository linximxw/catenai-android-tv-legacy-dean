package com.catenai.hotelos.legacy.data.net;

import org.junit.Test;

import okhttp3.Request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ApiHeadersTest {

    @Test
    public void applyCommonHeaders_addsGuideHeadersWhenTokenPresent() {
        Request request = ApiHeaders.applyCommonHeaders(
                new Request.Builder().url("https://catenai.cn/api/device/session"),
                "token-123",
                "ANDROID_TV",
                "daqian",
                "RK3128",
                "1.0.0"
        ).build();

        assertEquals("token-123", request.header("x-device-token"));
        assertEquals("ANDROID_TV", request.header("x-device-type"));
        assertEquals("daqian", request.header("x-device-vendor"));
        assertEquals("RK3128", request.header("x-device-model"));
        assertEquals("1.0.0", request.header("x-app-version"));
        assertEquals("application/json", request.header("Content-Type"));
    }

    @Test
    public void applyCommonHeaders_omitsTokenWhenBlank() {
        Request request = ApiHeaders.applyCommonHeaders(
                new Request.Builder().url("https://catenai.cn/api/device/session"),
                " ",
                "ANDROID_TV",
                "daqian",
                "RK3128",
                "1.0.0"
        ).build();

        assertNull(request.header("x-device-token"));
        assertEquals("ANDROID_TV", request.header("x-device-type"));
    }
}
