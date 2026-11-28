package com.chatsystem.server.auth;

/** Port for hashing and verifying stored account password digests. */
public interface PasswordHasher {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String encodedHash);
}
