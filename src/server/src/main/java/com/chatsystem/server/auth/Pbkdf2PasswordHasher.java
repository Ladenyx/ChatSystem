package com.chatsystem.server.auth;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

/** PBKDF2WithHmacSHA256 password hasher stored as {@code pbkdf2$sha256$<iterations>$<saltB64>$<hashB64>}. */
@Component
public class Pbkdf2PasswordHasher implements PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2";
    private static final String DIGEST = "sha256";
    private static final int ITERATIONS = 65_536;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String hash(String rawPassword) {
        Objects.requireNonNull(rawPassword, "rawPassword");
        if (rawPassword.isEmpty()) {
            throw new IllegalArgumentException("rawPassword must not be empty");
        }
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return hashWithSalt(rawPassword, salt, ITERATIONS);
    }

    String hashWithSalt(String rawPassword, byte[] salt, int iterations) {
        byte[] derived = derive(rawPassword.toCharArray(), salt, iterations, KEY_BITS);
        Base64.Encoder encoder = Base64.getEncoder();
        return PREFIX + "$" + DIGEST + "$" + iterations + "$"
                + encoder.encodeToString(salt) + "$" + encoder.encodeToString(derived);
    }

    @Override
    public boolean matches(String rawPassword, String encodedHash) {
        if (rawPassword == null || rawPassword.isEmpty() || encodedHash == null || encodedHash.isBlank()) {
            return false;
        }
        String[] parts = encodedHash.split("\\$");
        if (parts.length != 5 || !PREFIX.equals(parts[0]) || !DIGEST.equals(parts[1])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[2]);
            if (iterations <= 0) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[3]);
            byte[] expected = Base64.getDecoder().decode(parts[4]);
            if (salt.length == 0 || expected.length == 0) {
                return false;
            }
            byte[] actual = derive(rawPassword.toCharArray(), salt, iterations, expected.length * 8);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations, int keyBits) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("PBKDF2 algorithm unavailable", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
