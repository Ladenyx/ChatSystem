package com.chatsystem.server.api;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.api.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiHeaders.API_V1_PREFIX)
public class HealthController {
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    public HealthController(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @GetMapping("/health")
    public HealthResponse health() {
        try {
            Integer probe = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (!Integer.valueOf(1).equals(probe)) {
                throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Kiểm tra kết nối PostgreSQL không hợp lệ.");
            }
            return new HealthResponse("UP", "UP", clock.instant());
        } catch (DataAccessException ex) {
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE, "Không thể kết nối tới PostgreSQL.");
        }
    }

    public record HealthResponse(String status, String database, Instant serverTime) {
    }
}
