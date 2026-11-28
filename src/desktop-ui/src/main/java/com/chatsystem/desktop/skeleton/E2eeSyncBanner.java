package com.chatsystem.desktop.skeleton;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/** Banner displaying E2EE key-waiting or group epoch synchronization progress. */
public final class E2eeSyncBanner extends HBox {
    public enum Mode {
        HIDDEN,
        WAITING_FOR_KEY,
        SYNCING
    }

    private final Label messageLabel = new Label();
    private final ProgressBar progressBar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);

    public E2eeSyncBanner() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);
        getStyleClass().add("cs-state-banner");
        messageLabel.setWrapText(true);
        HBox.setHgrow(messageLabel, Priority.ALWAYS);
        progressBar.setPrefWidth(120);
        progressBar.setFocusTraversable(false);
        getChildren().addAll(messageLabel, progressBar);
        showHidden();
    }

    public void showHidden() {
        Skeletons.setShown(this, false);
    }

    public void showWaitingForKey(String message) {
        update(Mode.WAITING_FOR_KEY, nonBlankOr(message, "Đang chờ xác nhận hoặc đồng bộ khóa E2EE."), -1.0);
    }

    public void showSyncing(String message, double progress) {
        update(Mode.SYNCING, nonBlankOr(message, "Đang đồng bộ khóa nhóm E2EE..."), progress);
    }

    public void update(Mode mode, String message, double progress) {
        Objects.requireNonNull(mode, "mode");
        getStyleClass().removeAll("cs-state-banner-warning", "cs-state-banner-info");
        switch (mode) {
            case HIDDEN -> Skeletons.setShown(this, false);
            case WAITING_FOR_KEY -> {
                getStyleClass().add("cs-state-banner-warning");
                messageLabel.setText(nonBlankOr(message, "Đang chờ khóa giải mã E2EE."));
                progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                Skeletons.setShown(this, true);
            }
            case SYNCING -> {
                getStyleClass().add("cs-state-banner-info");
                messageLabel.setText(nonBlankOr(message, "Đang đồng bộ khóa E2EE..."));
                double resolved = (progress < 0.0 || !Double.isFinite(progress))
                        ? ProgressBar.INDETERMINATE_PROGRESS
                        : Math.min(1.0, progress);
                progressBar.setProgress(resolved);
                Skeletons.setShown(this, true);
            }
        }
    }

    private static String nonBlankOr(String candidate, String fallback) {
        return (candidate == null || candidate.isBlank()) ? fallback : candidate;
    }
}
