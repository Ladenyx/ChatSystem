package com.chatsystem.server.auth;

import java.util.UUID;

/** Authenticates opaque session tokens for REST requests, WSS handshakes, and live fan-out double-checks. */
public interface SessionAuthenticator {
    AuthenticatedSession authenticate(String rawBearerToken, UUID clientDeviceId);

    boolean isSessionStillActive(UUID sessionId);
}
