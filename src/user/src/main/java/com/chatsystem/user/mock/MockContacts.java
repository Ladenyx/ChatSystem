package com.chatsystem.user.mock;

import java.util.Map;

/** Relationship overlay for the shared MockData preview user. */
public final class MockContacts {
    public enum Relationship { NONE, FRIEND, INCOMING, OUTGOING, BLOCKED, BLOCKED_ME }

    private MockContacts() {
    }

    public static Map<String, Relationship> initialRelationships() {
        return Map.of(
                "usr-02", Relationship.FRIEND,
                "usr-03", Relationship.FRIEND,
                "usr-04", Relationship.FRIEND,
                "usr-05", Relationship.FRIEND,
                "usr-06", Relationship.FRIEND,
                "usr-07", Relationship.INCOMING,
                "usr-08", Relationship.INCOMING,
                "usr-09", Relationship.BLOCKED,
                "usr-10", Relationship.BLOCKED_ME,
                "usr-11", Relationship.OUTGOING);
    }
}
