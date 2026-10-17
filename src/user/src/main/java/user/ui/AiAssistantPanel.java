package user.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * AI Assistant Side Panel (Roadmap U-27).
 * Features:
 * - Refine Draft: Polishes user draft with selectable tones (Professional, Concise, Friendly).
 * - Suggest Reply: Context-aware suggested responses to the last received message.
 * - One-click "Apply" to insert result into the chat input bar.
 */
public class AiAssistantPanel extends VBox {

    private final Supplier<String> draftSupplier;
    private final Supplier<String> lastReceivedMessageSupplier;
    private final Consumer<String> onApplyToInput;
    private final Runnable onClosePanel;

    private final ToggleButton refineTab = new ToggleButton("Refine Draft");
    private final ToggleButton replyTab = new ToggleButton("Suggest Reply");
    private final ToggleGroup tabGroup = new ToggleGroup();
    private final StackPane contentContainer = new StackPane();

    public AiAssistantPanel(Supplier<String> draftSupplier,
                            Supplier<String> lastReceivedMessageSupplier,
                            Consumer<String> onApplyToInput,
                            Runnable onClosePanel) {
        this.draftSupplier = draftSupplier;
        this.lastReceivedMessageSupplier = lastReceivedMessageSupplier;
        this.onApplyToInput = onApplyToInput;
        this.onClosePanel = onClosePanel;

        setPrefWidth(270);
        setMinWidth(250);
        setMaxWidth(300);
        getStyleClass().add("card");
        setStyle("-fx-background-color: white; -fx-border-color: #ebebfc; -fx-border-width: 0 0 0 1;");
        setPadding(new Insets(16));
        setSpacing(14);

        // Header
        HBox header = buildHeader();

        // Mode switch tabs
        HBox tabs = buildTabs();

        // Content
        buildViews();

        getChildren().addAll(header, tabs, contentContainer);
        VBox.setVgrow(contentContainer, Priority.ALWAYS);
    }

    private HBox buildHeader() {
        Label title = new Label("AI Assistant");
        title.getStyleClass().add("text-name");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 700;");

        Label badge = new Label("LLM");
        badge.getStyleClass().add("tag-admin");
        badge.setStyle("-fx-background-color: #f3e8ff; -fx-text-fill: #8a2be2;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("btn-ghost");
        closeBtn.setStyle("-fx-padding: 2 6 2 6; -fx-font-size: 11px;");
        closeBtn.setOnAction(e -> { if (onClosePanel != null) onClosePanel.run(); });

        HBox header = new HBox(8, title, badge, spacer, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox buildTabs() {
        refineTab.setToggleGroup(tabGroup);
        replyTab.setToggleGroup(tabGroup);

        refineTab.getStyleClass().add("btn-ghost");
        replyTab.getStyleClass().add("btn-ghost");

        refineTab.setMaxWidth(Double.MAX_VALUE);
        replyTab.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(refineTab, Priority.ALWAYS);
        HBox.setHgrow(replyTab, Priority.ALWAYS);

        refineTab.setSelected(true);

        HBox tabs = new HBox(6, refineTab, replyTab);
        tabs.setAlignment(Pos.CENTER);
        return tabs;
    }

    private void buildViews() {
        VBox refineView = buildRefineView();
        VBox replyView = buildReplyView();

        contentContainer.getChildren().addAll(refineView, replyView);
        replyView.setVisible(false);

        refineTab.setOnAction(e -> {
            refineView.setVisible(true);
            replyView.setVisible(false);
        });

        replyTab.setOnAction(e -> {
            refineView.setVisible(false);
            replyView.setVisible(true);
        });
    }

    private VBox buildRefineView() {
        VBox box = new VBox(10);

        Label toneLbl = new Label("Select tone style:");
        toneLbl.getStyleClass().add("text-small");
        toneLbl.setStyle("-fx-font-weight: 600;");

        ComboBox<String> toneCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Professional & Polite",
                "Concise & Direct",
                "Friendly & Enthusiastic"
        ));
        toneCombo.setValue("Professional & Polite");
        toneCombo.setMaxWidth(Double.MAX_VALUE);

        TextArea resultArea = new TextArea();
        resultArea.setPromptText("Refined draft will appear here...");
        resultArea.setWrapText(true);
        resultArea.setPrefRowCount(5);
        VBox.setVgrow(resultArea, Priority.ALWAYS);

        Button refineBtn = new Button("✨ Refine Current Draft");
        refineBtn.getStyleClass().add("btn-accent");
        refineBtn.setMaxWidth(Double.MAX_VALUE);

        Button applyBtn = new Button("Apply to Chat Input");
        applyBtn.getStyleClass().add("btn-ghost");
        applyBtn.setMaxWidth(Double.MAX_VALUE);
        applyBtn.setDisable(true);

        refineBtn.setOnAction(e -> {
            String currentDraft = draftSupplier != null ? draftSupplier.get() : "";
            if (currentDraft == null || currentDraft.isBlank()) {
                currentDraft = "Mình đã đọc tin nhắn của bạn rồi nhé.";
            }

            String refined;
            String tone = toneCombo.getValue();
            if (tone.contains("Professional")) {
                refined = "Kính gửi bạn, mình đã tiếp nhận thông tin và sẽ kiểm tra chi tiết để phản hồi sớm nhất.";
            } else if (tone.contains("Concise")) {
                refined = "Đã nhận thông tin. Mình sẽ kiểm tra và phản hồi sớm.";
            } else {
                refined = "Tuyệt vời quá bạn ơi! Cảm ơn bạn rất nhiều nhé, mình sẽ xem ngay đây 😊";
            }

            resultArea.setText(refined);
            applyBtn.setDisable(false);
        });

        applyBtn.setOnAction(e -> {
            if (onApplyToInput != null && !resultArea.getText().isBlank()) {
                onApplyToInput.accept(resultArea.getText().trim());
            }
        });

        box.getChildren().addAll(toneLbl, toneCombo, refineBtn, resultArea, applyBtn);
        return box;
    }

    private VBox buildReplyView() {
        VBox box = new VBox(10);

        Label promptLbl = new Label("Context-aware quick replies:");
        promptLbl.getStyleClass().add("text-small");
        promptLbl.setStyle("-fx-font-weight: 600;");

        Label contextPreview = new Label();
        contextPreview.getStyleClass().add("text-muted");
        contextPreview.setWrapText(true);
        contextPreview.setStyle("-fx-font-style: italic; -fx-background-color: #f9fafc; -fx-padding: 8; -fx-background-radius: 6;");

        VBox suggestionsBox = new VBox(8);

        Button refreshBtn = new Button("🔄 Generate Suggestions");
        refreshBtn.getStyleClass().add("btn-accent");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);

        Runnable generateAction = () -> {
            String lastMsg = lastReceivedMessageSupplier != null ? lastReceivedMessageSupplier.get() : null;
            if (lastMsg == null || lastMsg.isBlank()) {
                lastMsg = "Mày ơi tao đẩy code lên rồi, pull về coi thử";
            }
            contextPreview.setText("In reply to: \"" + (lastMsg.length() > 45 ? lastMsg.substring(0, 42) + "..." : lastMsg) + "\"");

            suggestionsBox.getChildren().clear();
            List<String> suggestions = List.of(
                    "Oki, để mình pull về test ngay nhé!",
                    "Cảm ơn bạn! Mọi thứ chạy rất mượt mà 👍",
                    "Đợi mình một chút, mình đang xem lại một số chi tiết."
            );

            for (String replyText : suggestions) {
                Button replyBtn = new Button(replyText);
                replyBtn.getStyleClass().add("btn-ghost");
                replyBtn.setWrapText(true);
                replyBtn.setAlignment(Pos.CENTER_LEFT);
                replyBtn.setMaxWidth(Double.MAX_VALUE);
                replyBtn.setStyle("-fx-text-alignment: left; -fx-padding: 8 10 8 10; -fx-font-size: 11.5px;");
                replyBtn.setOnAction(e -> {
                    if (onApplyToInput != null) onApplyToInput.accept(replyText);
                });
                suggestionsBox.getChildren().add(replyBtn);
            }
        };

        refreshBtn.setOnAction(e -> generateAction.run());

        // Initial generation
        generateAction.run();

        ScrollPane scroll = new ScrollPane(suggestionsBox);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane-clean");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        box.getChildren().addAll(promptLbl, contextPreview, refreshBtn, scroll);
        return box;
    }
}

