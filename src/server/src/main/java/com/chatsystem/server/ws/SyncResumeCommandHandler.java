package com.chatsystem.server.ws;

import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.dto.messaging.SyncEventsResponse;
import com.chatsystem.common.ws.WsCommand;
import com.chatsystem.common.ws.WsEvent;
import com.chatsystem.common.ws.WsEventType;
import com.chatsystem.common.ws.WsSyncResumePayload;
import com.chatsystem.server.api.ApiException;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class SyncResumeCommandHandler implements WsCommandHandler {
    private final UserEventLog userEventLog;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public SyncResumeCommandHandler(UserEventLog userEventLog, ObjectMapper objectMapper, Clock clock) {
        this.userEventLog = Objects.requireNonNull(userEventLog, "userEventLog");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public WsEventType commandType() {
        return WsEventType.SYNC_RESUME;
    }

    @Override
    public boolean supportsScope(SessionScope scope) {
        return scope == SessionScope.USER_CLIENT;
    }

    @Override
    public void handle(WsConnection connection, WsCommand<JsonNode> command, WsEventDispatcher dispatcher) {
        WsSyncResumePayload resume;
        try {
            resume = objectMapper.treeToValue(command.payload(), WsSyncResumePayload.class);
        } catch (JacksonException | IllegalArgumentException ex) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Payload sync.resume không hợp lệ.");
        }
        SyncEventsResponse batch = userEventLog.fetchAfter(
                connection.session().userId(),
                resume.lastAckedSeq(),
                resume.limit(),
                ignored -> true
        );
        WsEvent<SyncEventsResponse> response = WsEvent.ephemeral(
                WsEventType.SYNC_BATCH,
                UUID.randomUUID(),
                command.requestId(),
                clock.instant(),
                batch
        );
        dispatcher.sendDirect(connection, response);
    }
}
