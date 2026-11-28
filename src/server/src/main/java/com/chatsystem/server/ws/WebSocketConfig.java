package com.chatsystem.server.ws;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.server.config.ChatServerProperties;
import java.util.Objects;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final ChatWebSocketHandler webSocketHandler;
    private final WebSocketAuthHandshakeInterceptor handshakeInterceptor;
    private final ChatServerProperties properties;

    public WebSocketConfig(
            ChatWebSocketHandler webSocketHandler,
            WebSocketAuthHandshakeInterceptor handshakeInterceptor,
            ChatServerProperties properties
    ) {
        this.webSocketHandler = Objects.requireNonNull(webSocketHandler, "webSocketHandler");
        this.handshakeInterceptor = Objects.requireNonNull(handshakeInterceptor, "handshakeInterceptor");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] allowedOrigins = properties.ws().allowedOrigins().toArray(String[]::new);
        registry.addHandler(webSocketHandler, ApiHeaders.WS_PATH)
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins(allowedOrigins);
    }
}
