package com.chatsystem.admin.controller.users;

import com.chatsystem.admin.AdminViews;
import com.chatsystem.admin.mock.UserManagementFixtures;
import com.chatsystem.admin.navigation.AdminViewInputs;
import com.chatsystem.admin.navigation.AdminViewInputs.Action;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockLoginLog;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.mock.model.MockUser.Status;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import java.text.Collator;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public final class UsersController implements ViewController {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withZone(MockData.DISPLAY_ZONE);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")
            .withZone(MockData.DISPLAY_ZONE);
    private static final PseudoClass MISSING = PseudoClass.getPseudoClass("missing");
    private static final PseudoClass DESTRUCTIVE = PseudoClass.getPseudoClass("destructive");

    @FXML private TextField nameFilter;
    @FXML private TextField usernameFilter;
    @FXML private ToggleGroup statusFilters;
    @FXML private ToggleButton allUsers;
    @FXML private ToggleButton activeUsers;
    @FXML private ToggleButton lockedUsers;
    @FXML private ToggleButton deletedUsers;
    @FXML private Hyperlink clearFilters;
    @FXML private Label userCount;
    @FXML private TableView<MockUser> userTable;
    @FXML private TableColumn<MockUser, MockUser> userIdentity;
    @FXML private TableColumn<MockUser, Status> userStatus;
    @FXML private TableColumn<MockUser, Instant> userCreated;
    @FXML private VBox unselectedProfile;
    @FXML private ScrollPane userInspector;
    @FXML private Label profileInitials;
    @FXML private Label profileName;
    @FXML private Label profileHandle;
    @FXML private Label profileIdentity;
    @FXML private Label profileStatus;
    @FXML private Label deletionNotice;
    @FXML private Label profileEmail;
    @FXML private Label profileAddress;
    @FXML private Label profileBirthDate;
    @FXML private Label profileGender;
    @FXML private Button editUser;
    @FXML private Button toggleLock;
    @FXML private FontIcon lockIcon;
    @FXML private Button resetPassword;
    @FXML private Button deleteOrRestore;
    @FXML private FontIcon lifecycleIcon;
    @FXML private TableView<MockLoginLog> loginHistory;
    @FXML private TableColumn<MockLoginLog, String> loginAt;
    @FXML private TableColumn<MockLoginLog, String> loginApp;
    @FXML private TableView<MockUser> friends;
    @FXML private TableColumn<MockUser, String> friendName;
    @FXML private TableColumn<MockUser, String> friendUsername;
    @FXML private Label friendCount;

    private final FilteredList<MockUser> filtered = new FilteredList<>(
            FXCollections.observableArrayList(UserManagementFixtures.users()));
    private ViewNavigator navigator;

    @FXML
    private void initialize() {
        configureTables();
        activeUsers.setUserData(Status.ACTIVE);
        lockedUsers.setUserData(Status.LOCKED);
        deletedUsers.setUserData(Status.DELETED);
        nameFilter.textProperty().addListener((observable, previous, current) -> filterUsers());
        usernameFilter.textProperty().addListener((observable, previous, current) -> filterUsers());
        statusFilters.selectedToggleProperty().addListener((observable, previous, current) -> {
            if (current == null) {
                statusFilters.selectToggle(previous);
            } else {
                filterUsers();
            }
        });
        userCount.textProperty().bind(Bindings.size(filtered).asString("%d tài khoản"));
        clearFilters.visibleProperty().bind(clearFilters.disableProperty().not());
        clearFilters.managedProperty().bind(clearFilters.visibleProperty());
        userTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, previous, selected) -> showProfile(selected));
        filterUsers();
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
    }

    private void configureTables() {
        userIdentity.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue()));
        userIdentity.setComparator(Comparator.comparing(MockUser::fullName, Collator.getInstance(Locale.of("vi", "VN"))));
        userIdentity.setCellFactory(column -> new IdentityCell());
        userStatus.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue().status()));
        userStatus.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(Status status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                showStatus(badge, status);
                setGraphic(badge);
            }
        });
        userCreated.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue().createdAt()));
        userCreated.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Instant createdAt, boolean empty) {
                super.updateItem(createdAt, empty);
                setText(empty || createdAt == null ? null : DATE.format(createdAt));
            }
        });
        var sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sorted);
        userTable.getSortOrder().add(userIdentity);
        userTable.getFocusModel().focus(-1);

        bindText(loginAt, login -> DATE_TIME.format(login.loggedInAt()));
        bindText(loginApp, login -> login.scope() == MockLoginLog.Scope.USER_CLIENT ? "User Client" : "Admin App");
        bindText(friendName, MockUser::fullName);
        bindText(friendUsername, user -> "@" + user.username());
    }

    private void filterUsers() {
        String name = filterTerm(nameFilter.getText());
        String username = filterTerm(usernameFilter.getText());
        var selectedFilter = statusFilters.getSelectedToggle();
        Status status = selectedFilter == null ? null : (Status) selectedFilter.getUserData();
        MockUser selected = userTable.getSelectionModel().getSelectedItem();
        filtered.setPredicate(user -> (status == null || user.status() == status)
                && filterTerm(user.fullName()).contains(name) && filterTerm(user.username()).contains(username));
        if (selected != null && filtered.contains(selected)) {
            userTable.getSelectionModel().select(selected);
        } else {
            userTable.getSelectionModel().clearSelection();
        }
        clearFilters.setDisable(name.isEmpty() && username.isEmpty() && status == null);
    }

    @FXML
    private void clearFilters() {
        nameFilter.clear();
        usernameFilter.clear();
        statusFilters.selectToggle(allUsers);
        nameFilter.requestFocus();
    }

    private void showProfile(MockUser user) {
        setShown(unselectedProfile, user == null);
        setShown(userInspector, user != null);
        if (user == null) {
            return;
        }

        var names = user.fullName().strip().split("\\s+");
        String first = names[0].substring(0, 1);
        String last = names.length > 1 ? names[names.length - 1].substring(0, 1) : "";
        profileInitials.setText((first + last).toUpperCase(Locale.ROOT));
        profileName.setText(user.fullName());
        profileHandle.setText("@" + user.username());
        profileIdentity.setText("ID " + user.id());
        showStatus(profileStatus, user.status());
        showProfileValue(profileEmail, user.email());
        showProfileValue(profileAddress, user.address());
        showProfileValue(profileBirthDate, user.birthDate().map(DATE::format).orElse(""));
        showProfileValue(profileGender, switch (user.gender()) {
            case MALE -> "Nam";
            case FEMALE -> "Nữ";
            case UNSPECIFIED -> "";
        });

        boolean deleted = user.status() == Status.DELETED;
        setShown(deletionNotice, deleted);
        deletionNotice.setText(user.deletedAt().map(at -> "Đã xóa " + DATE_TIME.format(at)
                + "\nKhôi phục trước " + DATE_TIME.format(at.plus(Duration.ofDays(30)))).orElse(""));
        editUser.setDisable(deleted);
        toggleLock.setDisable(deleted);
        resetPassword.setDisable(deleted);
        boolean locked = user.status() == Status.LOCKED;
        toggleLock.setText(locked ? "Mở khóa tài khoản" : "Khóa tài khoản");
        lockIcon.setIconCode(locked ? Feather.UNLOCK : Feather.LOCK);
        deleteOrRestore.setText(deleted ? "Khôi phục trước hạn" : "Xóa mềm 30 ngày");
        lifecycleIcon.setIconCode(deleted ? Feather.ROTATE_CCW : Feather.TRASH_2);
        deleteOrRestore.pseudoClassStateChanged(DESTRUCTIVE, !deleted);

        setDetailRows(loginHistory, UserManagementFixtures.loginHistory(user.id()));
        setDetailRows(friends, UserManagementFixtures.friends(user.id()));
        friendCount.setText(friends.getItems().size() + " bạn bè trực tiếp");
        userInspector.setVvalue(0);
    }

    @FXML
    private void openCreateUser() {
        navigator.showModal(AdminViews.EDIT_USER);
    }

    @FXML
    private void openEditUser() {
        var user = userTable.getSelectionModel().getSelectedItem();
        navigator.showModal(AdminViews.EDIT_USER, new AdminViewInputs.EditUser(user.id()));
    }

    @FXML
    private void openLockConfirmation() {
        var user = userTable.getSelectionModel().getSelectedItem();
        openConfirmation(user, user.status() == Status.LOCKED ? Action.UNLOCK_USER : Action.LOCK_USER);
    }

    @FXML
    private void openResetConfirmation() {
        openConfirmation(userTable.getSelectionModel().getSelectedItem(), Action.RESET_PASSWORD);
    }

    @FXML
    private void openDeletionConfirmation() {
        var user = userTable.getSelectionModel().getSelectedItem();
        openConfirmation(user, user.status() == Status.DELETED ? Action.RESTORE_USER : Action.SOFT_DELETE_USER);
    }

    private void openConfirmation(MockUser user, Action action) {
        navigator.showModal(AdminViews.CONFIRM_ACTION, new AdminViewInputs.ConfirmAction(action, user.id()));
    }

    @FXML
    private void openLoginLogs() {
        navigator.navigate(AdminViews.LOGIN_LOGS);
    }

    private static <T> void bindText(TableColumn<T, String> column, Function<T, String> text) {
        column.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(text.apply(row.getValue())));
    }

    private static <T> void setDetailRows(TableView<T> table, List<T> rows) {
        table.getItems().setAll(rows);
        table.setPrefHeight(36 + table.getFixedCellSize() * Math.clamp(rows.size(), 2, 5));
        table.setMaxHeight(table.getPrefHeight());
        table.getSelectionModel().clearSelection();
        table.getFocusModel().focus(-1);
        table.scrollTo(0);
    }

    private static void showProfileValue(Label label, String value) {
        boolean missing = value.isBlank();
        label.setText(missing ? "Chưa cung cấp" : value);
        label.pseudoClassStateChanged(MISSING, missing);
    }

    private static void showStatus(Label label, Status status) {
        String text = switch (status) {
            case ACTIVE -> "Hoạt động";
            case LOCKED -> "Bị khóa";
            case DELETED -> "Xóa mềm";
        };
        if (label.getGraphic() == null) {
            var dot = new Region();
            dot.getStyleClass().add("admin-status-dot");
            label.setGraphic(dot);
        }
        label.setText(text);
        label.getStyleClass().setAll("label", "admin-user-status", "status-" + status.name().toLowerCase(Locale.ROOT));
    }

    private static String filterTerm(String text) {
        return text.strip().toLowerCase(Locale.ROOT);
    }

    private static void setShown(Node node, boolean shown) {
        node.setManaged(shown);
        node.setVisible(shown);
    }

    private static final class IdentityCell extends TableCell<MockUser, MockUser> {
        private final Label name = new Label();
        private final Label username = new Label();
        private final VBox identity = new VBox(2, name, username);
        private final Tooltip tooltip = new Tooltip();

        private IdentityCell() {
            name.getStyleClass().add("admin-user-name");
            username.getStyleClass().add("caption");
            identity.setMinWidth(0);
        }

        @Override
        protected void updateItem(MockUser user, boolean empty) {
            super.updateItem(user, empty);
            if (empty || user == null) {
                setGraphic(null);
                setTooltip(null);
                setAccessibleText(null);
                return;
            }
            String details = user.fullName() + " · @" + user.username();
            name.setText(user.fullName());
            username.setText("@" + user.username());
            tooltip.setText(details);
            setAccessibleText(details);
            setTooltip(tooltip);
            setGraphic(identity);
        }
    }
}
