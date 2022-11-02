package com.github.basdxz.vnetfetch.httpgetter.impl;

import com.github.basdxz.vnetfetch.httpgetter.IHttpGetter;
import lombok.*;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpException;

import java.io.IOException;
import java.net.URI;

import static com.github.basdxz.vnetfetch.httpgetter.impl.ByteArrayResponseHandler.byteArrayResponseHandler;

@AllArgsConstructor
public class HttpGetter implements IHttpGetter {
    @NonNull
    protected final CloseableHttpClient httpClient;

    @Override
    public byte[] get(@NonNull URI uri) throws HttpException, IOException {
        return byteArrayResponseHandler().handleResponse(sendHttpGetRequest(uri));
    }

    protected CloseableHttpResponse sendHttpGetRequest(@NonNull URI uri) throws IOException {
        return httpClient.execute(new HttpGet(uri));
    }
}
