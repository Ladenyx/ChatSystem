package com.chatsystem.desktop.client;

import com.chatsystem.common.api.ApiHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

/** Externalized HTTP/WSS client configuration loaded from properties or defaults (no server secrets). */
public record ClientConfig(
        URI baseUri,
        Duration connectTimeout,
        Duration requestTimeout,
        Path tlsTrustStore,
        String tlsTrustStorePassword
) {
    public static final String BASE_URL_PROPERTY = "chatsystem.server.base-url";
    public static final String CONNECT_TIMEOUT_SEC_PROPERTY = "chatsystem.server.connect-timeout-sec";
    public static final String REQUEST_TIMEOUT_SEC_PROPERTY = "chatsystem.server.request-timeout-sec";
    public static final String TLS_TRUST_STORE_PROPERTY = "chatsystem.server.tls-trust-store";
    public static final String TLS_TRUST_STORE_PASSWORD_PROPERTY = "chatsystem.server.tls-trust-store-password";
    public static final String CONFIG_PATH_SYSTEM_PROPERTY = "chatsystem.client.config";

    public static final URI DEFAULT_BASE_URI = URI.create("http://127.0.0.1:8080");
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(20);

    public ClientConfig {
        Objects.requireNonNull(baseUri, "baseUri");
        Objects.requireNonNull(connectTimeout, "connectTimeout");
        Objects.requireNonNull(requestTimeout, "requestTimeout");
        String scheme = baseUri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("baseUri scheme must be http or https: " + baseUri);
        }
        if (baseUri.getHost() == null || baseUri.getHost().isBlank()) {
            throw new IllegalArgumentException("baseUri must contain a host: " + baseUri);
        }
        if (connectTimeout.isNegative() || connectTimeout.isZero()) {
            throw new IllegalArgumentException("connectTimeout must be > 0");
        }
        if (requestTimeout.isNegative() || requestTimeout.isZero()) {
            throw new IllegalArgumentException("requestTimeout must be > 0");
        }
    }

    public static ClientConfig defaults() {
        return new ClientConfig(
                DEFAULT_BASE_URI,
                DEFAULT_CONNECT_TIMEOUT,
                DEFAULT_REQUEST_TIMEOUT,
                null,
                null
        );
    }

    public static ClientConfig load(Path configPath) throws IOException {
        Objects.requireNonNull(configPath, "configPath");
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(configPath)) {
            properties.load(in);
        }
        return fromProperties(properties);
    }

    public static ClientConfig fromProperties(Properties properties) {
        Objects.requireNonNull(properties, "properties");
        String rawUrl = trimToNull(properties.getProperty(BASE_URL_PROPERTY));
        URI baseUri = rawUrl == null ? DEFAULT_BASE_URI : URI.create(rawUrl);
        Duration connectTimeout = parsePositiveSeconds(
                properties.getProperty(CONNECT_TIMEOUT_SEC_PROPERTY),
                DEFAULT_CONNECT_TIMEOUT
        );
        Duration requestTimeout = parsePositiveSeconds(
                properties.getProperty(REQUEST_TIMEOUT_SEC_PROPERTY),
                DEFAULT_REQUEST_TIMEOUT
        );
        String trustStoreRaw = trimToNull(properties.getProperty(TLS_TRUST_STORE_PROPERTY));
        Path trustStore = trustStoreRaw == null ? null : Path.of(trustStoreRaw);
        String trustStorePassword = trimToNull(properties.getProperty(TLS_TRUST_STORE_PASSWORD_PROPERTY));
        return new ClientConfig(baseUri, connectTimeout, requestTimeout, trustStore, trustStorePassword);
    }

    public static ClientConfig fromSystemPropertiesOrDefaults() {
        String explicitPath = trimToNull(System.getProperty(CONFIG_PATH_SYSTEM_PROPERTY));
        if (explicitPath != null) {
            try {
                return load(Path.of(explicitPath));
            } catch (IOException ex) {
                throw new IllegalStateException("Failed reading client config from " + explicitPath, ex);
            }
        }
        for (Path candidate : new Path[]{
                Path.of("client.properties"),
                Path.of("config/client.properties"),
                Path.of("release/config/client.properties")
        }) {
            if (Files.isRegularFile(candidate)) {
                try {
                    return load(candidate);
                } catch (IOException ex) {
                    throw new IllegalStateException("Failed reading client config from " + candidate, ex);
                }
            }
        }
        String sysUrl = trimToNull(System.getProperty(BASE_URL_PROPERTY));
        if (sysUrl != null) {
            Properties props = new Properties();
            props.setProperty(BASE_URL_PROPERTY, sysUrl);
            return fromProperties(props);
        }
        return defaults();
    }

    public URI resolve(String pathAndQuery) {
        Objects.requireNonNull(pathAndQuery, "pathAndQuery");
        String normalizedBase = stripTrailingSlash(baseUri.toString());
        String normalizedPath = pathAndQuery.startsWith("/") ? pathAndQuery : "/" + pathAndQuery;
        return URI.create(normalizedBase + normalizedPath);
    }

    public URI wsUri() {
        String wsScheme = "https".equalsIgnoreCase(baseUri.getScheme()) ? "wss" : "ws";
        String basePath = baseUri.getPath() == null ? "" : stripTrailingSlash(baseUri.getPath());
        String authority = baseUri.getRawAuthority();
        return URI.create(wsScheme + "://" + authority + basePath + ApiHeaders.WS_PATH);
    }

    private static Duration parsePositiveSeconds(String raw, Duration fallback) {
        String trimmed = trimToNull(raw);
        if (trimmed == null) {
            return fallback;
        }
        long seconds = Long.parseLong(trimmed);
        if (seconds <= 0) {
            throw new IllegalArgumentException("Timeout seconds must be > 0, got: " + seconds);
        }
        return Duration.ofSeconds(seconds);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String stripTrailingSlash(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(0, end);
    }
}
