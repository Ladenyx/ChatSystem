package com.chatsystem.admin;

import static org.junit.jupiter.api.Assertions.*;

import atlantafx.base.controls.Popover;
import com.chatsystem.admin.navigation.AdminViewInputs;
import com.chatsystem.desktop.DesktopWindow;
import com.chatsystem.desktop.controller.MockDataPreviewController;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockGroup;
import com.chatsystem.desktop.mock.model.MockLoginLog;
import com.chatsystem.desktop.mock.model.MockMonthlyStatistics;
import com.chatsystem.desktop.mock.model.MockSpamReport;
import com.chatsystem.desktop.mock.model.MockUser;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "chatsystem.uiTests", matches = "true")
class AdminApplicationTest {
    private static final ConcurrentLinkedQueue<Throwable> FX_ERRORS = new ConcurrentLinkedQueue<>();

    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> {
            Thread.currentThread().setUncaughtExceptionHandler((thread, error) -> FX_ERRORS.add(error));
            Platform.setImplicitExit(false);
            ready.countDown();
        });
        assertTrue(ready.await(10, TimeUnit.SECONDS));
    }

    @AfterAll
    static void stopJavaFx() {
        Platform.exit();
    }

    @AfterEach
    void noUnhandledJavaFxErrors() {
        var errors = new java.util.ArrayList<>(FX_ERRORS);
        FX_ERRORS.clear();
        assertTrue(errors.isEmpty(), () -> "Unhandled JavaFX errors: " + errors);
    }

    @Test
    void signInOpensOverviewWithoutKeepingThePreviewPassword() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = AdminApplication.createNavigator();
            var root = navigator.getRoot();
            stage.setScene(new Scene(root, 1024, 640));
            stage.show();
            try {
                assertEquals(AdminViews.SIGN_IN, navigator.currentViewProperty().get());
                var sidebar = root.lookup("#workspaceSidebar");
                var toolbar = root.lookup("#workspaceToolbar");
                assertFalse(sidebar.isVisible() || sidebar.isManaged());
                assertFalse(toolbar.isVisible() || toolbar.isManaged());

                var username = (TextField) root.lookup("#signInUsername");
                var password = (PasswordField) root.lookup("#signInPassword");
                var submit = (Button) root.lookup("#signInSubmit");
                assertTrue(submit.isDefaultButton());
                assertTrue(submit.isDisabled());
                root.applyCss();
                root.layout();
                var form = root.lookup("#signInForm");
                var bounds = form.localToScene(form.getBoundsInLocal());
                assertEquals(512, (bounds.getMinX() + bounds.getMaxX()) / 2, 1);
                assertTrue(bounds.getMaxY() < stage.getScene().getHeight());
                username.requestFocus();
                capture(stage, "admin-sign-in-light");

                password.setText("example");
                assertTrue(submit.isDisabled());
                username.setText("quantri");
                assertFalse(submit.isDisabled());
                navigator.themes().toggle();
                submit.requestFocus();
                capture(stage, "admin-sign-in-dark");

                submit.fire();
                assertEquals(AdminViews.OVERVIEW, navigator.currentViewProperty().get());
                assertTrue(sidebar.isVisible() && sidebar.isManaged());
                assertTrue(toolbar.isVisible() && toolbar.isManaged());
                assertTrue(password.getText().isEmpty());
                assertFalse(navigator.goBack());
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @Test
    void sidebarOpensEveryRouteAndKeepsItsSelectionWhenGoingBack() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = AdminApplication.createNavigator();
            navigator.reset(AdminViews.OVERVIEW);
            DesktopWindow.show(stage, AdminApplication.WINDOW_TITLE, navigator);
            try {
                var root = stage.getScene().getRoot();
                capture(stage, "admin-light");
                for (var view : AdminViews.PAGES) {
                    ToggleButton button = (ToggleButton) root.lookup("#nav-" + view.id());
                    button.fire();
                    assertTrue(button.isSelected());
                    assertEquals(view, navigator.currentViewProperty().get());
                }
                ((Button) root.lookup("#goBack")).fire();
                assertTrue(((ToggleButton) root.lookup("#nav-A-SCR-08")).isSelected());
                ((Button) root.lookup("#displayOptions")).fire();
                Popover display = Window.getWindows().stream()
                        .filter(Popover.class::isInstance).map(Popover.class::cast)
                        .findFirst().orElseThrow();
                ((ToggleButton) display.getContentNode().lookup("#darkTheme")).fire();
                assertTrue(root.getPseudoClassStates().contains(javafx.css.PseudoClass.getPseudoClass("dark")));
                ((CheckBox) display.getContentNode().lookup("#reducedMotion")).fire();
                assertTrue(navigator.themes().reducedMotionProperty().get());
                display.hide();
                navigator.reset(AdminViews.OVERVIEW);
                capture(stage, "admin-dark");
                navigator.showModal(AdminViews.EDIT_USER);
                assertTrue(Window.getWindows().size() > 1);
                ((ToggleButton) root.lookup("#nav-A-SCR-03")).fire();
                assertEquals(1, Window.getWindows().size());
            } finally {
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @Test
    void overviewShowsTheFixtureSnapshotAndOpensDetailRoutes() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = AdminApplication.createNavigator();
            navigator.reset(AdminViews.OVERVIEW);
            stage.setScene(new Scene(navigator.getRoot(), 1728, 1000));
            stage.show();
            try {
                var root = navigator.getRoot();
                root.applyCss();
                root.layout();

                MockData data = MockData.create();
                var online = data.users().stream().filter(user -> user.presence() == MockUser.Presence.ONLINE).count();
                var pending = data.spamReports().stream()
                        .filter(report -> report.status() == MockSpamReport.Status.PENDING).count();
                assertTrue(((Button) root.lookup("#totalUsersCard")).getHeight() <= 120,
                        "KPI cards fit in the first row");
                assertEquals(Integer.toString(data.users().size()), ((Label) root.lookup("#totalUsersValue")).getText());
                assertEquals(Long.toString(online), ((Label) root.lookup("#onlineUsersValue")).getText());
                assertEquals(Integer.toString(data.groups().size()), ((Label) root.lookup("#totalGroupsValue")).getText());
                assertEquals(Long.toString(pending), ((Label) root.lookup("#pendingSpamValue")).getText());

                var trend = (LineChart<?, ?>) root.lookup("#monthlyTrend");
                assertTrue(trend.getHeight() >= 280, "The trend uses the extra height of a larger window");
                assertEquals(2, trend.getData().size());
                for (var series : trend.getData()) {
                    assertEquals(12, series.getData().size());
                    assertEquals("T1", series.getData().getFirst().getXValue());
                    assertEquals("T12", series.getData().getLast().getXValue());
                }
                var monthly = data.monthlyStatistics(data.snapshotDate().getYear());
                assertEquals(monthly.stream().map(MockMonthlyStatistics::registrations).toList(),
                        trend.getData().get(0).getData().stream().map(XYChart.Data::getYValue).toList());
                assertEquals(monthly.stream().map(MockMonthlyStatistics::activeUsers).toList(),
                        trend.getData().get(1).getData().stream().map(XYChart.Data::getYValue).toList());

                var expectedReports = data.spamReports().stream()
                        .sorted(Comparator.comparing(MockSpamReport::createdAt).reversed()).limit(3).toList();
                var expectedLogins = data.loginLogs().stream()
                        .sorted(Comparator.comparing(MockLoginLog::loggedInAt).reversed()).limit(3).toList();
                assertEquals(expectedReports, ((TableView<?>) root.lookup("#recentReports")).getItems());
                assertEquals(expectedLogins, ((TableView<?>) root.lookup("#recentLogins")).getItems());

                navigator.themes().toggle();
                capture(stage, "admin-wide-dark");

                var shortcuts = Map.of(
                        "#totalUsersCard", AdminViews.USERS, "#onlineUsersCard", AdminViews.USERS,
                        "#totalGroupsCard", AdminViews.GROUPS, "#pendingSpamCard", AdminViews.SPAM,
                        "#registrationsLink", AdminViews.REGISTRATIONS, "#activityLink", AdminViews.ACTIVITY,
                        "#allReportsLink", AdminViews.SPAM, "#allLoginsLink", AdminViews.LOGIN_LOGS);
                for (var shortcut : shortcuts.entrySet()) {
                    ((ButtonBase) root.lookup(shortcut.getKey())).fire();
                    assertEquals(shortcut.getValue(), navigator.currentViewProperty().get(), shortcut.getKey());
                    navigator.goBack();
                    assertEquals(AdminViews.OVERVIEW, navigator.currentViewProperty().get());
                }
            } finally {
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @Test
    void analyticsSelectMonthsAndFilterTheirOwnTables() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = AdminApplication.createNavigator();
            var root = navigator.getRoot();
            stage.setScene(new Scene(root, 1366, 768));
            stage.show();
            try {
                var data = MockData.create();
                navigator.navigate(AdminViews.REGISTRATIONS);
                root.applyCss();
                root.layout();
                var registrations = (TableView<?>) root.lookup("#registrationRows");
                assertEquals(12, ((BarChart<?, ?>) root.lookup("#monthlyChart")).getData().getFirst().getData().size());
                capture(stage, "admin-analytics-registrations-light");
                var year = (ComboBox<?>) root.lookup("#chartYear");
                year.getSelectionModel().select(year.getItems().indexOf(2025));
                ((Button) root.lookup("#month-5")).fire();
                var from = (DatePicker) root.lookup("#fromDate");
                var to = (DatePicker) root.lookup("#toDate");
                assertEquals(LocalDate.of(2025, 5, 1), from.getValue());
                assertEquals(LocalDate.of(2025, 5, 31), to.getValue());
                assertEquals(data.monthlyStatistics(2025).get(4).registrations(), registrations.getItems().size());
                assertEquals("Bảng đang lọc tháng 5/2025", ((Label) root.lookup("#monthHint")).getText());
                ((Button) root.lookup("#month-5")).fire();
                assertEquals(data.activityFrom(), from.getValue());
                assertEquals("Chọn tháng để lọc bảng", ((Label) root.lookup("#monthHint")).getText());
                from.setValue(to.getValue().plusDays(1));
                assertTrue(root.lookup("#filterError").isVisible());
                assertTrue(registrations.getItems().isEmpty());
                ((Button) root.lookup("#clearAnalyticsFilters")).fire();
                assertFalse(root.lookup("#filterError").isVisible());

                navigator.navigate(AdminViews.ACTIVITY);
                root.applyCss();
                root.layout();
                var trend = (LineChart<?, ?>) root.lookup("#monthlyChart");
                assertEquals(12, trend.getData().stream().mapToInt(series -> series.getData().size()).sum());
                assertEquals(0, trend.getData().getLast().getData().getLast().getYValue());
                ((Button) root.lookup("#month-11")).fire();
                var activity = (TableView<?>) root.lookup("#activityRows");
                assertEquals(data.monthlyStatistics(2026).get(10).activeUsers(), IntStream.range(0, activity.getItems().size())
                        .filter(index -> (Integer) activity.getColumns().get(3).getCellData(index) > 0).count());
                ((ComboBox<?>) root.lookup("#activityMetric")).getSelectionModel().select(2);
                ((ToggleButton) root.lookup("#greaterThan")).fire();
                ((TextField) root.lookup("#threshold")).setText("0");
                assertFalse(activity.getItems().isEmpty());
                assertTrue(IntStream.range(0, activity.getItems().size())
                        .allMatch(index -> (Integer) activity.getColumns().get(5).getCellData(index) > 0));
                navigator.themes().toggle();
                capture(stage, "admin-analytics-activity-dark");

                navigator.navigate(AdminViews.FRIEND_NETWORK);
                root.applyCss();
                root.layout();
                var network = (TableView<?>) root.lookup("#networkRows");
                var snapshot = List.copyOf(network.getItems());
                assertEquals(18, snapshot.size());
                var networkYear = (ComboBox<?>) root.lookup("#chartYear");
                networkYear.getSelectionModel().select(networkYear.getItems().indexOf(2025));
                assertEquals(snapshot, network.getItems(), "Changing the chart year leaves the current network intact");
                ((TextField) root.lookup("#threshold")).setText("0");
                assertTrue(network.getItems().size() > 0 && network.getItems().size() < snapshot.size());
                assertTrue(IntStream.range(0, network.getItems().size())
                        .allMatch(index -> (Integer) network.getColumns().get(3).getCellData(index) == 0));
                ((Button) root.lookup("#clearAnalyticsFilters")).fire();
                networkYear.getSelectionModel().select(networkYear.getItems().indexOf(2026));
                navigator.themes().toggle();
                capture(stage, "admin-analytics-friends-light");
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @Test
    void eachRegisteredViewLoadsItsOwnFxml() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            assertEquals(11, AdminViews.ALL.size());
            for (var view : AdminViews.ALL) {
                var resource = view.resourceOwner().getResource(view.resource());
                assertNotNull(resource, view.id());
                assertInstanceOf(Parent.class, new FXMLLoader(resource).load(), view.id());
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @Test
    void sidebarFitsTheMinimumContentSize() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = AdminApplication.createNavigator();
            navigator.reset(AdminViews.OVERVIEW);
            try {
                stage.setScene(new Scene(navigator.getRoot(), 1024, 640));
                stage.show();
                navigator.getRoot().applyCss();
                navigator.getRoot().layout();
                assertEquals(1024, stage.getScene().getWidth());
                assertEquals(640, stage.getScene().getHeight());
                var lastNavigation = navigator.getRoot().lookup("#nav-A-SCR-09");
                assertTrue(lastNavigation.localToScene(lastNavigation.getBoundsInLocal()).getMaxY() < 640);
                var overview = (ScrollPane) navigator.getRoot().lookup(".admin-overview");
                assertTrue(overview.getContent().getLayoutBounds().getWidth()
                        <= overview.getViewportBounds().getWidth() + 1);
                var activityLegend = overview.lookup("#activityLegend");
                assertTrue(activityLegend.isVisible() && activityLegend.getBoundsInParent().getWidth() > 0);
                capture(stage, "admin-small");

                ((ToggleButton) navigator.getRoot().lookup("#nav-A-SCR-06")).fire();
                navigator.getRoot().applyCss();
                navigator.getRoot().layout();
                var timeline = (TableView<?>) navigator.getRoot().lookup("#loginEvents");
                var bounds = timeline.localToScene(timeline.getBoundsInLocal());
                assertTrue(bounds.getMaxX() <= 1024 && bounds.getMaxY() <= 640);
                assertTrue(timeline.getVisibleLeafColumns().stream().mapToDouble(TableColumn::getWidth).sum()
                        <= timeline.getWidth());
                assertLoginTextAlignment(timeline, 0);
                capture(stage, "admin-login-logs-compact");

                navigator.navigate(MockDataPreviewController.VIEW);
                assertEquals(MockDataPreviewController.VIEW, navigator.currentViewProperty().get());
                navigator.getRoot().applyCss();
                navigator.getRoot().layout();
                assertEquals(20, ((TableView<?>) navigator.getRoot().lookup("#mockUsers")).getItems().size());
                capture(stage, "admin-mock-users-small");
                TabPane tabs = (TabPane) navigator.getRoot().lookup("#mockTabs");
                tabs.getSelectionModel().select(3);
                navigator.getRoot().applyCss();
                navigator.getRoot().layout();
                assertTrue(((TableView<?>) navigator.getRoot().lookup("#mockReports")).getHeight() >= 120);
                capture(stage, "admin-mock-spam-small");
                tabs.getSelectionModel().select(4);
                navigator.themes().toggle();
                navigator.getRoot().applyCss();
                navigator.getRoot().layout();
                capture(stage, "admin-mock-chart-small-dark");
                var months = navigator.getRoot().lookup("#mockMonths");
                assertTrue(months.localToScene(months.getBoundsInLocal()).getMaxY() < 640);

                navigator.navigate(AdminViews.ACTIVITY);
                navigator.getRoot().applyCss();
                navigator.getRoot().layout();
                var analytics = (ScrollPane) navigator.getRoot().lookup(".admin-analytics-view");
                assertTrue(analytics.getContent().getLayoutBounds().getWidth()
                        <= analytics.getViewportBounds().getWidth() + 1);
                var activity = (TableView<?>) analytics.lookup("#activityRows");
                var headers = activity.lookup(".column-header-background");
                double firstRowBottom = headers.localToScene(headers.getBoundsInLocal()).getMaxY()
                        + activity.getFixedCellSize();
                assertTrue(firstRowBottom <= stage.getScene().getHeight(), "The first activity row is visible");
                capture(stage, "admin-analytics-activity-compact");
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @Test
    void systemLoginTimelineShowsAllEventsInVietnamTime() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = AdminApplication.createNavigator();
            var root = navigator.getRoot();
            stage.setScene(new Scene(root, 1366, 768));
            stage.show();
            try {
                navigator.navigate(AdminViews.USERS);
                root.applyCss();
                root.layout();
                var data = MockData.create();
                var directory = (TableView<?>) root.lookup("#userTable");
                directory.getSelectionModel().select(directory.getItems().indexOf(data.previewUser()));
                root.applyCss();
                root.layout();
                ((Hyperlink) root.lookup("#systemLoginLogs")).fire();
                assertEquals(AdminViews.LOGIN_LOGS, navigator.currentViewProperty().get());

                var timeline = (TableView<?>) root.lookup("#loginEvents");
                var events = data.loginLogs().stream()
                        .sorted(Comparator.comparing(MockLoginLog::loggedInAt).reversed()).toList();
                assertEquals(events, timeline.getItems());
                assertEquals(events.size() + " lượt đăng nhập", ((Label) root.lookup("#loginCount")).getText());
                var columns = timeline.getColumns();
                assertEquals(3, columns.size());
                assertFalse(timeline.isEditable());
                assertTrue(columns.stream().noneMatch(TableColumn::isSortable));
                var latestAccount = data.user(events.getFirst().userId());
                assertEquals("15/11/2026 09:00", columns.get(0).getCellData(0));
                assertEquals(latestAccount.username(), columns.get(1).getCellData(0));
                assertEquals(latestAccount.fullName(), columns.get(2).getCellData(0));
                assertLoginTextAlignment(timeline, 0);
                capture(stage, "admin-login-logs-light");
                navigator.themes().toggle();
                assertLoginTextAlignment(timeline, 0);
                capture(stage, "admin-login-logs-dark");

                timeline.getSelectionModel().selectFirst();
                timeline.getFocusModel().focus(0);
                timeline.requestFocus();
                assertLoginTextAlignment(timeline, 0);
                capture(stage, "admin-login-logs-focus-dark");

                navigator.goBack();
                assertEquals(AdminViews.USERS, navigator.currentViewProperty().get());
                assertEquals(data.previewUser(), directory.getSelectionModel().getSelectedItem());
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @Test
    void userFiltersKeepTheInspectorOnTheSelectedAccount() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = AdminApplication.createNavigator();
            navigator.reset(AdminViews.OVERVIEW);
            var root = navigator.getRoot();
            stage.setScene(new Scene(root, 1366, 768));
            stage.show();
            try {
                ((Button) root.lookup("#totalUsersCard")).fire();
                root.applyCss();
                root.layout();
                var data = MockData.create();
                var selected = data.previewUser();
                @SuppressWarnings("unchecked")
                var directory = (TableView<MockUser>) root.lookup("#userTable");
                var inspector = (ScrollPane) root.lookup("#userInspector");
                assertFalse(inspector.isVisible());
                directory.getSelectionModel().select(directory.getItems().indexOf(selected));
                assertTrue(inspector.isVisible());
                assertEquals(selected.fullName(), ((Label) root.lookup("#profileName")).getText());
                root.applyCss();
                root.layout();
                directory.scrollTo(Math.max(0, directory.getSelectionModel().getSelectedIndex() - 3));
                capture(stage, "admin-users-light");
                assertEquals(data.loginLogs().stream().filter(login -> login.userId().equals(selected.id())).toList(),
                        ((TableView<?>) root.lookup("#loginHistory")).getItems());

                ((TextField) root.lookup("#nameFilter")).setText("  NGUYỄN  ");
                ((TextField) root.lookup("#usernameFilter")).setText("MINHAN");
                assertEquals(List.of(selected), directory.getItems());
                assertEquals(selected, directory.getSelectionModel().getSelectedItem());
                ((ToggleButton) root.lookup("#lockedUsers")).fire();
                assertTrue(directory.getItems().isEmpty());
                assertFalse(inspector.isVisible());
                ((Hyperlink) root.lookup("#clearFilters")).fire();
                assertEquals(data.users().size(), directory.getItems().size());
                directory.getSelectionModel().select(selected);
                directory.scrollTo(Math.max(0, directory.getSelectionModel().getSelectedIndex() - 3));
                ((TabPane) root.lookup("#userDetails")).getSelectionModel().select(1);
                navigator.themes().toggle();
                root.applyCss();
                root.layout();
                capture(stage, "admin-users-dark");
                var friends = (TableView<?>) root.lookup("#friends");
                assertFalse(friends.getItems().isEmpty());
                assertFalse(friends.getItems().contains(selected));
                var created = directory.getColumns().getLast();
                created.setSortType(TableColumn.SortType.DESCENDING);
                directory.getSortOrder().setAll(List.of(created));
                assertEquals(data.users().stream().map(MockUser::createdAt).sorted(Comparator.reverseOrder()).toList(),
                        directory.getItems().stream().map(MockUser::createdAt).toList());
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @Test
    void userDialogsUseTheAccountFromTheirRouteInput() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = AdminApplication.createNavigator();
            var root = navigator.getRoot();
            stage.setScene(new Scene(root, 1024, 640));
            stage.show();
            try {
                navigator.navigate(AdminViews.USERS);
                root.applyCss();
                root.layout();
                var data = MockData.create();
                var selected = data.previewUser();
                var directory = (TableView<?>) root.lookup("#userTable");
                directory.getSelectionModel().select(directory.getItems().indexOf(selected));
                directory.scrollTo(directory.getSelectionModel().getSelectedIndex());
                root.applyCss();
                root.layout();
                capture(stage, "admin-users-compact");
                assertTrue(root.lookup("#userInspector").localToScene(
                        root.lookup("#userInspector").getBoundsInLocal()).getMaxX() <= 1024);
                var actions = (FlowPane) root.lookup(".admin-profile-actions");
                assertTrue(actions.getChildren().stream().allMatch(
                        action -> action.getBoundsInParent().getMaxX() <= actions.getWidth() + 1));

                ((Button) root.lookup("#createUser")).fire();
                var form = ownedDialogPane(stage);
                assertTrue(((TextField) form.lookup("#username")).isEditable());
                assertTrue(form.lookup("#submitUserDialog").isDisabled());
                capture(form.getScene(), "admin-user-create");
                ((Button) form.lookup("#cancelUserDialog")).fire();

                ((Button) root.lookup("#editUser")).fire();
                form = ownedDialogPane(stage);
                var username = (TextField) form.lookup("#username");
                assertFalse(username.isEditable());
                assertEquals(selected.username(), username.getText());
                assertFalse(form.lookup("#submitUserDialog").isDisabled());
                capture(form.getScene(), "admin-user-edit");
                ((Button) form.lookup("#cancelUserDialog")).fire();

                ((ToggleButton) root.lookup("#deletedUsers")).fire();
                directory.getSelectionModel().selectFirst();
                assertTrue(root.lookup("#editUser").isDisabled());
                var notice = (Label) root.lookup("#deletionNotice");
                assertTrue(notice.isVisible());
                root.applyCss();
                root.layout();
                assertTrue(notice.getHeight() + 1 >= notice.prefHeight(notice.getWidth()),
                        "The deletion notice fits both timestamps");
                capture(stage, "admin-users-deleted-compact");
                ((Button) root.lookup("#deleteOrRestore")).fire();
                var restore = ownedDialogPane(stage);
                assertEquals("Khôi phục tài khoản?", ((Label) restore.lookup("#confirmationTitle")).getText());
                ((Button) restore.lookup("#cancelUserDialog")).fire();

                navigator.navigate(AdminViews.SPAM);
                navigator.themes().toggle();
                var reported = data.user(data.spamReports().getFirst().reportedUserId());
                var dialog = navigator.showModal(AdminViews.CONFIRM_ACTION,
                        new AdminViewInputs.ConfirmAction(AdminViewInputs.Action.LOCK_USER, reported.id()));
                var pane = dialog.getDialogPane();
                assertEquals("@" + reported.username(), ((Label) pane.lookup("#account")).getText());
                assertTrue(((Label) pane.lookup("#accountId")).getText().endsWith(reported.id()));
                capture(pane.getScene(), "admin-user-confirm-lock");
                var confirm = (Button) pane.lookup("#submitUserDialog");
                assertFalse(confirm.isDefaultButton());
                confirm.fire();
                assertEquals(ButtonBar.ButtonData.OK_DONE, dialog.getResult().getButtonData());
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @Test
    void groupsExplorerFiltersAndShowsMembers() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = AdminApplication.createNavigator();
            navigator.reset(AdminViews.OVERVIEW);
            var root = navigator.getRoot();
            stage.setScene(new Scene(root, 1366, 768));
            stage.show();
            try {
                ((Button) root.lookup("#totalGroupsCard")).fire();
                root.applyCss();
                root.layout();
                assertEquals(AdminViews.GROUPS, navigator.currentViewProperty().get());

                var data = MockData.create();
                @SuppressWarnings("unchecked")
                var directory = (TableView<MockGroup>) root.lookup("#groupsTable");
                var inspector = (ScrollPane) root.lookup("#groupInspector");
                var emptyInspector = (Node) root.lookup("#unselectedGroup");
                var countLabel = (Label) root.lookup("#groupCount");
                var nameFilter = (TextField) root.lookup("#groupNameFilter");
                var clearFilter = (Hyperlink) root.lookup("#clearGroupFilter");

                assertEquals(data.groups().size(), directory.getItems().size());
                assertEquals(data.groups().size() + " nhóm chat", countLabel.getText());
                assertFalse(inspector.isVisible());
                assertTrue(emptyInspector.isVisible());

                // Filter by name
                nameFilter.setText("Java");
                root.applyCss();
                root.layout();
                assertEquals(1, directory.getItems().size());
                assertEquals("Nhóm đồ án Java", directory.getItems().getFirst().name());
                assertEquals("1 nhóm chat", countLabel.getText());
                assertTrue(clearFilter.isVisible());

                // Clear filter
                clearFilter.fire();
                root.applyCss();
                root.layout();
                assertEquals(data.groups().size(), directory.getItems().size());

                // Select first group
                directory.getSelectionModel().select(0);
                root.applyCss();
                root.layout();
                assertTrue(inspector.isVisible());
                assertFalse(emptyInspector.isVisible());

                var selectedGroup = directory.getSelectionModel().getSelectedItem();
                assertEquals("Nhóm đồ án Java", ((Label) root.lookup("#groupTitle")).getText());
                assertEquals("Mã nhóm: " + selectedGroup.id(), ((Label) root.lookup("#groupId")).getText());
                assertEquals(selectedGroup.members().size() + " thành viên",
                        ((Label) root.lookup("#groupMemberCountBadge")).getText());

                var adminTable = (TableView<?>) root.lookup("#adminTable");
                var membersTable = (TableView<?>) root.lookup("#membersTable");
                assertEquals(2, adminTable.getItems().size());
                assertEquals(6, membersTable.getItems().size());
                assertEquals("2 quản trị viên", ((Label) root.lookup("#adminCount")).getText());
                assertEquals("6 thành viên thường", ((Label) root.lookup("#memberCount")).getText());
                assertTrue(adminTable.lookupAll(".admin-role-badge").stream()
                        .filter(Label.class::isInstance).map(Label.class::cast)
                        .anyMatch(role -> role.getText().equals("Admin nhóm")));

                capture(stage, "admin-groups-explorer-light");
                navigator.themes().toggle();
                capture(stage, "admin-groups-explorer-dark");
                navigator.themes().toggle();
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @Test
    void spamModerationInspectsEvidenceAndExecutesActions() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = AdminApplication.createNavigator();
            navigator.reset(AdminViews.OVERVIEW);
            var root = navigator.getRoot();
            stage.setScene(new Scene(root, 1366, 768));
            stage.show();
            try {
                ((Button) root.lookup("#pendingSpamCard")).fire();
                root.applyCss();
                root.layout();
                assertEquals(AdminViews.SPAM, navigator.currentViewProperty().get());

                var data = MockData.create();
                @SuppressWarnings("unchecked")
                var directory = (TableView<MockSpamReport>) root.lookup("#reportsTable");
                var inspector = (ScrollPane) root.lookup("#reportInspector");
                var emptyInspector = (Node) root.lookup("#unselectedReport");
                var countLabel = (Label) root.lookup("#reportCount");
                var clearFilters = (Hyperlink) root.lookup("#clearSpamFilters");

                assertEquals(data.spamReports().size(), directory.getItems().size());
                assertEquals(data.spamReports().size() + " báo cáo", countLabel.getText());
                assertFalse(inspector.isVisible());
                assertTrue(emptyInspector.isVisible());

                // Filter by pending
                var pendingToggle = (ToggleButton) root.lookup("#pendingReports");
                pendingToggle.fire();
                root.applyCss();
                root.layout();
                long expectedPending = data.spamReports().stream()
                        .filter(r -> r.status() == MockSpamReport.Status.PENDING).count();
                assertEquals(expectedPending, directory.getItems().size());

                // Clear filter
                clearFilters.fire();
                root.applyCss();
                root.layout();
                assertEquals(data.spamReports().size(), directory.getItems().size());
                pendingToggle.fire();

                // Choose an ordinary active account, not the system Admin reported in another fixture.
                int pendingIdx = IntStream.range(0, directory.getItems().size())
                        .filter(i -> {
                            var user = data.user(directory.getItems().get(i).reportedUserId());
                            return user.role() == MockUser.Role.USER && user.status() == MockUser.Status.ACTIVE;
                        })
                        .findFirst().orElseThrow();
                directory.getSelectionModel().select(pendingIdx);
                root.applyCss();
                root.layout();
                assertTrue(inspector.isVisible());
                assertFalse(emptyInspector.isVisible());

                var selectedReport = directory.getSelectionModel().getSelectedItem();
                assertEquals("Báo cáo #" + selectedReport.id(), ((Label) root.lookup("#reportIdTitle")).getText());
                assertEquals("Chờ xử lý", ((Label) root.lookup("#reportStatusBadge")).getText());

                var lockButton = (Button) root.lookup("#lockReportedUser");
                assertTrue(lockButton.isVisible());
                assertFalse(lockButton.isDisable());

                var previewList = (VBox) root.lookup("#chatPreviewList");
                assertEquals(selectedReport.evidence().size(), previewList.getChildren().size());
                int dualIndex = IntStream.range(0, selectedReport.evidence().size())
                        .filter(i -> selectedReport.evidence().get(i).selected()
                                && selectedReport.evidence().get(i).context())
                        .findFirst().orElseThrow();
                var dualEvidence = previewList.getChildren().get(dualIndex);
                assertNotNull(dualEvidence.lookup(".tag-selected"), "The selected message tag is shown");
                assertNotNull(dualEvidence.lookup(".tag-context"), "The same evidence row also shows its context tag");
                capture(stage, "admin-spam-pending-light");

                // Cancel leaves the reported account and the report unchanged.
                lockButton.fire();
                var confirmDialog = ownedDialogPane(stage);
                var preservation = (Label) confirmDialog.lookup("#preservationNotice");
                assertTrue(preservation.isVisible());
                assertTrue(preservation.getText().contains("bằng chứng"));
                capture(confirmDialog.getScene(), "admin-spam-confirm-lock");
                ((Button) confirmDialog.lookup("#cancelUserDialog")).fire();
                assertEquals("Hoạt động", ((Label) root.lookup("#reportedUserStatus")).getText());
                assertEquals("Chờ xử lý", ((Label) root.lookup("#reportStatusBadge")).getText());

                lockButton.fire();
                ((Button) ownedDialogPane(stage).lookup("#submitUserDialog")).fire();

                root.applyCss();
                root.layout();
                assertEquals("Bị khóa", ((Label) root.lookup("#reportedUserStatus")).getText());
                assertTrue(lockButton.isDisable());
                assertEquals("Tài khoản đã bị khóa", lockButton.getText());
                assertEquals("Chờ xử lý", ((Label) root.lookup("#reportStatusBadge")).getText());
                assertEquals(expectedPending, directory.getItems().size());

                // Test mark reviewed action (DEC-10)
                var markReviewedBtn = (Button) root.lookup("#markReviewed");
                var noteInput = (TextField) root.lookup("#reviewNoteField");
                var noteError = (Label) root.lookup("#reviewNoteError");

                // Empty note validation
                markReviewedBtn.fire();
                root.applyCss();
                root.layout();
                assertTrue(noteError.isVisible());

                // Non-empty note
                String note = "Đã xem bằng chứng đính kèm và khóa tài khoản";
                noteInput.setText(note);
                markReviewedBtn.fire();
                root.applyCss();
                root.layout();

                assertEquals(expectedPending - 1, directory.getItems().size());
                assertEquals((expectedPending - 1) + " báo cáo", countLabel.getText());
                assertTrue(directory.getItems().stream().noneMatch(report -> report.id().equals(selectedReport.id())));
                assertTrue(emptyInspector.isVisible());

                ((ToggleButton) root.lookup("#reviewedReports")).fire();
                var reviewed = directory.getItems().stream()
                        .filter(report -> report.id().equals(selectedReport.id())).findFirst().orElseThrow();
                directory.getSelectionModel().select(reviewed);
                root.applyCss();
                root.layout();

                assertEquals("Đã xử lý", ((Label) root.lookup("#reportStatusBadge")).getText());
                var reviewedBanner = (Node) root.lookup(".admin-reviewed-banner");
                assertNotNull(reviewedBanner);
                assertTrue(reviewedBanner.isVisible());
                assertTrue(((Label) root.lookup("#reviewedNoteText")).getText().contains(note));

                capture(stage, "admin-spam-moderation-light");
                navigator.themes().toggle();
                capture(stage, "admin-spam-moderation-dark");
                navigator.themes().toggle();
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @Test
    void spamFiltersByUsernameAndValidatesTypedVietnameseDates() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            var locale = Locale.getDefault(Locale.Category.FORMAT);
            Locale.setDefault(Locale.Category.FORMAT, Locale.of("en", "VN"));
            Stage stage = new Stage();
            try (var navigator = AdminApplication.createNavigator()) {
                navigator.reset(AdminViews.OVERVIEW);
                navigator.navigate(AdminViews.SPAM);
                var root = navigator.getRoot();
                stage.setScene(new Scene(root, 1024, 640));
                stage.show();
                root.applyCss();
                root.layout();
                @SuppressWarnings("unchecked")
                var directory = (TableView<MockSpamReport>) root.lookup("#reportsTable");
                var search = (TextField) root.lookup("#reportedUserFilter");
                var start = (DatePicker) root.lookup("#startDateFilter");
                var end = (DatePicker) root.lookup("#endDateFilter");
                var error = (Label) root.lookup("#spamDateError");
                var data = MockData.create();
                var account = data.user(data.spamReports().getFirst().reportedUserId());

                search.setText(account.fullName());
                assertTrue(directory.getItems().isEmpty(), "The filter does not search full names");
                search.setText(" " + account.username().toUpperCase(Locale.ROOT) + " ");
                var byUsername = data.spamReports().stream()
                        .filter(report -> data.user(report.reportedUserId()).username().contains(account.username()))
                        .toList();
                assertEquals(byUsername, directory.getItems());
                search.clear();

                var date = data.spamReports().getFirst().createdAt().atZone(MockData.DISPLAY_ZONE).toLocalDate();
                var formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT);
                start.getEditor().setText(formatter.format(date));
                start.commitValue();
                end.getEditor().setText(formatter.format(date));
                end.commitValue();
                assertEquals(date, start.getValue());
                assertEquals(date, end.getValue());
                assertEquals(formatter.format(date), start.getEditor().getText());
                assertFalse(error.isVisible());
                var sameDate = data.spamReports().stream()
                        .filter(report -> report.createdAt().atZone(MockData.DISPLAY_ZONE).toLocalDate().equals(date))
                        .toList();
                assertEquals(sameDate, directory.getItems(), "The end date includes the entire selected local day");

                start.getEditor().setText(formatter.format(date.plusDays(1)));
                assertTrue(error.isVisible(), "A reversed range reports an error instead of an empty result");
                assertEquals(sameDate, directory.getItems(), "Invalid dates do not replace the last valid results");

                var editor = start.getEditor();
                editor.requestFocus();
                editor.setText("29/02/2026");
                assertTrue(error.isVisible(), "A non-existent day is not normalized to another date");
                editor.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER,
                        false, false, false, false));
                editor.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.ENTER,
                        false, false, false, false));
                search.requestFocus();
                assertSame(search, stage.getScene().getFocusOwner());
                assertTrue(error.isVisible(), "Invalid text remains visible after Enter and moving focus");
                assertEquals("29/02/2026", editor.getText());
                assertEquals(date, start.getValue(), "Neither entering nor leaving invalid text changes the date");
                assertEquals(sameDate, directory.getItems());

                ((Hyperlink) root.lookup("#clearSpamFilters")).fire();
                assertFalse(error.isVisible());
                assertEquals(data.spamReports(), directory.getItems());
            } finally {
                stage.close();
                Locale.setDefault(Locale.Category.FORMAT, locale);
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @Test
    void moderationFitsCompactWindowAndProtectsSystemAdmin() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = AdminApplication.createNavigator();
            var root = navigator.getRoot();
            stage.setScene(new Scene(root, 1024, 640));
            stage.show();
            try {
                navigator.reset(AdminViews.OVERVIEW);
                navigator.navigate(AdminViews.GROUPS);
                root.applyCss();
                root.layout();
                @SuppressWarnings("unchecked")
                var groups = (TableView<MockGroup>) root.lookup("#groupsTable");
                groups.getSelectionModel().selectFirst();
                var selectedGroup = groups.getSelectionModel().getSelectedItem();
                assertTrue(groups.getColumns().stream().noneMatch(TableColumn::isSortable));
                @SuppressWarnings("unchecked")
                var sort = (ComboBox<String>) root.lookup("#groupSortOrder");
                sort.getSelectionModel().select("Tên nhóm A–Z");
                var alphabetical = Comparator.comparing(MockGroup::name, Collator.getInstance(Locale.of("vi", "VN")));
                var data = MockData.create();
                assertEquals(data.groups().stream().sorted(alphabetical).toList(), groups.getItems());
                assertEquals(selectedGroup, groups.getSelectionModel().getSelectedItem());
                root.applyCss();
                root.layout();
                assertVisibleColumnsFit(groups);
                capture(stage, "admin-groups-explorer-compact");

                navigator.navigate(AdminViews.SPAM);
                root.applyCss();
                root.layout();
                @SuppressWarnings("unchecked")
                var directory = (TableView<MockSpamReport>) root.lookup("#reportsTable");
                directory.getSelectionModel().select(directory.getItems().stream()
                        .filter(report -> data.user(report.reportedUserId()).role() == MockUser.Role.ADMIN)
                        .findFirst().orElseThrow());
                root.applyCss();
                root.layout();
                assertVisibleColumnsFit(directory);
                assertTrue(directory.getVisibleLeafColumns().stream()
                        .anyMatch(column -> column.getText().equals("Trạng thái")), "Report status remains visible");

                var lock = (Button) root.lookup("#lockReportedUser");
                assertTrue(lock.isDisabled(), "A system Admin cannot be locked via a report");
                assertEquals("Hoạt động", ((Label) root.lookup("#reportedUserStatus")).getText());
                lock.fire();
                assertTrue(Window.getWindows().stream().noneMatch(window -> window instanceof Stage dialog
                        && dialog.getOwner() == stage && dialog.getScene().getRoot() instanceof DialogPane));

                var inspector = (ScrollPane) root.lookup("#reportInspector");
                assertTrue(inspector.getContent().getLayoutBounds().getWidth()
                        <= inspector.getViewportBounds().getWidth() + 1);
                assertWrappedTextFits((Label) root.lookup("#reportReason"));
                var selectedBubble = root.lookup(".bubble-selected");
                assertWrappedTextFits((Label) selectedBubble.lookup(".admin-bubble-text"));
                capture(stage, "admin-spam-moderation-compact");
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    private static void assertVisibleColumnsFit(TableView<?> table) {
        double width = table.getVisibleLeafColumns().stream().mapToDouble(TableColumn::getWidth).sum();
        assertTrue(width <= table.getWidth() + 1, () -> "Columns need " + width + " px, table has " + table.getWidth());
    }

    private static void assertWrappedTextFits(Label label) {
        assertTrue(label.isWrapText() && label.getWidth() > 0 && label.getHeight() > 0);
        assertTrue(label.getHeight() >= label.prefHeight(label.getWidth()) - 1,
                () -> "Not enough room for the full text: " + label.getText());
    }

    private static void assertLoginTextAlignment(TableView<?> timeline, int rowIndex) {
        var root = timeline.getScene().getRoot();
        root.applyCss();
        root.layout();
        var row = timeline.lookupAll(".table-row-cell").stream()
                .filter(node -> node instanceof TableRow<?> candidate && candidate.getIndex() == rowIndex)
                .findFirst().orElseThrow();
        var headers = textLeftEdges(timeline, ".column-header .label .text");
        assertEquals(timeline.getVisibleLeafColumns().size(), headers.size());
        assertEquals(headers, textLeftEdges(row, ".table-cell .text"), "Headers and cells share their text origins");
    }

    private static List<Double> textLeftEdges(Node parent, String selector) {
        return parent.lookupAll(selector).stream()
                .map(text -> text.localToScene(text.getLayoutBounds()).getMinX())
                .sorted().toList();
    }

    private static DialogPane ownedDialogPane(Stage owner) {
        return Window.getWindows().stream().filter(Stage.class::isInstance).map(Stage.class::cast)
                .filter(stage -> stage.getOwner() == owner).map(stage -> (DialogPane) stage.getScene().getRoot())
                .findFirst().orElseThrow();
    }

    private static void capture(Stage stage, String name) throws Exception {
        capture(stage.getScene(), name);
    }

    private static void capture(Scene scene, String name) throws Exception {
        if (!Boolean.getBoolean("chatsystem.capture")) {
            return;
        }
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        WritableImage image = scene.snapshot(null);
        BufferedImage output = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < output.getHeight(); y++) {
            for (int x = 0; x < output.getWidth(); x++) {
                output.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        Path directory = Path.of("target", "screenshots");
        Files.createDirectories(directory);
        ImageIO.write(output, "png", directory.resolve(name + ".png").toFile());
    }
}
