package com.chatsystem.desktop.skeleton;

import com.chatsystem.desktop.async.LoadFailure;
import com.chatsystem.desktop.async.ViewLoadState;
import com.chatsystem.desktop.async.ViewLoadStatus;
import java.util.Objects;

/** Pure presentation projection of {@link ViewLoadState} for rendering 5-state UI containers. */
public record StatePresentation(
        boolean showSkeleton,
        boolean showContent,
        boolean showRefreshBar,
        boolean showEmptyPanel,
        boolean showFullErrorPanel,
        boolean showInlineErrorBanner,
        boolean offline,
        boolean retryVisible,
        String errorMessage
) {
    public static StatePresentation from(ViewLoadState<?> state) {
        Objects.requireNonNull(state, "state");
        ViewLoadStatus status = state.status();
        LoadFailure failure = state.failure();
        boolean hasData = state.hasData();
        boolean isError = status == ViewLoadStatus.RETRYABLE_ERROR || status == ViewLoadStatus.DISCONNECTED;
        return new StatePresentation(
                status == ViewLoadStatus.LOADING,
                status == ViewLoadStatus.CONTENT || (isError && hasData),
                status == ViewLoadStatus.CONTENT && state.refreshing(),
                status == ViewLoadStatus.EMPTY,
                isError && !hasData,
                isError && hasData,
                status == ViewLoadStatus.DISCONNECTED,
                failure != null && failure.retryable(),
                failure == null ? null : failure.message()
        );
    }
}
