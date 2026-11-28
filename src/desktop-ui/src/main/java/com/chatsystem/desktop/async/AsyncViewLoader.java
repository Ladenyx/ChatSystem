package com.chatsystem.desktop.async;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Coordinates off-FX-thread loading, 5-state emission, refresh retention, and stale-response discarding
 * across context/filter/selection/session changes.
 */
public final class AsyncViewLoader<K, T> implements AutoCloseable {
    private final Supplier<Object> sessionMarkerSupplier;
    private final Predicate<T> emptyPredicate;
    private final Consumer<ViewLoadState<T>> stateConsumer;
    private final Executor ioExecutor;
    private final Executor uiExecutor;

    private final Object lock = new Object();
    private long requestSeq;
    private K activeContextKey;
    private Object activeSessionMarker;
    private T currentData;
    private CompletableFuture<T> inFlightFuture;
    private boolean disposed;

    public AsyncViewLoader(
            Supplier<Object> sessionMarkerSupplier,
            Predicate<T> emptyPredicate,
            Consumer<ViewLoadState<T>> stateConsumer
    ) {
        this(
                sessionMarkerSupplier,
                emptyPredicate,
                stateConsumer,
                UiExecutors.ioExecutor(),
                UiExecutors.fxExecutor()
        );
    }

    public AsyncViewLoader(
            Supplier<Object> sessionMarkerSupplier,
            Predicate<T> emptyPredicate,
            Consumer<ViewLoadState<T>> stateConsumer,
            Executor ioExecutor,
            Executor uiExecutor
    ) {
        this.sessionMarkerSupplier = Objects.requireNonNull(sessionMarkerSupplier, "sessionMarkerSupplier");
        this.emptyPredicate = Objects.requireNonNull(emptyPredicate, "emptyPredicate");
        this.stateConsumer = Objects.requireNonNull(stateConsumer, "stateConsumer");
        this.ioExecutor = Objects.requireNonNull(ioExecutor, "ioExecutor");
        this.uiExecutor = Objects.requireNonNull(uiExecutor, "uiExecutor");
    }

    public void load(K contextKey, Supplier<CompletableFuture<T>> taskSupplier) {
        Objects.requireNonNull(taskSupplier, "taskSupplier");
        CompletableFuture<T> previousToCancel;
        ViewLoadState<T> initialState;
        long expectedSeq;
        Object expectedSession = sessionMarkerSupplier.get();

        synchronized (lock) {
            if (disposed) {
                return;
            }
            previousToCancel = inFlightFuture;
            inFlightFuture = null;
            expectedSeq = ++requestSeq;
            boolean sameContext = Objects.equals(activeContextKey, contextKey)
                    && Objects.equals(activeSessionMarker, expectedSession);
            if (sameContext && currentData != null) {
                initialState = ViewLoadState.refreshing(currentData);
            } else {
                currentData = null;
                initialState = ViewLoadState.loading();
            }
            activeContextKey = contextKey;
            activeSessionMarker = expectedSession;
        }

        if (previousToCancel != null) {
            previousToCancel.cancel(true);
        }
        uiExecutor.execute(() -> stateConsumer.accept(initialState));

        CompletableFuture<T> future;
        try {
            future = Objects.requireNonNull(taskSupplier.get(), "taskSupplier must not return null");
        } catch (Throwable ex) {
            uiExecutor.execute(() -> applyFailure(expectedSeq, contextKey, expectedSession, ex));
            return;
        }

        synchronized (lock) {
            if (disposed || requestSeq != expectedSeq) {
                future.cancel(true);
                return;
            }
            inFlightFuture = future;
        }

        future.whenComplete((result, throwable) -> uiExecutor.execute(() -> {
            if (throwable != null) {
                applyFailure(expectedSeq, contextKey, expectedSession, throwable);
            } else {
                applySuccess(expectedSeq, contextKey, expectedSession, result);
            }
        }));
    }

    public void loadBlocking(K contextKey, Supplier<T> blockingIoSupplier) {
        Objects.requireNonNull(blockingIoSupplier, "blockingIoSupplier");
        load(contextKey, () -> CompletableFuture.supplyAsync(blockingIoSupplier, ioExecutor));
    }

    public void cancelCurrent() {
        CompletableFuture<T> toCancel;
        synchronized (lock) {
            requestSeq++;
            toCancel = inFlightFuture;
            inFlightFuture = null;
        }
        if (toCancel != null) {
            toCancel.cancel(true);
        }
    }

    public void reset() {
        CompletableFuture<T> toCancel;
        synchronized (lock) {
            requestSeq++;
            activeContextKey = null;
            activeSessionMarker = null;
            currentData = null;
            toCancel = inFlightFuture;
            inFlightFuture = null;
        }
        if (toCancel != null) {
            toCancel.cancel(true);
        }
    }

    @Override
    public void close() {
        CompletableFuture<T> toCancel;
        synchronized (lock) {
            disposed = true;
            requestSeq++;
            activeContextKey = null;
            activeSessionMarker = null;
            currentData = null;
            toCancel = inFlightFuture;
            inFlightFuture = null;
        }
        if (toCancel != null) {
            toCancel.cancel(true);
        }
    }

    private void applySuccess(long expectedSeq, K contextKey, Object expectedSession, T result) {
        ViewLoadState<T> nextState;
        synchronized (lock) {
            if (!isCurrentLocked(expectedSeq, contextKey, expectedSession)) {
                return;
            }
            inFlightFuture = null;
            if (result == null || emptyPredicate.test(result)) {
                currentData = null;
                nextState = ViewLoadState.empty();
            } else {
                currentData = result;
                nextState = ViewLoadState.content(result);
            }
        }
        stateConsumer.accept(nextState);
    }

    private void applyFailure(long expectedSeq, K contextKey, Object expectedSession, Throwable throwable) {
        LoadFailure failure = LoadFailure.fromThrowable(throwable);
        ViewLoadState<T> nextState;
        synchronized (lock) {
            if (!isCurrentLocked(expectedSeq, contextKey, expectedSession)) {
                return;
            }
            inFlightFuture = null;
            nextState = ViewLoadState.errorRetainingData(failure, currentData);
            currentData = nextState.data();
        }
        stateConsumer.accept(nextState);
    }

    private boolean isCurrentLocked(long expectedSeq, K contextKey, Object expectedSession) {
        return !disposed
                && requestSeq == expectedSeq
                && Objects.equals(activeContextKey, contextKey)
                && Objects.equals(activeSessionMarker, expectedSession)
                && Objects.equals(sessionMarkerSupplier.get(), expectedSession);
    }
}
