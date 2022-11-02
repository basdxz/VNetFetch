package com.github.basdxz.vnetfetch.httpgetter.test;

import com.github.basdxz.vnetfetch.httpgetter.impl.HttpGetter;
import com.github.basdxz.vnetfetch.util.HttpClientUtil;
import lombok.*;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public class HttpGetterTest {
    @Test
    @SneakyThrows
    public void manualTest() {
        try (val httpClient = HttpClientUtil.httpClient()) {
            System.out.println(new String(new HttpGetter(httpClient).get(URI.create("https://launchermeta.mojang.com/mc/game/version_manifest.json")), StandardCharsets.UTF_8));
        }
    }
}