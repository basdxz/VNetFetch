package com.github.basdxz.vnetfetch.util;

import lombok.*;
import lombok.experimental.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.util.Optional;

import static com.github.basdxz.vnetfetch.util.Constants.ETAG_HEADER;

// TODO: Move the http client out of this project.
@UtilityClass
public final class HttpClientUtil {
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
            //TODO: logging
            System.err.println("HTTP Client already disposed!");
            return;
        }
        try {
            httpClient.close();
        } catch (IOException e) {
            //TODO: logging
            System.err.println("Exception closing HTTP Client");
            e.printStackTrace();
        } finally {
            httpClient = null;
        }
    }

    public static Optional<String> getETag(@NonNull MessageHeaders messageHeaders) {
        if (!messageHeaders.containsHeader(ETAG_HEADER))
            return Optional.empty();
        try {
            return Optional.of(messageHeaders.getHeader(ETAG_HEADER).getValue());
        } catch (ProtocolException e) {
            //TODO: Fancy debug message
            return Optional.empty();
        }
    }
}
