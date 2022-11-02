package com.github.basdxz.vnetfetch.util;

import lombok.experimental.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;

import static com.github.basdxz.vnetfetch.util.LoggingUtil.log;

@UtilityClass
public final class HttpClientTestUtil {
    private static final Timeout MAX_TIMEOUT = Timeout.ofSeconds(5);
    private static final RequestConfig REQUEST_CONFIG = newRequestConfig();

    private static CloseableHttpClient httpClient;

    private static RequestConfig newRequestConfig() {
        return RequestConfig.custom()
                            .setConnectionRequestTimeout(MAX_TIMEOUT)
                            .setResponseTimeout(MAX_TIMEOUT)
                            .setConnectTimeout(MAX_TIMEOUT)
                            .build();
    }

    public static CloseableHttpClient httpClient() {
        if (httpClient == null) {
            httpClient = HttpClients.custom()
                                    .setDefaultRequestConfig(REQUEST_CONFIG)
                                    .build();
        }
        return httpClient;
    }


    public static boolean isHttpClientShutdown() {
        return httpClient == null;
    }

    public static void disposeHttpClient() {
        if (httpClient == null) {
            log().warn("HTTP Client already disposed");
            return;
        }
        try {
            httpClient.close();
        } catch (IOException e) {
            log().error("Failed to dispose HTTP Client", e);
        } finally {
            httpClient = null;
        }
    }
}
