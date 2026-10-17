package admin.ui;

import common.mock.MockData;
import common.model.User;
import common.ui.component.AvatarLabel;
import common.ui.component.StatusBadge;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;

/**
 * Screen #6 (A-06): Dialog showing friend list for a specific user.
 */
public class UserFriendListDialog extends Dialog<Void> {

    public UserFriendListDialog(User user) {
        setTitle("Friend List — " + (user != null ? user.getFullName() : "User"));
        setHeaderText(null);

        DialogPane pane = getDialogPane();
        pane.getStyleClass().add("card");
        pane.getStylesheets().add(getClass().getResource("/common/theme.css").toExternalForm());
        pane.getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(14);
        content.setPadding(new Insets(10));
        content.setPrefWidth(600);
        content.setPrefHeight(440);

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        AvatarLabel avatar = new AvatarLabel(user != null ? user.getInitials() : "?", false);
        VBox meta = new VBox(2);
        Label nameLbl = new Label(user != null ? user.getFullName() : "Unknown User");
        nameLbl.getStyleClass().add("section-title");
        Label subLbl = new Label("@" + (user != null ? user.getUsername() : "") + " • Connections Directory");
        subLbl.getStyleClass().add("text-muted");
        meta.getChildren().addAll(nameLbl, subLbl);

        header.getChildren().addAll(avatar, meta);

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Filter friends by name, username or email...");
        searchField.getStyleClass().add("search-field");
        searchField.setMaxWidth(Double.MAX_VALUE);

        // Friends data
        List<User> friends = MockData.getUserFriends(user != null ? user.getId() : 0);
        ObservableList<User> masterData = FXCollections.observableArrayList(friends);
        FilteredList<User> filtered = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal == null ? "" : newVal.trim().toLowerCase();
            filtered.setPredicate(friend -> {
                if (q.isEmpty()) return true;
                return friend.getFullName().toLowerCase().contains(q)
                        || friend.getUsername().toLowerCase().contains(q)
                        || (friend.getEmail() != null && friend.getEmail().toLowerCase().contains(q));
            });
        });

        TableView<User> table = new TableView<>(filtered);
        table.getStyleClass().add("table-view-clean");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No friends found for this user."));
        VBox.setVgrow(table, Priority.ALWAYS);

        // Columns: Friend (Avatar + Name), Username, Email, Online Status, Account Status
        TableColumn<User, User> userCol = new TableColumn<>("Friend");
        userCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        userCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    AvatarLabel av = new AvatarLabel(item.getInitials(), false, true);
                    Label name = new Label(item.getFullName());
                    name.getStyleClass().add("text-semibold");
                    HBox box = new HBox(8, av, name);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });
        userCol.setPrefWidth(180);

        TableColumn<User, String> unameCol = new TableColumn<>("Username");
        unameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper("@" + c.getValue().getUsername()));
        unameCol.setPrefWidth(120);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEmail()));
        emailCol.setPrefWidth(140);

        TableColumn<User, User> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    StatusBadge badge = StatusBadge.onlineStatus(item);
                    setGraphic(badge);
                }
            }
        });
        statusCol.setPrefWidth(90);

        table.getColumns().addAll(userCol, unameCol, emailCol, statusCol);

        Label summaryLbl = new Label("Total direct friends: " + friends.size());
        summaryLbl.getStyleClass().add("text-muted");

        content.getChildren().addAll(header, searchField, table, summaryLbl);
        pane.setContent(content);
    }

    public static void showDialog(Window owner, User user) {
        UserFriendListDialog dialog = new UserFriendListDialog(user);
        if (owner != null) dialog.initOwner(owner);
        dialog.showAndWait();
    }
}

