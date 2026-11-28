package com.chatsystem.desktop.async;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;

/** Shared off-FX-thread I/O executor (virtual threads) and JavaFX Application Thread dispatcher. */
public final class UiExecutors {
    private static final ExecutorService IO_EXECUTOR = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("chatsystem-ui-io-", 0).factory()
    );

    private static final Executor FX_EXECUTOR = command -> {
        Objects.requireNonNull(command, "command");
        if (Platform.isFxApplicationThread()) {
            command.run();
        } else {
            Platform.runLater(command);
        }
    };

    private UiExecutors() {
    }

    public static ExecutorService ioExecutor() {
        return IO_EXECUTOR;
    }

    public static Executor fxExecutor() {
        return FX_EXECUTOR;
    }
}
