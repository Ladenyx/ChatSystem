package com.chatsystem.desktop.mock.model;

import java.time.Instant;

/** A successful login sample; the two application session scopes remain distinct. */
public record MockLoginLog(String id, String userId, Instant loggedInAt, Scope scope) {
    public enum Scope { USER_CLIENT, ADMIN_APP }
}
