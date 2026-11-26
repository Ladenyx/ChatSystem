package com.chatsystem.common.domain;

/** Account lifecycle status matching {@code users.status} and DEC-07. */
public enum AccountStatus {
    ACTIVE,
    LOCKED,
    DELETED
}
