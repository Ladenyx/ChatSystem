package com.chatsystem.server.auth;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.dto.auth.SessionInfo;
import com.chatsystem.common.ws.SessionRevocationReason;
import com.chatsystem.server.api.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes active session inspection and logout while login/registration flows belong to Auth domain work. */
@RestController
@RequestMapping(ApiHeaders.API_V1_PREFIX + "/auth")
public class AuthSessionController {
    private final SessionRevocationService revocationService;

    public AuthSessionController(SessionRevocationService revocationService) {
        this.revocationService = Objects.requireNonNull(revocationService, "revocationService");
    }

    @GetMapping("/me")
    public SessionInfo currentSession(HttpServletRequest request) {
        return requireSession(request).toSessionInfo();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        AuthenticatedSession session = requireSession(request);
        revocationService.revokeSingle(
                session.sessionId(),
                SessionRevocationReason.LOGOUT,
                "Phiên đăng nhập đã được đăng xuất."
        );
        return ResponseEntity.noContent().build();
    }

    private static AuthenticatedSession requireSession(HttpServletRequest request) {
        Object attr = request.getAttribute(AuthenticatedSession.REQUEST_ATTRIBUTE);
        if (attr instanceof AuthenticatedSession session) {
            return session;
        }
        throw new ApiException(ErrorCode.UNAUTHORIZED, "Không tìm thấy phiên đăng nhập hợp lệ.");
    }
}
