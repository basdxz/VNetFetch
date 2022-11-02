package com.github.basdxz.vnetfetch.httpgetter;

import lombok.*;
import org.apache.hc.core5.http.HttpException;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public interface ICachedHttpGetter extends IHttpGetter {
    byte[] get(@NonNull URI uri, @NonNull File dataFile) throws HttpException, IOException;
}
