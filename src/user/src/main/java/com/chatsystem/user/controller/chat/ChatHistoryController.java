package com.chatsystem.user.controller.chat;

import atlantafx.base.controls.Popover;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserApplication;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ChatMessageRow;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.user.messaging.ConversationCatalog;
import com.chatsystem.user.navigation.UserViewInputs;
import java.util.List;
import java.util.Objects;
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
import javafx.stage.Popup;
import javafx.stage.PopupWindow.AnchorLocation;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public final class ChatHistoryController {
    private static final double ASSISTANT_GAP = 8;
    private static final PseudoClass ASSISTANT_OPEN = PseudoClass.getPseudoClass("open");
    private static final String TOKENS_CSS = Objects.requireNonNull(
            UserApplication.class.getResource("/com/chatsystem/user/css/chat-tokens.css")).toExternalForm();
    private static final String CHAT_CSS = Objects.requireNonNull(
            UserApplication.class.getResource("/com/chatsystem/user/css/chat.css")).toExternalForm();

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
    private Popup messageActionPopup;
    private ChatMessageRow activeMenuRow;

    @FXML
    private void openAssistant() {
        closeMessageMenu();
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
            closeMessageMenu();
            draftField.clear();
            messageRows.getChildren().setAll(catalog.messagesFor(conversation.id()).stream()
                    .map(message -> createMessageRow(catalog, message)).toList());
            historyScroll.setVvalue(1);
            var active = conversation;
            Platform.runLater(() -> {
                if (active.equals(this.conversation)) {
                    historyScroll.applyCss();
                    historyScroll.layout();
                    historyScroll.setVvalue(1);
                }
            });
        }
    }

    private ChatMessageRow createMessageRow(ConversationCatalog catalog, MockMessage message) {
        String senderName = catalog.user(message.senderId()).fullName();
        boolean outgoing = catalog.currentUserId().equals(message.senderId());
        boolean canReport = !outgoing && message.acceptedByServer() && message.state() != MockMessage.State.RECALLED;
        if (!canReport) {
            return new ChatMessageRow(message, senderName, outgoing, null);
        }
        var rowHolder = new ChatMessageRow[1];
        rowHolder[0] = new ChatMessageRow(message, senderName, false,
                anchor -> openMessageMenu(rowHolder[0], message, anchor));
        return rowHolder[0];
    }

    private void openMessageMenu(ChatMessageRow row, MockMessage message, Button anchor) {
        if (messageActionPopup != null && messageActionPopup.isShowing() && activeMenuRow == row) {
            closeMessageMenu();
            return;
        }
        closeMessageMenu();

        VBox menu = new VBox(2);
        menu.setId("messageActionMenu");
        menu.getStyleClass().add("chat-message-menu");
        navigator.themes().apply(menu);
        menu.getStylesheets().addAll(TOKENS_CSS, CHAT_CSS);

        Button reportBtn = new Button("Báo cáo", FontIcon.of(Feather.FLAG, 14));
        reportBtn.setId("messageActionReport");
        reportBtn.setMaxWidth(Double.MAX_VALUE);
        reportBtn.getStyleClass().addAll("chat-message-menu-item", "chat-message-menu-danger");
        reportBtn.setOnAction(event -> {
            closeMessageMenu();
            navigator.showModal(UserViews.REPORT_SPAM,
                    new UserViewInputs.ReportSpam(conversation.id(), List.of(message.id())), true);
        });
        menu.getChildren().add(reportBtn);

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        popup.setAnchorLocation(AnchorLocation.WINDOW_TOP_LEFT);
        popup.getContent().add(menu);
        popup.setOnHidden(event -> {
            if (activeMenuRow == row) {
                row.setMenuOpen(false);
                activeMenuRow = null;
            }
            if (messageActionPopup == popup) {
                messageActionPopup = null;
            }
        });

        menu.applyCss();
        menu.layout();
        var bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        double menuHeight = menu.prefHeight(-1);
        double menuWidth = menu.prefWidth(-1);
        var ownerWindow = anchor.getScene().getWindow();
        double x = bounds.getMinX();
        if (ownerWindow != null && x + menuWidth > ownerWindow.getX() + ownerWindow.getWidth() - 8) {
            x = Math.max(ownerWindow.getX() + 8, bounds.getMaxX() - menuWidth);
        }
        double y = bounds.getMaxY() + 4;
        if (ownerWindow != null) {
            double minY = ownerWindow.getY() + 8;
            double maxY = Math.max(minY, ownerWindow.getY() + ownerWindow.getHeight() - menuHeight - 8);
            if (y + menuHeight > ownerWindow.getY() + ownerWindow.getHeight() - 8) {
                y = bounds.getMinY() - menuHeight - 4;
            }
            y = Math.clamp(y, minY, maxY);
        }

        messageActionPopup = popup;
        activeMenuRow = row;
        row.setMenuOpen(true);
        popup.show(anchor, x, y);
        reportBtn.requestFocus();
    }

    private void closeMessageMenu() {
        if (messageActionPopup != null) {
            Popup closing = messageActionPopup;
            messageActionPopup = null;
            closing.hide();
        }
        if (activeMenuRow != null) {
            activeMenuRow.setMenuOpen(false);
            activeMenuRow = null;
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
        closeMessageMenu();
        if (navigator.isDrawerOpen()) {
            navigator.closeDrawer();
        } else {
            var input = new UserViewInputs.MessageSearch(conversation.id(), this::scrollToMessage);
            navigator.showDrawer(UserViews.CONVERSATION_INFO, input, true);
        }
    }
}
