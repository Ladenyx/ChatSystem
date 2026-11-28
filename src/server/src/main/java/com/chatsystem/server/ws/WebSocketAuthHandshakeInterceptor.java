package com.chatsystem.server.ws;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.server.api.ApiException;
import com.chatsystem.server.auth.AuthHeaderExtractor;
import com.chatsystem.server.auth.AuthenticatedSession;
import com.chatsystem.server.auth.SessionAuthenticator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/** Authenticates WebSocket upgrade requests strictly from HTTP headers before switching protocols. */
@Component
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {
    public static final String SESSION_ATTRIBUTE = AuthenticatedSession.REQUEST_ATTRIBUTE;

    private final SessionAuthenticator authenticator;

    public WebSocketAuthHandshakeInterceptor(SessionAuthenticator authenticator) {
        this.authenticator = Objects.requireNonNull(authenticator, "authenticator");
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        try {
            String rawToken = AuthHeaderExtractor.extractBearerToken(
                    request.getHeaders().getFirst(ApiHeaders.AUTHORIZATION)
            );
            UUID deviceId = AuthHeaderExtractor.parseOptionalDeviceId(
                    request.getHeaders().getFirst(ApiHeaders.CLIENT_DEVICE_ID)
            );
            AuthenticatedSession session = authenticator.authenticate(rawToken, deviceId);
            attributes.put(SESSION_ATTRIBUTE, session);
            return true;
        } catch (ApiException ex) {
            response.setStatusCode(HttpStatus.valueOf(ex.getErrorCode().defaultStatus()));
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // No-op.
    }
}
