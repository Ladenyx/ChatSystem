package com.chatsystem.user.service.model;

/**
 * Choice of target user to report in a conversation.
 */
public record TargetUserOption(
        String id,
        String displayName
) {
    @Override
    public String toString() {
        return displayName;
    }
}
