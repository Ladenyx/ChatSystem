package com.chatsystem.desktop.skeleton;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/** Compact inline delivery state indicator for outgoing chat bubbles ({@code SENDING} / {@code SENT} / {@code FAILED}). */
public final class MessageSendStatusView extends HBox {
    public enum Status {
        SENDING,
        SENT,
        FAILED
    }

    private final Label statusLabel = new Label();
    private final Hyperlink retryLink = new Hyperlink("Thử lại");
    private Runnable onRetry;

    public MessageSendStatusView() {
        setAlignment(Pos.CENTER_RIGHT);
        setSpacing(6);
        retryLink.setOnAction(event -> {
            if (onRetry != null) {
                onRetry.run();
            }
        });
        getChildren().addAll(statusLabel, retryLink);
        setStatus(Status.SENT, null);
    }

    public void setStatus(Status status, Runnable onRetry) {
        Objects.requireNonNull(status, "status");
        this.onRetry = onRetry;
        statusLabel.getStyleClass().removeAll("cs-msg-status-sending", "cs-msg-status-failed");
        switch (status) {
            case SENDING -> {
                statusLabel.setText("Đang gửi...");
                statusLabel.getStyleClass().add("cs-msg-status-sending");
                Skeletons.setShown(retryLink, false);
                Skeletons.setShown(this, true);
            }
            case SENT -> {
                Skeletons.setShown(retryLink, false);
                Skeletons.setShown(this, false);
            }
            case FAILED -> {
                statusLabel.setText("Gửi thất bại");
                statusLabel.getStyleClass().add("cs-msg-status-failed");
                Skeletons.setShown(retryLink, onRetry != null);
                Skeletons.setShown(this, true);
            }
        }
    }
}
