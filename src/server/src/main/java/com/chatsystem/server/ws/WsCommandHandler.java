package com.chatsystem.server.ws;

import com.chatsystem.common.domain.SessionScope;
import com.chatsystem.common.ws.WsCommand;
import com.chatsystem.common.ws.WsEventType;
import tools.jackson.databind.JsonNode;

/** Strategy interface for handling an individual {@code CLIENT_TO_SERVER} WebSocket command. */
public interface WsCommandHandler {
    WsEventType commandType();

    boolean supportsScope(SessionScope scope);

    void handle(WsConnection connection, WsCommand<JsonNode> command, WsEventDispatcher dispatcher);
}
