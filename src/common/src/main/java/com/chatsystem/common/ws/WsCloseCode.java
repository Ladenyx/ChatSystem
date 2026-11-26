package com.chatsystem.common.ws;

/** Standard and application-specific WebSocket close codes defined in {@code websocket-protocol.md}. */
public enum WsCloseCode {
    NORMAL_CLOSURE(1000, false),
    UNAUTHORIZED(4001, false),
    SESSION_REVOKED(4002, false),
    ACCOUNT_RESTRICTED(4003, false),
    ADMIN_SESSION_REPLACED(4004, false),
    HEARTBEAT_TIMEOUT(4008, true),
    RESYNC_REQUIRED(4009, true);

    private final int code;
    private final boolean reconnectable;

    WsCloseCode(int code, boolean reconnectable) {
        this.code = code;
        this.reconnectable = reconnectable;
    }

    public int code() {
        return code;
    }

    public boolean reconnectable() {
        return reconnectable;
    }

    public static WsCloseCode fromCode(int code) {
        for (WsCloseCode candidate : values()) {
            if (candidate.code == code) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Unknown WebSocket close code: " + code);
    }
}
