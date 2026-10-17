package admin.ui;

import common.model.ChatGroup;
import common.model.User;
import common.ui.component.AvatarLabel;
import common.ui.component.StatusBadge;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.time.format.DateTimeFormatter;

/**
 * Screen #9 (A-09): Dialog showing members and administrators of a chat group.
 */
public class GroupMembersDialog extends Dialog<Void> {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

    public GroupMembersDialog(ChatGroup group) {
        setTitle("Group Members & Admins — " + (group != null ? group.getName() : "Group"));
        setHeaderText(null);

        DialogPane pane = getDialogPane();
        pane.getStyleClass().add("card");
        pane.getStylesheets().add(getClass().getResource("/common/theme.css").toExternalForm());
        pane.getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(14);
        content.setPadding(new Insets(10));
        content.setPrefWidth(640);
        content.setPrefHeight(480);

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        AvatarLabel avatar = new AvatarLabel(group != null ? group.getInitials() : "G", true);
        VBox meta = new VBox(4);
        HBox nameLine = new HBox(8);
        nameLine.setAlignment(Pos.CENTER_LEFT);

        Label nameLbl = new Label(group != null ? group.getName() : "Unnamed Group");
        nameLbl.getStyleClass().add("section-title");
        nameLine.getChildren().add(nameLbl);

        if (group != null && group.isEncrypted()) {
            Label encBadge = new Label("🔒 Encrypted");
            encBadge.getStyleClass().add("badge-encrypted");
            nameLine.getChildren().add(encBadge);
        }

        String createdStr = (group != null && group.getCreatedAt() != null) ? group.getCreatedAt().format(DATE_FMT) : "—";
        Label subLbl = new Label("Created: " + createdStr + " • Total members: " + (group != null ? group.getMembers().size() : 0));
        subLbl.getStyleClass().add("text-muted");

        meta.getChildren().addAll(nameLine, subLbl);
        header.getChildren().addAll(avatar, meta);

        // TabPane with Members and Admins
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Tab 1: Members
        Tab membersTab = new Tab("Members (" + (group != null ? group.getMembers().size() : 0) + ")");
        TableView<User> membersTable = createMembersTable(group, false);
        membersTab.setContent(membersTable);

        // Tab 2: Admins
        Tab adminsTab = new Tab("Administrators (" + (group != null ? group.getAdmins().size() : 0) + ")");
        TableView<User> adminsTable = createMembersTable(group, true);
        adminsTab.setContent(adminsTable);

        tabPane.getTabs().addAll(membersTab, adminsTab);

        content.getChildren().addAll(header, tabPane);
        pane.setContent(content);
    }

    private TableView<User> createMembersTable(ChatGroup group, boolean adminsOnly) {
        TableView<User> table = new TableView<>();
        table.getStyleClass().add("table-view-clean");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        if (group != null) {
            table.setItems(FXCollections.observableArrayList(adminsOnly ? group.getAdmins() : group.getMembers()));
        }

        TableColumn<User, User> userCol = new TableColumn<>("Name");
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

                    if (!adminsOnly && group != null && group.getAdmins().contains(item)) {
                        Label adminTag = new Label("ADMIN");
                        adminTag.getStyleClass().add("tag-admin");
                        box.getChildren().add(adminTag);
                    }
                    setGraphic(box);
                }
            }
        });
        userCol.setPrefWidth(220);

        TableColumn<User, String> unameCol = new TableColumn<>("Username");
        unameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper("@" + c.getValue().getUsername()));
        unameCol.setPrefWidth(130);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEmail()));
        emailCol.setPrefWidth(160);

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
        return table;
    }

    public static void showDialog(Window owner, ChatGroup group) {
        GroupMembersDialog dialog = new GroupMembersDialog(group);
        if (owner != null) dialog.initOwner(owner);
        dialog.showAndWait();
    }
}

