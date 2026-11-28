package com.chatsystem.server.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SessionTokenHasherTest {
    @Test
    void sha256HexReturnsDeterministicLowercase64CharDigest() {
        String first = SessionTokenHasher.sha256Hex("opaque-token-sample");
        String second = SessionTokenHasher.sha256Hex("opaque-token-sample");
        String other = SessionTokenHasher.sha256Hex("opaque-token-other");

        assertThat(first)
                .hasSize(64)
                .matches("[0-9a-f]{64}")
                .isEqualTo(second)
                .isNotEqualTo(other);
    }

    @Test
    void sha256HexRejectsBlankInput() {
        assertThatThrownBy(() -> SessionTokenHasher.sha256Hex("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
