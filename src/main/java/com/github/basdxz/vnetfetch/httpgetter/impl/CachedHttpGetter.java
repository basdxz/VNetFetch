package com.github.basdxz.vnetfetch.httpgetter.impl;

import com.github.basdxz.vnetfetch.util.FileUtil;
import com.github.basdxz.vnetfetch.util.HttpClientUtil;
import lombok.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

import static com.github.basdxz.vnetfetch.httpgetter.impl.ByteArrayResponseHandler.byteArrayResponseHandler;
import static com.github.basdxz.vnetfetch.util.Constants.ETAG_FILE_EXTENSION;
import static com.github.basdxz.vnetfetch.util.Constants.SHA1_FILE_EXTENSION;
import static com.github.basdxz.vnetfetch.util.FileUtil.fileInputStream;
import static com.github.basdxz.vnetfetch.util.HttpClientUtil.getETag;
import static com.github.basdxz.vnetfetch.util.LoggingUtil.log;


// Exceptions
public class CachedHttpGetter extends HttpGetter {
    @NonNull
    protected final File cacheDirectory;

    public CachedHttpGetter(@NonNull CloseableHttpClient httpClient, @NonNull File cacheDirectory) {
        super(httpClient);
        this.cacheDirectory = cacheDirectory;
    }

    @SneakyThrows
    public static void main(String[] args) {
        val daLink = URI.create("https://launchermeta.mojang.com/mc/game/version_manifest.json");
        try (val httpClient = HttpClientUtil.httpClient()) {
            val theBook = new CachedHttpGetter(httpClient, new File("cache"));
            val dataz = theBook.get(daLink);
            val txt = new String(dataz, StandardCharsets.UTF_8);
            System.out.println(txt);
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public byte[] get(@NonNull URI uri) throws HttpException, IOException {
        val dataCacheDirectory = dataCacheDirectory(uri);
        val dataFileName = dataFileName(uri);
        cacheHit:
        {
            val cachedData = loadCachedData(dataCacheDirectory, dataFileName)
                    .orElse(null);
            if (cachedData == null)
                break cacheHit;
            val cachedSha1 = loadCachedSha1(dataCacheDirectory, dataFileName)
                    .orElse(null);
            if (cachedSha1 == null)
                break cacheHit;
            val cachedDataSha1 = DigestUtils.sha1Hex(cachedData);
            if (!cachedSha1.equals(cachedDataSha1))
                break cacheHit;
            val cachedEtag = loadCachedEtag(dataCacheDirectory, dataFileName)
                    .orElse(null);
            if (cachedEtag == null)
                break cacheHit;
            try {
                val response = sendHttpGetRequest(uri, cachedEtag);
                val data = byteArrayResponseHandler().handleResponse(response);
                val eTag = getETag(response).orElse(null);
                if (eTag != null) {
                    val sha1 = DigestUtils.sha1Hex(data);
                    cacheResponse(dataCacheDirectory, dataFileName, data, sha1, eTag);
                }
                return data;
            } catch (IOException e) {
                if (e instanceof UnknownHostException) {
                    log().warn("URI {} is unreachable, but the file {} was provided from disk cache",
                               uri,
                               dataFileName);
                    return cachedData;
                }
                if (e instanceof HttpResponseException httpResponseException)
                    if (httpResponseException.getStatusCode() == HttpStatus.SC_NOT_MODIFIED)
                        return cachedData;
                throw e;
            }
        }
        val response = sendHttpGetRequest(uri);
        val data = byteArrayResponseHandler().handleResponse(response);
        val eTag = getETag(response).orElse(null);
        if (eTag != null) {
            val sha1 = DigestUtils.sha1Hex(data);
            cacheResponse(dataCacheDirectory, dataFileName, data, sha1, eTag);
        }
        return data;
    }

    protected File dataCacheDirectory(@NonNull URI uri) {
        return new File(cacheDirectory, DigestUtils.sha1Hex(uri.toString()));
    }

    protected String dataFileName(@NonNull URI uri) {
        val uriPath = uri.getPath();
        val dataFileName = uriPath.substring(uriPath.lastIndexOf('/') + 1);
        if (dataFileName.isEmpty())
            throw new IllegalArgumentException("Invalid URI, must point to file: " + uri);
        return dataFileName;
    }

    protected Optional<byte[]> loadCachedData(@NonNull File dataCacheDirectory, @NonNull String dataFileName) {
        try (val fileInputStream = fileInputStream(newDataFile(dataCacheDirectory, dataFileName))) {
            return Optional.of(fileInputStream.readAllBytes());
        } catch (IOException e) {
            log().debug("File: {} not found in cache", dataFileName);
            return Optional.empty();
        }
    }

    protected Optional<String> loadCachedSha1(@NonNull File dataCacheDirectory, @NonNull String dataFileName) {
        try {
            return Optional.of(IOUtils.toString(
                    fileInputStream(newSha1File(dataCacheDirectory, dataFileName)), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log().debug("SHA1 Hash for file: {} not found in cache", dataFileName);
            return Optional.empty();
        }
    }

    protected Optional<String> loadCachedEtag(@NonNull File dataCacheDirectory, @NonNull String dataFileName) {
        try {
            return Optional.of(IOUtils.toString(
                    fileInputStream(newETagFile(dataCacheDirectory, dataFileName)), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log().debug("ETag Hash for file: {} not found in cache", dataFileName);
            return Optional.empty();
        }
    }

    protected void cacheResponse(@NonNull File dataCacheDirectory,
                                 @NonNull String dataFileName,
                                 @NonNull byte[] data,
                                 @NonNull String sha1,
                                 @NonNull String eTag) throws IOException {
        prepareDataCacheDirectory(dataCacheDirectory);
        cacheData(dataCacheDirectory, dataFileName, data);
        cacheSha1(dataCacheDirectory, dataFileName, sha1);
        cacheEtag(dataCacheDirectory, dataFileName, eTag);
    }

    protected void prepareDataCacheDirectory(@NonNull File dataCacheDirectory) throws IOException {
        try {
            FileUtil.createDirectory(dataCacheDirectory);
        } catch (IOException e) {
            throw new IOException("Failed to create directory :" + dataCacheDirectory.getAbsolutePath(), e);
        }
        try {
            FileUtils.cleanDirectory(dataCacheDirectory);
        } catch (IOException e) {
            throw new IOException("Failed to clean directory :" + dataCacheDirectory.getAbsolutePath(), e);
        }
    }

    protected void cacheData(@NonNull File dataCacheDirectory, @NonNull String dataFileName, @NonNull byte[] data)
            throws IOException {
        Files.write(newDataFile(dataCacheDirectory, dataFileName).toPath(), data);
    }

    protected void cacheSha1(@NonNull File dataCacheDirectory, @NonNull String dataFileName, @NonNull String sha1)
            throws IOException {
        Files.writeString(newSha1File(dataCacheDirectory, dataFileName).toPath(), sha1);
    }

    protected void cacheEtag(@NonNull File dataCacheDirectory, @NonNull String dataFileName, @NonNull String eTag)
            throws IOException {
        Files.writeString(newETagFile(dataCacheDirectory, dataFileName).toPath(), eTag);
    }

    protected File newDataFile(@NonNull File dataCacheDirectory, @NonNull String dataFileName) {
        return new File(dataCacheDirectory, dataFileName);
    }

    protected File newSha1File(@NonNull File dataCacheDirectory, @NonNull String dataFileName) {
        return new File(dataCacheDirectory, dataFileName + "." + SHA1_FILE_EXTENSION);
    }

    protected File newETagFile(@NonNull File dataCacheDirectory, @NonNull String dataFileName) {
        return new File(dataCacheDirectory, dataFileName + "." + ETAG_FILE_EXTENSION);
    }

    protected CloseableHttpResponse sendHttpGetRequest(@NonNull URI uri, @NonNull String etag) throws IOException {
        val httpGet = new HttpGet(uri);
        httpGet.addHeader(HttpHeaders.IF_NONE_MATCH, etag);
        return httpClient.execute(httpGet);
    }
}
