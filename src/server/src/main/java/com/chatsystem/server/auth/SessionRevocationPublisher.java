package com.chatsystem.server.auth;

import com.chatsystem.common.ws.SessionRevocationReason;
import java.util.UUID;

/** Pushes live {@code session.revoked} frames and closes affected WebSocket connections. */
public interface SessionRevocationPublisher {
    void publishSessionRevoked(UUID sessionId, SessionRevocationReason reason, String message);
}
