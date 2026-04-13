package com.catenai.hotelos.legacy.data.net;

import android.content.Context;
import android.os.Build;

import com.catenai.hotelos.legacy.BuildConfig;
import com.catenai.hotelos.legacy.device.DeviceInfoProvider;
import com.google.gson.Gson;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;

public final class ApiClient {
    private static final int CONNECT_TIMEOUT_SECONDS = 8;
    private static final int READ_TIMEOUT_SECONDS = 12;
    private static final int WRITE_TIMEOUT_SECONDS = 12;
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final HttpUrl baseUrl;
    private final OkHttpClient httpClient;
    private final DeviceInfoProvider deviceInfoProvider;
    private final Gson gson;

    public ApiClient(Context context) {
        this(context, new DeviceInfoProvider(context));
    }

    public ApiClient(Context context, DeviceInfoProvider deviceInfoProvider) {
        this(context, deviceInfoProvider, BuildConfig.BASE_URL);
    }

    public ApiClient(Context context, DeviceInfoProvider deviceInfoProvider, String baseUrl) {
        Context appContext = context.getApplicationContext();
        this.deviceInfoProvider = deviceInfoProvider == null
                ? new DeviceInfoProvider(appContext)
                : deviceInfoProvider;
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.gson = new Gson();
        this.httpClient = createDefaultClient();
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public DeviceInfoProvider getDeviceInfoProvider() {
        return deviceInfoProvider;
    }

    public HttpUrl getBaseUrl() {
        return baseUrl;
    }

    public HttpUrl resolve(String relativePath) {
        String normalizedPath = normalizeRelativePath(relativePath);
        HttpUrl resolvedUrl = baseUrl.resolve(normalizedPath);
        if (resolvedUrl == null) {
            throw new IllegalArgumentException("Unable to resolve API path: " + relativePath);
        }
        return resolvedUrl;
    }

    public Request.Builder newRequestBuilder(String relativePath, String deviceToken) {
        return ApiHeaders.applyCommonHeaders(
                new Request.Builder().url(resolve(relativePath)),
                deviceToken,
                deviceInfoProvider.getDeviceType(),
                deviceInfoProvider.getDeviceVendor(),
                deviceInfoProvider.getDeviceModel(),
                deviceInfoProvider.getAppVersion()
        );
    }

    public Request buildGet(String relativePath, String deviceToken) {
        return newRequestBuilder(relativePath, deviceToken).get().build();
    }

    public Request buildPostJson(String relativePath, String deviceToken, Object payload) {
        return newRequestBuilder(relativePath, deviceToken)
                .post(createJsonBody(payload))
                .build();
    }

    public RequestBody createJsonBody(Object payload) {
        String jsonBody = payload == null ? "{}" : gson.toJson(payload);
        return RequestBody.create(JSON_MEDIA_TYPE, jsonBody);
    }

    private OkHttpClient createDefaultClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .dns(new Ipv4FirstDns());

        enableTls12OnPreLollipop(builder);
        return builder.build();
    }

    private void enableTls12OnPreLollipop(OkHttpClient.Builder builder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        try {
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                return;
            }

            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[]{trustManager}, null);

            builder.sslSocketFactory(new Tls12SocketFactory(sslContext.getSocketFactory()), trustManager);
            builder.connectionSpecs(Arrays.asList(
                    ConnectionSpec.MODERN_TLS,
                    ConnectionSpec.COMPATIBLE_TLS,
                    ConnectionSpec.CLEARTEXT
            ));
        } catch (GeneralSecurityException ignored) {
            // Keep the default stack when TLS 1.2 customization is unavailable.
        }
    }

    private static HttpUrl normalizeBaseUrl(String rawBaseUrl) {
        String normalizedBaseUrl = rawBaseUrl == null ? "" : rawBaseUrl.trim();
        if (normalizedBaseUrl.isEmpty()) {
            throw new IllegalStateException("BASE_URL is empty");
        }
        if (!normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl + "/";
        }
        HttpUrl parsedUrl = HttpUrl.parse(normalizedBaseUrl);
        if (parsedUrl == null) {
            throw new IllegalStateException("Invalid BASE_URL: " + rawBaseUrl);
        }
        return parsedUrl;
    }

    private static String normalizeRelativePath(String relativePath) {
        String normalizedPath = relativePath == null ? "" : relativePath.trim();
        if (normalizedPath.isEmpty()) {
            return "";
        }
        return normalizedPath.startsWith("/") ? normalizedPath.substring(1) : normalizedPath;
    }

    private static final class Ipv4FirstDns implements Dns {
        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            List<InetAddress> allAddresses = Arrays.asList(InetAddress.getAllByName(hostname));
            List<InetAddress> ipv4Addresses = new ArrayList<InetAddress>();
            List<InetAddress> remainingAddresses = new ArrayList<InetAddress>();

            for (InetAddress address : allAddresses) {
                if (address.getAddress().length == 4) {
                    ipv4Addresses.add(address);
                } else {
                    remainingAddresses.add(address);
                }
            }

            ipv4Addresses.addAll(remainingAddresses);
            return ipv4Addresses;
        }
    }
}
