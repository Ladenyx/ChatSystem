package user.ui;

import common.mock.MockData;
import common.model.ChatGroup;
import common.model.Conversation;
import common.model.Message;
import common.model.User;
import common.ui.component.AvatarLabel;
import common.ui.component.NavSidebar;
import common.ui.component.StatusBadge;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

/**
 * Main window for the User Client.
 * Hosts the NavSidebar and dynamically switches content between:
 * - Chats (Main messaging view with Screen #9 ChatAreaView)
 * - Friends (Screen #5, U-06, U-09, U-10)
 * - Online (Screen #7, U-11)
 * - Requests (Screen #6, U-08)
 * - Find people (Screen #8, U-07)
 * - Groups (Screen #14, #15, U-19 to U-25)
 * - Search history (Screen #10, U-18)
 * - Profile (Screen #12, #13, U-03, U-04)
 */
public class MainWindowView extends BorderPane {

    private final BorderPane contentFrame = new BorderPane();
    private final NavSidebar sidebar;
    private final ObservableList<Conversation> conversationsList = FXCollections.observableArrayList();
    private final ListView<Conversation> conversationListView = new ListView<>();
    private final StackPane chatAreaContainer = new StackPane();
    private final Node welcomePane;
    private final Node chatsView;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public MainWindowView(Runnable onLogout) {
        getStyleClass().add("main-bg");
        contentFrame.getStyleClass().add("content-area");

        // Build navigation rail with all core user tabs
        sidebar = new NavSidebar(this::switchTab, "Chats", "Friends", "Online", "Requests", "Find people", "Groups", "Search");
        sidebar.addBottomAction("Profile", () -> switchTab("Profile"));
        sidebar.addBottomAction("Log out", onLogout);
        contentFrame.setLeft(sidebar);

        // Pre-build default Chats View
        welcomePane = buildWelcomePane();
        chatsView = buildChatsView();

        // Default tab: Chats
        sidebar.select("Chats");
        contentFrame.setCenter(chatsView);

        setCenter(contentFrame);
    }

    private Node buildChatsView() {
        VBox conversations = new VBox(12);
        conversations.setPadding(new Insets(20, 14, 16, 14));
        conversations.setPrefWidth(300);
        conversations.setStyle("-fx-border-color: #ebebfc; -fx-border-width: 0 1 0 0;");

        Label heading = new Label("Messages");
        heading.getStyleClass().add("section-title");

        TextField search = new TextField();
        search.setPromptText("Search conversations...");
        search.getStyleClass().add("search-field");

        conversationListView.getStyleClass().add("list-view-clean");
        conversationsList.setAll(MockData.CONVERSATIONS);
        FilteredList<Conversation> filtered = new FilteredList<>(conversationsList, p -> true);
        conversationListView.setItems(filtered);

        search.textProperty().addListener((obs, oldText, newText) -> {
            String query = newText == null ? "" : newText.trim().toLowerCase();
            filtered.setPredicate(conversation -> conversation.getOtherUser() != null
                    && (conversation.getOtherUser().getFullName().toLowerCase().contains(query)
                    || conversation.getOtherUser().getUsername().toLowerCase().contains(query)));
        });

        conversationListView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation conversation, boolean empty) {
                super.updateItem(conversation, empty);
                if (empty || conversation == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                User contact = conversation.getOtherUser();

                // Top: Name + Time
                Label name = new Label(contact == null ? "Conversation" : contact.getFullName());
                name.getStyleClass().add("text-name");

                String timeText = "";
                if (conversation.getLastMessage() != null && conversation.getLastMessage().getSentAt() != null) {
                    timeText = conversation.getLastMessage().getSentAt().format(TIME_FORMATTER);
                }
                Label timeLbl = new Label(timeText);
                timeLbl.getStyleClass().add("text-muted");
                timeLbl.setStyle("-fx-font-size: 10.5px;");

                Region nameSpacer = new Region();
                HBox.setHgrow(nameSpacer, Priority.ALWAYS);

                HBox topRow = new HBox(6, name, nameSpacer, timeLbl);
                topRow.setAlignment(Pos.CENTER_LEFT);

                // Bottom: Preview + Unread Badge
                String preview = conversation.getLastMessage() == null ? "No messages yet" : conversation.getLastMessage().getContent();
                Label message = new Label(preview);
                message.getStyleClass().add("text-muted");
                message.setMaxWidth(160);
                message.setWrapText(false);

                HBox bottomRow = new HBox(6, message);
                bottomRow.setAlignment(Pos.CENTER_LEFT);

                if (conversation.getUnreadCount() > 0) {
                    Region unreadSpacer = new Region();
                    HBox.setHgrow(unreadSpacer, Priority.ALWAYS);
                    Label badge = new Label(String.valueOf(conversation.getUnreadCount()));
                    badge.getStyleClass().add("badge-unread");
                    bottomRow.getChildren().addAll(unreadSpacer, badge);
                }

                VBox details = new VBox(3, topRow, bottomRow);
                HBox.setHgrow(details, Priority.ALWAYS);

                AvatarLabel avatar = new AvatarLabel(contact);
                HBox row = new HBox(10, avatar, details);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 8, 6, 8));
                row.getStyleClass().add("conv-item");

                setGraphic(row);
                setText(null);
            }
        });

        // Listen for conversation selection to load ChatAreaView
        conversationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldConv, newConv) -> {
            if (newConv != null) {
                newConv.setUnreadCount(0);
                conversationListView.refresh();
                ChatAreaView chatArea = new ChatAreaView(newConv, this::refreshConversationsList);
                chatAreaContainer.getChildren().setAll(chatArea);
            } else {
                chatAreaContainer.getChildren().setAll(welcomePane);
            }
        });

        VBox.setVgrow(conversationListView, Priority.ALWAYS);
        conversations.getChildren().addAll(heading, search, conversationListView);

        // Right side of Chats: ChatAreaContainer
        chatAreaContainer.getChildren().setAll(welcomePane);
        HBox.setHgrow(chatAreaContainer, Priority.ALWAYS);

        // Pre-select first conversation if available
        if (!conversationsList.isEmpty()) {
            conversationListView.getSelectionModel().select(0);
        }

        HBox split = new HBox(conversations, chatAreaContainer);
        HBox.setHgrow(chatAreaContainer, Priority.ALWAYS);
        return split;
    }

    private Node buildWelcomePane() {
        VBox welcome = new VBox(14);
        welcome.setAlignment(Pos.CENTER);
        welcome.setPadding(new Insets(32));

        Label logo = new AvatarLabel(MockData.CURRENT_USER);
        Label title = new Label("Welcome back, " + MockData.CURRENT_USER.getFullName());
        title.getStyleClass().add("section-title");

        Label hint = new Label("Select a conversation from the left to start messaging, or pick a friend.");
        hint.getStyleClass().add("login-subtitle");

        StatusBadge status = StatusBadge.online(MockData.CURRENT_USER.isOnline());
        welcome.getChildren().addAll(logo, title, hint, status);
        return welcome;
    }

    private void switchTab(String tabName) {
        if (tabName == null) return;
        switch (tabName) {
            case "Chats" -> contentFrame.setCenter(chatsView);
            case "Friends" -> contentFrame.setCenter(new FriendListView(this::openChatWithUser));
            case "Online" -> contentFrame.setCenter(new OnlineFriendsView(this::openChatWithUser, this::openCreateGroupWithUser));
            case "Requests" -> contentFrame.setCenter(new FriendRequestsView(this::refreshConversationsList));
            case "Find people" -> contentFrame.setCenter(new FindPeopleView(this::openChatWithUser));
            case "Groups" -> contentFrame.setCenter(buildGroupsOverview());
            case "Search" -> contentFrame.setCenter(new SearchAllChatView(this::handleJumpToSearchResult));
            case "Profile" -> AccountInfoDialog.showDialog();
        }
    }

    private Node buildGroupsOverview() {
        BorderPane groupPane = new BorderPane();
        groupPane.getStyleClass().add("content-area");
        groupPane.setPadding(new Insets(24, 28, 24, 28));

        Label title = new Label("Chat Groups");
        title.getStyleClass().add("section-title");

        Button createGroupBtn = new Button("➕ Create Group");
        createGroupBtn.getStyleClass().add("btn-accent");
        createGroupBtn.setOnAction(e -> CreateGroupDialog.showDialog(null).ifPresent(this::openGroupChat));

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        HBox titleRow = new HBox(12, title, titleSpacer, createGroupBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Encrypted group chats with your teams, classmates, and project groups.");
        subtitle.getStyleClass().add("login-subtitle");

        VBox header = new VBox(8, titleRow, subtitle);
        header.setPadding(new Insets(0, 0, 16, 0));
        groupPane.setTop(header);

        ListView<ChatGroup> groupListView = new ListView<>();
        groupListView.getStyleClass().add("list-view-clean");
        groupListView.setItems(FXCollections.observableArrayList(MockData.GROUPS));
        groupListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ChatGroup group, boolean empty) {
                super.updateItem(group, empty);
                if (empty || group == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label avatar = new Label(group.getInitials());
                avatar.getStyleClass().addAll("avatar", "avatar-group");
                avatar.setMinSize(40, 40);
                avatar.setAlignment(Pos.CENTER);

                Label nameLbl = new Label(group.getName());
                nameLbl.getStyleClass().add("text-name");

                Label countLbl = new Label(group.getMembers().size() + " members" + (group.isEncrypted() ? " • 🔒 Encrypted" : ""));
                countLbl.getStyleClass().add("text-muted");

                VBox info = new VBox(4, nameLbl, countLbl);
                HBox.setHgrow(info, Priority.ALWAYS);

                Button manageBtn = new Button("⚙️ Info");
                manageBtn.getStyleClass().add("btn-ghost");
                manageBtn.setOnAction(e -> ManageGroupDialog.showDialog(group, groupListView::refresh));

                Button openBtn = new Button("Open Chat");
                openBtn.getStyleClass().add("btn-accent");
                openBtn.setOnAction(e -> openGroupChat(group));

                HBox actions = new HBox(8, manageBtn, openBtn);
                actions.setAlignment(Pos.CENTER_RIGHT);

                HBox card = new HBox(16, avatar, info, actions);
                card.setAlignment(Pos.CENTER_LEFT);
                card.getStyleClass().add("card");
                card.setPadding(new Insets(12, 16, 12, 16));
                setGraphic(card);
                setText(null);
            }
        });

        groupPane.setCenter(groupListView);
        return groupPane;
    }

    public void openChatWithUser(User user) {
        sidebar.select("Chats");
        switchTab("Chats");

        // Select or create conversation in list
        for (Conversation c : conversationsList) {
            if (c.getOtherUser() != null && c.getOtherUser().getId() == user.getId()) {
                conversationListView.getSelectionModel().select(c);
                return;
            }
        }

        // If not already in conversation list, add new conversation at top
        Conversation newConv = new Conversation(conversationsList.size() + 1, user, java.time.LocalDateTime.now());
        conversationsList.add(0, newConv);
        conversationListView.getSelectionModel().select(newConv);
    }

    public void openGroupChat(ChatGroup group) {
        sidebar.select("Chats");
        switchTab("Chats");
        conversationListView.getSelectionModel().clearSelection();
        ChatAreaView chatArea = new ChatAreaView(group, this::refreshConversationsList);
        chatAreaContainer.getChildren().setAll(chatArea);
    }

    private void openCreateGroupWithUser(User user) {
        CreateGroupDialog.showDialog(user).ifPresent(this::openGroupChat);
    }

    private void handleJumpToSearchResult(Conversation conv, ChatGroup group, Message msg) {
        sidebar.select("Chats");
        switchTab("Chats");

        if (conv != null) {
            for (Conversation c : conversationsList) {
                if (c.getId() == conv.getId()) {
                    conversationListView.getSelectionModel().select(c);
                    Platform.runLater(() -> {
                        if (!chatAreaContainer.getChildren().isEmpty() &&
                                chatAreaContainer.getChildren().get(0) instanceof ChatAreaView cav) {
                            cav.highlightAndScrollToMessage(msg);
                        }
                    });
                    return;
                }
            }
        } else if (group != null) {
            openGroupChat(group);
            Platform.runLater(() -> {
                if (!chatAreaContainer.getChildren().isEmpty() &&
                        chatAreaContainer.getChildren().get(0) instanceof ChatAreaView cav) {
                    cav.highlightAndScrollToMessage(msg);
                }
            });
        }
    }

    public void refreshConversationsList() {
        conversationListView.refresh();
    }
}
