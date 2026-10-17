package user.ui;

import common.mock.MockData;
import common.model.ChatGroup;
import common.model.Message;
import common.model.User;
import common.ui.component.AvatarLabel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Screen #14: Create Group Dialog (Roadmap U-19, U-25).
 * Features:
 * - Group name input.
 * - End-to-end encryption toggle option (U-25).
 * - Multi-member selection from friends list (requires at least 1 other member, U-19).
 * - Real-time member search and selection counter.
 */
public class CreateGroupDialog extends Dialog<ChatGroup> {

    private final TextField groupNameField = new TextField();
    private final CheckBox encryptionCheckBox = new CheckBox("Enable end-to-end encryption 🔒");
    private final Set<User> selectedMembers = new HashSet<>();
    private final Label selectedCountLabel = new Label("Selected: 0 members");
    private final Label errorLabel = new Label();

    public CreateGroupDialog(User preSelectedFriend) {
        setTitle("Create Group Chat");
        setHeaderText(null);

        DialogPane pane = getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        Button createBtn = (Button) pane.lookupButton(ButtonType.OK);
        createBtn.setText("Create Group");
        createBtn.getStyleClass().add("btn-accent");

        if (preSelectedFriend != null) {
            selectedMembers.add(preSelectedFriend);
        }

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setPrefWidth(420);
        root.setPrefHeight(480);

        Label title = new Label("Create New Group");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label("Give your group a name and choose at least one friend to join.");
        subtitle.getStyleClass().add("login-subtitle");

        errorLabel.getStyleClass().add("text-danger");
        errorLabel.setVisible(false);

        // Group name input
        Label nameLbl = new Label("Group Name:");
        nameLbl.getStyleClass().add("text-bold");
        groupNameField.setPromptText("e.g. Study Group, Project Team...");
        groupNameField.getStyleClass().add("search-field");
        groupNameField.setStyle("-fx-background-color: white;");

        // Encryption option
        encryptionCheckBox.setStyle("-fx-font-weight: 600; -fx-padding: 4 0 4 0;");

        // Member selection header & search
        Label membersLbl = new Label("Select Members (at least 1):");
        membersLbl.getStyleClass().add("text-bold");

        TextField searchField = new TextField();
        searchField.setPromptText("Filter friends by name...");
        searchField.getStyleClass().add("search-field");

        // Friends list with checkboxes
        List<User> friends = MockData.getCurrentUserFriends();
        ObservableList<User> masterFriends = FXCollections.observableArrayList(friends);
        FilteredList<User> filteredFriends = new FilteredList<>(masterFriends, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal == null ? "" : newVal.trim().toLowerCase();
            filteredFriends.setPredicate(u -> query.isEmpty()
                    || u.getFullName().toLowerCase().contains(query)
                    || u.getUsername().toLowerCase().contains(query));
        });

        ListView<User> listView = new ListView<>(filteredFriends);
        listView.getStyleClass().add("list-view-clean");
        VBox.setVgrow(listView, Priority.ALWAYS);

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                CheckBox cb = new CheckBox();
                cb.setSelected(selectedMembers.contains(user));
                cb.setOnAction(e -> {
                    if (cb.isSelected()) {
                        selectedMembers.add(user);
                    } else {
                        selectedMembers.remove(user);
                    }
                    updateSelectedCount();
                });

                AvatarLabel avatar = new AvatarLabel(user);
                avatar.setStyle("-fx-min-width: 32px; -fx-min-height: 32px; -fx-font-size: 11px;");

                Label name = new Label(user.getFullName());
                name.getStyleClass().add("text-name");

                Label username = new Label("@" + user.getUsername());
                username.getStyleClass().add("text-muted");

                HBox info = new HBox(8, name, username);
                info.setAlignment(Pos.CENTER_LEFT);

                HBox row = new HBox(10, cb, avatar, info);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(4, 8, 4, 8));

                // Click on row to toggle checkbox
                row.setOnMouseClicked(e -> {
                    if (e.getTarget() != cb) {
                        cb.setSelected(!cb.isSelected());
                        cb.fireEvent(new javafx.event.ActionEvent());
                    }
                });

                setGraphic(row);
                setText(null);
            }
        });

        selectedCountLabel.getStyleClass().add("text-muted");
        updateSelectedCount();

        root.getChildren().addAll(
                title, subtitle, errorLabel,
                nameLbl, groupNameField,
                encryptionCheckBox,
                membersLbl, searchField, listView,
                selectedCountLabel
        );
        pane.setContent(root);

        // Validation upon submit
        createBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String gName = groupNameField.getText().trim();
            if (gName.isBlank()) {
                errorLabel.setText("Please enter a group name.");
                errorLabel.setVisible(true);
                event.consume();
                return;
            }
            if (selectedMembers.isEmpty()) {
                errorLabel.setText("Please select at least one other member (U-19 requirement).");
                errorLabel.setVisible(true);
                event.consume();
                return;
            }
        });

        setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String gName = groupNameField.getText().trim();
                boolean encrypted = encryptionCheckBox.isSelected();

                User me = MockData.CURRENT_USER;
                List<User> allMembers = new ArrayList<>();
                allMembers.add(me);
                allMembers.addAll(selectedMembers);

                ChatGroup newGroup = new ChatGroup(
                        MockData.GROUPS.size() + 1,
                        gName,
                        me.getId(),
                        LocalDateTime.now(),
                        encrypted
                );
                newGroup.getMembers().addAll(allMembers);
                newGroup.getAdmins().add(me);

                // Add welcome message
                Message welcomeMsg = new Message(
                        (int) (System.currentTimeMillis() % 100000),
                        me.getId(),
                        me.getFullName(),
                        null,
                        newGroup.getId(),
                        "Group created by " + me.getFullName() + (encrypted ? " [🔒 End-to-end encrypted]" : ""),
                        Message.Type.GROUP,
                        Message.Status.SENT,
                        LocalDateTime.now()
                );
                newGroup.getMessages().add(welcomeMsg);

                MockData.GROUPS.add(newGroup);
                return newGroup;
            }
            return null;
        });
    }

    private void updateSelectedCount() {
        selectedCountLabel.setText("Selected: " + selectedMembers.size() + " friend" + (selectedMembers.size() == 1 ? "" : "s"));
    }

    public static Optional<ChatGroup> showDialog(User preSelectedFriend) {
        return new CreateGroupDialog(preSelectedFriend).showAndWait();
    }
}

