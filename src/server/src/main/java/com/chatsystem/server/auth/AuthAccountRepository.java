package com.chatsystem.server.auth;

import com.chatsystem.common.domain.SessionScope;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Persistence port for account lookup, session creation, and login/app-open audit writes. */
public interface AuthAccountRepository {
    Optional<AuthAccountRecord> findByUsernameIgnoreCase(String username);

    UUID insertSession(UUID userId, SessionScope scope, String tokenHash, Instant createdAt);

    void insertLoginLog(UUID userId, SessionScope scope, Instant loginAt);

    void recordAppOpen(UUID userId, UUID launchId, SessionScope scope);
}
