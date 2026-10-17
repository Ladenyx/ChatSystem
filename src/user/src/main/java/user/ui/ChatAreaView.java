package user.ui;

import common.mock.MockData;
import common.model.ChatGroup;
import common.model.Conversation;
import common.model.Message;
import common.model.User;
import common.ui.component.AvatarLabel;
import common.ui.component.ConfirmDialog;
import common.ui.component.StatusBadge;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen #9: Complete Chat Window Area (Roadmap: U-12 to U-17, U-24, U-25, U-26, U-27).
 * Features:
 * - Real-time message bubbles with sent/received alignment and status ticks.
 * - Group chats with sender names and encryption indicator (U-24, U-25).
 * - Multi-message selection and deletion (U-16).
 * - Delete full chat history with confirmation (U-15).
 * - In-conversation text search with jump-to-match scrolling (U-17).
 * - Spam report action on user and message (U-26).
 * - Integrated AI writing assistant sidebar with draft refinement & reply suggestions (U-27).
 */
public class ChatAreaView extends BorderPane {

    private Conversation currentConversation;
    private ChatGroup currentGroup;
    private final boolean isGroupMode;

    private final VBox messageBox = new VBox(6);
    private final ScrollPane scrollPane = new ScrollPane(messageBox);
    private final TextField inputField = new TextField();
    private final List<MessageBubbleView> bubbleViews = new ArrayList<>();

    // In-chat search (U-17)
    private final InChatSearchBar inChatSearchBar = new InChatSearchBar();
    private final List<Integer> searchMatchIndices = new ArrayList<>();
    private int currentSearchMatchIndex = -1;

    // Multi-message delete toolbar (U-16)
    private final HBox selectionBar = new HBox(12);
    private final Label selectionCountLabel = new Label("0 selected");
    private boolean isSelectionMode = false;

    // AI Assistant Side Panel (U-27)
    private final AiAssistantPanel aiPanel;
    private boolean isAiPanelOpen = false;

    // Header labels
    private final Label chatTitleLabel = new Label();
    private final Label chatSubtitleLabel = new Label();
    private final Label lockBadge = new Label("🔒 Encrypted");
    private final HBox titleRow = new HBox(8);
    private final StackPane headerAvatarContainer = new StackPane();

    private final Runnable onConversationUpdated;

    public ChatAreaView(Conversation conversation, Runnable onConversationUpdated) {
        this.currentConversation = conversation;
        this.currentGroup = null;
        this.isGroupMode = false;
        this.onConversationUpdated = onConversationUpdated;

        aiPanel = new AiAssistantPanel(
                this::getCurrentDraft,
                this::getLastReceivedMessageContent,
                this::applyAiSuggestion,
                this::toggleAiPanel
        );

        setupUI();
        loadConversation(conversation);
    }

    public ChatAreaView(ChatGroup group, Runnable onConversationUpdated) {
        this.currentConversation = null;
        this.currentGroup = group;
        this.isGroupMode = true;
        this.onConversationUpdated = onConversationUpdated;

        aiPanel = new AiAssistantPanel(
                this::getCurrentDraft,
                this::getLastReceivedMessageContent,
                this::applyAiSuggestion,
                this::toggleAiPanel
        );

        setupUI();
        loadGroup(group);
    }

    private void setupUI() {
        getStyleClass().add("content-area");

        // Top Header and Search Toolbar
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(buildHeader(), inChatSearchBar, buildSelectionBar());
        inChatSearchBar.setVisible(false);
        inChatSearchBar.setManaged(false);
        selectionBar.setVisible(false);
        selectionBar.setManaged(false);
        setTop(topContainer);

        // Center: Scrollable Message List
        messageBox.setPadding(new Insets(16, 12, 16, 12));
        messageBox.setFillWidth(true);

        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane-clean");
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        setCenter(scrollPane);

        // Bottom: Chat Input Bar
        setBottom(buildInputBar());

        // Setup In-chat Search Handlers (U-17)
        setupSearchHandlers();
    }

    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setStyle("-fx-background-color: white; -fx-border-color: #ebebfc; -fx-border-width: 0 0 1 0;");

        chatTitleLabel.getStyleClass().add("text-name");
        chatTitleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 700;");

        lockBadge.getStyleClass().add("badge-encrypted");
        lockBadge.setVisible(false);

        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getChildren().addAll(chatTitleLabel, lockBadge);

        chatSubtitleLabel.getStyleClass().add("text-muted");

        VBox titleDetails = new VBox(2, titleRow, chatSubtitleLabel);
        HBox.setHgrow(titleDetails, Priority.ALWAYS);

        // Toolbar Buttons
        Button searchBtn = new Button("🔍 Find");
        searchBtn.getStyleClass().add("btn-ghost");
        searchBtn.setTooltip(new Tooltip("Search text in this chat (U-17)"));
        searchBtn.setOnAction(e -> toggleSearchBar());

        MenuButton actionsMenu = new MenuButton("⋯ Actions");
        actionsMenu.getStyleClass().add("btn-ghost");

        MenuItem selectDeleteMsgItem = new MenuItem("🗑️ Select Messages to Delete");
        selectDeleteMsgItem.setOnAction(e -> enterSelectionMode());

        MenuItem clearHistoryItem = new MenuItem("⚠️ Clear Entire History");
        clearHistoryItem.setOnAction(e -> handleClearHistory());

        MenuItem reportSpamItem = new MenuItem("🚩 Report Spam");
        reportSpamItem.setOnAction(e -> handleReportSpam(null));

        actionsMenu.getItems().addAll(selectDeleteMsgItem, clearHistoryItem, new SeparatorMenuItem(), reportSpamItem);

        Button manageGroupBtn = new Button("⚙️ Group Info");
        manageGroupBtn.getStyleClass().add("btn-ghost");
        manageGroupBtn.setTooltip(new Tooltip("Manage members, rename, and security (U-20 to U-23, U-25)"));
        manageGroupBtn.setVisible(isGroupMode);
        manageGroupBtn.setManaged(isGroupMode);
        manageGroupBtn.setOnAction(e -> {
            if (currentGroup != null) {
                ManageGroupDialog.showDialog(currentGroup, () -> {
                    loadGroup(currentGroup);
                    if (onConversationUpdated != null) onConversationUpdated.run();
                });
            }
        });

        Button aiBtn = new Button("✨ AI Assistant");
        aiBtn.getStyleClass().add("btn-accent");
        aiBtn.setTooltip(new Tooltip("Refine draft or suggest replies (U-27)"));
        aiBtn.setOnAction(e -> toggleAiPanel());

        HBox toolbar = new HBox(8, searchBtn, manageGroupBtn, actionsMenu, aiBtn);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(headerAvatarContainer, titleDetails, toolbar);
        return header;
    }

    private HBox buildSelectionBar() {
        selectionBar.setAlignment(Pos.CENTER_LEFT);
        selectionBar.setPadding(new Insets(8, 20, 8, 20));
        selectionBar.setStyle("-fx-background-color: #fff3e0; -fx-border-color: #ffe0b2; -fx-border-width: 0 0 1 0;");

        selectionCountLabel.getStyleClass().add("text-bold");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteSelectedBtn = new Button("Delete Selected");
        deleteSelectedBtn.getStyleClass().add("btn-warn");
        deleteSelectedBtn.setOnAction(e -> handleDeleteSelectedMessages());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-ghost");
        cancelBtn.setOnAction(e -> exitSelectionMode());

        selectionBar.getChildren().addAll(selectionCountLabel, spacer, deleteSelectedBtn, cancelBtn);
        return selectionBar;
    }

    private HBox buildInputBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(12, 20, 14, 20));
        bar.setStyle("-fx-background-color: white; -fx-border-color: #ebebfc; -fx-border-width: 1 0 0 0;");

        inputField.setPromptText("Type your message here... (Press Enter to send)");
        inputField.getStyleClass().add("chat-input");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setOnAction(e -> handleSendMessage());

        Button sendBtn = new Button("➤");
        sendBtn.getStyleClass().add("btn-send");
        sendBtn.setTooltip(new Tooltip("Send message"));
        sendBtn.setOnAction(e -> handleSendMessage());

        bar.getChildren().addAll(inputField, sendBtn);
        return bar;
    }

    // ── SEARCH IN CONVERSATION (U-17) ──────────────────────────────────────────

    private void setupSearchHandlers() {
        inChatSearchBar.setOnQueryChanged(this::performSearch);
        inChatSearchBar.setOnNext(this::nextSearchMatch);
        inChatSearchBar.setOnPrev(this::prevSearchMatch);
        inChatSearchBar.setOnClose(this::closeSearchBar);
    }

    private void toggleSearchBar() {
        boolean show = !inChatSearchBar.isVisible();
        inChatSearchBar.setVisible(show);
        inChatSearchBar.setManaged(show);
        if (show) {
            inChatSearchBar.focusInput();
        } else {
            closeSearchBar();
        }
    }

    private void closeSearchBar() {
        inChatSearchBar.setVisible(false);
        inChatSearchBar.setManaged(false);
        inChatSearchBar.reset();
        clearHighlights();
    }

    private void performSearch(String query) {
        clearHighlights();
        searchMatchIndices.clear();
        currentSearchMatchIndex = -1;

        if (query == null || query.isBlank()) {
            inChatSearchBar.updateMatchCount(0, 0);
            return;
        }

        String lowerQuery = query.toLowerCase();
        for (int i = 0; i < bubbleViews.size(); i++) {
            MessageBubbleView bubble = bubbleViews.get(i);
            if (bubble.getMessage().getContent().toLowerCase().contains(lowerQuery)) {
                searchMatchIndices.add(i);
            }
        }

        if (!searchMatchIndices.isEmpty()) {
            currentSearchMatchIndex = 0;
            jumpToMatch(currentSearchMatchIndex);
        }
        inChatSearchBar.updateMatchCount(currentSearchMatchIndex, searchMatchIndices.size());
    }

    private void nextSearchMatch() {
        if (searchMatchIndices.isEmpty()) return;
        currentSearchMatchIndex = (currentSearchMatchIndex + 1) % searchMatchIndices.size();
        jumpToMatch(currentSearchMatchIndex);
        inChatSearchBar.updateMatchCount(currentSearchMatchIndex, searchMatchIndices.size());
    }

    private void prevSearchMatch() {
        if (searchMatchIndices.isEmpty()) return;
        currentSearchMatchIndex = (currentSearchMatchIndex - 1 + searchMatchIndices.size()) % searchMatchIndices.size();
        jumpToMatch(currentSearchMatchIndex);
        inChatSearchBar.updateMatchCount(currentSearchMatchIndex, searchMatchIndices.size());
    }

    private void jumpToMatch(int matchIdx) {
        clearHighlights();
        if (matchIdx >= 0 && matchIdx < searchMatchIndices.size()) {
            int bubbleIdx = searchMatchIndices.get(matchIdx);
            MessageBubbleView bubble = bubbleViews.get(bubbleIdx);
            bubble.setHighlighted(true);

            // Scroll to the bubble position
            Platform.runLater(() -> {
                double totalHeight = messageBox.getBoundsInLocal().getHeight();
                if (totalHeight > 0) {
                    double nodeY = bubble.getBoundsInParent().getMinY();
                    scrollPane.setVvalue(Math.min(1.0, Math.max(0.0, nodeY / totalHeight)));
                }
            });
        }
    }

    private void clearHighlights() {
        for (MessageBubbleView bubble : bubbleViews) {
            bubble.setHighlighted(false);
        }
    }

    // ── DATA LOADING ─────────────────────────────────────────────────────────

    public void loadConversation(Conversation conversation) {
        this.currentConversation = conversation;
        this.currentGroup = null;

        User other = conversation.getOtherUser();
        chatTitleLabel.setText(other != null ? other.getFullName() : "Direct Message");
        lockBadge.setVisible(false);
        chatSubtitleLabel.setText(other != null && other.isOnline() ? "Active now" : "Offline");

        headerAvatarContainer.getChildren().clear();
        if (other != null) {
            headerAvatarContainer.getChildren().add(new AvatarLabel(other));
        }

        refreshMessagesList(conversation.getMessages(), false);
    }

    public void loadGroup(ChatGroup group) {
        this.currentGroup = group;
        this.currentConversation = null;

        chatTitleLabel.setText(group.getName());
        lockBadge.setVisible(group.isEncrypted());
        chatSubtitleLabel.setText(group.getMembers().size() + " members" + (group.isEncrypted() ? " • End-to-end encrypted" : ""));

        headerAvatarContainer.getChildren().clear();
        Label groupAvatar = new Label(group.getInitials());
        groupAvatar.getStyleClass().addAll("avatar", "avatar-group");
        groupAvatar.setMinSize(40, 40);
        groupAvatar.setAlignment(Pos.CENTER);
        headerAvatarContainer.getChildren().add(groupAvatar);

        refreshMessagesList(group.getMessages(), true);
    }

    private void refreshMessagesList(List<Message> messages, boolean isGroup) {
        messageBox.getChildren().clear();
        bubbleViews.clear();

        for (Message msg : messages) {
            boolean isMine = msg.getSenderId() == MockData.CURRENT_USER.getId();
            MessageBubbleView bubble = new MessageBubbleView(
                    msg, isMine, isGroup,
                    this::handleDeleteSingleMessage,
                    this::handleReportSpam
            );
            bubble.selectedProperty().addListener((obs, oldV, newV) -> updateSelectionCount());
            bubbleViews.add(bubble);
            messageBox.getChildren().add(bubble);
        }

        scrollToBottom();
    }

    // ── SEND MESSAGE (U-12, U-24) ─────────────────────────────────────────────

    private void handleSendMessage() {
        String text = inputField.getText();
        if (text == null || text.isBlank()) return;

        User me = MockData.CURRENT_USER;
        Message newMsg;

        if (isGroupMode && currentGroup != null) {
            newMsg = new Message(
                    (int) (System.currentTimeMillis() % 100000),
                    me.getId(), me.getFullName(),
                    null, currentGroup.getId(),
                    text.trim(),
                    Message.Type.GROUP,
                    Message.Status.SENT,
                    LocalDateTime.now()
            );
            currentGroup.getMessages().add(newMsg);
        } else if (currentConversation != null) {
            newMsg = new Message(
                    (int) (System.currentTimeMillis() % 100000),
                    me.getId(), me.getFullName(),
                    currentConversation.getOtherUser().getId(), null,
                    text.trim(),
                    Message.Type.DIRECT,
                    Message.Status.DELIVERED,
                    LocalDateTime.now()
            );
            currentConversation.getMessages().add(newMsg);
            currentConversation.setLastMessage(newMsg);
            currentConversation.setUpdatedAt(LocalDateTime.now());
        } else {
            return;
        }

        inputField.clear();

        // Add bubble directly to view
        MessageBubbleView bubble = new MessageBubbleView(
                newMsg, true, isGroupMode,
                this::handleDeleteSingleMessage,
                this::handleReportSpam
        );
        bubble.selectedProperty().addListener((obs, oldV, newV) -> updateSelectionCount());
        bubbleViews.add(bubble);
        messageBox.getChildren().add(bubble);

        scrollToBottom();

        if (onConversationUpdated != null) {
            onConversationUpdated.run();
        }
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    // ── DELETION & MODERATION (U-15, U-16, U-26) ──────────────────────────────

    private void enterSelectionMode() {
        isSelectionMode = true;
        selectionBar.setVisible(true);
        selectionBar.setManaged(true);
        for (MessageBubbleView bubble : bubbleViews) {
            bubble.setSelectionMode(true);
        }
        updateSelectionCount();
    }

    private void exitSelectionMode() {
        isSelectionMode = false;
        selectionBar.setVisible(false);
        selectionBar.setManaged(false);
        for (MessageBubbleView bubble : bubbleViews) {
            bubble.setSelectionMode(false);
        }
    }

    private void updateSelectionCount() {
        long count = bubbleViews.stream().filter(MessageBubbleView::isSelected).count();
        selectionCountLabel.setText(count + " message" + (count == 1 ? "" : "s") + " selected");
    }

    private void handleDeleteSelectedMessages() {
        List<Message> toDelete = bubbleViews.stream()
                .filter(MessageBubbleView::isSelected)
                .map(MessageBubbleView::getMessage)
                .toList();

        if (toDelete.isEmpty()) return;

        boolean confirm = ConfirmDialog.confirm(
                "Delete Messages",
                "Delete " + toDelete.size() + " selected message(s)? This cannot be undone."
        );
        if (confirm) {
            if (currentConversation != null) {
                currentConversation.getMessages().removeAll(toDelete);
                refreshMessagesList(currentConversation.getMessages(), false);
            } else if (currentGroup != null) {
                currentGroup.getMessages().removeAll(toDelete);
                refreshMessagesList(currentGroup.getMessages(), true);
            }
            exitSelectionMode();
            if (onConversationUpdated != null) onConversationUpdated.run();
        }
    }

    private void handleDeleteSingleMessage(Message message) {
        boolean confirm = ConfirmDialog.confirm(
                "Delete Message",
                "Delete this message? This action removes it from your chat."
        );
        if (confirm) {
            if (currentConversation != null) {
                currentConversation.getMessages().remove(message);
                refreshMessagesList(currentConversation.getMessages(), false);
            } else if (currentGroup != null) {
                currentGroup.getMessages().remove(message);
                refreshMessagesList(currentGroup.getMessages(), true);
            }
            if (onConversationUpdated != null) onConversationUpdated.run();
        }
    }

    private void handleClearHistory() {
        boolean confirm = ConfirmDialog.confirm(
                "Clear History",
                "Are you sure you want to permanently clear the full chat history? All messages will be deleted."
        );
        if (confirm) {
            if (currentConversation != null) {
                currentConversation.getMessages().clear();
                currentConversation.setLastMessage(null);
                refreshMessagesList(currentConversation.getMessages(), false);
            } else if (currentGroup != null) {
                currentGroup.getMessages().clear();
                refreshMessagesList(currentGroup.getMessages(), true);
            }
            if (onConversationUpdated != null) onConversationUpdated.run();
        }
    }

    private void handleReportSpam(Message msgContext) {
        User target = currentConversation != null ? currentConversation.getOtherUser() : MockData.USERS.get(1);
        ReportSpamDialog.show(target, msgContext);
    }

    // ── AI ASSISTANT INTEGRATION (U-27) ───────────────────────────────────────

    private void toggleAiPanel() {
        isAiPanelOpen = !isAiPanelOpen;
        if (isAiPanelOpen) {
            setRight(aiPanel);
        } else {
            setRight(null);
        }
    }

    private String getCurrentDraft() {
        return inputField.getText();
    }

    private String getLastReceivedMessageContent() {
        for (int i = bubbleViews.size() - 1; i >= 0; i--) {
            Message m = bubbleViews.get(i).getMessage();
            if (m.getSenderId() != MockData.CURRENT_USER.getId()) {
                return m.getContent();
            }
        }
        return "";
    }

    private void applyAiSuggestion(String suggestion) {
        if (suggestion != null) {
            inputField.setText(suggestion);
            inputField.requestFocus();
            inputField.positionCaret(suggestion.length());
        }
    }

    /**
     * Highlights a specific message and scrolls it into view (used by SearchAllChatView).
     */
    public void highlightAndScrollToMessage(Message targetMsg) {
        if (targetMsg == null) return;
        clearHighlights();
        for (MessageBubbleView bubble : bubbleViews) {
            if (bubble.getMessage().getId() == targetMsg.getId()) {
                bubble.setHighlighted(true);
                Platform.runLater(() -> {
                    double totalHeight = messageBox.getBoundsInLocal().getHeight();
                    if (totalHeight > 0) {
                        double nodeY = bubble.getBoundsInParent().getMinY();
                        scrollPane.setVvalue(Math.min(1.0, Math.max(0.0, nodeY / totalHeight)));
                    }
                });
                break;
            }
        }
    }
}

