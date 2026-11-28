package com.chatsystem.desktop.skeleton;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.common.api.ApiError;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.desktop.async.LoadFailure;
import com.chatsystem.desktop.async.ViewLoadState;
import com.chatsystem.desktop.client.ApiClientException;
import java.io.IOException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class StatePresentationTest {
    @Test
    void projectsAllFiveStatesAndRetainedVsDroppedErrorModes() {
        StatePresentation loading = StatePresentation.from(ViewLoadState.loading());
        assertTrue(loading.showSkeleton());
        assertFalse(loading.showContent());
        assertFalse(loading.showRefreshBar());
        assertFalse(loading.showEmptyPanel());
        assertFalse(loading.showFullErrorPanel());
        assertFalse(loading.showInlineErrorBanner());

        StatePresentation content = StatePresentation.from(ViewLoadState.content("rows"));
        assertFalse(content.showSkeleton());
        assertTrue(content.showContent());
        assertFalse(content.showRefreshBar());

        StatePresentation refreshing = StatePresentation.from(ViewLoadState.refreshing("rows"));
        assertFalse(refreshing.showSkeleton());
        assertTrue(refreshing.showContent());
        assertTrue(refreshing.showRefreshBar());

        StatePresentation empty = StatePresentation.from(ViewLoadState.empty());
        assertTrue(empty.showEmptyPanel());
        assertFalse(empty.showContent());
        assertFalse(empty.showSkeleton());

        LoadFailure disconnected = LoadFailure.fromThrowable(new IOException("offline"));
        StatePresentation retainedOffline = StatePresentation.from(ViewLoadState.errorRetainingData(disconnected, "rows"));
        assertTrue(retainedOffline.showContent());
        assertTrue(retainedOffline.showInlineErrorBanner());
        assertFalse(retainedOffline.showFullErrorPanel());
        assertTrue(retainedOffline.offline());
        assertTrue(retainedOffline.retryVisible());

        LoadFailure forbidden = LoadFailure.fromThrowable(new ApiClientException(
                ApiError.of(ErrorCode.FORBIDDEN, "Không có quyền", Instant.now(), "/api/v1/admin/users", "req-1"),
                403
        ));
        StatePresentation droppedForbidden = StatePresentation.from(ViewLoadState.errorRetainingData(forbidden, "rows"));
        assertFalse(droppedForbidden.showContent());
        assertFalse(droppedForbidden.showInlineErrorBanner());
        assertTrue(droppedForbidden.showFullErrorPanel());
        assertFalse(droppedForbidden.offline());
        assertFalse(droppedForbidden.retryVisible());
        assertEquals("Không có quyền", droppedForbidden.errorMessage());
    }
}
