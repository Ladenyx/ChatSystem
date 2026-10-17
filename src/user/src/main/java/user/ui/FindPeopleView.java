package user.ui;

import common.mock.MockData;
import common.model.User;
import common.ui.component.AvatarLabel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.Consumer;

/**
 * Screen #8: Find People (Roadmap: U-07).
 * Features:
 * - Search users by name or username.
 * - Blockers are strictly hidden.
 * - Send friend requests with instant status change.
 * - Direct Chat option for existing friends.
 */
public class FindPeopleView extends BorderPane {

    private final ObservableList<User> masterUsers = FXCollections.observableArrayList();
    private final FilteredList<User> filteredUsers;
    private final ListView<User> listView = new ListView<>();
    private final Label countBadge = new Label();
    private final Consumer<User> onStartChat;

    public FindPeopleView(Consumer<User> onStartChat) {
        this.onStartChat = onStartChat;
        getStyleClass().add("content-area");
        setPadding(new Insets(24, 28, 24, 28));

        // Filter out current user and blocked users
        masterUsers.setAll(MockData.USERS.stream()
                .filter(u -> u.getId() != MockData.CURRENT_USER.getId() && u.getId() != 0) // exclude self and system admin
                .filter(u -> !MockData.isBlocked(u.getId())) // blockers hidden
                .toList());

        filteredUsers = new FilteredList<>(masterUsers, p -> true);

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
        Label title = new Label("Find People");
        title.getStyleClass().add("section-title");

        countBadge.getStyleClass().add("badge-offline");

        HBox titleRow = new HBox(12, title, countBadge);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Discover classmates, colleagues, and send friend requests to start chatting.");
        subtitle.getStyleClass().add("login-subtitle");

        TextField searchField = new TextField();
        searchField.setPromptText("Search by full name or @username...");
        searchField.getStyleClass().add("search-field");
        searchField.setMaxWidth(380);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = (newVal == null) ? "" : newVal.trim().toLowerCase();
            filteredUsers.setPredicate(user -> {
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
        listView.setItems(filteredUsers);
        listView.setPlaceholder(new Label("No users found matching your search."));

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // Left: Avatar
                AvatarLabel avatar = new AvatarLabel(user);

                // Middle: User info
                Label nameLbl = new Label(user.getFullName());
                nameLbl.getStyleClass().add("text-name");

                Label usernameLbl = new Label("@" + user.getUsername());
                usernameLbl.getStyleClass().add("text-muted");

                HBox nameRow = new HBox(8, nameLbl, usernameLbl);
                nameRow.setAlignment(Pos.CENTER_LEFT);

                boolean isFriend = MockData.isFriend(user.getId());
                boolean isPending = MockData.hasOutgoingRequest(user.getId());

                if (isFriend) {
                    Label friendBadge = new Label("Friend");
                    friendBadge.getStyleClass().add("badge-active");
                    nameRow.getChildren().add(friendBadge);
                } else if (isPending) {
                    Label pendingBadge = new Label("Request Sent");
                    pendingBadge.getStyleClass().add("badge-locked");
                    nameRow.getChildren().add(pendingBadge);
                }

                Label infoLbl = new Label(user.getEmail() + " • " + user.getAddress());
                infoLbl.getStyleClass().add("text-small");
                infoLbl.setStyle("-fx-text-fill: #8a8a95;");

                VBox infoBox = new VBox(4, nameRow, infoLbl);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                // Right: Action Buttons
                HBox actions = new HBox(8);
                actions.setAlignment(Pos.CENTER_RIGHT);

                if (isFriend) {
                    Button chatBtn = new Button("Chat");
                    chatBtn.getStyleClass().add("btn-accent");
                    chatBtn.setOnAction(e -> {
                        if (onStartChat != null) onStartChat.accept(user);
                    });
                    actions.getChildren().add(chatBtn);
                } else if (isPending) {
                    Button pendingBtn = new Button("Sent");
                    pendingBtn.getStyleClass().add("btn-ghost");
                    pendingBtn.setDisable(true);
                    actions.getChildren().add(pendingBtn);
                } else {
                    Button addFriendBtn = new Button("Add Friend");
                    addFriendBtn.getStyleClass().add("btn-accent");
                    addFriendBtn.setOnAction(e -> {
                        MockData.sendFriendRequest(user.getId());
                        refreshList();
                    });
                    actions.getChildren().add(addFriendBtn);
                }

                // Full Row Card
                HBox row = new HBox(16, avatar, infoBox, actions);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("card");
                row.setPadding(new Insets(12, 16, 12, 16));
                HBox.setHgrow(row, Priority.ALWAYS);

                setGraphic(row);
                setText(null);
            }
        });
    }

    public void refreshList() {
        masterUsers.setAll(MockData.USERS.stream()
                .filter(u -> u.getId() != MockData.CURRENT_USER.getId() && u.getId() != 0)
                .filter(u -> !MockData.isBlocked(u.getId()))
                .toList());
        updateCount();
    }

    private void updateCount() {
        countBadge.setText(filteredUsers.size() + " users");
    }
}

