package com.chatsystem.common.ws;

/** Reason carried inside a {@code session.revoked} WebSocket event before the socket closes. */
public enum SessionRevocationReason {
    ADMIN_NEW_LOGIN,
    ACCOUNT_LOCKED,
    ACCOUNT_DELETED,
    PASSWORD_CHANGED,
    LOGOUT
}
