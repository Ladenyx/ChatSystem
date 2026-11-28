package com.chatsystem.server.ws;

import com.chatsystem.common.ws.WsCloseCode;
import com.chatsystem.server.auth.AuthenticatedSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/** Thread-safe wrapper around a live {@link WebSocketSession} tracking its authenticated session and activity. */
public final class WsConnection {
    private static final Logger LOG = LoggerFactory.getLogger(WsConnection.class);
    private static final int MAX_CLOSE_REASON_BYTES = 120;

    private final WebSocketSession delegate;
    private final AuthenticatedSession session;
    private final Object sendLock = new Object();
    private volatile Instant lastActivityAt;

    public WsConnection(WebSocketSession delegate, AuthenticatedSession session, Instant connectedAt) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.session = Objects.requireNonNull(session, "session");
        this.lastActivityAt = Objects.requireNonNull(connectedAt, "connectedAt");
    }

    public String wsId() {
        return delegate.getId();
    }

    public AuthenticatedSession session() {
        return session;
    }

    public Instant lastActivityAt() {
        return lastActivityAt;
    }

    public void touch(Instant now) {
        lastActivityAt = Objects.requireNonNull(now, "now");
    }

    public boolean isOpen() {
        return delegate.isOpen();
    }

    public boolean sendText(String json) {
        Objects.requireNonNull(json, "json");
        synchronized (sendLock) {
            if (!delegate.isOpen()) {
                return false;
            }
            try {
                delegate.sendMessage(new TextMessage(json));
                return true;
            } catch (IOException | IllegalStateException ex) {
                LOG.debug("Failed sending WS frame to {}: {}", delegate.getId(), ex.getMessage());
                return false;
            }
        }
    }

    public void close(WsCloseCode closeCode, String reason) {
        Objects.requireNonNull(closeCode, "closeCode");
        synchronized (sendLock) {
            closeRaw(delegate, closeCode, reason);
        }
    }

    public static void closeRaw(WebSocketSession session, WsCloseCode closeCode, String reason) {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(closeCode, "closeCode");
        if (!session.isOpen()) {
            return;
        }
        try {
            session.close(new CloseStatus(closeCode.code(), truncateUtf8(reason)));
        } catch (IOException | IllegalStateException ex) {
            LOG.debug("Failed closing WS {}: {}", session.getId(), ex.getMessage());
        }
    }

    private static String truncateUtf8(String reason) {
        if (reason == null || reason.isBlank()) {
            return "";
        }
        String trimmed = reason.trim();
        byte[] bytes = trimmed.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= MAX_CLOSE_REASON_BYTES) {
            return trimmed;
        }
        int end = trimmed.length();
        while (end > 0 && trimmed.substring(0, end).getBytes(StandardCharsets.UTF_8).length > MAX_CLOSE_REASON_BYTES) {
            end--;
        }
        return trimmed.substring(0, end);
    }
}
