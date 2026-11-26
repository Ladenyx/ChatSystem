package com.chatsystem.common.ws;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Canonical Raw JSON WebSocket frame types for Client -> Server commands and Server -> Client events. */
public enum WsEventType {
    // Client -> Server commands
    SYSTEM_PING("system.ping", Direction.CLIENT_TO_SERVER, false),
    SYNC_RESUME("sync.resume", Direction.CLIENT_TO_SERVER, false),
    MESSAGE_SEND("message.send", Direction.CLIENT_TO_SERVER, false),
    RECEIPT_DELIVERED("receipt.delivered", Direction.CLIENT_TO_SERVER, false),
    RECEIPT_READ("receipt.read", Direction.CLIENT_TO_SERVER, false),

    // Server -> Client control & presence events (ephemeral, seq == null)
    SYSTEM_CONNECTED("system.connected", Direction.SERVER_TO_CLIENT, false),
    SYSTEM_PONG("system.pong", Direction.SERVER_TO_CLIENT, false),
    SYSTEM_ACK("system.ack", Direction.SERVER_TO_CLIENT, false),
    SYSTEM_ERROR("system.error", Direction.SERVER_TO_CLIENT, false),
    SESSION_REVOKED("session.revoked", Direction.SERVER_TO_CLIENT, false),
    SYNC_BATCH("sync.batch", Direction.SERVER_TO_CLIENT, false),
    PRESENCE_SNAPSHOT("presence.snapshot", Direction.SERVER_TO_CLIENT, false),
    PRESENCE_CHANGED("presence.changed", Direction.SERVER_TO_CLIENT, false),

    // Server -> Client sequenced User Client domain events (seq != null)
    MESSAGE_ACK("message.ack", Direction.SERVER_TO_CLIENT, true),
    MESSAGE_CREATED("message.created", Direction.SERVER_TO_CLIENT, true),
    MESSAGE_RECALLED("message.recalled", Direction.SERVER_TO_CLIENT, true),
    MESSAGE_DELETED_FOR_SELF("message.deleted_for_self", Direction.SERVER_TO_CLIENT, true),
    CONVERSATION_CLEARED("conversation.cleared", Direction.SERVER_TO_CLIENT, true),
    RECEIPT_UPDATED("receipt.updated", Direction.SERVER_TO_CLIENT, true),
    FRIEND_REQUEST_CREATED("friend_request.created", Direction.SERVER_TO_CLIENT, true),
    FRIEND_REQUEST_UPDATED("friend_request.updated", Direction.SERVER_TO_CLIENT, true),
    FRIENDSHIP_CREATED("friendship.created", Direction.SERVER_TO_CLIENT, true),
    FRIENDSHIP_REMOVED("friendship.removed", Direction.SERVER_TO_CLIENT, true),
    BLOCK_UPDATED("block.updated", Direction.SERVER_TO_CLIENT, true),
    GROUP_CREATED("group.created", Direction.SERVER_TO_CLIENT, true),
    GROUP_RENAMED("group.renamed", Direction.SERVER_TO_CLIENT, true),
    GROUP_MEMBERS_CHANGED("group.members_changed", Direction.SERVER_TO_CLIENT, true),
    GROUP_ROLE_UPDATED("group.role_updated", Direction.SERVER_TO_CLIENT, true),
    GROUP_CLOSED("group.closed", Direction.SERVER_TO_CLIENT, true),
    E2EE_DEVICE_TRUST_REQUESTED("e2ee.device_trust_requested", Direction.SERVER_TO_CLIENT, true),
    E2EE_DEVICE_TRUSTED("e2ee.device_trusted", Direction.SERVER_TO_CLIENT, true),
    E2EE_DEVICE_REVOKED("e2ee.device_revoked", Direction.SERVER_TO_CLIENT, true),
    E2EE_GROUP_EPOCH_ROTATED("e2ee.group_epoch_rotated", Direction.SERVER_TO_CLIENT, true),
    E2EE_KEY_ENVELOPE_AVAILABLE("e2ee.key_envelope_available", Direction.SERVER_TO_CLIENT, true),

    // Server -> Client Admin App push events (ephemeral, ADMIN_APP scope only)
    ADMIN_KPI_UPDATED("admin.kpi.updated", Direction.SERVER_TO_CLIENT, false),
    ADMIN_SPAM_REPORT_CREATED("admin.spam_report.created", Direction.SERVER_TO_CLIENT, false),
    ADMIN_SPAM_REPORT_REVIEWED("admin.spam_report.reviewed", Direction.SERVER_TO_CLIENT, false),
    ADMIN_USER_STATUS_CHANGED("admin.user.status_changed", Direction.SERVER_TO_CLIENT, false);

    public enum Direction {
        CLIENT_TO_SERVER,
        SERVER_TO_CLIENT
    }

    private static final Map<String, WsEventType> BY_WIRE_NAME = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(WsEventType::wireName, Function.identity()));

    private final String wireName;
    private final Direction direction;
    private final boolean sequenced;

    WsEventType(String wireName, Direction direction, boolean sequenced) {
        this.wireName = wireName;
        this.direction = direction;
        this.sequenced = sequenced;
    }

    public String wireName() {
        return wireName;
    }

    public Direction direction() {
        return direction;
    }

    public boolean sequenced() {
        return sequenced;
    }

    public static Optional<WsEventType> findByWireName(String wireName) {
        if (wireName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_WIRE_NAME.get(wireName));
    }

    public static WsEventType fromWireName(String wireName) {
        Objects.requireNonNull(wireName, "wireName");
        return findByWireName(wireName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown WebSocket event type: " + wireName));
    }
}
