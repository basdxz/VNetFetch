package com.github.basdxz.vnetfetch.util;

import lombok.*;
import lombok.experimental.*;
import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.ProtocolException;

import java.util.Optional;

import static com.github.basdxz.vnetfetch.util.Constants.ETAG_HEADER;


@UtilityClass
public final class HttpClientUtil {
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
