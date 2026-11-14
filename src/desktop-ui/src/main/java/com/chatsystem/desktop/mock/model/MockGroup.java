package com.chatsystem.desktop.mock.model;

import java.time.Instant;
import java.util.List;

/** All group samples represent E2EE groups; no keys or ciphertext are generated. */
public record MockGroup(String id, String name, Instant createdAt, List<Member> members) {
    public MockGroup {
        members = List.copyOf(members);
    }

    public record Member(String userId, Role role) {
    }

    public enum Role { ADMIN, MEMBER }
}
