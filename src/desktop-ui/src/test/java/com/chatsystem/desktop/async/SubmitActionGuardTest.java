package com.chatsystem.desktop.async;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SubmitActionGuardTest {
    private static final Executor DIRECT = Runnable::run;

    @Test
    void rejectsDuplicateClicksUntilCompletionAndAllowsRetryAfterFailure() {
        List<Boolean> busyEvents = new ArrayList<>();
        List<String> successes = new ArrayList<>();
        List<LoadFailure> failures = new ArrayList<>();
        SubmitActionGuard guard = new SubmitActionGuard(() -> "s1", busyEvents::add, DIRECT, DIRECT);

        CompletableFuture<String> firstCall = new CompletableFuture<>();
        assertTrue(guard.submit(() -> firstCall, successes::add, failures::add));
        assertTrue(guard.isSubmitting());
        assertFalse(guard.submit(() -> CompletableFuture.completedFuture("dup"), successes::add, failures::add));

        firstCall.completeExceptionally(new IOException("network error"));
        assertFalse(guard.isSubmitting());
        assertEquals(List.of(true, false), busyEvents);
        assertTrue(successes.isEmpty());
        assertEquals(1, failures.size());
        assertEquals(ViewLoadStatus.DISCONNECTED, failures.getFirst().status());

        assertTrue(guard.submit(
                () -> CompletableFuture.completedFuture("saved"),
                successes::add,
                failures::add
        ));
        assertEquals(List.of("saved"), successes);
        assertEquals(List.of(true, false, true, false), busyEvents);
    }

    @Test
    void discardsLateCallbackWhenCancelledOrSessionChanged() {
        AtomicReference<String> session = new AtomicReference<>("s1");
        List<Boolean> busyEvents = new ArrayList<>();
        List<String> successes = new ArrayList<>();
        List<LoadFailure> failures = new ArrayList<>();
        SubmitActionGuard guard = new SubmitActionGuard(session::get, busyEvents::add, DIRECT, DIRECT);

        CompletableFuture<String> pending = new CompletableFuture<>();
        assertTrue(guard.submit(() -> pending, successes::add, failures::add));
        guard.cancel();
        assertFalse(guard.isSubmitting());
        pending.complete("stale");
        assertTrue(successes.isEmpty());
        assertTrue(failures.isEmpty());

        CompletableFuture<String> sessionPending = new CompletableFuture<>();
        assertTrue(guard.submit(() -> sessionPending, successes::add, failures::add));
        session.set("s2");
        sessionPending.complete("wrong-session");
        assertTrue(successes.isEmpty());
        assertEquals(List.of(true, false, true), busyEvents);
    }
}
