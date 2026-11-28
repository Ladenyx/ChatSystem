package com.chatsystem.server.auth;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.server.api.ApiException;
import java.util.UUID;

/** Shared parser for {@code Authorization: Bearer} and optional {@code X-Client-Device-Id} headers. */
public final class AuthHeaderExtractor {
    private AuthHeaderExtractor() {
    }

    public static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(ApiHeaders.BEARER_PREFIX)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Yêu cầu header Authorization: Bearer <token>.");
        }
        String token = authorizationHeader.substring(ApiHeaders.BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Mã Bearer token không được để trống.");
        }
        return token;
    }

    public static UUID parseOptionalDeviceId(String rawDeviceId) {
        if (rawDeviceId == null || rawDeviceId.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(rawDeviceId.trim());
        } catch (IllegalArgumentException ex) {
            throw ApiException.validation(
                    ApiHeaders.CLIENT_DEVICE_ID,
                    "INVALID_UUID",
                    "Header X-Client-Device-Id phải là UUID hợp lệ."
            );
        }
    }
}
