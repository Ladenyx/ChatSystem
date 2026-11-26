package com.chatsystem.common.domain;

/** Immutable conversation kind matching {@code conversations.type} in PostgreSQL. */
public enum ConversationType {
    DIRECT,
    GROUP
}
