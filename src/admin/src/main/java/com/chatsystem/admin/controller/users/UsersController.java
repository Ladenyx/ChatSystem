package com.chatsystem.admin.controller.users;

import com.chatsystem.admin.AdminApplication;
import com.chatsystem.admin.AdminViews;
import com.chatsystem.admin.mock.UserManagementFixtures;
import com.chatsystem.admin.navigation.AdminViewInputs;
import com.chatsystem.admin.navigation.AdminViewInputs.Action;
import com.chatsystem.admin.service.AdminUserDirectoryService;
import com.chatsystem.admin.service.AdminUserFilter;
import com.chatsystem.admin.service.MockAdminUserDirectoryService;
import com.chatsystem.common.api.OffsetPage;
import com.chatsystem.common.api.SortDirection;
import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.dto.admin.AdminUserListItemDto;
import com.chatsystem.desktop.async.AsyncViewLoader;
import com.chatsystem.desktop.async.ViewLoadState;
import com.chatsystem.desktop.async.ViewLoadStatus;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockLoginLog;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewController;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.desktop.skeleton.StateContentPane;
import com.chatsystem.desktop.skeleton.TableSkeleton;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

public final class UsersController implements ViewController {
    private static final int PAGE_SIZE = 25;
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
    @FXML private StackPane userTableHost;
    @FXML private TableView<AdminUserListItemDto> userTable;
    @FXML private TableColumn<AdminUserListItemDto, AdminUserListItemDto> userIdentity;
    @FXML private TableColumn<AdminUserListItemDto, AccountStatus> userStatus;
    @FXML private TableColumn<AdminUserListItemDto, Instant> userCreated;
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

    private final SimpleBooleanProperty reducedMotion = new SimpleBooleanProperty(false);
    private final PauseTransition filterDebounce = new PauseTransition(javafx.util.Duration.millis(250));
    private StateContentPane tableStatePane;
    private AsyncViewLoader<AdminUserFilter, OffsetPage<AdminUserListItemDto>> directoryLoader;
    private ViewNavigator navigator;
    private boolean suppressSortReload;
    private int currentPage;

    @FXML
    private void initialize() {
        configureTables();
        tableStatePane = new StateContentPane(userTable, new TableSkeleton(), reducedMotion);
        tableStatePane.setEmptyState("Không tìm thấy tài khoản", "Thử thay đổi từ khóa hoặc bộ lọc trạng thái.");
        tableStatePane.setOnRetry(this::reloadDirectory);
        userTableHost.getChildren().setAll(tableStatePane);

        activeUsers.setUserData(AccountStatus.ACTIVE);
        lockedUsers.setUserData(AccountStatus.LOCKED);
        deletedUsers.setUserData(AccountStatus.DELETED);

        filterDebounce.setOnFinished(event -> reloadDirectory());
        nameFilter.textProperty().addListener((observable, previous, current) -> scheduleFilterReload());
        usernameFilter.textProperty().addListener((observable, previous, current) -> scheduleFilterReload());
        statusFilters.selectedToggleProperty().addListener((observable, previous, current) -> {
            if (current == null) {
                statusFilters.selectToggle(previous);
            } else {
                currentPage = 0;
                reloadDirectory();
            }
        });
        clearFilters.visibleProperty().bind(clearFilters.disableProperty().not());
        clearFilters.managedProperty().bind(clearFilters.visibleProperty());
        userTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, previous, selected) -> showProfile(selected));

        directoryLoader = new AsyncViewLoader<>(
                () -> AdminApplication.getServices().sessionStore().sessionToken().orElse(""),
                page -> page == null || page.items().isEmpty(),
                this::onDirectoryState
        );
        if (!directoryService().isLiveBackend()) {
            reloadDirectory();
        } else {
            clearFilters.setDisable(true);
            userCount.setText("0 tài khoản");
        }
    }

    @Override
    public void onEnter(ViewNavigator navigator, View view) {
        this.navigator = navigator;
        reducedMotion.bind(navigator.themes().reducedMotionProperty());
        if (directoryService().isLiveBackend()) {
            reloadDirectory();
        }
    }

    @Override
    public void onLeave() {
        filterDebounce.stop();
        if (directoryLoader != null) {
            directoryLoader.cancelCurrent();
        }
    }

    private void configureTables() {
        userIdentity.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue()));
        userIdentity.setCellFactory(column -> new IdentityCell());
        userStatus.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue().status()));
        userStatus.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(AccountStatus status, boolean empty) {
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
        suppressSortReload = true;
        userTable.setSortPolicy(table -> {
            if (!suppressSortReload) {
                currentPage = 0;
                reloadDirectory();
            }
            return true;
        });
        userTable.getSortOrder().add(userIdentity);
        suppressSortReload = false;
        userTable.getFocusModel().focus(-1);

        bindText(loginAt, login -> DATE_TIME.format(login.loggedInAt()));
        bindText(loginApp, login -> login.scope() == MockLoginLog.Scope.USER_CLIENT ? "User Client" : "Admin App");
        bindText(friendName, MockUser::fullName);
        bindText(friendUsername, user -> "@" + user.username());
    }

    private void scheduleFilterReload() {
        currentPage = 0;
        if (!directoryService().isLiveBackend()) {
            reloadDirectory();
            return;
        }
        filterDebounce.playFromStart();
    }

    private void reloadDirectory() {
        filterDebounce.stop();
        AdminUserFilter filter = buildFilter();
        clearFilters.setDisable(filter.name() == null && filter.username() == null && filter.status() == null);

        AdminUserDirectoryService service = directoryService();
        if (!service.isLiveBackend()) {
            OffsetPage<AdminUserListItemDto> page = service.listUsers(filter).join();
            onDirectoryState(page.items().isEmpty() ? ViewLoadState.empty() : ViewLoadState.content(page));
            return;
        }
        directoryLoader.load(filter, () -> service.listUsers(filter));
    }

    private void onDirectoryState(ViewLoadState<OffsetPage<AdminUserListItemDto>> state) {
        tableStatePane.apply(state);
        OffsetPage<AdminUserListItemDto> page = state.data();
        if (page == null) {
            if (state.status() != ViewLoadStatus.LOADING) {
                userTable.getItems().clear();
                userTable.getSelectionModel().clearSelection();
                userCount.setText("0 tài khoản");
            }
            return;
        }
        AdminUserListItemDto selected = userTable.getSelectionModel().getSelectedItem();
        UUID selectedId = selected == null ? null : selected.id();
        userTable.getItems().setAll(page.items());
        userCount.setText(page.totalItems() + " tài khoản");
        if (selectedId != null) {
            page.items().stream()
                    .filter(item -> item.id().equals(selectedId))
                    .findFirst()
                    .ifPresentOrElse(
                            item -> userTable.getSelectionModel().select(item),
                            () -> userTable.getSelectionModel().clearSelection()
                    );
        }
    }

    private AdminUserFilter buildFilter() {
        var selectedFilter = statusFilters.getSelectedToggle();
        AccountStatus status = selectedFilter == null ? null : (AccountStatus) selectedFilter.getUserData();
        String sortBy = "fullName";
        SortDirection sortDir = SortDirection.ASC;
        if (!userTable.getSortOrder().isEmpty()) {
            TableColumn<AdminUserListItemDto, ?> sortColumn = userTable.getSortOrder().getFirst();
            if (sortColumn == userCreated) {
                sortBy = "createdAt";
            }
            if (sortColumn.getSortType() == TableColumn.SortType.DESCENDING) {
                sortDir = SortDirection.DESC;
            }
        }
        return new AdminUserFilter(
                nameFilter.getText(),
                usernameFilter.getText(),
                status,
                sortBy,
                sortDir,
                currentPage,
                PAGE_SIZE
        );
    }

    @FXML
    private void clearFilters() {
        nameFilter.clear();
        usernameFilter.clear();
        statusFilters.selectToggle(allUsers);
        currentPage = 0;
        reloadDirectory();
        nameFilter.requestFocus();
    }

    private void showProfile(AdminUserListItemDto item) {
        setShown(unselectedProfile, item == null);
        setShown(userInspector, item != null);
        if (item == null) {
            return;
        }

        Optional<MockUser> mockUser = (directoryService() instanceof MockAdminUserDirectoryService mockService)
                ? mockService.findMockUser(item.id())
                : Optional.empty();
        if (mockUser.isPresent()) {
            renderMockProfile(mockUser.get());
            return;
        }
        renderListSummaryProfile(item);
    }

    private void renderMockProfile(MockUser user) {
        AccountStatus status = AccountStatus.valueOf(user.status().name());
        renderHeader(user.fullName(), user.username(), "ID " + user.id(), status);
        showProfileValue(profileEmail, user.email());
        showProfileValue(profileAddress, user.address());
        showProfileValue(profileBirthDate, user.birthDate().map(DATE::format).orElse(""));
        showProfileValue(profileGender, switch (user.gender()) {
            case MALE -> "Nam";
            case FEMALE -> "Nữ";
            case UNSPECIFIED -> "";
        });

        boolean deleted = status == AccountStatus.DELETED;
        setShown(deletionNotice, deleted);
        deletionNotice.setText(user.deletedAt().map(at -> "Đã xóa " + DATE_TIME.format(at)
                + "\nKhôi phục trước " + DATE_TIME.format(at.plus(Duration.ofDays(30)))).orElse(""));
        configureActionButtons(status, user.role() == MockUser.Role.ADMIN, false);

        setDetailRows(loginHistory, UserManagementFixtures.loginHistory(user.id()));
        setDetailRows(friends, UserManagementFixtures.friends(user.id()));
        friendCount.setText(friends.getItems().size() + " bạn bè trực tiếp");
        userInspector.setVvalue(0);
    }

    private void renderListSummaryProfile(AdminUserListItemDto item) {
        renderHeader(item.fullName(), item.username(), "ID " + item.id(), item.status());
        showProfileValue(profileEmail, "");
        showProfileValue(profileAddress, "");
        showProfileValue(profileBirthDate, "");
        showProfileValue(profileGender, "");

        boolean deleted = item.status() == AccountStatus.DELETED;
        setShown(deletionNotice, deleted);
        deletionNotice.setText(deleted ? "Tài khoản đang ở trạng thái xóa mềm (chờ khôi phục)." : "");
        configureActionButtons(item.status(), item.role() == UserRole.ADMIN, true);

        setDetailRows(loginHistory, List.of());
        setDetailRows(friends, List.of());
        friendCount.setText("0 bạn bè trực tiếp");
        userInspector.setVvalue(0);
    }

    private void renderHeader(String fullName, String username, String identityText, AccountStatus status) {
        profileInitials.setText(initialsOf(fullName));
        profileName.setText(fullName);
        profileHandle.setText("@" + username);
        profileIdentity.setText(identityText);
        showStatus(profileStatus, status);
    }

    private void configureActionButtons(AccountStatus status, boolean isAdminAccount, boolean summaryOnly) {
        boolean deleted = status == AccountStatus.DELETED;
        boolean locked = status == AccountStatus.LOCKED;
        editUser.setDisable(summaryOnly || deleted);
        toggleLock.setDisable(summaryOnly || deleted || isAdminAccount);
        resetPassword.setDisable(summaryOnly || deleted || isAdminAccount);
        deleteOrRestore.setDisable(summaryOnly || isAdminAccount);
        toggleLock.setText(locked ? "Mở khóa tài khoản" : "Khóa tài khoản");
        lockIcon.setIconCode(locked ? Feather.UNLOCK : Feather.LOCK);
        deleteOrRestore.setText(deleted ? "Khôi phục trước hạn" : "Xóa mềm 30 ngày");
        lifecycleIcon.setIconCode(deleted ? Feather.ROTATE_CCW : Feather.TRASH_2);
        deleteOrRestore.pseudoClassStateChanged(DESTRUCTIVE, !deleted);
    }

    @FXML
    private void openCreateUser() {
        navigator.showModal(AdminViews.EDIT_USER);
    }

    @FXML
    private void openEditUser() {
        var item = userTable.getSelectionModel().getSelectedItem();
        if (item == null) {
            return;
        }
        navigator.showModal(AdminViews.EDIT_USER, new AdminViewInputs.EditUser(selectedTargetId(item)));
    }

    @FXML
    private void openLockConfirmation() {
        var item = userTable.getSelectionModel().getSelectedItem();
        if (item == null) {
            return;
        }
        openConfirmation(item, item.status() == AccountStatus.LOCKED ? Action.UNLOCK_USER : Action.LOCK_USER);
    }

    @FXML
    private void openResetConfirmation() {
        var item = userTable.getSelectionModel().getSelectedItem();
        if (item == null) {
            return;
        }
        openConfirmation(item, Action.RESET_PASSWORD);
    }

    @FXML
    private void openDeletionConfirmation() {
        var item = userTable.getSelectionModel().getSelectedItem();
        if (item == null) {
            return;
        }
        openConfirmation(item, item.status() == AccountStatus.DELETED ? Action.RESTORE_USER : Action.SOFT_DELETE_USER);
    }

    private void openConfirmation(AdminUserListItemDto item, Action action) {
        navigator.showModal(AdminViews.CONFIRM_ACTION, new AdminViewInputs.ConfirmAction(action, selectedTargetId(item)));
    }

    private String selectedTargetId(AdminUserListItemDto item) {
        if (directoryService() instanceof MockAdminUserDirectoryService mockService) {
            return mockService.findMockUser(item.id()).map(MockUser::id).orElse(item.id().toString());
        }
        return item.id().toString();
    }

    @FXML
    private void openLoginLogs() {
        navigator.navigate(AdminViews.LOGIN_LOGS);
    }

    private static AdminUserDirectoryService directoryService() {
        return AdminApplication.getServices().adminUserDirectoryService();
    }

    private static String initialsOf(String fullName) {
        String trimmed = fullName == null ? "" : fullName.strip();
        if (trimmed.isEmpty()) {
            return "?";
        }
        var names = trimmed.split("\\s+");
        String first = names[0].substring(0, 1);
        String last = names.length > 1 ? names[names.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase(Locale.ROOT);
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
        boolean missing = value == null || value.isBlank();
        label.setText(missing ? "Chưa cung cấp" : value);
        label.pseudoClassStateChanged(MISSING, missing);
    }

    private static void showStatus(Label label, AccountStatus status) {
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

    private static void setShown(Node node, boolean shown) {
        node.setManaged(shown);
        node.setVisible(shown);
    }

    private static final class IdentityCell extends TableCell<AdminUserListItemDto, AdminUserListItemDto> {
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
        protected void updateItem(AdminUserListItemDto user, boolean empty) {
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
