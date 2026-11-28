package com.chatsystem.server.api;

import com.chatsystem.common.api.ApiHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Propagates or assigns {@code X-Request-Id} for request tracing and {@code ApiError.requestId}. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID_ATTR = RequestIdFilter.class.getName() + ".REQUEST_ID";
    public static final String MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = parseOrGenerateUuid(request.getHeader(ApiHeaders.REQUEST_ID));
        request.setAttribute(REQUEST_ID_ATTR, requestId);
        response.setHeader(ApiHeaders.REQUEST_ID, requestId);
        MDC.put(MDC_KEY, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    public static String resolve(HttpServletRequest request) {
        Object attr = request.getAttribute(REQUEST_ID_ATTR);
        if (attr instanceof String id && !id.isBlank()) {
            return id;
        }
        return parseOrGenerateUuid(request.getHeader(ApiHeaders.REQUEST_ID));
    }

    private static String parseOrGenerateUuid(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return UUID.randomUUID().toString();
        }
        try {
            return UUID.fromString(candidate.trim()).toString();
        } catch (IllegalArgumentException ignored) {
            return UUID.randomUUID().toString();
        }
    }
}
