package com.chatsystem.user.controller.chat;

import atlantafx.base.controls.Popover;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ChatMessageRow;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.user.messaging.ConversationCatalog;
import com.chatsystem.user.navigation.UserViewInputs;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.PopupWindow.AnchorLocation;

public final class ChatHistoryController {
    private static final double ASSISTANT_GAP = 8;
    private static final PseudoClass ASSISTANT_OPEN = PseudoClass.getPseudoClass("open");

    @FXML private Label conversationTitle;
    @FXML private Label conversationStatus;
    @FXML private ConversationAvatar conversationAvatar;
    @FXML private ScrollPane historyScroll;
    @FXML private VBox messageRows;
    @FXML private TextArea draftField;
    @FXML private HBox composer;
    @FXML private Button aiAssistantButton;

    private ViewNavigator navigator;
    private MockConversation conversation;

    @FXML
    private void openAssistant() {
        String conversationId = conversation.id();
        String draft = draftField.getText();
        var mode = draft.isBlank() ? UserViewInputs.AiMode.RECOMMEND : UserViewInputs.AiMode.REFINE;
        var input = new UserViewInputs.AiAssistant(conversationId, mode, draft, suggestion -> {
            if (conversation.id().equals(conversationId) && draftField.getText().equals(draft)) {
                draftField.setText(suggestion);
                draftField.requestFocus();
                draftField.positionCaret(suggestion.length());
            }
        });
        var popup = navigator.showPopover(UserViews.AI_ASSISTANT, aiAssistantButton, input, assistant -> {
            assistant.setArrowLocation(Popover.ArrowLocation.BOTTOM_RIGHT);
            assistant.setArrowSize(0);
            assistant.setCornerRadius(12);
            assistant.setHeaderAlwaysVisible(false);
            assistant.setCloseButtonEnabled(false);
            assistant.getStyleClass().add("chat-ai-popup");
            var pane = (Region) assistant.getContentNode();
            navigator.getRoot().layout();
            var insets = composer.getInsets();
            double availableWidth = composer.getWidth() - insets.getLeft() - insets.getRight();
            pane.setPrefWidth(Math.min(pane.getPrefWidth(), availableWidth));
            assistant.getRoot().getStylesheets().addAll(pane.getStylesheets());
            assistant.showingProperty().addListener((observable, wasShowing, showing) ->
                    aiAssistantButton.pseudoClassStateChanged(ASSISTANT_OPEN, showing));
        });

        positionAssistant(popup);
    }

    private void positionAssistant(Popover popup) {
        popup.getRoot().applyCss();
        popup.getRoot().layout();
        var composerBounds = composer.localToScreen(composer.getLayoutBounds());
        popup.setAnchorLocation(AnchorLocation.WINDOW_BOTTOM_RIGHT);
        popup.setAnchorY(composerBounds.getMinY() - ASSISTANT_GAP);
        double right = composerBounds.getMaxX() - composer.getInsets().getRight();
        popup.setAnchorX(right);
        // GTK can restore the initial X position while processing the vertical move.
        Platform.runLater(() -> {
            if (popup.isShowing()) {
                popup.setAnchorX(right);
            }
        });
    }

    public void show(ViewNavigator navigator, ConversationCatalog catalog, MockConversation conversation) {
        this.navigator = navigator;
        boolean changed = !conversation.equals(this.conversation);
        this.conversation = conversation;
        conversationTitle.setText(conversation.title());
        conversationStatus.setText(catalog.statusText(conversation));
        conversationStatus.setGraphic(ConversationAvatar.statusGraphic(conversation, catalog));
        conversationAvatar.show(conversation, catalog);
        if (changed) {
            draftField.clear();
            messageRows.getChildren().setAll(catalog.messagesFor(conversation.id()).stream()
                    .map(message -> new ChatMessageRow(message, catalog.user(message.senderId()).fullName(),
                            catalog.currentUserId().equals(message.senderId()))).toList());
            historyScroll.setVvalue(1);
        }
    }

    public void scrollToMessage(String id) {
        var active = conversation;
        Platform.runLater(() -> {
            if (!active.equals(conversation)) {
                return;
            }
            messageRows.getChildren().stream()
                    .filter(node -> node instanceof ChatMessageRow row && row.messageId().equals(id))
                    .findFirst().ifPresent(row -> {
                        historyScroll.layout();
                        double scrollable = messageRows.getHeight() - historyScroll.getViewportBounds().getHeight();
                        if (scrollable > 0) {
                            historyScroll.setVvalue(Math.clamp(row.getLayoutY() / scrollable, 0, 1));
                        }
                    });
        });
    }

    @FXML
    private void openInfo() {
        if (navigator.isDrawerOpen()) {
            navigator.closeDrawer();
        } else {
            var input = new UserViewInputs.MessageSearch(conversation.id(), this::scrollToMessage);
            navigator.showDrawer(UserViews.CONVERSATION_INFO, input, true);
        }
    }
}
