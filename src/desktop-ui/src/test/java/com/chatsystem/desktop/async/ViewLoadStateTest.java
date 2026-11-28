package com.chatsystem.desktop.async;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.common.api.ApiError;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.desktop.client.ApiClientException;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class ViewLoadStateTest {
    @Test
    void enforcesInvariantsAcrossAllFiveStates() {
        ViewLoadState<String> loading = ViewLoadState.loading();
        assertEquals(ViewLoadStatus.LOADING, loading.status());
        assertFalse(loading.hasData());

        ViewLoadState<String> empty = ViewLoadState.empty();
        assertEquals(ViewLoadStatus.EMPTY, empty.status());
        assertFalse(empty.hasData());

        ViewLoadState<String> content = ViewLoadState.content("rows");
        assertEquals(ViewLoadStatus.CONTENT, content.status());
        assertTrue(content.hasData());
        assertFalse(content.refreshing());

        ViewLoadState<String> refreshing = ViewLoadState.refreshing("rows");
        assertEquals(ViewLoadStatus.CONTENT, refreshing.status());
        assertTrue(refreshing.refreshing());
        assertEquals("rows", refreshing.data());

        LoadFailure disconnected = LoadFailure.fromThrowable(new IOException("reset"));
        ViewLoadState<String> retained = ViewLoadState.errorRetainingData(disconnected, "rows");
        assertEquals(ViewLoadStatus.DISCONNECTED, retained.status());
        assertEquals("rows", retained.data());

        LoadFailure forbidden = LoadFailure.fromThrowable(new ApiClientException(
                ApiError.of(ErrorCode.FORBIDDEN, "Denied", Instant.now(), "/api/v1/x", "req-1"),
                403
        ));
        ViewLoadState<String> dropped = ViewLoadState.errorRetainingData(forbidden, "rows");
        assertEquals(ViewLoadStatus.RETRYABLE_ERROR, dropped.status());
        assertNull(dropped.data());

        assertThrows(IllegalArgumentException.class,
                () -> new ViewLoadState<>(ViewLoadStatus.LOADING, "rows", false, null));
        assertThrows(IllegalArgumentException.class,
                () -> new ViewLoadState<>(ViewLoadStatus.CONTENT, null, false, null));
        assertThrows(IllegalArgumentException.class,
                () -> new ViewLoadState<>(ViewLoadStatus.RETRYABLE_ERROR, null, true, disconnected));
    }

    @Test
    void classifiesThrowablesIntoDisconnectedRetryableAndAuthLoss() {
        LoadFailure transport = LoadFailure.fromThrowable(
                new CompletionException(new ApiClientException(0, "Network down", true, new IOException()))
        );
        assertEquals(ViewLoadStatus.DISCONNECTED, transport.status());
        assertTrue(transport.retryable());
        assertFalse(transport.isPermissionOrSessionLoss());

        LoadFailure timeout = LoadFailure.fromThrowable(new TimeoutException("timed out"));
        assertEquals(ViewLoadStatus.DISCONNECTED, timeout.status());
        assertTrue(timeout.retryable());

        LoadFailure serverBusy = LoadFailure.fromThrowable(new ApiClientException(
                ApiError.of(ErrorCode.SERVICE_UNAVAILABLE, "DB busy", Instant.now(), "/api/v1/users", "req-2"),
                503
        ));
        assertEquals(ViewLoadStatus.RETRYABLE_ERROR, serverBusy.status());
        assertTrue(serverBusy.retryable());
        assertFalse(serverBusy.isPermissionOrSessionLoss());

        LoadFailure revoked = LoadFailure.fromThrowable(new ApiClientException(
                ApiError.of(ErrorCode.SESSION_REVOKED, "Revoked", Instant.now(), "/api/v1/users", "req-3"),
                401
        ));
        assertTrue(revoked.isSessionExpired());
        assertTrue(revoked.isPermissionOrSessionLoss());
        assertFalse(revoked.retryable());

        LoadFailure unexpected = LoadFailure.fromThrowable(new RuntimeException("sensitive internal stack"));
        assertEquals(ViewLoadStatus.RETRYABLE_ERROR, unexpected.status());
        assertEquals("Đã xảy ra lỗi khi tải dữ liệu.", unexpected.message());
    }
}
