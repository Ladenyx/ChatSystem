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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public final class ChatHistoryController {
    private static final double ASSISTANT_GAP = 8;
    private static final String SELECTION_DEFAULT_NOTE =
            "Chỉ xóa ở phía bạn · Những người khác vẫn xem được";
    private static final PseudoClass ASSISTANT_OPEN = PseudoClass.getPseudoClass("open");
    private static final PseudoClass OPTION_SELECTED = PseudoClass.getPseudoClass("selected");
    private static final String TOKENS_CSS = Objects.requireNonNull(
            UserApplication.class.getResource("/com/chatsystem/user/css/chat-tokens.css")).toExternalForm();
    private static final String CHAT_CSS = Objects.requireNonNull(
            UserApplication.class.getResource("/com/chatsystem/user/css/chat.css")).toExternalForm();

    @FXML private Label conversationTitle;
    @FXML private Label conversationStatus;
    @FXML private ConversationAvatar conversationAvatar;
    @FXML private HBox normalHeader;
    @FXML private HBox selectionHeader;
    @FXML private Label selectionCountLabel;
    @FXML private ScrollPane historyScroll;
    @FXML private VBox messageRows;
    @FXML private VBox emptyHistory;
    @FXML private TextArea draftField;
    @FXML private HBox composer;
    @FXML private HBox selectionActionBar;
    @FXML private Label selectionBarNote;
    @FXML private Button deleteSelectedButton;
    @FXML private Button aiAssistantButton;

    private ViewNavigator navigator;
    private ConversationCatalog catalog;
    private MockConversation conversation;
    private Popup messageActionPopup;
    private ChatMessageRow activeMenuRow;
    private Dialog<ButtonType> activeDialog;
    private boolean selectionMode;
    private final java.util.Set<String> selectedMessageIds = new java.util.LinkedHashSet<>();
    private final Runnable catalogListener = this::refreshMessages;

    @FXML
    private void initialize() {
        for (Node node : List.of(normalHeader, selectionHeader, composer, selectionActionBar,
                historyScroll, emptyHistory)) {
            node.managedProperty().bind(node.visibleProperty());
        }
        Node rootPane = normalHeader.getParent() != null ? normalHeader.getParent().getParent() : null;
        if (rootPane != null) {
            rootPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (selectionMode && event.getCode() == KeyCode.ESCAPE) {
                    exitSelectionMode();
                    event.consume();
                }
            });
        }
    }

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
        if (this.catalog != catalog) {
            if (this.catalog != null) {
                this.catalog.removeChangeListener(catalogListener);
            }
            this.catalog = catalog;
        }
        if (this.catalog != null) {
            this.catalog.addChangeListener(catalogListener);
        }
        boolean changed = !conversation.equals(this.conversation);
        this.conversation = conversation;
        conversationTitle.setText(conversation.title());
        conversationStatus.setText(catalog.statusText(conversation));
        conversationStatus.setGraphic(ConversationAvatar.statusGraphic(conversation, catalog));
        conversationAvatar.show(conversation, catalog);
        if (changed) {
            closeMessageMenu();
            closeMessageDialog();
            exitSelectionModeState(false);
            draftField.clear();
            renderMessages(true);
        } else {
            refreshMessages();
        }
    }

    public void onLeave() {
        closeMessageMenu();
        closeMessageDialog();
        exitSelectionModeState(false);
        if (catalog != null) {
            catalog.removeChangeListener(catalogListener);
        }
    }

    private void refreshMessages() {
        if (conversation == null || catalog == null || historyScroll.getScene() == null) {
            return;
        }
        renderMessages(false);
    }

    private void renderMessages(boolean scrollToBottom) {
        double prevVvalue = historyScroll.getVvalue();
        boolean wasAtBottom = scrollToBottom || prevVvalue >= 0.98;
        List<MockMessage> messages = catalog.messagesFor(conversation.id());
        if (selectionMode) {
            java.util.Set<String> eligibleIds = messages.stream()
                    .filter(catalog::canDeleteForSelf)
                    .map(MockMessage::id)
                    .collect(java.util.stream.Collectors.toSet());
            selectedMessageIds.retainAll(eligibleIds);
            updateSelectionControls();
        }
        boolean empty = messages.isEmpty();
        emptyHistory.setVisible(empty);
        historyScroll.setVisible(!empty);
        messageRows.getChildren().setAll(messages.stream()
                .map(message -> createMessageRow(catalog, message)).toList());
        if (empty) {
            return;
        }
        if (wasAtBottom) {
            historyScroll.setVvalue(1);
        }
        var active = conversation;
        Platform.runLater(() -> {
            if (active.equals(this.conversation)) {
                historyScroll.applyCss();
                historyScroll.layout();
                historyScroll.setVvalue(wasAtBottom ? 1 : prevVvalue);
            }
        });
    }

    private ChatMessageRow createMessageRow(ConversationCatalog catalog, MockMessage message) {
        String senderName = catalog.user(message.senderId()).fullName();
        boolean outgoing = catalog.currentUserId().equals(message.senderId());
        boolean eligible = catalog.canDeleteForSelf(message);
        if (selectionMode) {
            boolean selected = selectedMessageIds.contains(message.id());
            return new ChatMessageRow(
                    message,
                    senderName,
                    outgoing,
                    null,
                    true,
                    selected,
                    eligible ? checked -> toggleSelection(message.id(), checked) : null);
        }
        if (!eligible) {
            return new ChatMessageRow(message, senderName, outgoing, null);
        }
        var rowHolder = new ChatMessageRow[1];
        rowHolder[0] = new ChatMessageRow(message, senderName, outgoing,
                anchor -> openMessageMenu(rowHolder[0], message, outgoing, anchor));
        return rowHolder[0];
    }

    private void openMessageMenu(ChatMessageRow row, MockMessage message, boolean outgoing, Button anchor) {
        if (messageActionPopup != null && messageActionPopup.isShowing() && activeMenuRow == row) {
            closeMessageMenu();
            return;
        }
        closeMessageMenu();

        VBox menu = new VBox(2);
        menu.setId("messageActionMenu");
        menu.getStyleClass().add("chat-message-menu");
        menu.setFocusTraversable(true);
        navigator.themes().apply(menu);
        menu.getStylesheets().addAll(TOKENS_CSS, CHAT_CSS);

        if (outgoing) {
            Button recallBtn = menuItem("messageActionRecall", "Thu hồi", Feather.ROTATE_CCW, false,
                    () -> openRecallDialog(message));
            Button selectBtn = menuItem("messageActionSelect", "Chọn nhiều tin", Feather.CHECK_SQUARE, false,
                    () -> enterSelectionMode(message.id()));
            menu.getChildren().addAll(recallBtn, selectBtn);
        } else {
            Button removeBtn = menuItem("messageActionRemoveForSelf", "Gỡ", Feather.TRASH_2, false,
                    () -> openRemoveForSelfDialog(message));
            Button selectBtn = menuItem("messageActionSelect", "Chọn nhiều tin", Feather.CHECK_SQUARE, false,
                    () -> enterSelectionMode(message.id()));
            Button reportBtn = menuItem("messageActionReport", "Báo cáo", Feather.FLAG, true,
                    () -> navigator.showModal(UserViews.REPORT_SPAM,
                            new UserViewInputs.ReportSpam(conversation.id(), List.of(message.id())), true));
            menu.getChildren().addAll(removeBtn, selectBtn, reportBtn);
        }

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
        var menuBounds = menu.getBoundsInParent();
        double shadowOffsetX = -menuBounds.getMinX();
        double shadowOffsetY = -menuBounds.getMinY();
        double cardWidth = menu.prefWidth(-1);
        double cardHeight = menu.prefHeight(-1);

        var bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        var historyBounds = historyScroll.localToScreen(historyScroll.getBoundsInLocal());
        var ownerWindow = anchor.getScene().getWindow();

        double cardX = outgoing ? bounds.getMaxX() - cardWidth : bounds.getMinX();
        if (historyBounds != null) {
            if (!outgoing && cardX + cardWidth > historyBounds.getMaxX() - 8) {
                cardX = bounds.getMaxX() - cardWidth;
            } else if (outgoing && cardX < historyBounds.getMinX() + 8) {
                cardX = bounds.getMinX();
            }
            double minX = historyBounds.getMinX() + 8;
            double maxX = Math.max(minX, historyBounds.getMaxX() - cardWidth - 8);
            cardX = Math.clamp(cardX, minX, maxX);
        } else if (ownerWindow != null && cardX + cardWidth > ownerWindow.getX() + ownerWindow.getWidth() - 8) {
            cardX = Math.max(ownerWindow.getX() + 8, bounds.getMaxX() - cardWidth);
        }

        double cardY = bounds.getMaxY() + 4;
        double areaTop = historyBounds != null ? historyBounds.getMinY() + 8
                : (ownerWindow != null ? ownerWindow.getY() + 8 : cardY);
        double areaBottom = historyBounds != null ? historyBounds.getMaxY() - 8
                : (ownerWindow != null ? ownerWindow.getY() + ownerWindow.getHeight() - 8 : cardY + cardHeight);
        if (cardY + cardHeight > areaBottom) {
            cardY = bounds.getMinY() - cardHeight - 4;
        }
        cardY = Math.clamp(cardY, areaTop, Math.max(areaTop, areaBottom - cardHeight));

        messageActionPopup = popup;
        activeMenuRow = row;
        row.setMenuOpen(true);
        popup.show(anchor, cardX - shadowOffsetX, cardY - shadowOffsetY);
        menu.requestFocus();
    }

    private Button menuItem(String id, String text, Feather icon, boolean danger, Runnable action) {
        Button button = new Button(text, FontIcon.of(icon, 14));
        button.setId(id);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("chat-message-menu-item");
        if (danger) {
            button.getStyleClass().add("chat-message-menu-danger");
        }
        button.setOnAction(event -> {
            closeMessageMenu();
            action.run();
        });
        return button;
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

    private void closeMessageDialog() {
        if (activeDialog != null) {
            Dialog<ButtonType> closing = activeDialog;
            activeDialog = null;
            ButtonType cancel = closing.getDialogPane().getButtonTypes().stream()
                    .filter(type -> type.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE)
                    .findFirst().orElse(null);
            if (cancel != null) {
                closing.setResult(cancel);
            }
            closing.close();
        }
    }

    private void enterSelectionMode(String initialMessageId) {
        closeMessageDialog();
        selectionMode = true;
        selectedMessageIds.clear();
        if (initialMessageId != null) {
            selectedMessageIds.add(initialMessageId);
        }
        normalHeader.setVisible(false);
        selectionHeader.setVisible(true);
        composer.setVisible(false);
        selectionActionBar.setVisible(true);
        resetSelectionNote();
        updateSelectionControls();
        renderMessages(false);
    }

    @FXML
    private void exitSelectionMode() {
        exitSelectionModeState(true);
    }

    private void exitSelectionModeState(boolean rerender) {
        if (!selectionMode && selectedMessageIds.isEmpty()) {
            return;
        }
        selectionMode = false;
        selectedMessageIds.clear();
        selectionHeader.setVisible(false);
        normalHeader.setVisible(true);
        selectionActionBar.setVisible(false);
        composer.setVisible(true);
        resetSelectionNote();
        if (rerender && conversation != null && catalog != null) {
            renderMessages(false);
        }
    }

    private void toggleSelection(String messageId, boolean checked) {
        if (checked && selectedMessageIds.size() >= ConversationCatalog.MAX_BULK_DELETE
                && !selectedMessageIds.contains(messageId)) {
            messageRows.getChildren().stream()
                    .filter(node -> node instanceof ChatMessageRow row && row.messageId().equals(messageId))
                    .map(ChatMessageRow.class::cast)
                    .findFirst()
                    .ifPresent(row -> row.setSelected(false));
            selectionBarNote.setText("Chỉ có thể chọn tối đa "
                    + ConversationCatalog.MAX_BULK_DELETE + " tin nhắn mỗi lần.");
            if (!selectionBarNote.getStyleClass().contains("chat-selection-warning")) {
                selectionBarNote.getStyleClass().add("chat-selection-warning");
            }
            return;
        }
        if (checked) {
            selectedMessageIds.add(messageId);
        } else {
            selectedMessageIds.remove(messageId);
        }
        resetSelectionNote();
        updateSelectionControls();
    }

    private void resetSelectionNote() {
        selectionBarNote.setText(SELECTION_DEFAULT_NOTE);
        selectionBarNote.getStyleClass().remove("chat-selection-warning");
    }

    private void updateSelectionControls() {
        int count = selectedMessageIds.size();
        selectionCountLabel.setText("Đã chọn " + count + " tin nhắn");
        deleteSelectedButton.setText(count > 0 ? "Xóa phía mình (" + count + ")" : "Xóa phía mình");
        deleteSelectedButton.setDisable(count == 0);
    }

    private void openRecallDialog(MockMessage message) {
        boolean canRecallEveryone = catalog.canRecallForEveryone(message);
        ToggleGroup scopeGroup = new ToggleGroup();
        RadioButton everyoneRadio = new RadioButton();
        RadioButton selfRadio = new RadioButton();
        everyoneRadio.setSelected(canRecallEveryone);
        selfRadio.setSelected(!canRecallEveryone);

        String everyoneDesc = canRecallEveryone
                ? "Tin nhắn này sẽ bị thu hồi với mọi người trong đoạn chat. Nội dung sẽ được thay bằng thông báo đã thu hồi và không còn xuất hiện trong tìm kiếm."
                : "Đã quá 48 giờ kể từ khi gửi nên không thể thu hồi với mọi người. Bạn vẫn có thể gỡ tin nhắn này ở phía mình.";
        HBox everyoneRow = recallOptionRow(
                "recallForEveryoneOption", "recallForEveryoneRadio", scopeGroup, everyoneRadio,
                "Thu hồi với mọi người", everyoneDesc, canRecallEveryone);
        HBox selfRow = recallOptionRow(
                "recallForSelfOption", "recallForSelfRadio", scopeGroup, selfRadio,
                "Thu hồi với bạn",
                "Chúng tôi sẽ gỡ tin nhắn này ở phía bạn. Những người khác trong đoạn chat vẫn có thể xem được.",
                true);

        VBox options = new VBox(10, everyoneRow, selfRow);
        showMessageDialog(
                "recallMessageDialog",
                "Bạn muốn thu hồi tin nhắn này ở phía ai?",
                "recallDialogTitle",
                "recallDialogClose",
                460,
                options,
                "recallDialogCancel",
                "Hủy",
                "recallDialogConfirm",
                "Gỡ",
                "chat-primary",
                () -> {
                    if (everyoneRadio.isSelected()) {
                        catalog.recallForEveryone(message.id());
                    } else {
                        catalog.deleteForSelf(List.of(message.id()));
                    }
                });
    }

    private void openRemoveForSelfDialog(MockMessage message) {
        Label detail = new Label(
                "Chúng tôi sẽ gỡ tin nhắn này cho bạn. Những thành viên khác trong đoạn chat vẫn có thể xem được.");
        detail.getStyleClass().add("chat-message-dialog-detail");
        detail.setWrapText(true);
        showMessageDialog(
                "removeForSelfDialog",
                "Gỡ đối với bạn",
                "removeForSelfTitle",
                "removeForSelfClose",
                420,
                detail,
                "removeForSelfCancel",
                "Hủy",
                "removeForSelfConfirm",
                "Gỡ",
                "chat-primary",
                () -> catalog.deleteForSelf(List.of(message.id())));
    }

    @FXML
    private void confirmBulkDelete() {
        if (selectedMessageIds.isEmpty()) {
            return;
        }
        int count = selectedMessageIds.size();
        Label detail = new Label(count
                + " tin nhắn đã chọn sẽ bị gỡ khỏi lịch sử và tìm kiếm của bạn. Những người khác trong đoạn chat vẫn có thể xem được.");
        detail.getStyleClass().add("chat-message-dialog-detail");
        detail.setWrapText(true);
        showMessageDialog(
                "bulkDeleteConfirmDialog",
                "Xóa " + count + " tin nhắn ở phía bạn?",
                "bulkDeleteTitle",
                "bulkDeleteClose",
                420,
                detail,
                "bulkDeleteCancel",
                "Hủy",
                "bulkDeleteConfirm",
                "Xóa phía mình",
                "chat-danger-solid",
                () -> {
                    List<String> ids = List.copyOf(selectedMessageIds);
                    exitSelectionModeState(false);
                    catalog.deleteForSelf(ids);
                });
    }

    private HBox recallOptionRow(String cardId, String radioId, ToggleGroup group,
                                 RadioButton radio, String titleText, String descText, boolean enabled) {
        radio.setId(radioId);
        radio.setToggleGroup(group);
        radio.setDisable(!enabled);

        Label title = new Label(titleText);
        title.getStyleClass().add("chat-recall-option-title");
        title.setWrapText(true);

        Label desc = new Label(descText);
        desc.getStyleClass().add("chat-recall-option-desc");
        desc.setWrapText(true);
        desc.setMinHeight(Region.USE_PREF_SIZE);

        VBox copy = new VBox(4, title, desc);
        copy.setMinWidth(0);
        HBox.setHgrow(copy, Priority.ALWAYS);

        HBox row = new HBox(12, radio, copy);
        row.setId(cardId);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("chat-recall-option");
        if (!enabled) {
            row.getStyleClass().add("is-disabled");
        } else {
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && !isInsideNode(event.getTarget(), radio)) {
                    radio.setSelected(true);
                    radio.requestFocus();
                    event.consume();
                }
            });
        }
        radio.selectedProperty().addListener((obs, oldVal, selected) ->
                row.pseudoClassStateChanged(OPTION_SELECTED, selected));
        row.pseudoClassStateChanged(OPTION_SELECTED, radio.isSelected());
        return row;
    }

    private void showMessageDialog(String dialogId, String titleText, String titleId, String closeButtonId,
                                   double prefWidth, Node bodyContent,
                                   String cancelId, String cancelText,
                                   String confirmId, String confirmText, String confirmStyleClass,
                                   Runnable onConfirm) {
        closeMessageDialog();
        Dialog<ButtonType> dialog = new Dialog<>();
        Window owner = historyScroll.getScene() != null ? historyScroll.getScene().getWindow() : null;
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle(titleText);

        var pane = dialog.getDialogPane();
        pane.setId(dialogId);
        pane.getStyleClass().add("chat-message-dialog");
        pane.setPrefWidth(prefWidth);

        Label title = new Label(titleText);
        title.setId(titleId);
        title.getStyleClass().add("chat-message-dialog-title");
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);

        Button closeButton = new Button();
        closeButton.setId(closeButtonId);
        closeButton.getStyleClass().add("chat-icon-btn");
        closeButton.setGraphic(FontIcon.of(Feather.X, 16));
        closeButton.setAccessibleText("Đóng hộp thoại");
        closeButton.setTooltip(new Tooltip("Đóng (Esc)"));

        HBox header = new HBox(12, title, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("chat-message-dialog-header");

        VBox body = new VBox(bodyContent);
        body.getStyleClass().add("chat-message-dialog-body");

        pane.setContent(new VBox(header, body));

        ButtonType cancelType = new ButtonType(cancelText, ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType confirmType = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().setAll(cancelType, confirmType);

        Button cancelButton = (Button) pane.lookupButton(cancelType);
        Button confirmButton = (Button) pane.lookupButton(confirmType);
        ButtonBar.setButtonUniformSize(cancelButton, false);
        ButtonBar.setButtonUniformSize(confirmButton, false);
        cancelButton.setId(cancelId);
        cancelButton.getStyleClass().add("chat-dialog-cancel");
        cancelButton.setDefaultButton(true);
        confirmButton.setId(confirmId);
        confirmButton.getStyleClass().add(confirmStyleClass);
        confirmButton.setDefaultButton(false);
        closeButton.setOnAction(event -> cancelButton.fire());

        if (navigator != null) {
            navigator.themes().apply(pane);
        }
        for (String sheet : List.of(TOKENS_CSS, CHAT_CSS)) {
            if (!pane.getStylesheets().contains(sheet)) {
                pane.getStylesheets().add(sheet);
            }
        }

        dialog.setOnHidden(event -> {
            if (activeDialog == dialog) {
                activeDialog = null;
            }
            if (dialog.getResult() == confirmType) {
                onConfirm.run();
            }
        });
        activeDialog = dialog;
        dialog.show();
        Platform.runLater(cancelButton::requestFocus);
    }

    private static boolean isInsideNode(Object target, Node ancestor) {
        if (!(target instanceof Node current)) {
            return false;
        }
        while (current != null) {
            if (current == ancestor) {
                return true;
            }
            current = current.getParent();
        }
        return false;
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
