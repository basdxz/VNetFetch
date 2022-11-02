package com.github.basdxz.vnetfetch.httpgetter.impl;

import com.github.basdxz.vnetfetch.httpgetter.BaseByteArrayResponseHandler;
import lombok.*;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

import java.io.IOException;

import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor(access = PROTECTED)
public class ByteArrayResponseHandler extends BaseByteArrayResponseHandler {
    protected static final HttpClientResponseHandler<byte[]> INSTANCE = new ByteArrayResponseHandler();

    @Override
    public byte[] handleEntity(HttpEntity entity) throws IOException {
        return entity.getContent().readAllBytes();
    }

    public static HttpClientResponseHandler<byte[]> byteArrayResponseHandler() {
        return INSTANCE;
    }
}
