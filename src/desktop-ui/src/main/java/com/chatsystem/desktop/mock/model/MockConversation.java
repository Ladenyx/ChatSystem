package com.chatsystem.desktop.mock.model;

import java.util.List;

/** Group conversation IDs match MockGroup IDs. Unread counts belong to the preview user. */
public record MockConversation(String id, String title, Kind kind, List<String> participantIds,
                               int unreadCount) {
    public MockConversation {
        participantIds = List.copyOf(participantIds);
    }

    public boolean encrypted() {
        return kind == Kind.GROUP;
    }

    public enum Kind { DIRECT, GROUP }
}
