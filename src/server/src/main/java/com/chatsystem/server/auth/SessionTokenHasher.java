package com.chatsystem.server.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/** Computes lowercase hex SHA-256 hashes of opaque session tokens before querying {@code user_sessions}. */
public final class SessionTokenHasher {
    private static final HexFormat HEX = HexFormat.of();

    private SessionTokenHasher() {
    }

    public static String sha256Hex(String rawToken) {
        Objects.requireNonNull(rawToken, "rawToken");
        if (rawToken.isBlank()) {
            throw new IllegalArgumentException("rawToken must not be blank");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
        }
    }
}
