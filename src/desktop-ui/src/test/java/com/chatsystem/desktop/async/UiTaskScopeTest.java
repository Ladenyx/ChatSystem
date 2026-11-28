package com.chatsystem.desktop.async;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class UiTaskScopeTest {
    @Test
    void cancelAllCancelsTrackedFuturesAndResourcesWhileRemainingReusable() {
        UiTaskScope scope = new UiTaskScope();
        AtomicInteger closedCount = new AtomicInteger();
        scope.register(closedCount::incrementAndGet);
        CompletableFuture<String> tracked = scope.track(new CompletableFuture<>());

        scope.cancelAll();
        assertTrue(tracked.isCancelled());
        assertEquals(1, closedCount.get());
        assertFalse(scope.isClosed());

        CompletableFuture<String> reEntered = scope.track(new CompletableFuture<>());
        assertFalse(reEntered.isCancelled());
        scope.close();
        assertTrue(reEntered.isCancelled());
        assertTrue(scope.isClosed());
    }
}
