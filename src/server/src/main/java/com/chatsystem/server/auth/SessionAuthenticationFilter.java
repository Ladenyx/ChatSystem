package com.chatsystem.server.auth;

import com.chatsystem.common.api.ApiError;
import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.server.api.ApiException;
import com.chatsystem.server.api.RequestIdFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

/** Validates Bearer opaque session tokens and {@code SessionScope} for all protected {@code /api/v1/**} routes. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class SessionAuthenticationFilter extends OncePerRequestFilter {
    private final SessionAuthenticator authenticator;
    private final SessionAuthorizationPolicy authorizationPolicy;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public SessionAuthenticationFilter(
            SessionAuthenticator authenticator,
            SessionAuthorizationPolicy authorizationPolicy,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.authenticator = Objects.requireNonNull(authenticator, "authenticator");
        this.authorizationPolicy = Objects.requireNonNull(authorizationPolicy, "authorizationPolicy");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith(ApiHeaders.API_V1_PREFIX + "/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (authorizationPolicy.isPublic(request.getMethod(), path)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String rawToken = AuthHeaderExtractor.extractBearerToken(request.getHeader(ApiHeaders.AUTHORIZATION));
            UUID deviceId = AuthHeaderExtractor.parseOptionalDeviceId(request.getHeader(ApiHeaders.CLIENT_DEVICE_ID));
            AuthenticatedSession session = authenticator.authenticate(rawToken, deviceId);
            authorizationPolicy.authorize(path, session);
            request.setAttribute(AuthenticatedSession.REQUEST_ATTRIBUTE, session);
            filterChain.doFilter(request, response);
        } catch (ApiException ex) {
            writeApiError(request, response, ex);
        }
    }

    private void writeApiError(
            HttpServletRequest request,
            HttpServletResponse response,
            ApiException ex
    ) throws IOException {
        ApiError error = ApiError.of(
                ex.getErrorCode(),
                ex.getMessage(),
                clock.instant(),
                request.getRequestURI(),
                RequestIdFilter.resolve(request),
                ex.getDetails()
        );
        response.setStatus(error.status());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), error);
    }
}
