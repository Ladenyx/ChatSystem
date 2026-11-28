package com.chatsystem.desktop;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

/** Shared JavaFX toolkit bootstrap for UI tests running in the same Surefire JVM fork. */
public final class FxTestRuntime {
    private FxTestRuntime() {
    }

    public static void ensureStarted() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                ready.countDown();
            });
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(() -> {
                Platform.setImplicitExit(false);
                ready.countDown();
            });
        }
        assertTrue(ready.await(10, TimeUnit.SECONDS), "JavaFX did not start");
    }
}
