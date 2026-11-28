package com.chatsystem.desktop.async;

import java.util.Objects;

/** Immutable UI state snapshot enforcing invariants across the five loading states and refresh retention. */
public record ViewLoadState<T>(
        ViewLoadStatus status,
        T data,
        boolean refreshing,
        LoadFailure failure
) {
    public ViewLoadState {
        Objects.requireNonNull(status, "status");
        switch (status) {
            case LOADING, EMPTY -> {
                if (data != null || refreshing || failure != null) {
                    throw new IllegalArgumentException(status + " state must not carry data, refreshing, or failure");
                }
            }
            case CONTENT -> {
                if (data == null || failure != null) {
                    throw new IllegalArgumentException("CONTENT state requires non-null data and null failure");
                }
            }
            case RETRYABLE_ERROR, DISCONNECTED -> {
                if (failure == null || refreshing) {
                    throw new IllegalArgumentException(status + " state requires non-null failure and refreshing=false");
                }
            }
        }
    }

    public static <T> ViewLoadState<T> loading() {
        return new ViewLoadState<>(ViewLoadStatus.LOADING, null, false, null);
    }

    public static <T> ViewLoadState<T> content(T data) {
        Objects.requireNonNull(data, "data");
        return new ViewLoadState<>(ViewLoadStatus.CONTENT, data, false, null);
    }

    public static <T> ViewLoadState<T> empty() {
        return new ViewLoadState<>(ViewLoadStatus.EMPTY, null, false, null);
    }

    public static <T> ViewLoadState<T> refreshing(T existingData) {
        Objects.requireNonNull(existingData, "existingData");
        return new ViewLoadState<>(ViewLoadStatus.CONTENT, existingData, true, null);
    }

    public static <T> ViewLoadState<T> error(LoadFailure failure) {
        Objects.requireNonNull(failure, "failure");
        return new ViewLoadState<>(failure.status(), null, false, failure);
    }

    /** Retains viewable cached data on transient failures while stripping it on permission or session loss. */
    public static <T> ViewLoadState<T> errorRetainingData(LoadFailure failure, T existingData) {
        Objects.requireNonNull(failure, "failure");
        T retained = (existingData != null && failure.retryable() && !failure.isPermissionOrSessionLoss())
                ? existingData
                : null;
        return new ViewLoadState<>(failure.status(), retained, false, failure);
    }

    public boolean hasData() {
        return data != null;
    }
}
