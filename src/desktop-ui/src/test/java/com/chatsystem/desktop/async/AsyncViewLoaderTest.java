package com.chatsystem.desktop.async;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.common.api.ApiError;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.desktop.client.ApiClientException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AsyncViewLoaderTest {
    private static final Executor DIRECT = Runnable::run;

    @Test
    void transitionsLoadingToContentOrEmptyAndRetainsDataOnSameContextRefresh() {
        List<ViewLoadState<List<String>>> states = new ArrayList<>();
        AsyncViewLoader<String, List<String>> loader = new AsyncViewLoader<>(
                () -> "session-1",
                List::isEmpty,
                states::add,
                DIRECT,
                DIRECT
        );

        CompletableFuture<List<String>> first = new CompletableFuture<>();
        loader.load("filter-a", () -> first);
        assertEquals(1, states.size());
        assertEquals(ViewLoadStatus.LOADING, states.getLast().status());

        first.complete(List.of("alice", "bob"));
        assertEquals(ViewLoadStatus.CONTENT, states.getLast().status());
        assertFalse(states.getLast().refreshing());
        assertEquals(List.of("alice", "bob"), states.getLast().data());

        // Same context refresh keeps existing rows visible while refreshing
        CompletableFuture<List<String>> refresh = new CompletableFuture<>();
        loader.load("filter-a", () -> refresh);
        assertEquals(ViewLoadStatus.CONTENT, states.getLast().status());
        assertTrue(states.getLast().refreshing());
        assertEquals(List.of("alice", "bob"), states.getLast().data());

        refresh.complete(List.of());
        assertEquals(ViewLoadStatus.EMPTY, states.getLast().status());
        assertNull(states.getLast().data());
    }

    @Test
    void discardsStaleResponsesOnContextSwitchSessionChangeAndLeave() {
        AtomicReference<String> session = new AtomicReference<>("session-1");
        List<ViewLoadState<String>> states = new ArrayList<>();
        AsyncViewLoader<String, String> loader = new AsyncViewLoader<>(
                session::get,
                String::isBlank,
                states::add,
                DIRECT,
                DIRECT
        );

        CompletableFuture<String> reqA = new CompletableFuture<>();
        CompletableFuture<String> reqB = new CompletableFuture<>();
        loader.load("row-1", () -> reqA);
        loader.load("row-2", () -> reqB);

        assertEquals(2, states.size());
        assertEquals(ViewLoadStatus.LOADING, states.get(0).status());
        assertEquals(ViewLoadStatus.LOADING, states.get(1).status());

        reqA.complete("stale row-1");
        assertEquals(2, states.size());

        reqB.complete("fresh row-2");
        assertEquals(3, states.size());
        assertEquals("fresh row-2", states.getLast().data());

        // Session change discards late response
        CompletableFuture<String> reqC = new CompletableFuture<>();
        loader.load("row-3", () -> reqC);
        session.set("session-2");
        reqC.complete("stale session-1 data");
        assertEquals(4, states.size());
        assertEquals(ViewLoadStatus.LOADING, states.getLast().status());

        // cancelCurrent() (e.g. onLeave) ignores late response
        CompletableFuture<String> reqD = new CompletableFuture<>();
        loader.load("row-4", () -> reqD);
        loader.cancelCurrent();
        reqD.complete("after leave");
        assertEquals(5, states.size());
        assertTrue(reqD.isCancelled());
    }

    @Test
    void retainsViewableDataOnDisconnectedRefreshButDropsItImmediatelyOnPermissionLoss() {
        List<ViewLoadState<String>> states = new ArrayList<>();
        AsyncViewLoader<String, String> loader = new AsyncViewLoader<>(
                () -> "session-1",
                String::isBlank,
                states::add,
                DIRECT,
                DIRECT
        );

        loader.load("ctx", () -> CompletableFuture.completedFuture("cached-rows"));
        assertEquals("cached-rows", states.getLast().data());

        CompletableFuture<String> transientFail = new CompletableFuture<>();
        loader.load("ctx", () -> transientFail);
        transientFail.completeExceptionally(new IOException("offline"));
        assertEquals(ViewLoadStatus.DISCONNECTED, states.getLast().status());
        assertEquals("cached-rows", states.getLast().data());

        CompletableFuture<String> authFail = new CompletableFuture<>();
        loader.load("ctx", () -> authFail);
        authFail.completeExceptionally(new ApiClientException(
                ApiError.of(ErrorCode.FORBIDDEN, "No access", Instant.now(), "/api/v1/admin/users", "req-9"),
                403
        ));
        assertEquals(ViewLoadStatus.RETRYABLE_ERROR, states.getLast().status());
        assertNull(states.getLast().data());
    }

    @Test
    void keepsMasterAndDetailLoadersIndependentAndRunsBlockingWorkOnIoExecutor() {
        List<ViewLoadState<String>> masterStates = new ArrayList<>();
        List<ViewLoadState<String>> detailStates = new ArrayList<>();
        List<String> executedOn = new ArrayList<>();
        Executor namedIo = task -> {
            executedOn.add("io-thread");
            task.run();
        };

        AsyncViewLoader<String, String> master = new AsyncViewLoader<>(
                () -> "s1", String::isBlank, masterStates::add, namedIo, DIRECT
        );
        AsyncViewLoader<String, String> detail = new AsyncViewLoader<>(
                () -> "s1", String::isBlank, detailStates::add, namedIo, DIRECT
        );

        CompletableFuture<String> detailFuture = new CompletableFuture<>();
        master.loadBlocking("page-0", () -> "user-table");
        detail.load("user-42", () -> detailFuture);

        assertEquals("user-table", masterStates.getLast().data());
        assertEquals(ViewLoadStatus.LOADING, detailStates.getLast().status());
        assertEquals(List.of("io-thread"), executedOn);

        detailFuture.complete("profile-42");
        assertEquals("profile-42", detailStates.getLast().data());
        assertEquals("user-table", masterStates.getLast().data());
    }
}
