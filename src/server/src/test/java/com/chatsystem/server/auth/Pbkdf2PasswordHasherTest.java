package com.chatsystem.server.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class Pbkdf2PasswordHasherTest {
    private static final String SEED_ADMIN_HASH =
            "pbkdf2$sha256$65536$V2hhdHNVcEFkbWluU2FsdA==$aXmt/Mq+SpYsUB63qBsjcmXrmetLHFiThTOSbsY/hnQ=";
    private static final String SEED_USER_HASH =
            "pbkdf2$sha256$65536$V2hhdHNVcFVzZXJTYWx0MQ==$g5dLpqBRue3mhrFRB+Xvbc91lZ1ojydYcTPWXhAPJAI=";

    private final Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();

    @Test
    void hashesWithRandomSaltsAndVerifiesPasswords() {
        String h1 = hasher.hash("MatKhau#2026");
        String h2 = hasher.hash("MatKhau#2026");

        assertThat(h1).isNotEqualTo(h2);
        assertThat(hasher.matches("MatKhau#2026", h1)).isTrue();
        assertThat(hasher.matches("MatKhau#2026", h2)).isTrue();
        assertThat(hasher.matches("SaiMatKhau", h1)).isFalse();
        assertThat(hasher.matches(null, h1)).isFalse();
        assertThat(hasher.matches("MatKhau#2026", "malformed")).isFalse();
    }

    @Test
    void matchesHashesEmbeddedInSeedSql() {
        assertThat(hasher.matches("Admin@123", SEED_ADMIN_HASH)).isTrue();
        assertThat(hasher.matches("User@123", SEED_USER_HASH)).isTrue();
        assertThat(hasher.matches("Admin@123", SEED_USER_HASH)).isFalse();
    }
}
