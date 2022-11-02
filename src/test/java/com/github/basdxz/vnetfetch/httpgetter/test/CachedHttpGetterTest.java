package com.github.basdxz.vnetfetch.httpgetter.test;

import com.github.basdxz.vnetfetch.httpgetter.impl.CachedHttpGetter;
import com.github.basdxz.vnetfetch.util.HttpClientUtil;
import lombok.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class CachedHttpGetterTest {
    @Test
    @SneakyThrows
    public void manualTest() {
        val daLink = URI.create("https://launchermeta.mojang.com/mc/game/version_manifest.json");
        try (val httpClient = HttpClientUtil.httpClient()) {
            val theBook = new CachedHttpGetter(httpClient, new File("cache"));
            val dataz = theBook.get(daLink);
            val txt = new String(dataz, StandardCharsets.UTF_8);
            System.out.println(txt);
        }
    }
}