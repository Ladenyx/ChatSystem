package com.chatsystem.server.auth;

import com.chatsystem.common.ws.SessionRevocationReason;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Coordinates database revocation and immediate WebSocket termination for revoked sessions. */
@Service
public class SessionRevocationService {
    private final SessionRepository sessionRepository;
    private final SessionRevocationPublisher revocationPublisher;
    private final Clock clock;

    public SessionRevocationService(
            SessionRepository sessionRepository,
            SessionRevocationPublisher revocationPublisher,
            Clock clock
    ) {
        this.sessionRepository = Objects.requireNonNull(sessionRepository, "sessionRepository");
        this.revocationPublisher = Objects.requireNonNull(revocationPublisher, "revocationPublisher");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public boolean revokeSingle(UUID sessionId, SessionRevocationReason reason, String message) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(message, "message");
        boolean revoked = sessionRepository.revokeSession(sessionId, clock.instant());
        if (revoked) {
            revocationPublisher.publishSessionRevoked(sessionId, reason, message);
        }
        return revoked;
    }

    public List<UUID> revokeAllForUser(
            UUID userId,
            SessionRevocationReason reason,
            String message,
            UUID excludedSessionId
    ) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(message, "message");
        List<UUID> revokedIds = sessionRepository.revokeAllUserSessions(userId, clock.instant(), excludedSessionId);
        revokedIds.forEach(sessionId -> revocationPublisher.publishSessionRevoked(sessionId, reason, message));
        return revokedIds;
    }

    public List<UUID> revokeActiveAdminSessions(
            UUID userId,
            SessionRevocationReason reason,
            String message,
            UUID excludedSessionId
    ) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(message, "message");
        List<UUID> revokedIds = sessionRepository.revokeActiveAdminSessions(userId, clock.instant(), excludedSessionId);
        revokedIds.forEach(sessionId -> revocationPublisher.publishSessionRevoked(sessionId, reason, message));
        return revokedIds;
    }
}
