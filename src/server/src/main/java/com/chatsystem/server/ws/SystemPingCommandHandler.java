package com.chatsystem.server.ws;

import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.ws.WsCommand;
import com.chatsystem.common.ws.WsEvent;
import com.chatsystem.common.ws.WsEventType;
import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class SystemPingCommandHandler implements WsCommandHandler {
    private final Clock clock;

    public SystemPingCommandHandler(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public WsEventType commandType() {
        return WsEventType.SYSTEM_PING;
    }

    @Override
    public boolean supportsScope(SessionScope scope) {
        return scope == SessionScope.USER_CLIENT || scope == SessionScope.ADMIN_APP;
    }

    @Override
    public void handle(WsConnection connection, WsCommand<JsonNode> command, WsEventDispatcher dispatcher) {
        WsEvent<Map<String, Object>> pong = WsEvent.ephemeral(
                WsEventType.SYSTEM_PONG,
                UUID.randomUUID(),
                command.requestId(),
                clock.instant(),
                Map.of("serverTime", clock.instant())
        );
        dispatcher.sendDirect(connection, pong);
    }
}
