package com.chatsystem.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApiContractModelsTest {
    private static final Instant NOW = Instant.parse("2026-11-15T03:00:00Z");

    @Test
    void everyErrorCodeHasValidHttpErrorStatus() {
        for (ErrorCode code : ErrorCode.values()) {
            assertTrue(code.defaultStatus() >= 400 && code.defaultStatus() <= 599,
                    () -> code + " has invalid HTTP status: " + code.defaultStatus());
        }
    }

    @Test
    void apiErrorFactoryDerivesStatusRetryableAndCopiesDetailsDefensively() {
        var mutableDetails = new ArrayList<>(List.of(
                new FieldError("username", "INVALID_FORMAT", "Must be 6-30 ASCII chars")));

        ApiError error = ApiError.of(
                ErrorCode.VALIDATION_ERROR,
                "Dữ liệu không hợp lệ.",
                NOW,
                "/api/v1/admin/users",
                "req-01",
                mutableDetails
        );
        mutableDetails.clear();

        assertEquals(400, error.status());
        assertFalse(error.retryable());
        assertEquals(1, error.details().size());
        assertThrows(UnsupportedOperationException.class, () -> error.details().add(
                new FieldError("email", "REQUIRED", "Required")));
    }

    @Test
    void apiErrorRejectsNonErrorHttpStatus() {
        assertThrows(IllegalArgumentException.class, () -> new ApiError(
                ErrorCode.INTERNAL_ERROR, "Unexpected", 200, NOW, "/api/v1/test", "req-02", true, List.of()));
    }

    @Test
    void cursorPageEnforcesCursorWhenHasMoreIsTrue() {
        var page = new CursorPage<>(List.of("a", "b"), "opaque-cursor", true);
        assertEquals(2, page.items().size());
        assertTrue(page.hasMore());

        assertThrows(IllegalArgumentException.class, () -> new CursorPage<>(List.of("a"), null, true));
        assertThrows(IllegalArgumentException.class, () -> new CursorPage<>(List.of("a"), "   ", true));
        assertEquals(0, CursorPage.empty().items().size());
    }

    @Test
    void offsetPageComputesTotalPagesAccuratelyAndRejectsMismatch() {
        var empty = OffsetPage.of(List.of(), 0, 25, 0);
        assertEquals(0, empty.totalPages());

        var exact = OffsetPage.of(List.of("u1", "u2"), 0, 25, 50);
        assertEquals(2, exact.totalPages());

        var remainder = OffsetPage.of(List.of("u1"), 2, 25, 51);
        assertEquals(3, remainder.totalPages());

        assertThrows(IllegalArgumentException.class,
                () -> new OffsetPage<>(List.of("u1"), 0, 25, 51, 2));
        assertThrows(IllegalArgumentException.class,
                () -> OffsetPage.of(List.of(), 0, ApiHeaders.MAX_PAGE_SIZE + 1, 0));
    }
}
