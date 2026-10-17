package user.ui;

import common.mock.MockData;
import common.model.ChatGroup;
import common.model.User;
import common.ui.component.AvatarLabel;
import common.ui.component.ConfirmDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Screen #15: Manage Group Dialog (Roadmap U-20 to U-23, U-25).
 * Features:
 * - Rename a group (U-20).
 * - Add new members from friends list (U-21).
 * - Grant group-admin rights (U-22).
 * - Remove members (group admins only) (U-23).
 * - Toggle end-to-end encryption (U-25).
 */
public class ManageGroupDialog extends Dialog<Boolean> {

    private final ChatGroup group;
    private final boolean isCurrentUserAdmin;
    private final TextField groupNameField = new TextField();
    private final CheckBox encryptionCheckBox = new CheckBox("End-to-end encryption 🔒");
    private final ObservableList<User> membersList = FXCollections.observableArrayList();
    private final ListView<User> membersListView = new ListView<>();
    private final ComboBox<User> addMemberCombo = new ComboBox<>();
    private final Runnable onGroupUpdated;

    public ManageGroupDialog(ChatGroup group, Runnable onGroupUpdated) {
        this.group = group;
        this.onGroupUpdated = onGroupUpdated;
        this.isCurrentUserAdmin = group.getAdmins().stream()
                .anyMatch(a -> a.getId() == MockData.CURRENT_USER.getId());

        setTitle("Manage Group · " + group.getName());
        setHeaderText(null);

        DialogPane pane = getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);

        VBox root = new VBox(14);
        root.setPadding(new Insets(16));
        root.setPrefWidth(460);
        root.setPrefHeight(520);

        // Header
        VBox header = buildHeader();

        // Rename & Security Section (U-20, U-25)
        VBox detailsSection = buildDetailsSection();

        // Add Member Section (U-21)
        VBox addMemberSection = buildAddMemberSection();

        // Members List Section (U-22, U-23)
        VBox membersSection = buildMembersSection();

        root.getChildren().addAll(header, detailsSection, addMemberSection, membersSection);
        pane.setContent(root);

        setResultConverter(btn -> true);
    }

    private VBox buildHeader() {
        Label avatar = new Label(group.getInitials());
        avatar.getStyleClass().addAll("avatar", "avatar-group");
        avatar.setMinSize(44, 44);
        avatar.setAlignment(Pos.CENTER);

        Label title = new Label(group.getName());
        title.getStyleClass().add("section-title");

        Label subtitle = new Label(group.getMembers().size() + " members" + (group.isEncrypted() ? " • 🔒 Encrypted" : ""));
        subtitle.getStyleClass().add("login-subtitle");

        HBox top = new HBox(12, avatar, new VBox(2, title, subtitle));
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(0, 0, 8, 0));
        top.setStyle("-fx-border-color: #ebebfc; -fx-border-width: 0 0 1 0;");
        return new VBox(top);
    }

    private VBox buildDetailsSection() {
        Label secTitle = new Label("Group Settings");
        secTitle.getStyleClass().add("text-bold");

        groupNameField.setText(group.getName());
        groupNameField.getStyleClass().add("search-field");
        groupNameField.setStyle("-fx-background-color: white;");
        HBox.setHgrow(groupNameField, Priority.ALWAYS);

        Button renameBtn = new Button("Rename");
        renameBtn.getStyleClass().add("btn-accent");
        renameBtn.setOnAction(e -> {
            String newName = groupNameField.getText().trim();
            if (!newName.isBlank() && !newName.equals(group.getName())) {
                group.setName(newName);
                notifyUpdated();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Group Renamed");
                alert.setHeaderText(null);
                alert.setContentText("Group name changed to: " + newName);
                alert.showAndWait();
            }
        });

        HBox renameRow = new HBox(8, groupNameField, renameBtn);
        renameRow.setAlignment(Pos.CENTER_LEFT);

        encryptionCheckBox.setSelected(group.isEncrypted());
        encryptionCheckBox.setOnAction(e -> {
            group.setEncrypted(encryptionCheckBox.isSelected());
            notifyUpdated();
        });

        VBox box = new VBox(6, secTitle, renameRow, encryptionCheckBox);
        return box;
    }

    private VBox buildAddMemberSection() {
        Label addTitle = new Label("Add Friends to Group (U-21)");
        addTitle.getStyleClass().add("text-bold");

        // Filter friends who are NOT already in the group
        refreshAddMemberCombo();
        addMemberCombo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(addMemberCombo, Priority.ALWAYS);

        addMemberCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) { setText(null); return; }
                setText(user.getFullName() + " (@" + user.getUsername() + ")");
            }
        });
        addMemberCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) { setText("Choose a friend to add..."); return; }
                setText(user.getFullName() + " (@" + user.getUsername() + ")");
            }
        });

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("btn-accent");
        addBtn.setOnAction(e -> {
            User selected = addMemberCombo.getValue();
            if (selected != null) {
                group.getMembers().add(selected);
                refreshMembersList();
                refreshAddMemberCombo();
                notifyUpdated();
            }
        });

        HBox addRow = new HBox(8, addMemberCombo, addBtn);
        addRow.setAlignment(Pos.CENTER_LEFT);

        return new VBox(6, addTitle, addRow);
    }

    private VBox buildMembersSection() {
        Label membersTitle = new Label("Members (" + group.getMembers().size() + ")");
        membersTitle.getStyleClass().add("text-bold");

        membersListView.getStyleClass().add("list-view-clean");
        VBox.setVgrow(membersListView, Priority.ALWAYS);
        refreshMembersList();

        membersListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                AvatarLabel avatar = new AvatarLabel(user);
                avatar.setStyle("-fx-min-width: 32px; -fx-min-height: 32px; -fx-font-size: 11px;");

                Label name = new Label(user.getFullName());
                name.getStyleClass().add("text-name");

                boolean isAdmin = group.getAdmins().stream().anyMatch(a -> a.getId() == user.getId());

                HBox nameRow = new HBox(6, name);
                nameRow.setAlignment(Pos.CENTER_LEFT);

                if (isAdmin) {
                    Label adminBadge = new Label("Admin");
                    adminBadge.getStyleClass().add("tag-admin");
                    nameRow.getChildren().add(adminBadge);
                }

                Label username = new Label("@" + user.getUsername());
                username.getStyleClass().add("text-muted");

                VBox info = new VBox(2, nameRow, username);
                HBox.setHgrow(info, Priority.ALWAYS);

                HBox actions = new HBox(6);
                actions.setAlignment(Pos.CENTER_RIGHT);

                // Admin privileges: Grant Admin (U-22), Remove Member (U-23)
                if (isCurrentUserAdmin) {
                    if (!isAdmin) {
                        Button makeAdminBtn = new Button("Grant Admin");
                        makeAdminBtn.getStyleClass().add("btn-ghost");
                        makeAdminBtn.setStyle("-fx-font-size: 10.5px; -fx-padding: 3 8 3 8;");
                        makeAdminBtn.setOnAction(e -> {
                            group.getAdmins().add(user);
                            refreshMembersList();
                            notifyUpdated();
                        });
                        actions.getChildren().add(makeAdminBtn);
                    }

                    // Remove member (cannot remove oneself)
                    if (user.getId() != MockData.CURRENT_USER.getId()) {
                        Button removeBtn = new Button("Remove");
                        removeBtn.getStyleClass().add("btn-warn");
                        removeBtn.setStyle("-fx-font-size: 10.5px; -fx-padding: 3 8 3 8;");
                        removeBtn.setOnAction(e -> {
                            boolean confirm = ConfirmDialog.confirm(
                                    "Remove Member",
                                    "Remove " + user.getFullName() + " from this group?"
                            );
                            if (confirm) {
                                group.getMembers().remove(user);
                                group.getAdmins().remove(user);
                                refreshMembersList();
                                refreshAddMemberCombo();
                                notifyUpdated();
                            }
                        });
                        actions.getChildren().add(removeBtn);
                    }
                }

                HBox row = new HBox(10, avatar, info, actions);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(4, 6, 4, 6));

                setGraphic(row);
                setText(null);
            }
        });

        return new VBox(6, membersTitle, membersListView);
    }

    private void refreshMembersList() {
        membersList.setAll(group.getMembers());
        membersListView.setItems(membersList);
    }

    private void refreshAddMemberCombo() {
        List<User> friendsNotInGroup = MockData.getCurrentUserFriends().stream()
                .filter(f -> group.getMembers().stream().noneMatch(m -> m.getId() == f.getId()))
                .toList();
        addMemberCombo.setItems(FXCollections.observableArrayList(friendsNotInGroup));
    }

    private void notifyUpdated() {
        if (onGroupUpdated != null) onGroupUpdated.run();
    }

    public static void showDialog(ChatGroup group, Runnable onGroupUpdated) {
        new ManageGroupDialog(group, onGroupUpdated).showAndWait();
    }
}

