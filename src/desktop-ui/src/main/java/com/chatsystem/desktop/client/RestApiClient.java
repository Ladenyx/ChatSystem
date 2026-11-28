package com.chatsystem.desktop.client;

import com.chatsystem.common.api.ApiError;
import com.chatsystem.common.api.ApiHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/** Shared asynchronous HTTPS/HTTP REST client built on JDK {@link HttpClient} and Jackson 3. */
public final class RestApiClient {
    private final ClientConfig config;
    private final ClientSessionStore sessionStore;
    private final HttpClient httpClient;
    private final JsonMapper jsonMapper;

    public RestApiClient(ClientConfig config, ClientSessionStore sessionStore) {
        this(config, sessionStore, buildHttpClient(config), JsonMapper.builder().build());
    }

    public RestApiClient(
            ClientConfig config,
            ClientSessionStore sessionStore,
            HttpClient httpClient,
            JsonMapper jsonMapper
    ) {
        this.config = Objects.requireNonNull(config, "config");
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.jsonMapper = Objects.requireNonNull(jsonMapper, "jsonMapper");
    }

    public ClientConfig config() {
        return config;
    }

    public HttpClient httpClient() {
        return httpClient;
    }

    public <T> CompletableFuture<T> getAsync(String pathAndQuery, Class<T> responseType) {
        return sendAsync("GET", pathAndQuery, null, responseType);
    }

    public <T> CompletableFuture<T> getAsync(String pathAndQuery, TypeReference<T> responseType) {
        Objects.requireNonNull(responseType, "responseType");
        return sendAsync("GET", pathAndQuery, null, true, body -> jsonMapper.readValue(body, responseType));
    }

    public <T> CompletableFuture<T> postAsync(String pathAndQuery, Object body, Class<T> responseType) {
        return sendAsync("POST", pathAndQuery, body, responseType);
    }

    public CompletableFuture<Void> postEmptyAsync(String pathAndQuery, Object body) {
        return sendAsync("POST", pathAndQuery, body, Void.class);
    }

    public <T> CompletableFuture<T> patchAsync(String pathAndQuery, Object body, Class<T> responseType) {
        return sendAsync("PATCH", pathAndQuery, body, responseType);
    }

    public CompletableFuture<Void> deleteAsync(String pathAndQuery) {
        return sendAsync("DELETE", pathAndQuery, null, Void.class);
    }

    private <T> CompletableFuture<T> sendAsync(
            String method,
            String pathAndQuery,
            Object body,
            Class<T> responseType
    ) {
        Objects.requireNonNull(responseType, "responseType");
        boolean expectBody = responseType != Void.class;
        return sendAsync(method, pathAndQuery, body, expectBody, text -> jsonMapper.readValue(text, responseType));
    }

    private <T> CompletableFuture<T> sendAsync(
            String method,
            String pathAndQuery,
            Object body,
            boolean expectBody,
            Function<String, T> reader
    ) {
        Objects.requireNonNull(reader, "reader");
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(config.resolve(pathAndQuery))
                .timeout(config.requestTimeout())
                .header("Accept", "application/json")
                .header(ApiHeaders.REQUEST_ID, UUID.randomUUID().toString());
        sessionStore.sessionToken()
                .ifPresent(token -> builder.header(ApiHeaders.AUTHORIZATION, ApiHeaders.BEARER_PREFIX + token));
        sessionStore.deviceId()
                .ifPresent(deviceId -> builder.header(ApiHeaders.CLIENT_DEVICE_ID, deviceId.toString()));

        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.noBody();
        if (body != null) {
            builder.header("Content-Type", "application/json; charset=utf-8");
            try {
                publisher = HttpRequest.BodyPublishers.ofString(jsonMapper.writeValueAsString(body), StandardCharsets.UTF_8);
            } catch (JacksonException ex) {
                return CompletableFuture.failedFuture(
                        new ApiClientException(0, "Không thể mã hóa dữ liệu yêu cầu JSON.", false, ex)
                );
            }
        }
        builder.method(method, publisher);

        return httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .handle((response, throwable) -> {
                    if (throwable != null) {
                        Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
                        throw new ApiClientException(0, "Không thể kết nối tới máy chủ.", true, cause);
                    }
                    return parseResponse(response, expectBody, reader);
                });
    }

    private <T> T parseResponse(HttpResponse<String> response, boolean expectBody, Function<String, T> reader) {
        int status = response.statusCode();
        String responseBody = response.body();
        if (status >= 200 && status < 300) {
            if (!expectBody || responseBody == null || responseBody.isBlank()) {
                return null;
            }
            try {
                return reader.apply(responseBody);
            } catch (JacksonException ex) {
                throw new ApiClientException(status, "Phản hồi JSON từ máy chủ không hợp lệ.", false, ex);
            }
        }
        ApiError parsedError = tryParseApiError(responseBody);
        if (parsedError != null) {
            throw new ApiClientException(parsedError, status);
        }
        boolean retryable = status >= 500 || status == 429;
        throw new ApiClientException(status, "Yêu cầu thất bại với mã HTTP " + status + ".", retryable, null);
    }

    private ApiError tryParseApiError(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }
        try {
            return jsonMapper.readValue(responseBody, ApiError.class);
        } catch (JacksonException ignored) {
            return null;
        }
    }

    static HttpClient buildHttpClient(ClientConfig config) {
        Objects.requireNonNull(config, "config");
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(config.connectTimeout());
        if (config.tlsTrustStore() != null) {
            builder.sslContext(loadSslContext(config.tlsTrustStore(), config.tlsTrustStorePassword()));
        }
        return builder.build();
    }

    private static SSLContext loadSslContext(Path trustStorePath, String password) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            char[] passwordChars = password == null ? null : password.toCharArray();
            try (InputStream in = Files.newInputStream(trustStorePath)) {
                keyStore.load(in, passwordChars);
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext;
        } catch (IOException | GeneralSecurityException ex) {
            throw new IllegalStateException("Failed loading client TLS truststore from " + trustStorePath, ex);
        }
    }
}
