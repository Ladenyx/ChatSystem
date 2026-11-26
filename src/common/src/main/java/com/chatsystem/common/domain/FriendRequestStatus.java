package com.chatsystem.common.domain;

/** Friend request lifecycle status matching {@code friend_requests.status}. */
public enum FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED
}
