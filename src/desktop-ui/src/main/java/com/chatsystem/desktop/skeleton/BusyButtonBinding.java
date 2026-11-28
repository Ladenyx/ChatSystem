package com.chatsystem.desktop.skeleton;

import java.util.Objects;
import java.util.function.Consumer;
import javafx.scene.control.Button;

/** Binds a submit {@link Button} to {@link com.chatsystem.desktop.async.SubmitActionGuard} busy state. */
public final class BusyButtonBinding implements AutoCloseable {
    private final Button button;
    private final String defaultText;
    private final String busyText;
    private boolean closed;

    public BusyButtonBinding(Button button, String busyText) {
        this.button = Objects.requireNonNull(button, "button");
        this.defaultText = button.getText() == null ? "" : button.getText();
        Objects.requireNonNull(busyText, "busyText");
        if (busyText.isBlank()) {
            throw new IllegalArgumentException("busyText must not be blank");
        }
        this.busyText = busyText;
    }

    public Consumer<Boolean> busyConsumer() {
        return busy -> setBusy(Boolean.TRUE.equals(busy));
    }

    public void setBusy(boolean busy) {
        if (closed) {
            return;
        }
        button.setDisable(busy);
        button.setText(busy ? busyText : defaultText);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        button.setDisable(false);
        button.setText(defaultText);
    }
}
