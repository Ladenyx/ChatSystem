package com.chatsystem.desktop.skeleton;

import com.chatsystem.desktop.async.ScrollAnchorMath;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

/** Top-of-history status indicator for loading older cursor pages while preserving visible scroll position. */
public final class OlderMessagesHeader extends HBox {
    private final ProgressIndicator spinner = new ProgressIndicator();
    private final Label label = new Label();
    private final Button retryButton = new Button("Thử lại");
    private Runnable onRetry;

    public OlderMessagesHeader() {
        setAlignment(Pos.CENTER);
        setSpacing(8);
        getStyleClass().add("cs-older-messages-header");
        spinner.setPrefSize(14, 14);
        spinner.setMaxSize(14, 14);
        spinner.setFocusTraversable(false);
        retryButton.setOnAction(event -> {
            if (onRetry != null) {
                onRetry.run();
            }
        });
        getChildren().addAll(spinner, label, retryButton);
        showIdle();
    }

    public void showIdle() {
        Skeletons.setShown(this, false);
    }

    public void showLoading() {
        label.setText("Đang tải tin nhắn cũ...");
        Skeletons.setShown(spinner, true);
        Skeletons.setShown(retryButton, false);
        Skeletons.setShown(this, true);
    }

    public void showError(String message, Runnable onRetry) {
        this.onRetry = onRetry;
        label.setText((message == null || message.isBlank()) ? "Không thể tải thêm tin nhắn cũ." : message);
        Skeletons.setShown(spinner, false);
        Skeletons.setShown(retryButton, onRetry != null);
        Skeletons.setShown(this, true);
    }

    public static void preserveScrollOnPrepend(ScrollPane scrollPane, double oldContentHeight, double oldVvalue) {
        Objects.requireNonNull(scrollPane, "scrollPane");
        scrollPane.applyCss();
        scrollPane.layout();
        Node content = scrollPane.getContent();
        if (content == null) {
            return;
        }
        double newContentHeight = content.getBoundsInLocal().getHeight();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        scrollPane.setVvalue(ScrollAnchorMath.preservePrependAnchor(
                oldContentHeight,
                newContentHeight,
                viewportHeight,
                oldVvalue
        ));
    }
}
