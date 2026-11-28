package com.chatsystem.server.ws;

import com.chatsystem.common.api.ApiError;
import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.ws.SessionRevocationReason;
import com.chatsystem.common.ws.WsCloseCode;
import com.chatsystem.common.ws.WsCommand;
import com.chatsystem.common.ws.WsConnectedPayload;
import com.chatsystem.common.ws.WsEvent;
import com.chatsystem.common.ws.WsEventType;
import com.chatsystem.server.api.ApiException;
import com.chatsystem.server.auth.AuthenticatedSession;
import com.chatsystem.server.auth.SessionAuthenticator;
import com.chatsystem.server.config.ChatServerProperties;
import java.time.Clock;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Central Raw-JSON WebSocket handler for {@code /ws} across {@code USER_CLIENT} and {@code ADMIN_APP}. */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final TypeReference<WsCommand<JsonNode>> COMMAND_TYPE = new TypeReference<>() {
    };

    private final WsSessionRegistry sessionRegistry;
    private final SessionAuthenticator authenticator;
    private final PresenceService presenceService;
    private final UserEventLog userEventLog;
    private final WsEventDispatcher dispatcher;
    private final Map<WsEventType, WsCommandHandler> handlers;
    private final ObjectMapper objectMapper;
    private final ChatServerProperties properties;
    private final Clock clock;

    public ChatWebSocketHandler(
            WsSessionRegistry sessionRegistry,
            SessionAuthenticator authenticator,
            PresenceService presenceService,
            UserEventLog userEventLog,
            WsEventDispatcher dispatcher,
            List<WsCommandHandler> commandHandlers,
            ObjectMapper objectMapper,
            ChatServerProperties properties,
            Clock clock
    ) {
        this.sessionRegistry = Objects.requireNonNull(sessionRegistry, "sessionRegistry");
        this.authenticator = Objects.requireNonNull(authenticator, "authenticator");
        this.presenceService = Objects.requireNonNull(presenceService, "presenceService");
        this.userEventLog = Objects.requireNonNull(userEventLog, "userEventLog");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.handlers = new EnumMap<>(WsEventType.class);
        for (WsCommandHandler handler : Objects.requireNonNull(commandHandlers, "commandHandlers")) {
            this.handlers.put(handler.commandType(), handler);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        Object attr = webSocketSession.getAttributes().get(WebSocketAuthHandshakeInterceptor.SESSION_ATTRIBUTE);
        if (!(attr instanceof AuthenticatedSession session)) {
            WsConnection.closeRaw(webSocketSession, WsCloseCode.UNAUTHORIZED, "Missing authenticated session");
            return;
        }
        WsConnection connection = sessionRegistry.register(webSocketSession, session);
        long latestSeq = userEventLog.latestSeq(session.userId());
        WsEvent<WsConnectedPayload> connected = WsEvent.ephemeral(
                WsEventType.SYSTEM_CONNECTED,
                UUID.randomUUID(),
                null,
                clock.instant(),
                new WsConnectedPayload(
                        session.sessionId(),
                        session.userId(),
                        session.scope(),
                        latestSeq,
                        clock.instant(),
                        properties.ws().heartbeatIntervalSec()
                )
        );
        dispatcher.sendDirect(connection, connected);
        presenceService.onConnected(connection);
    }

    @Override
    protected void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) {
        WsConnection connection = sessionRegistry.findByWsId(webSocketSession.getId());
        if (connection == null) {
            return;
        }
        connection.touch(clock.instant());
        if (!authenticator.isSessionStillActive(connection.session().sessionId())) {
            sessionRegistry.revokeConnection(
                    connection,
                    SessionRevocationReason.LOGOUT,
                    "Phiên đăng nhập không còn hiệu lực."
            );
            return;
        }
        UUID requestId = null;
        try {
            WsCommand<JsonNode> command = objectMapper.readValue(message.getPayload(), COMMAND_TYPE);
            requestId = command.requestId();
            WsCommandHandler handler = handlers.get(command.eventType());
            if (handler == null) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "Lệnh WebSocket chưa được hỗ trợ: " + command.type());
            }
            if (!handler.supportsScope(connection.session().scope())) {
                throw new ApiException(
                        ErrorCode.SESSION_SCOPE_MISMATCH,
                        "Phạm vi phiên hiện tại không được phép thực hiện lệnh " + command.type() + "."
                );
            }
            handler.handle(connection, command, dispatcher);
        } catch (ApiException ex) {
            sendError(connection, requestId, ex.getErrorCode(), ex.getMessage(), ex.getDetails());
        } catch (JacksonException | IllegalArgumentException ex) {
            sendError(connection, requestId, ErrorCode.VALIDATION_ERROR, "Khung lệnh WebSocket không hợp lệ.", List.of());
        } catch (Exception ex) {
            sendError(connection, requestId, ErrorCode.INTERNAL_ERROR, "Lỗi xử lý lệnh WebSocket.", List.of());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus status) {
        WsConnection connection = sessionRegistry.unregister(webSocketSession.getId());
        if (connection != null) {
            presenceService.onDisconnected(connection);
        }
    }

    private void sendError(
            WsConnection connection,
            UUID requestId,
            ErrorCode errorCode,
            String message,
            List<com.chatsystem.common.api.FieldError> details
    ) {
        String requestIdStr = requestId != null ? requestId.toString() : UUID.randomUUID().toString();
        ApiError error = ApiError.of(
                errorCode,
                message,
                clock.instant(),
                ApiHeaders.WS_PATH,
                requestIdStr,
                details
        );
        WsEvent<ApiError> errorEvent = WsEvent.ephemeral(
                WsEventType.SYSTEM_ERROR,
                UUID.randomUUID(),
                requestId,
                clock.instant(),
                error
        );
        dispatcher.sendDirect(connection, errorEvent);
    }
}
