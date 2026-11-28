package com.chatsystem.server.ws;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.common.dto.messaging.SyncEventsResponse;
import com.chatsystem.server.api.ApiException;
import com.chatsystem.server.auth.AuthenticatedSession;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HTTP catch-up endpoint backing {@code GET /api/v1/sync/events} alongside WS {@code sync.resume}. */
@RestController
@RequestMapping(ApiHeaders.API_V1_PREFIX + "/sync")
public class SyncController {
    private final UserEventLog userEventLog;

    public SyncController(UserEventLog userEventLog) {
        this.userEventLog = Objects.requireNonNull(userEventLog, "userEventLog");
    }

    @GetMapping("/events")
    public SyncEventsResponse fetchEvents(
            @RequestParam(name = "afterSeq", defaultValue = "0") long afterSeq,
            @RequestParam(name = "limit", defaultValue = "200") int limit,
            HttpServletRequest request
    ) {
        if (afterSeq < 0) {
            throw ApiException.validation("afterSeq", "MIN_VALUE", "afterSeq phải lớn hơn hoặc bằng 0.");
        }
        if (limit < 1 || limit > ApiHeaders.MAX_SYNC_BATCH_SIZE) {
            throw ApiException.validation(
                    "limit",
                    "OUT_OF_RANGE",
                    "limit phải nằm trong khoảng 1.." + ApiHeaders.MAX_SYNC_BATCH_SIZE + "."
            );
        }
        Object attr = request.getAttribute(AuthenticatedSession.REQUEST_ATTRIBUTE);
        if (!(attr instanceof AuthenticatedSession session)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Không tìm thấy phiên đăng nhập hợp lệ.");
        }
        return userEventLog.fetchAfter(session.userId(), afterSeq, limit, ignored -> true);
    }
}
