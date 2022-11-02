package com.github.basdxz.vnetfetch.httpgetter;

import lombok.*;
import org.apache.hc.core5.http.HttpException;

import java.io.IOException;
import java.net.URI;

public interface IHttpGetter {
    byte[] get(@NonNull URI uri) throws HttpException, IOException;
}
