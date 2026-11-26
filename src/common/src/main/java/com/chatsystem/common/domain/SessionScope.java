package com.chatsystem.common.domain;

/** Application session scope matching {@code user_sessions.scope} and {@code login_logs.session_scope}. */
public enum SessionScope {
    USER_CLIENT,
    ADMIN_APP
}
