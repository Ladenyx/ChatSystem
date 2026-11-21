package com.chatsystem.user.controller.chat;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockMessage.State;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.controller.ai.AiAssistantController;
import com.chatsystem.user.controller.ai.OfflineAiAssistantService.Mode;
import com.chatsystem.user.control.ChatIcon;
import com.chatsystem.user.control.ChatMessageRow;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.user.controller.chat.OfflineChatDemoService.Access;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Duration;

/** Displays a session-local history and composer inside the MainShell. */
public final class ChatHistoryController {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final LocalDate TODAY = MockData.SNAPSHOT_AT.atZone(MockData.DISPLAY_ZONE).toLocalDate();

    @FXML private HBox normalHeader;
    @FXML private Label conversationTitle;
    @FXML private Label conversationStatus;
    @FXML private ConversationAvatar conversationAvatar;
    @FXML private HBox selectionHeader;
    @FXML private Label selectedCount;
    @FXML private Button deleteSelectedButton;
    @FXML private ScrollPane historyScroll;
    @FXML private VBox historyNotice;
    @FXML private VBox messageRows;
    @FXML private VBox composer;
    @FXML private TextArea draftField;
    @FXML private Button sendButton;
    @FXML private Button refineButton;
    @FXML private Button recommendButton;
    @FXML private VBox selectionFooter;
    @FXML private VBox readOnlyFooter;
    @FXML private Label readOnlyTitle;
    @FXML private Label readOnlyText;

    private final Set<String> selectedIds = new LinkedHashSet<>();
    private ViewNavigator navigator;
    private ChatDemoFlow flow;
    private MockConversation conversation;
    private Runnable sidebarChanged;
    private int historyLimit;
    private boolean selecting;
    private boolean restoringDraft;
    private long renderVersion;
    private long draftRevision;
    private Alert confirmation;
    private String highlightedMessageId;
    private Runnable focusSearch;

    @FXML
    private void initialize() {
        normalHeader.managedProperty().bind(normalHeader.visibleProperty());
        selectionHeader.managedProperty().bind(selectionHeader.visibleProperty());
        composer.managedProperty().bind(composer.visibleProperty());
        selectionFooter.managedProperty().bind(selectionFooter.visibleProperty());
        readOnlyFooter.managedProperty().bind(readOnlyFooter.visibleProperty());
        historyNotice.visibleProperty().bind(Bindings.isNotEmpty(historyNotice.getChildren()));
        historyNotice.managedProperty().bind(historyNotice.visibleProperty());
        draftField.textProperty().addListener((observable, old, draft) -> {
            draftRevision++;
            if (!restoringDraft && conversation != null && flow != null) {
                flow.saveDraft(conversation.id(), draft);
            }
            updateComposerButtons();
        });
        draftField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                sendMessage();
                event.consume();
            }
        });
    }

    public void show(ViewNavigator navigator, ChatDemoFlow flow, MockConversation chat, Runnable sidebarChanged) {
        this.navigator = navigator;
        this.sidebarChanged = sidebarChanged;
        boolean changed = this.flow != flow || conversation == null || !conversation.id().equals(chat.id());
        this.flow = flow;
        conversation = chat;
        if (changed) {
            navigator.closePopover();
            historyLimit = OfflineChatDemoService.PAGE_SIZE;
            selectedIds.clear();
            selecting = false;
            highlightedMessageId = null;
            focusSearch = null;
            restoringDraft = true;
            draftField.setText(flow.draft(chat.id()));
            restoringDraft = false;
        }
        updateHeader();
        updateChrome();
        ScrollTarget start = flow.chat().access(chat.id()) == Access.MISSING_KEY
                ? new ScrollTarget(false, null, 0, 0) : ScrollTarget.bottom();
        renderHistory(changed ? start : currentPosition(true));
    }

    public void detach() {
        renderVersion++;
        navigator = null;
        sidebarChanged = null;
        if (confirmation != null) {
            confirmation.close();
        }
        if (selecting) {
            selecting = false;
            selectedIds.clear();
        }
    }

    @FXML
    private void openInfo() {
        if (navigator.isDrawerOpen()) {
            navigator.closeDrawer();
        } else {
            navigator.showDrawer(UserViews.CONVERSATION_INFO, true);
        }
    }

    public void revealMessage(String messageId, int requiredLimit) {
        highlightedMessageId = messageId;
        focusSearch = null;
        selecting = false;
        selectedIds.clear();
        updateChrome();
        historyLimit = Math.max(historyLimit, requiredLimit);
        renderHistory(new ScrollTarget(false, messageId, -24, 0));
    }

    public void showUnavailableResult(Runnable focusSearch) {
        this.focusSearch = focusSearch;
        highlightedMessageId = null;
        renderHistory(currentPosition(false));
    }

    public void refreshAfterDrawerChange(MockConversation updated) {
        ScrollTarget position = currentPosition(true);
        conversation = updated;
        updateHeader();
        highlightedMessageId = null;
        focusSearch = null;
        selecting = false;
        selectedIds.clear();
        updateChrome();
        renderHistory(position);
    }

    private void updateHeader() {
        conversationTitle.setText(conversation.title());
        conversationStatus.setText(flow.statusText(conversation));
        conversationAvatar.show(conversation, flow);
    }

    @FXML
    private void sendMessage() {
        if (flow == null || conversation == null || selecting) {
            return;
        }
        MockMessage message = flow.chat().send(conversation.id(), draftField.getText());
        if (message != null) {
            draftField.clear();
            renderHistory(ScrollTarget.bottom());
            sidebarChanged.run();
            simulateSend(message.conversationId(), message.id(), false);
        }
    }

    @FXML
    private void refineDraft() {
        openAiAssistant(Mode.REFINE, refineButton);
    }

    @FXML
    private void recommendReply() {
        openAiAssistant(Mode.RECOMMEND, recommendButton);
    }

    private void openAiAssistant(Mode mode, Button anchor) {
        if (!canCompose() || (mode == Mode.REFINE && draftField.getText().isBlank())) {
            return;
        }
        var session = flow;
        var owner = navigator;
        String chatId = conversation.id();
        long version = draftRevision;
        AiAssistantController.open(owner, anchor, new AiAssistantController.Input(mode, conversation.title(), draftField.getText(),
                () -> session.chat().availableAiContext(chatId),
                senderId -> MockData.PREVIEW_USER_ID.equals(senderId) ? "Bạn" : session.data().user(senderId).fullName(),
                () -> navigator == owner && flow == session && session.isCurrent(owner) && canCompose()
                        && conversation.id().equals(chatId) && draftRevision == version,
                draftField::setText));
    }

    private boolean canCompose() {
        return navigator != null && flow != null && conversation != null && !selecting
                && flow.chat().access(conversation.id()) == Access.AVAILABLE;
    }

    private void updateComposerButtons() {
        boolean enabled = canCompose();
        boolean blank = draftField.getText().isBlank();
        sendButton.setDisable(!enabled || blank);
        refineButton.setDisable(!enabled || blank);
        recommendButton.setDisable(!enabled);
    }

    private void retry(MockMessage message) {
        if (flow.chat().retry(message.conversationId(), message.id())) {
            renderHistory(currentPosition(true));
            simulateSend(message.conversationId(), message.id(), true);
        }
    }

    private void simulateSend(String chatId, String messageId, boolean retry) {
        ChatDemoFlow session = flow;
        ViewNavigator owner = navigator;
        after(650, () -> {
            if (!session.isCurrent(owner)) {
                return;
            }
            boolean stored = session.chat().acknowledge(chatId, messageId, retry);
            refreshAfterSend(session, chatId);
            MockConversation target = session.chat().conversations().stream()
                    .filter(chat -> chat.id().equals(chatId)).findFirst().orElseThrow();
            if (stored && (target.encrypted() || session.isOnline(target))) {
                simulateReceipts(session, owner, chatId, messageId);
            }
        });
    }

    private void simulateReceipts(ChatDemoFlow session, ViewNavigator owner, String chatId, String messageId) {
        after(550, () -> {
            if (!session.isCurrent(owner) || !session.chat().advanceReceipt(chatId, messageId, State.DELIVERED)) {
                return;
            }
            refreshAfterSend(session, chatId);
            after(550, () -> {
                if (session.isCurrent(owner) && session.chat().advanceReceipt(chatId, messageId, State.READ)) {
                    refreshAfterSend(session, chatId);
                }
            });
        });
    }

    private static void after(double milliseconds, Runnable task) {
        PauseTransition wait = new PauseTransition(Duration.millis(milliseconds));
        wait.setOnFinished(event -> task.run());
        wait.play();
    }

    private void refreshAfterSend(ChatDemoFlow session, String chatId) {
        if (navigator != null && flow == session && conversation.id().equals(chatId)) {
            renderHistory(currentPosition(true));
        }
    }

    private void updateChrome() {
        normalHeader.setVisible(!selecting);
        selectionHeader.setVisible(selecting);
        selectedCount.setText("Đã chọn " + selectedIds.size() + " tin nhắn");
        deleteSelectedButton.setDisable(selectedIds.isEmpty());
        selectionFooter.setVisible(selecting);
        Access access = flow.chat().access(conversation.id());
        readOnlyFooter.setVisible(!selecting && access.readOnlyGroup());
        if (access.readOnlyGroup()) {
            readOnlyTitle.setText(access == Access.GROUP_LEFT ? "Bạn đã rời nhóm" : "Bạn đã bị xóa khỏi nhóm");
            readOnlyText.setText(groupHistoryNotice(access));
        }
        composer.setVisible(!selecting && (access == Access.AVAILABLE || access == Access.MISSING_KEY));
        draftField.setDisable(access != Access.AVAILABLE);
        draftField.setPromptText(access == Access.MISSING_KEY ? "Cần khóa E2EE hợp lệ để gửi tin." : "Nhập tin nhắn");
        updateComposerButtons();
    }

    private void enterSelection(MockMessage message) {
        ScrollTarget position = currentPosition(false);
        selecting = true;
        selectedIds.add(message.id());
        updateChrome();
        renderHistory(position);
        focusCheckBox(message.id());
    }

    private void toggleMessage(String messageId) {
        if (!selectedIds.remove(messageId)) {
            selectedIds.add(messageId);
        }
        updateChrome();
    }

    private void focusCheckBox(String messageId) {
        var checkbox = messageRows.lookup("#select-" + messageId);
        if (checkbox != null) {
            checkbox.requestFocus();
        }
    }

    @FXML
    private void cancelSelection() {
        ScrollTarget position = currentPosition(false);
        selecting = false;
        selectedIds.clear();
        updateChrome();
        renderHistory(position);
        if (composer.isVisible() && !draftField.isDisabled()) {
            draftField.requestFocus();
        }
    }

    @FXML
    private void deleteSelected() {
        if (!selectedIds.isEmpty()) {
            confirmDelete(Set.copyOf(selectedIds));
        }
    }

    private void confirmDelete(Set<String> ids) {
        confirm("Xóa phía mình", "Xóa " + ids.size() + " tin nhắn khỏi lịch sử của bạn?",
                () -> flow.chat().deleteForSelf(conversation.id(), ids));
    }

    private void confirmRecall(MockMessage message) {
        confirm("Thu hồi với mọi người", "Tin nhắn sẽ được thay bằng dấu thông báo thu hồi.",
                () -> flow.chat().recall(conversation.id(), message.id()));
    }

    private void confirm(String actionLabel, String text, Runnable action) {
        ButtonType proceed = new ButtonType(actionLabel);
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION, text, proceed, ButtonType.CANCEL);
        dialog.setTitle(actionLabel);
        dialog.setHeaderText(null);
        dialog.initOwner(historyScroll.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        navigator.themes().apply(dialog.getDialogPane());
        confirmation = dialog;
        dialog.setOnHidden(event -> {
            confirmation = null;
            if (navigator != null && dialog.getResult() == proceed) {
                ScrollTarget position = currentPosition(false);
                action.run();
                selecting = false;
                selectedIds.clear();
                updateChrome();
                sidebarChanged.run();
                renderHistory(position);
            }
        });
        dialog.show();
    }

    private void renderHistory(ScrollTarget position) {
        long version = ++renderVersion;
        var chat = flow.chat();
        var page = chat.page(conversation.id(), historyLimit);
        Access access = chat.access(conversation.id());
        historyNotice.getChildren().clear();
        if (focusSearch != null) {
            Label feedback = new Label("Tin nhắn này không còn khả dụng. Kết quả đã được cập nhật.");
            feedback.setWrapText(true);
            Button retrySearch = new Button("Chọn kết quả khác");
            retrySearch.getStyleClass().add("chat-outline");
            retrySearch.setOnAction(event -> {
                Runnable action = focusSearch;
                focusSearch = null;
                renderHistory(currentPosition(false));
                action.run();
            });
            VBox state = new VBox(8, feedback, retrySearch);
            state.setId("unavailableSearchResult");
            state.getStyleClass().add("chat-search-feedback");
            historyNotice.getChildren().add(state);
        }
        messageRows.getChildren().clear();
        if (access != Access.AVAILABLE) {
            addAccessNotice(access);
        } else if (page.messages().stream().anyMatch(chat::canRetry)) {
            addWarning("Một số tin chưa được server xác nhận.");
        }
        if (page.hasOlder()) {
            Button older = new Button("Tải thêm tin nhắn cũ");
            older.setId("loadOlderMessages");
            older.getStyleClass().add("chat-outline");
            older.setOnAction(event -> {
                ScrollTarget anchor = currentPosition(false);
                historyLimit += OfflineChatDemoService.PAGE_SIZE;
                renderHistory(anchor);
            });
            messageRows.getChildren().add(older);
        }
        if (page.messages().isEmpty()) {
            Label empty = new Label("Chưa có tin nhắn");
            empty.getStyleClass().add("chat-caption");
            messageRows.getChildren().add(empty);
        }
        LocalDate previous = null;
        for (MockMessage message : page.messages()) {
            LocalDate date = message.sentAt().atZone(MockData.DISPLAY_ZONE).toLocalDate();
            if (!date.equals(previous)) {
                Label marker = new Label(date.equals(TODAY) ? "Hôm nay" : DATE.format(date));
                marker.getStyleClass().add("chat-caption");
                messageRows.getChildren().add(marker);
                previous = date;
            }
            var actions = new ChatMessageRow.Actions(
                    chat.canRetry(message) ? () -> retry(message) : null,
                    () -> {
                        if (selecting) {
                            toggleMessage(message.id());
                        } else {
                            enterSelection(message);
                        }
                    },
                    () -> confirmDelete(Set.of(message.id())),
                    chat.canRecall(message) ? () -> confirmRecall(message) : null);
            var row = new ChatMessageRow(message, flow.data().user(message.senderId()).fullName(),
                    access == Access.MISSING_KEY, selecting, selectedIds.contains(message.id()), actions);
            if (message.id().equals(highlightedMessageId) && message.state() != State.RECALLED) {
                row.setId("search-highlighted-message");
                row.getStyleClass().add("chat-search-highlight");
            }
            messageRows.getChildren().add(row);
        }
        Platform.runLater(() -> {
            if (version == renderVersion && navigator != null && historyScroll.getScene() != null) {
                restorePosition(position);
            }
        });
    }

    private void addAccessNotice(Access access) {
        String text = switch (access) {
            case BLOCKED_BY_SELF -> "Bạn đã chặn người này. Bạn không thể gửi tin nhắn.";
            case BLOCKED_BY_PEER -> "Bạn không thể gửi tin nhắn trong đoạn chat này.";
            case MISSING_KEY -> "Thiết bị chưa có khóa hợp lệ để giải mã lịch sử nhóm. Bản nháp được giữ.";
            case GROUP_LEFT, GROUP_REMOVED -> groupHistoryNotice(access);
            case AVAILABLE -> throw new IllegalArgumentException("Available conversations need no notice");
        };
        addWarning(text);
        if (access == Access.MISSING_KEY) {
            Button setup = new Button("Thiết lập / Khôi phục khóa");
            setup.getStyleClass().add("chat-primary");
            setup.setDisable(true);
            historyNotice.getChildren().add(setup);
        }
    }

    private static String groupHistoryNotice(Access access) {
        return access == Access.GROUP_LEFT ? "Bạn chỉ có thể xem lịch sử đã có trước khi rời nhóm."
                : "Bạn chỉ có thể xem những tin nhắn đã từng đọc trước khi bị xóa khỏi nhóm.";
    }

    private void addWarning(String text) {
        Label notice = new Label(text);
        notice.setWrapText(true);
        notice.getStyleClass().add("chat-warning-text");
        HBox row = new HBox(8, new ChatIcon("warning"), notice);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("chat-warning");
        historyNotice.getChildren().add(row);
    }

    private ScrollTarget currentPosition(boolean followBottom) {
        double height = scrollableHeight();
        if (followBottom && (height <= 0 || historyScroll.getVvalue() > 0.995)) {
            return ScrollTarget.bottom();
        }
        var viewport = historyScroll.lookup(".viewport");
        double offset = viewport == null ? historyScroll.getVvalue() * height
                : viewport.localToScene(0, 0).getY() - messageRows.localToScene(0, 0).getY();
        offset = Math.clamp(offset, 0, height);
        for (var node : messageRows.getChildren()) {
            if (node instanceof ChatMessageRow row && row.getBoundsInParent().getMaxY() > offset) {
                return new ScrollTarget(false, row.messageId(), offset - row.getBoundsInParent().getMinY(), offset);
            }
        }
        return new ScrollTarget(false, null, 0, offset);
    }

    private void restorePosition(ScrollTarget position) {
        var root = historyScroll.getScene().getRoot();
        root.applyCss();
        root.layout();
        double height = scrollableHeight();
        if (position.atBottom()) {
            historyScroll.setVvalue(height > 0 ? 1 : 0);
            root.layout();
            return;
        }
        double offset = position.fallback();
        for (var node : messageRows.getChildren()) {
            if (node instanceof ChatMessageRow row && row.messageId().equals(position.messageId())) {
                offset = row.getBoundsInParent().getMinY() + position.inset();
                break;
            }
        }
        historyScroll.setVvalue(height > 0 ? Math.clamp(offset / height, 0, 1) : 0);
        root.layout();
    }

    private double scrollableHeight() {
        return Math.max(0, messageRows.getLayoutBounds().getHeight() - historyScroll.getViewportBounds().getHeight());
    }

    private record ScrollTarget(boolean atBottom, String messageId, double inset, double fallback) {
        private static ScrollTarget bottom() {
            return new ScrollTarget(true, null, 0, 0);
        }
    }
}
