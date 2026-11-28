package com.chatsystem.desktop.skeleton;

import com.chatsystem.desktop.async.ViewLoadState;
import java.util.Objects;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Reusable JavaFX container rendering the five {@link ViewLoadState} projections:
 * initial skeleton, content (with optional top refresh indicator), empty state,
 * non-destructive inline error/offline banner, and full retryable error panel.
 */
public final class StateContentPane extends StackPane {
    private static final String DEFAULT_EMPTY_TITLE = "Chưa có dữ liệu";
    private static final String DEFAULT_EMPTY_SUBTITLE = "Dữ liệu phù hợp sẽ hiển thị tại đây.";

    private final Node skeleton;
    private final SkeletonPulse pulseHandle;
    private final BorderPane contentHost;
    private final HBox inlineBanner;
    private final Label inlineBannerMessage;
    private final Button inlineRetryButton;
    private final ProgressBar refreshBar;
    private final VBox emptyPanel;
    private final Label emptyTitleLabel;
    private final Label emptySubtitleLabel;
    private final VBox fullErrorPanel;
    private final Label fullErrorTitle;
    private final Label fullErrorMessage;
    private final Button fullRetryButton;
    private Runnable onRetry;

    public StateContentPane(Node content, Node skeleton) {
        this(content, skeleton, null);
    }

    public StateContentPane(Node content, Node skeleton, ObservableBooleanValue reducedMotion) {
        Objects.requireNonNull(content, "content");
        this.skeleton = Skeletons.nonInteractive(Objects.requireNonNull(skeleton, "skeleton"));
        this.pulseHandle = SkeletonPulse.attach(this.skeleton, reducedMotion);

        this.inlineBannerMessage = wrappingLabel();
        this.inlineRetryButton = retryButton();
        Region bannerSpacer = new Region();
        HBox.setHgrow(bannerSpacer, Priority.ALWAYS);
        this.inlineBanner = new HBox(12, inlineBannerMessage, bannerSpacer, inlineRetryButton);
        this.inlineBanner.setAlignment(Pos.CENTER_LEFT);
        this.inlineBanner.getStyleClass().add("cs-state-banner");

        this.contentHost = new BorderPane(content);
        this.contentHost.setTop(inlineBanner);

        this.refreshBar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        this.refreshBar.getStyleClass().add("cs-state-refresh-bar");
        this.refreshBar.setFocusTraversable(false);
        this.refreshBar.setMouseTransparent(true);
        StackPane.setAlignment(this.refreshBar, Pos.TOP_CENTER);

        this.emptyTitleLabel = new Label(DEFAULT_EMPTY_TITLE);
        this.emptyTitleLabel.getStyleClass().add("section-title");
        this.emptyTitleLabel.setWrapText(true);
        this.emptySubtitleLabel = new Label(DEFAULT_EMPTY_SUBTITLE);
        this.emptySubtitleLabel.getStyleClass().add("secondary-text");
        this.emptySubtitleLabel.setWrapText(true);
        this.emptyPanel = new VBox(8, emptyTitleLabel, emptySubtitleLabel);
        this.emptyPanel.setAlignment(Pos.CENTER);
        this.emptyPanel.getStyleClass().addAll("empty-state", "cs-state-panel");

        this.fullErrorTitle = new Label("Không thể tải dữ liệu");
        this.fullErrorTitle.getStyleClass().add("section-title");
        this.fullErrorTitle.setWrapText(true);
        this.fullErrorMessage = wrappingLabel();
        this.fullErrorMessage.getStyleClass().add("secondary-text");
        this.fullRetryButton = retryButton();
        this.fullErrorPanel = new VBox(12, fullErrorTitle, fullErrorMessage, fullRetryButton);
        this.fullErrorPanel.setAlignment(Pos.CENTER);
        this.fullErrorPanel.getStyleClass().add("cs-state-panel");

        getChildren().addAll(contentHost, this.skeleton, emptyPanel, fullErrorPanel, refreshBar);
        Skeletons.setShown(this.skeleton, false);
        Skeletons.setShown(inlineBanner, false);
        Skeletons.setShown(emptyPanel, false);
        Skeletons.setShown(fullErrorPanel, false);
        Skeletons.setShown(refreshBar, false);
    }

    public void setEmptyState(String title, String subtitle) {
        emptyTitleLabel.setText((title == null || title.isBlank()) ? DEFAULT_EMPTY_TITLE : title);
        emptySubtitleLabel.setText((subtitle == null || subtitle.isBlank()) ? DEFAULT_EMPTY_SUBTITLE : subtitle);
    }

    public void setOnRetry(Runnable onRetry) {
        this.onRetry = onRetry;
    }

    public void apply(ViewLoadState<?> state) {
        StatePresentation presentation = StatePresentation.from(state);
        Skeletons.setShown(skeleton, presentation.showSkeleton());
        Skeletons.setShown(contentHost, presentation.showContent());
        Skeletons.setShown(refreshBar, presentation.showRefreshBar());
        Skeletons.setShown(emptyPanel, presentation.showEmptyPanel());
        Skeletons.setShown(fullErrorPanel, presentation.showFullErrorPanel());
        Skeletons.setShown(inlineBanner, presentation.showInlineErrorBanner());

        if (presentation.showInlineErrorBanner()) {
            inlineBannerMessage.setText(presentation.errorMessage());
            inlineBanner.getStyleClass().removeAll("cs-state-banner-error", "cs-state-banner-offline");
            inlineBanner.getStyleClass().add(
                    presentation.offline() ? "cs-state-banner-offline" : "cs-state-banner-error"
            );
            Skeletons.setShown(inlineRetryButton, presentation.retryVisible());
        }
        if (presentation.showFullErrorPanel()) {
            fullErrorTitle.setText(presentation.offline() ? "Mất kết nối tới máy chủ" : "Không thể tải dữ liệu");
            fullErrorMessage.setText(presentation.errorMessage());
            Skeletons.setShown(fullRetryButton, presentation.retryVisible());
        }
    }

    public void dispose() {
        pulseHandle.close();
    }

    private Button retryButton() {
        Button button = new Button("Thử lại");
        button.setOnAction(event -> {
            if (onRetry != null) {
                onRetry.run();
            }
        });
        return button;
    }

    private static Label wrappingLabel() {
        Label label = new Label();
        label.setWrapText(true);
        return label;
    }
}
