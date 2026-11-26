package com.chatsystem.common.domain;

/** Reason recorded when a friend request transitions to {@code CANCELLED}, matching {@code friend_requests.cancel_reason}. */
public enum FriendRequestCancelReason {
    SENDER_WITHDREW,
    BLOCKED
}
