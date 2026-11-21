package com.chatsystem.admin;

import static org.junit.jupiter.api.Assertions.*;

import atlantafx.base.controls.Popover;
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
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
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

    private static void capture(Stage stage, String name) throws Exception {
        if (!Boolean.getBoolean("chatsystem.capture")) {
            return;
        }
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        WritableImage image = stage.getScene().snapshot(null);
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
