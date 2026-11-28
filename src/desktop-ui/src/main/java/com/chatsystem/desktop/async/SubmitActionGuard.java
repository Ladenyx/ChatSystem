package com.chatsystem.desktop.async;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Prevents duplicate submit clicks while an async form/dialog action is in flight and drops stale results. */
public final class SubmitActionGuard implements AutoCloseable {
    private final Supplier<Object> sessionMarkerSupplier;
    private final Consumer<Boolean> busyConsumer;
    private final Executor ioExecutor;
    private final Executor uiExecutor;

    private final Object lock = new Object();
    private long generation;
    private boolean inFlight;
    private CompletableFuture<?> inFlightFuture;
    private boolean disposed;

    public SubmitActionGuard(Supplier<Object> sessionMarkerSupplier, Consumer<Boolean> busyConsumer) {
        this(sessionMarkerSupplier, busyConsumer, UiExecutors.ioExecutor(), UiExecutors.fxExecutor());
    }

    public SubmitActionGuard(
            Supplier<Object> sessionMarkerSupplier,
            Consumer<Boolean> busyConsumer,
            Executor ioExecutor,
            Executor uiExecutor
    ) {
        this.sessionMarkerSupplier = Objects.requireNonNull(sessionMarkerSupplier, "sessionMarkerSupplier");
        this.busyConsumer = Objects.requireNonNull(busyConsumer, "busyConsumer");
        this.ioExecutor = Objects.requireNonNull(ioExecutor, "ioExecutor");
        this.uiExecutor = Objects.requireNonNull(uiExecutor, "uiExecutor");
    }

    public <R> boolean submit(
            Supplier<CompletableFuture<R>> action,
            Consumer<R> onSuccess,
            Consumer<LoadFailure> onFailure
    ) {
        Objects.requireNonNull(action, "action");
        long expectedGen;
        Object expectedSession = sessionMarkerSupplier.get();
        synchronized (lock) {
            if (disposed || inFlight) {
                return false;
            }
            inFlight = true;
            expectedGen = ++generation;
        }
        uiExecutor.execute(() -> busyConsumer.accept(true));

        CompletableFuture<R> future;
        try {
            future = CompletableFuture.supplyAsync(
                    () -> Objects.requireNonNull(action.get(), "action must not return null"),
                    ioExecutor
            ).thenCompose(Function.identity());
        } catch (Throwable ex) {
            uiExecutor.execute(() -> completeFailure(expectedGen, expectedSession, ex, onFailure));
            return true;
        }

        synchronized (lock) {
            if (disposed || generation != expectedGen) {
                future.cancel(true);
                return true;
            }
            inFlightFuture = future;
        }

        future.whenComplete((result, err) -> uiExecutor.execute(() -> {
            if (err != null) {
                completeFailure(expectedGen, expectedSession, err, onFailure);
            } else {
                completeSuccess(expectedGen, expectedSession, result, onSuccess);
            }
        }));
        return true;
    }

    public void cancel() {
        boolean wasBusy;
        CompletableFuture<?> toCancel;
        synchronized (lock) {
            generation++;
            wasBusy = inFlight;
            inFlight = false;
            toCancel = inFlightFuture;
            inFlightFuture = null;
        }
        if (toCancel != null) {
            toCancel.cancel(true);
        }
        if (wasBusy) {
            uiExecutor.execute(() -> busyConsumer.accept(false));
        }
    }

    public boolean isSubmitting() {
        synchronized (lock) {
            return inFlight;
        }
    }

    @Override
    public void close() {
        CompletableFuture<?> toCancel;
        synchronized (lock) {
            disposed = true;
            generation++;
            inFlight = false;
            toCancel = inFlightFuture;
            inFlightFuture = null;
        }
        if (toCancel != null) {
            toCancel.cancel(true);
        }
    }

    private <R> void completeSuccess(
            long expectedGen,
            Object expectedSession,
            R result,
            Consumer<R> onSuccess
    ) {
        if (!finishIfCurrent(expectedGen, expectedSession)) {
            return;
        }
        busyConsumer.accept(false);
        if (onSuccess != null) {
            onSuccess.accept(result);
        }
    }

    private void completeFailure(
            long expectedGen,
            Object expectedSession,
            Throwable throwable,
            Consumer<LoadFailure> onFailure
    ) {
        if (!finishIfCurrent(expectedGen, expectedSession)) {
            return;
        }
        busyConsumer.accept(false);
        if (onFailure != null) {
            onFailure.accept(LoadFailure.fromThrowable(throwable));
        }
    }

    private boolean finishIfCurrent(long expectedGen, Object expectedSession) {
        synchronized (lock) {
            if (disposed || generation != expectedGen) {
                return false;
            }
            inFlight = false;
            inFlightFuture = null;
            return Objects.equals(sessionMarkerSupplier.get(), expectedSession);
        }
    }
}
