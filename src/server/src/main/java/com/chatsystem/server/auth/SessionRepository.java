package com.chatsystem.server.auth;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Persistence port for opaque sessions stored in {@code user_sessions}. */
public interface SessionRepository {
    Optional<AuthenticatedSession> findByTokenHash(String tokenHash);

    Optional<AuthenticatedSession> findBySessionId(UUID sessionId);

    boolean revokeSession(UUID sessionId, Instant revokedAt);

    List<UUID> revokeAllUserSessions(UUID userId, Instant revokedAt, UUID excludedSessionId);

    List<UUID> revokeActiveAdminSessions(UUID userId, Instant revokedAt, UUID excludedSessionId);
}
