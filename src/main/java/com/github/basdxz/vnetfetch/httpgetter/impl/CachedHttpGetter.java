package com.github.basdxz.vnetfetch.httpgetter.impl;

import com.github.basdxz.vnetfetch.httpgetter.ICachedHttpGetter;
import com.github.basdxz.vnetfetch.util.FileUtil;
import lombok.*;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.nio.file.Paths;
import java.util.Optional;

import static com.github.basdxz.vnetfetch.httpgetter.impl.ByteArrayResponseHandler.byteArrayResponseHandler;
import static com.github.basdxz.vnetfetch.util.Constants.ETAG_FILE_EXTENSION;
import static com.github.basdxz.vnetfetch.util.Constants.SHA1_FILE_EXTENSION;
import static com.github.basdxz.vnetfetch.util.FileUtil.appendToFileName;
import static com.github.basdxz.vnetfetch.util.FileUtil.fileInputStream;
import static com.github.basdxz.vnetfetch.util.HttpClientUtil.getETag;
import static com.github.basdxz.vnetfetch.util.LoggingUtil.log;


public class CachedHttpGetter extends HttpGetter implements ICachedHttpGetter {
    @NonNull
    protected final File cacheDirectory;

    public CachedHttpGetter(@NonNull CloseableHttpClient httpClient, @NonNull File cacheDirectory) {
        super(httpClient);
        this.cacheDirectory = cacheDirectory;
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public byte[] get(@NonNull URI uri, @NonNull File dataFile) throws HttpException, IOException {
        cacheHit:
        {
            val cachedData = loadCachedData(dataFile)
                    .orElse(null);
            if (cachedData == null)
                break cacheHit;
            val cachedSha1 = loadCachedSha1(dataFile)
                    .orElse(null);
            if (cachedSha1 == null)
                break cacheHit;
            val cachedDataSha1 = DigestUtils.sha1Hex(cachedData);
            if (!cachedSha1.equalsIgnoreCase(cachedDataSha1))
                break cacheHit;
            val cachedEtag = loadCachedEtag(dataFile)
                    .orElse(null);
            if (cachedEtag == null)
                break cacheHit;
            try {
                val response = sendHttpGetRequest(uri, cachedEtag);
                val data = byteArrayResponseHandler().handleResponse(response);
                val eTag = getETag(response).orElse(null);
                if (eTag != null) {
                    val sha1 = DigestUtils.sha1Hex(data);
                    cacheResponse(dataFile, data, sha1, eTag);
                }
                return data;
            } catch (IOException e) {
                if (e instanceof UnknownHostException) {
                    log().warn("URI {} is unreachable, but the file {} was provided from disk cache",
                               uri,
                               dataFile.getAbsolutePath());
                    return cachedData;
                }
                if (e instanceof HttpResponseException httpResponseException)
                    if (httpResponseException.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
                        return cachedData;
                    }
                throw e;
            }
        }
        val response = sendHttpGetRequest(uri);
        val data = byteArrayResponseHandler().handleResponse(response);
        val eTag = getETag(response).orElse(null);
        if (eTag != null) {
            val sha1 = DigestUtils.sha1Hex(data);
            cacheResponse(dataFile, data, sha1, eTag);
        }
        return data;
    }

    @Override
    public byte[] get(@NonNull URI uri) throws HttpException, IOException {
        return get(uri, defaultDataFile(uri));
    }

    protected File defaultDataFile(@NonNull URI uri) {
        val uriPath = uri.getPath();
        val dataFileName = uriPath.substring(uriPath.lastIndexOf('/') + 1);
        if (dataFileName.isEmpty())
            throw new IllegalArgumentException("Invalid URI, must point to file: " + uri);
        val uriHash = DigestUtils.sha1Hex(uri.toString());
        val uriShortHash = uriHash.substring(0, 2);
        return Paths.get(cacheDirectory.getAbsolutePath(), uriShortHash, uriHash, dataFileName).toFile();
    }

    protected Optional<byte[]> loadCachedData(@NonNull File dataFile) {
        try (val fileInputStream = fileInputStream(dataFile)) {
            return Optional.of(fileInputStream.readAllBytes());
        } catch (IOException e) {
            log().debug("File: {} not found in cache", dataFile.getAbsolutePath());
            return Optional.empty();
        }
    }

    protected Optional<String> loadCachedSha1(@NonNull File dataFile) {
        val sha1File = newSha1File(dataFile);
        try {
            return Optional.of(IOUtils.toString(
                    fileInputStream(sha1File), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log().debug("SHA1 Hash for file: {} not found in cache", sha1File.getAbsolutePath());
            return Optional.empty();
        }
    }

    protected Optional<String> loadCachedEtag(@NonNull File dataFile) {
        val eTagFile = newETagFile(dataFile);
        try {
            return Optional.of(IOUtils.toString(
                    fileInputStream(eTagFile), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log().debug("ETag Hash for file: {} not found in cache", eTagFile.getAbsolutePath());
            return Optional.empty();
        }
    }

    protected void cacheResponse(@NonNull File dataFile,
                                 @NonNull byte[] data,
                                 @NonNull String sha1,
                                 @NonNull String eTag) throws IOException {
        prepareDataCacheDirectory(dataFile.getParentFile());
        cacheData(dataFile, data);
        cacheSha1(dataFile, sha1);
        cacheEtag(dataFile, eTag);
    }

    protected void prepareDataCacheDirectory(@NonNull File dataCacheDirectory) throws IOException {
        try {
            FileUtil.createDirectory(dataCacheDirectory);
        } catch (IOException e) {
            throw new IOException("Failed to create directory :" + dataCacheDirectory.getAbsolutePath(), e);
        }
    }

    protected void cacheData(@NonNull File dataFile, @NonNull byte[] data)
            throws IOException {
        Files.write(dataFile.toPath(), data);
    }

    protected void cacheSha1(@NonNull File dataFile, @NonNull String sha1)
            throws IOException {
        Files.writeString(newSha1File(dataFile).toPath(), sha1);
    }

    protected void cacheEtag(@NonNull File dataFile, @NonNull String eTag)
            throws IOException {
        Files.writeString(newETagFile(dataFile).toPath(), eTag);
    }


    protected File newSha1File(@NonNull File dataFile) {
        return appendToFileName(dataFile, "." + SHA1_FILE_EXTENSION);
    }

    protected File newETagFile(@NonNull File dataFile) {
        return appendToFileName(dataFile, "." + ETAG_FILE_EXTENSION);
    }

    protected CloseableHttpResponse sendHttpGetRequest(@NonNull URI uri, @NonNull String etag) throws IOException {
        val httpGet = new HttpGet(uri);
        httpGet.addHeader(HttpHeaders.IF_NONE_MATCH, etag);
        return httpClient.execute(httpGet);
    }
}
