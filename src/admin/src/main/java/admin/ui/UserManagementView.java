package admin.ui;

import common.mock.MockData;
import common.model.User;
import common.ui.component.AvatarLabel;
import common.ui.component.ConfirmDialog;
import common.ui.component.StatusBadge;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;

/**
 * Screen #3 (A-01 to A-04): User Management screen.
 * Supports filtering, sorting, Add/Edit/Delete, Lock/Unlock, Password Reset, and inspecting Login/Friend dialogs.
 */
public class UserManagementView extends VBox {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final ObservableList<User> masterUsers = FXCollections.observableArrayList();
    private final FilteredList<User> filteredUsers = new FilteredList<>(masterUsers, p -> true);
    private final SortedList<User> sortedUsers = new SortedList<>(filteredUsers);
    private final TableView<User> table = new TableView<>(sortedUsers);

    private final TextField searchField = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final ComboBox<String> sortCombo = new ComboBox<>();

    private final Button editBtn = new Button("Edit User");
    private final Button deleteBtn = new Button("Delete");
    private final Button lockUnlockBtn = new Button("Lock / Unlock");
    private final Button resetPassBtn = new Button("Reset Password");
    private final Button loginHistoryBtn = new Button("Login History");
    private final Button friendListBtn = new Button("Friend List");

    public UserManagementView() {
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("admin-content");

        buildHeader();
        buildFilterBar();
        buildTable();
        buildActionBar();

        reloadUsers();
        sortedUsers.comparatorProperty().bind(table.comparatorProperty());
    }

    private void buildHeader() {
        VBox titleBlock = new VBox(4);
        Label heading = new Label("User Management");
        heading.getStyleClass().add("admin-heading");
        Label sub = new Label("View, search, create, update, and moderate all user accounts across the platform.");
        sub.getStyleClass().add("text-muted");
        titleBlock.getChildren().addAll(heading, sub);
        getChildren().add(titleBlock);
    }

    private void buildFilterBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);

        searchField.setPromptText("Filter by name or username...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(280);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        statusFilter.setItems(FXCollections.observableArrayList("All Status", "Active Only", "Locked Only"));
        statusFilter.setValue("All Status");
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        sortCombo.setItems(FXCollections.observableArrayList(
                "Sort: Default",
                "Name (A → Z)",
                "Name (Z → A)",
                "Created (Newest)",
                "Created (Oldest)"
        ));
        sortCombo.setValue("Sort: Default");
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applySort(newVal));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ Add User");
        addBtn.getStyleClass().add("btn-accent");
        addBtn.setOnAction(e -> handleAddUser());

        bar.getChildren().addAll(searchField, statusFilter, sortCombo, spacer, addBtn);
        getChildren().add(bar);
    }

    private void buildTable() {
        table.getStyleClass().add("table-view-clean");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ID column
        TableColumn<User, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        idCol.setPrefWidth(55);

        // Name column (Avatar + Name)
        TableColumn<User, User> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    AvatarLabel av = new AvatarLabel(item.getInitials(), false, true);
                    Label nameLbl = new Label(item.getFullName());
                    nameLbl.getStyleClass().add("text-semibold");
                    HBox box = new HBox(8, av, nameLbl);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });
        nameCol.setPrefWidth(200);

        // Username column
        TableColumn<User, String> unameCol = new TableColumn<>("Username");
        unameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper("@" + c.getValue().getUsername()));
        unameCol.setPrefWidth(120);

        // Email column
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEmail()));
        emailCol.setPrefWidth(160);

        // Gender column
        TableColumn<User, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getGender() != null ? c.getValue().getGender().name() : "—"));
        genderCol.setPrefWidth(80);

        // Account Status column
        TableColumn<User, User> statusCol = new TableColumn<>("Account Status");
        statusCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(StatusBadge.accountStatus(item));
                }
            }
        });
        statusCol.setPrefWidth(110);

        // Online Status column
        TableColumn<User, User> onlineCol = new TableColumn<>("Presence");
        onlineCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        onlineCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(StatusBadge.onlineStatus(item));
                }
            }
        });
        onlineCol.setPrefWidth(90);

        // Created At column
        TableColumn<User, String> createdCol = new TableColumn<>("Registered");
        createdCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().format(DATE_FMT) : "—"));
        createdCol.setPrefWidth(110);

        table.getColumns().addAll(idCol, nameCol, unameCol, emailCol, genderCol, statusCol, onlineCol, createdCol);

        // Selection listener to update action buttons state
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSel = newSel != null;
            editBtn.setDisable(!hasSel);
            deleteBtn.setDisable(!hasSel);
            lockUnlockBtn.setDisable(!hasSel);
            resetPassBtn.setDisable(!hasSel);
            loginHistoryBtn.setDisable(!hasSel);
            friendListBtn.setDisable(!hasSel);

            if (hasSel) {
                lockUnlockBtn.setText(newSel.getStatus() == User.Status.ACTIVE ? "Lock Account" : "Unlock Account");
            } else {
                lockUnlockBtn.setText("Lock / Unlock");
            }
        });

        getChildren().add(table);
    }

    private void buildActionBar() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);

        editBtn.getStyleClass().add("btn-ghost");
        editBtn.setDisable(true);
        editBtn.setOnAction(e -> handleEditUser());

        deleteBtn.getStyleClass().add("btn-warn");
        deleteBtn.setDisable(true);
        deleteBtn.setOnAction(e -> handleDeleteUser());

        lockUnlockBtn.getStyleClass().add("btn-ghost");
        lockUnlockBtn.setDisable(true);
        lockUnlockBtn.setOnAction(e -> handleLockUnlock());

        resetPassBtn.getStyleClass().add("btn-ghost");
        resetPassBtn.setDisable(true);
        resetPassBtn.setOnAction(e -> handleResetPassword());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        loginHistoryBtn.getStyleClass().add("btn-ghost");
        loginHistoryBtn.setDisable(true);
        loginHistoryBtn.setOnAction(e -> handleViewLoginHistory());

        friendListBtn.getStyleClass().add("btn-ghost");
        friendListBtn.setDisable(true);
        friendListBtn.setOnAction(e -> handleViewFriendList());

        bar.getChildren().addAll(editBtn, deleteBtn, lockUnlockBtn, resetPassBtn, spacer, loginHistoryBtn, friendListBtn);
        getChildren().add(bar);
    }

    private void reloadUsers() {
        masterUsers.setAll(MockData.USERS.stream().filter(u -> u.getId() != 0).toList());
        applyFilters();
    }

    private void applyFilters() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String st = statusFilter.getValue();

        filteredUsers.setPredicate(u -> {
            if (u.getId() == 0) return false;
            boolean matchQuery = q.isEmpty()
                    || u.getFullName().toLowerCase().contains(q)
                    || u.getUsername().toLowerCase().contains(q)
                    || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q));

            boolean matchStatus = true;
            if ("Active Only".equals(st)) matchStatus = u.getStatus() == User.Status.ACTIVE;
            else if ("Locked Only".equals(st)) matchStatus = u.getStatus() == User.Status.LOCKED;

            return matchQuery && matchStatus;
        });
    }

    private void applySort(String criteria) {
        if (criteria == null) return;
        switch (criteria) {
            case "Name (A → Z)" -> masterUsers.sort(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER));
            case "Name (Z → A)" -> masterUsers.sort(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER).reversed());
            case "Created (Newest)" -> masterUsers.sort((a, b) -> {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
            case "Created (Oldest)" -> masterUsers.sort((a, b) -> {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return a.getCreatedAt().compareTo(b.getCreatedAt());
            });
            default -> masterUsers.sort(Comparator.comparingInt(User::getId));
        }
    }

    private void handleAddUser() {
        AddEditUserDialog.showDialog(getScene() != null ? getScene().getWindow() : null, null, newUser -> {
            MockData.addUser(newUser);
            reloadUsers();
            table.getSelectionModel().select(newUser);
        });
    }

    private void handleEditUser() {
        User sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        AddEditUserDialog.showDialog(getScene() != null ? getScene().getWindow() : null, sel, updated -> {
            MockData.updateUser(updated);
            reloadUsers();
            table.getSelectionModel().select(updated);
        });
    }

    private void handleDeleteUser() {
        User sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        boolean ok = ConfirmDialog.confirm("Delete User", "Are you sure you want to delete user \""
                + sel.getFullName() + "\" (@" + sel.getUsername() + ")? This action cannot be undone.");
        if (ok) {
            MockData.deleteUser(sel.getId());
            reloadUsers();
        }
    }

    private void handleLockUnlock() {
        User sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        MockData.toggleUserStatus(sel.getId());
        table.refresh();
        lockUnlockBtn.setText(sel.getStatus() == User.Status.ACTIVE ? "Lock Account" : "Unlock Account");
    }

    private void handleResetPassword() {
        User sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        String newPass = MockData.resetUserPassword(sel.getId());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Reset Successful");
        alert.setHeaderText("Temporary Password Generated");
        alert.setContentText("A new temporary password has been generated for @" + sel.getUsername() + " (" + sel.getEmail() + "):\n\n"
                + "  " + newPass + "\n\nAn email with instructions has been dispatched.");
        alert.showAndWait();
    }

    private void handleViewLoginHistory() {
        User sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        UserLoginHistoryDialog.showDialog(getScene() != null ? getScene().getWindow() : null, sel);
    }

    private void handleViewFriendList() {
        User sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        UserFriendListDialog.showDialog(getScene() != null ? getScene().getWindow() : null, sel);
    }
}

