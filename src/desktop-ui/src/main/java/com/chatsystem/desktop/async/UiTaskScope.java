package com.chatsystem.desktop.async;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Reusable controller/overlay task scope that cancels pending async operations in {@code onLeave()}. */
public final class UiTaskScope implements AutoCloseable {
    private static final System.Logger LOG = System.getLogger(UiTaskScope.class.getName());

    private final Object lock = new Object();
    private final Set<CompletableFuture<?>> futures = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<AutoCloseable> resources = new ArrayList<>();
    private boolean closed;

    public void register(AutoCloseable resource) {
        Objects.requireNonNull(resource, "resource");
        boolean closeImmediately = false;
        synchronized (lock) {
            if (closed) {
                closeImmediately = true;
            } else {
                resources.add(resource);
            }
        }
        if (closeImmediately) {
            closeQuietly(resource);
        }
    }

    public <T> CompletableFuture<T> track(CompletableFuture<T> future) {
        Objects.requireNonNull(future, "future");
        boolean cancelImmediately = false;
        synchronized (lock) {
            if (closed) {
                cancelImmediately = true;
            } else {
                futures.add(future);
            }
        }
        if (cancelImmediately) {
            future.cancel(true);
            return future;
        }
        future.whenComplete((ignored, err) -> {
            synchronized (lock) {
                futures.remove(future);
            }
        });
        return future;
    }

    public void cancelAll() {
        List<CompletableFuture<?>> futuresSnapshot;
        List<AutoCloseable> resourcesSnapshot;
        synchronized (lock) {
            futuresSnapshot = List.copyOf(futures);
            futures.clear();
            resourcesSnapshot = List.copyOf(resources);
            resources.clear();
        }
        futuresSnapshot.forEach(future -> future.cancel(true));
        resourcesSnapshot.forEach(UiTaskScope::closeQuietly);
    }

    public boolean isClosed() {
        synchronized (lock) {
            return closed;
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            closed = true;
        }
        cancelAll();
    }

    private static void closeQuietly(AutoCloseable resource) {
        try {
            resource.close();
        } catch (Exception ex) {
            LOG.log(System.Logger.Level.DEBUG, "Ignoring exception closing scoped resource: {0}", ex.getMessage());
        }
    }
}
