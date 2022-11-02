package com.github.basdxz.vnetfetch.httpgetter.test;

import com.github.basdxz.vnetfetch.httpgetter.impl.CachedHttpGetter;
import lombok.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static com.github.basdxz.vnetfetch.util.HttpClientTestUtil.httpClient;

// TODO: Proper Unit Testing
public class CachedHttpGetterTest {
    @Test
    @SneakyThrows
    public void manualTest() {
        val daLink = URI.create("https://launchermeta.mojang.com/mc/game/version_manifest.json");
        try (val httpClient = httpClient()) {
            val theBook = new CachedHttpGetter(httpClient, new File("cache"));
            val dataz = theBook.get(daLink);
            val txt = new String(dataz, StandardCharsets.UTF_8);
            System.out.println(txt);
        }
    }
}