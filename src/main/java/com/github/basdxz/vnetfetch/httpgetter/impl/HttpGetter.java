package com.github.basdxz.vnetfetch.httpgetter.impl;

import com.github.basdxz.vnetfetch.httpgetter.IHttpGetter;
import com.github.basdxz.vnetfetch.util.HttpClientUtil;
import lombok.*;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpException;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static com.github.basdxz.vnetfetch.httpgetter.impl.ByteArrayResponseHandler.byteArrayResponseHandler;

@AllArgsConstructor
public class HttpGetter implements IHttpGetter {
    @NonNull
    protected final CloseableHttpClient httpClient;

    @SneakyThrows
    public static void main(String[] args) {
        try (val httpClient = HttpClientUtil.httpClient()) {
            System.out.println(new String(new HttpGetter(httpClient).get(URI.create("https://launchermeta.mojang.com/mc/game/version_manifest.json")), StandardCharsets.UTF_8));
        }
    }

    @Override
    public byte[] get(@NonNull URI uri) throws HttpException, IOException {
        return byteArrayResponseHandler().handleResponse(sendHttpGetRequest(uri));
    }

    protected CloseableHttpResponse sendHttpGetRequest(@NonNull URI uri) throws IOException {
        return httpClient.execute(new HttpGet(uri));
    }
}
