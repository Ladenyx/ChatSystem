package com.chatsystem.desktop.client;

import com.chatsystem.common.dto.auth.SessionInfo;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/** Thread-safe in-memory holder for the active client session token, process launch ID, and device ID. */
public final class ClientSessionStore {
    private final UUID launchId = UUID.randomUUID();
    private final AtomicReference<String> sessionToken = new AtomicReference<>();
    private final AtomicReference<SessionInfo> sessionInfo = new AtomicReference<>();
    private final AtomicReference<UUID> deviceId = new AtomicReference<>();

    public UUID launchId() {
        return launchId;
    }

    public Optional<String> sessionToken() {
        return Optional.ofNullable(sessionToken.get());
    }

    public Optional<SessionInfo> sessionInfo() {
        return Optional.ofNullable(sessionInfo.get());
    }

    public Optional<UUID> deviceId() {
        return Optional.ofNullable(deviceId.get());
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId.set(deviceId);
    }

    public void updateSession(String token, SessionInfo info) {
        Objects.requireNonNull(token, "token");
        Objects.requireNonNull(info, "info");
        if (token.isBlank()) {
            throw new IllegalArgumentException("session token must not be blank");
        }
        sessionToken.set(token.trim());
        sessionInfo.set(info);
    }

    public void clear() {
        sessionToken.set(null);
        sessionInfo.set(null);
    }

    public boolean isAuthenticated() {
        return sessionToken.get() != null && sessionInfo.get() != null;
    }
}
