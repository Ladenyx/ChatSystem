package com.chatsystem.server.auth;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.dto.auth.LoginRequest;
import com.chatsystem.common.dto.auth.LoginResponse;
import java.util.Objects;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes {@code POST /api/v1/auth/login} for User Client and Admin App sessions. */
@RestController
@RequestMapping(ApiHeaders.API_V1_PREFIX + "/auth")
public class AuthLoginController {
    private final AuthLoginService authLoginService;

    public AuthLoginController(AuthLoginService authLoginService) {
        this.authLoginService = Objects.requireNonNull(authLoginService, "authLoginService");
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authLoginService.login(request);
    }
}
