package com.chatsystem.common.ws;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Payload for {@code presence.snapshot} sent after a {@code USER_CLIENT} connects. */
public record WsPresenceSnapshotPayload(List<UUID> onlineUserIds) {
    public WsPresenceSnapshotPayload {
        Objects.requireNonNull(onlineUserIds, "onlineUserIds");
        onlineUserIds = List.copyOf(onlineUserIds);
    }
}
