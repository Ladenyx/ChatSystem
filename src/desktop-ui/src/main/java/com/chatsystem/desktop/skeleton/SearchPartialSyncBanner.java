package com.chatsystem.desktop.skeleton;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/** Warning banner shown in message search views when local/E2EE history is not yet fully synchronized. */
public final class SearchPartialSyncBanner extends HBox {
    private final Label messageLabel = new Label();
    private final Button actionButton = new Button("Đồng bộ thêm");
    private Runnable onAction;

    public SearchPartialSyncBanner() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);
        getStyleClass().addAll("cs-state-banner", "cs-state-banner-warning");
        messageLabel.setWrapText(true);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        actionButton.setOnAction(event -> {
            if (onAction != null) {
                onAction.run();
            }
        });
        getChildren().addAll(messageLabel, spacer, actionButton);
        showHidden();
    }

    public void showHidden() {
        Skeletons.setShown(this, false);
    }

    public void showWarning(String message, String actionText, Runnable onAction) {
        this.onAction = onAction;
        messageLabel.setText((message == null || message.isBlank())
                ? "Lịch sử tìm kiếm cục bộ chưa được đồng bộ đầy đủ."
                : message);
        actionButton.setText((actionText == null || actionText.isBlank()) ? "Đồng bộ thêm" : actionText);
        Skeletons.setShown(actionButton, onAction != null);
        Skeletons.setShown(this, true);
    }
}
