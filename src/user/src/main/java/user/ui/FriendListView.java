package user.ui;

import common.mock.MockData;
import common.model.User;
import common.ui.component.AvatarLabel;
import common.ui.component.ConfirmDialog;
import common.ui.component.StatusBadge;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.Consumer;

/**
 * Screen #5: Friend List (Roadmap: U-06, U-09, U-10).
 * Features:
 * - Real-time filtering by full name or username.
 * - Live online/offline status display.
 * - Actions: Chat, Unfriend (U-09), Block (U-10).
 */
public class FriendListView extends BorderPane {

    private final ObservableList<User> masterFriends = FXCollections.observableArrayList();
    private final FilteredList<User> filteredFriends;
    private final ListView<User> listView = new ListView<>();
    private final Label countBadge = new Label();
    private final Consumer<User> onStartChat;

    public FriendListView(Consumer<User> onStartChat) {
        this.onStartChat = onStartChat;
        getStyleClass().add("content-area");
        setPadding(new Insets(24, 28, 24, 28));

        // Load friends from MockData
        masterFriends.setAll(MockData.getCurrentUserFriends());
        filteredFriends = new FilteredList<>(masterFriends, p -> true);

        // Header Section
        VBox header = createHeader();
        setTop(header);

        // Center List Section
        buildListView();
        VBox centerBox = new VBox(listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        setCenter(centerBox);

        updateCount();
    }

    private VBox createHeader() {
        Label title = new Label("Friends");
        title.getStyleClass().add("section-title");

        countBadge.getStyleClass().add("badge-active");

        HBox titleRow = new HBox(12, title, countBadge);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Manage your connections, check their status, or start a conversation.");
        subtitle.getStyleClass().add("login-subtitle");

        TextField searchField = new TextField();
        searchField.setPromptText("Filter friends by name or @username...");
        searchField.getStyleClass().add("search-field");
        searchField.setMaxWidth(380);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = (newVal == null) ? "" : newVal.trim().toLowerCase();
            filteredFriends.setPredicate(user -> {
                if (query.isEmpty()) return true;
                return user.getFullName().toLowerCase().contains(query)
                        || user.getUsername().toLowerCase().contains(query);
            });
            updateCount();
        });

        VBox header = new VBox(8, titleRow, subtitle, searchField);
        header.setPadding(new Insets(0, 0, 16, 0));
        return header;
    }

    private void buildListView() {
        listView.getStyleClass().add("list-view-clean");
        listView.setItems(filteredFriends);
        listView.setPlaceholder(new Label("No friends found matching your search."));

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // Left: Avatar with indicator
                AvatarLabel avatar = new AvatarLabel(user);

                Region dot = new Region();
                dot.getStyleClass().addAll("status-dot", user.isOnline() ? "status-dot-online" : "status-dot-offline");
                StackPane avatarContainer = new StackPane(avatar, dot);
                StackPane.setAlignment(dot, Pos.BOTTOM_RIGHT);

                // Middle: User Info
                Label nameLbl = new Label(user.getFullName());
                nameLbl.getStyleClass().add("text-name");

                Label usernameLbl = new Label("@" + user.getUsername());
                usernameLbl.getStyleClass().add("text-muted");

                HBox nameRow = new HBox(8, nameLbl, usernameLbl, StatusBadge.online(user.isOnline()));
                nameRow.setAlignment(Pos.CENTER_LEFT);

                Label emailLbl = new Label(user.getEmail() + " • " + user.getAddress());
                emailLbl.getStyleClass().add("text-small");
                emailLbl.setStyle("-fx-text-fill: #8a8a95;");

                VBox infoBox = new VBox(4, nameRow, emailLbl);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                // Right: Action Buttons
                Button chatBtn = new Button("Chat");
                chatBtn.getStyleClass().add("btn-accent");
                chatBtn.setOnAction(e -> {
                    if (onStartChat != null) onStartChat.accept(user);
                });

                Button unfriendBtn = new Button("Unfriend");
                unfriendBtn.getStyleClass().add("btn-ghost");
                unfriendBtn.setOnAction(e -> handleUnfriend(user));

                Button blockBtn = new Button("Block");
                blockBtn.getStyleClass().add("btn-warn");
                blockBtn.setOnAction(e -> handleBlock(user));

                HBox actions = new HBox(8, chatBtn, unfriendBtn, blockBtn);
                actions.setAlignment(Pos.CENTER_RIGHT);

                // Full Row Card
                HBox row = new HBox(16, avatarContainer, infoBox, actions);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("card");
                row.setPadding(new Insets(12, 16, 12, 16));
                HBox.setHgrow(row, Priority.ALWAYS);

                setGraphic(row);
                setText(null);
            }
        });
    }

    private void handleUnfriend(User user) {
        boolean confirmed = ConfirmDialog.confirm(
                "Unfriend",
                "Are you sure you want to remove " + user.getFullName() + " from your friends?"
        );
        if (confirmed) {
            MockData.unfriend(user.getId());
            refreshList();
        }
    }

    private void handleBlock(User user) {
        boolean confirmed = ConfirmDialog.confirm(
                "Block User",
                "Are you sure you want to block " + user.getFullName() + "?\n" +
                "They will be removed from your friends and cannot message or search for you."
        );
        if (confirmed) {
            MockData.blockUser(user.getId());
            refreshList();
        }
    }

    public void refreshList() {
        masterFriends.setAll(MockData.getCurrentUserFriends());
        updateCount();
    }

    private void updateCount() {
        countBadge.setText(filteredFriends.size() + " friends");
    }
}

