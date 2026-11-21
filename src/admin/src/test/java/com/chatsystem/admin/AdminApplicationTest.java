package com.chatsystem.admin;

import static org.junit.jupiter.api.Assertions.*;

import atlantafx.base.controls.Popover;
import com.chatsystem.admin.navigation.AdminViewInputs;
import com.chatsystem.desktop.DesktopWindow;
import com.chatsystem.desktop.controller.MockDataPreviewController;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockLoginLog;
import com.chatsystem.desktop.mock.model.MockMonthlyStatistics;
import com.chatsystem.desktop.mock.model.MockSpamReport;
import com.chatsystem.desktop.mock.model.MockUser;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
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
    void sidebarOpensEveryRouteAndKeepsItsSelectionWhenGoingBack() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = AdminApplication.createNavigator();
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
