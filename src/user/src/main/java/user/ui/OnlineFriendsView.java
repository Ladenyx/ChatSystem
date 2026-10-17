package user.ui;

import common.mock.MockData;
import common.model.User;
import common.ui.component.AvatarLabel;
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
 * Screen #7: Online Friends (Roadmap: U-11).
 * Features:
 * - Real-time filtering by friend name or username.
 * - Displays active/online friends.
 * - Actions: Chat, Create Group with this friend.
 */
public class OnlineFriendsView extends BorderPane {

    private final ObservableList<User> masterOnlineFriends = FXCollections.observableArrayList();
    private final FilteredList<User> filteredOnlineFriends;
    private final ListView<User> listView = new ListView<>();
    private final Label countBadge = new Label();
    private final Consumer<User> onStartChat;
    private final Consumer<User> onCreateGroup;

    public OnlineFriendsView(Consumer<User> onStartChat, Consumer<User> onCreateGroup) {
        this.onStartChat = onStartChat;
        this.onCreateGroup = onCreateGroup;
        getStyleClass().add("content-area");
        setPadding(new Insets(24, 28, 24, 28));

        // Load online friends
        masterOnlineFriends.setAll(MockData.getOnlineFriends());
        filteredOnlineFriends = new FilteredList<>(masterOnlineFriends, p -> true);

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
        Label title = new Label("Online Friends");
        title.getStyleClass().add("section-title");

        countBadge.getStyleClass().add("badge-active");

        HBox titleRow = new HBox(12, title, countBadge);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Friends who are currently active and available to chat or join a group call.");
        subtitle.getStyleClass().add("login-subtitle");

        TextField searchField = new TextField();
        searchField.setPromptText("Filter active friends by name or @username...");
        searchField.getStyleClass().add("search-field");
        searchField.setMaxWidth(380);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = (newVal == null) ? "" : newVal.trim().toLowerCase();
            filteredOnlineFriends.setPredicate(user -> {
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
        listView.setItems(filteredOnlineFriends);
        listView.setPlaceholder(new Label("No friends are currently online."));

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // Left: Avatar with green active dot
                AvatarLabel avatar = new AvatarLabel(user);
                Region dot = new Region();
                dot.getStyleClass().addAll("status-dot", "status-dot-online");
                StackPane avatarContainer = new StackPane(avatar, dot);
                StackPane.setAlignment(dot, Pos.BOTTOM_RIGHT);

                // Middle: User Info
                Label nameLbl = new Label(user.getFullName());
                nameLbl.getStyleClass().add("text-name");

                Label usernameLbl = new Label("@" + user.getUsername());
                usernameLbl.getStyleClass().add("text-muted");

                HBox nameRow = new HBox(8, nameLbl, usernameLbl, StatusBadge.online(true));
                nameRow.setAlignment(Pos.CENTER_LEFT);

                Label emailLbl = new Label(user.getEmail() + " • " + user.getAddress());
                emailLbl.getStyleClass().add("text-small");
                emailLbl.setStyle("-fx-text-fill: #8a8a95;");

                VBox infoBox = new VBox(4, nameRow, emailLbl);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                // Right: Actions (Chat & Create Group)
                Button chatBtn = new Button("Chat");
                chatBtn.getStyleClass().add("btn-accent");
                chatBtn.setOnAction(e -> {
                    if (onStartChat != null) onStartChat.accept(user);
                });

                Button createGroupBtn = new Button("Create Group");
                createGroupBtn.getStyleClass().add("btn-ghost");
                createGroupBtn.setOnAction(e -> {
                    if (onCreateGroup != null) {
                        onCreateGroup.accept(user);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Create Group");
                        alert.setHeaderText(null);
                        alert.setContentText("Start a group chat with " + user.getFullName() + ".");
                        alert.showAndWait();
                    }
                });

                HBox actions = new HBox(8, chatBtn, createGroupBtn);
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

    public void refreshList() {
        masterOnlineFriends.setAll(MockData.getOnlineFriends());
        updateCount();
    }

    private void updateCount() {
        countBadge.setText(filteredOnlineFriends.size() + " active now");
    }
}

