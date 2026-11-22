package com.chatsystem.user;

import static org.junit.jupiter.api.Assertions.*;

import atlantafx.base.controls.ToggleSwitch;
import com.chatsystem.desktop.controller.MockDataPreviewController;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockGroup;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockMonthlyStatistics;
import com.chatsystem.desktop.mock.model.MockSpamReport;
import com.chatsystem.desktop.mock.model.MockUser;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.chart.BarChart;
import javafx.scene.image.WritableImage;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "chatsystem.uiTests", matches = "true")
class UserApplicationTest {
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
    void shellLoadsAndButtonsNavigateWithBothThemes() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            UserApplication app = new UserApplication();
            app.start(stage);
            try {
                var root = stage.getScene().getRoot();
                enterPreviewWorkspace(root);
                root.applyCss();
                root.layout();
                assertTrue(((ToggleButton) root.lookup("#navChats")).isSelected());
                assertTrue(root.lookup("#emptyWorkspace").isVisible());
                capture(stage, "user-light");
                ((ToggleButton) root.lookup("#navContacts")).fire();
                assertTrue(((ToggleButton) root.lookup("#navContacts")).isSelected());
                assertFalse(((Button) root.lookup("#goBack")).isDisabled());
                ((Button) root.lookup("#goBack")).fire();
                assertTrue(((ToggleButton) root.lookup("#navChats")).isSelected());
                var settings = (Button) root.lookup("#workspaceSettings");
                settings.fire();
                var settingsMenu = settingsPopup();
                var settingsContent = (javafx.scene.layout.VBox) settingsMenu.getScene().getRoot().lookup("#settingsContent");
                var darkModeSwitch = (ToggleSwitch) settingsContent.lookup("#darkModeSwitch");
                assertSame(darkModeSwitch, settingsContent.getChildren().get(1));
                assertEquals("openProfile", settingsContent.getChildren().get(2).getId());
                assertFalse(darkModeSwitch.isSelected());
                settingsContent.applyCss();
                assertEquals(Color.WHITE, settingsContent.getBackground().getFills().getFirst().getFill());
                capture(settingsMenu, "settings-light");
                darkModeSwitch.fire();
                assertTrue(settingsMenu.isShowing());
                settingsMenu.hide();
                settings.fire();
                assertTrue(darkModeSwitch.isSelected());
                settingsContent.applyCss();
                assertEquals(Color.web("#1f1f1f"), settingsContent.getBackground().getFills().getFirst().getFill());
                capture(settingsPopup(), "settings-dark");
                darkModeSwitch.fire();
                assertFalse(root.getPseudoClassStates().contains(javafx.css.PseudoClass.getPseudoClass("dark")));
                darkModeSwitch.fire();
                settingsPopup().hide();
                assertTrue(root.getPseudoClassStates().contains(javafx.css.PseudoClass.getPseudoClass("dark")));
                root.applyCss();
                assertEquals(Color.web("#1f1f1f"), ((Region) root.lookup("#conversationSidebar"))
                        .getBackground().getFills().getFirst().getFill());
                capture(stage, "user-dark");
                var conversations = (ListView<?>) root.lookup("#conversationItems");
                ((ToggleButton) root.lookup("#groupFilter")).fire();
                assertTrue(conversations.getItems().stream().allMatch(item -> ((MockConversation) item).encrypted()));
                ((ToggleButton) root.lookup("#allFilter")).fire();
                var filter = (TextField) root.lookup("#conversationFilter");
                filter.setText("không có kết quả");
                assertTrue(conversations.getItems().isEmpty());
                filter.clear();
                conversations.getSelectionModel().selectFirst();
                assertFalse(root.lookup("#emptyWorkspace").isVisible());
                assertFalse(root.lookupAll(".chat-message-row").isEmpty());
                assertTrue(root.lookup("#sendMessage").isDisabled());
                ((Button) root.lookup("#conversationInfo")).fire();
                assertNotNull(root.lookup("#drawer"));
                capture(stage, "user-drawer");
                ((Button) root.lookup("#conversationInfo")).fire();
                assertNull(root.lookup("#drawer"));
                ((Button) root.lookup("#conversationInfo")).fire();
                assertNotNull(root.lookup("#drawer"));
                ((Button) root.lookup("#drawerClose")).fire();
                assertNull(root.lookup("#drawer"));
                fireSetting(root, "openProfile");
                assertTrue(javafx.stage.Window.getWindows().size() > 1);
                capture(Window.getWindows().stream().filter(window -> window != stage).findFirst().orElseThrow(),
                        "user-modal");
                ((ToggleButton) root.lookup("#navContacts")).fire();
                assertEquals(1, javafx.stage.Window.getWindows().size());
                fireSetting(root, "displayOptions");
                Window popup = Window.getWindows().stream()
                        .filter(window -> window.getScene().getRoot().lookup("#lightTheme") != null)
                        .findFirst().orElseThrow();
                capture(popup, "user-popover");
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
            assertEquals(14, UserViews.ALL.size());
            for (var view : UserViews.ALL) {
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
    void shellFitsTheMinimumContentSize() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = UserApplication.createNavigator();
            try {
                var root = navigator.getRoot();
                var scene = new Scene(root, 1024, 640);
                stage.setScene(scene);
                root.resize(1024, 640);
                enterPreviewWorkspace(root);
                root.applyCss();
                root.layout();
                assertEquals(280, ((Region) root.lookup("#conversationSidebar")).getWidth());
                assertEquals(672, ((Region) root.lookup("#chatWorkspace")).getWidth());
                assertTrue(root.lookup("#conversationItems").lookupAll(".scroll-bar").stream()
                        .map(ScrollBar.class::cast)
                        .noneMatch(bar -> bar.getOrientation() == Orientation.HORIZONTAL && bar.isVisible()));
                capture(stage, "user-small");
                ((ListView<?>) root.lookup("#conversationItems")).getSelectionModel().selectFirst();
                ((Button) root.lookup("#conversationInfo")).fire();
                root.applyCss();
                root.layout();
                assertEquals(320, ((Region) root.lookup("#drawer")).getWidth());
                assertEquals(352, ((Region) root.lookup("#chatWorkspace")).getWidth());
                capture(stage, "user-small-drawer");
                navigator.themes().toggle();
                capture(stage, "user-small-drawer-dark");
                navigator.closeDrawer();
                root.resize(1280, 720);
                root.layout();
                assertEquals(384, ((Region) root.lookup("#conversationSidebar")).getWidth());
                assertEquals(824, ((Region) root.lookup("#chatWorkspace")).getWidth());
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
    @SuppressWarnings("unchecked")
    void mockPreviewBindsRecordsAndUpdatesListsAndTwelveMonthChart() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = UserApplication.createNavigator();
            try {
                stage.setScene(new Scene(navigator.getRoot(), 1280, 720));
                stage.show();
                var root = navigator.getRoot();
                enterPreviewWorkspace(root);
                root.applyCss();
                root.layout();
                fireSetting(root, "openMockData");
                assertEquals(MockDataPreviewController.VIEW, navigator.currentViewProperty().get());
                root.applyCss();
                root.layout();
                var users = (TableView<MockUser>) root.lookup("#mockUsers");
                assertEquals(20, users.getItems().size());
                assertEquals("minhan", users.getColumns().getFirst().getCellObservableValue(0).getValue());
                users.getSortOrder().add(users.getColumns().get(1));
                assertEquals("minhan", MockData.create().users().getFirst().username());
                capture(stage, "user-mock-users");
                TabPane tabs = (TabPane) root.lookup("#mockTabs");
                assertEquals(5, tabs.getTabs().size());
                tabs.getSelectionModel().select(1);
                root.applyCss();
                root.layout();
                var choices = (ComboBox<MockConversation>) root.lookup("#mockConversation");
                var messages = (ListView<MockMessage>) root.lookup("#mockMessages");
                assertEquals(56, messages.getItems().size());
                capture(stage, "user-mock-messages");
                choices.getSelectionModel().selectFirst();
                assertEquals(8, messages.getItems().size());
                assertEquals("direct-01", messages.getItems().getFirst().conversationId());
                tabs.getSelectionModel().select(2);
                root.applyCss();
                root.layout();
                var groups = (TableView<MockGroup>) root.lookup("#mockGroups");
                assertEquals(4, groups.getItems().size());
                assertEquals(8, groups.getItems().getFirst().members().size());
                tabs.getSelectionModel().select(3);
                root.applyCss();
                root.layout();
                var reports = (TableView<MockSpamReport>) root.lookup("#mockReports");
                var evidence = (ListView<MockSpamReport.Evidence>) root.lookup("#mockEvidence");
                assertEquals(6, reports.getItems().size());
                assertEquals(31, evidence.getItems().size());
                reports.getSelectionModel().selectLast();
                assertEquals(reports.getSelectionModel().getSelectedItem().evidence(), evidence.getItems());
                tabs.getSelectionModel().select(4);
                root.applyCss();
                root.layout();
                var chart = (BarChart<String, Number>) root.lookup("#mockChart");
                var months = (TableView<MockMonthlyStatistics>) root.lookup("#mockMonths");
                var year = (ComboBox<Integer>) root.lookup("#mockYear");
                var metric = (ComboBox<String>) root.lookup("#mockMetric");
                assertEquals(12, chart.getData().getFirst().getData().size());
                assertEquals(12, months.getItems().size());
                assertEquals(0, chart.getData().getFirst().getData().getLast().getYValue());
                year.setValue(2025);
                metric.getSelectionModel().select(1);
                assertEquals(YearMonth.of(2025, 12), months.getItems().getLast().month());
                assertEquals(8, chart.getData().getFirst().getData().getLast().getYValue());
                year.setValue(2026);
                navigator.themes().toggle();
                capture(stage, "user-mock-chart-dark");
                ((Button) root.lookup("#goBack")).fire();
                assertEquals(UserViews.CONVERSATIONS, navigator.currentViewProperty().get());
                fireSetting(root, "openMockData");
                assertSame(tabs, root.lookup("#mockTabs"));
                assertEquals(4, tabs.getSelectionModel().getSelectedIndex());
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(30, TimeUnit.SECONDS);
    }

    @Test
    @SuppressWarnings("unchecked")
    void conversationSearchAndGroupDrawerRenderInBothThemes() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = UserApplication.createNavigator();
            try {
                stage.setScene(new Scene(navigator.getRoot(), 1280, 720));
                stage.show();
                var root = navigator.getRoot();
                enterPreviewWorkspace(root);
                root.applyCss();
                root.layout();

                var filter = (TextField) root.lookup("#conversationFilter");
                filter.setText("giao diện");
                root.applyCss();
                root.layout();
                assertTrue(root.lookup("#globalSearch").isVisible());
                assertNotNull(root.lookup("#globalConversationHeading"));
                assertNotNull(root.lookup("#globalMessageHeading"));
                capture(stage, "drawer-global-search-light");

                filter.clear();
                var conversations = (ListView<MockConversation>) root.lookup("#conversationItems");
                var direct = conversations.getItems().stream()
                        .filter(item -> item.id().equals("direct-01")).findFirst().orElseThrow();
                conversations.getSelectionModel().select(direct);
                ((Button) root.lookup("#conversationInfo")).fire();
                root.applyCss();
                root.layout();
                assertNotNull(root.lookup("#drawer"));
                ((Button) root.lookup("#searchInConversation")).fire();
                var localSearch = (TextField) root.lookup("#conversationSearchField");
                localSearch.setText("mình");
                root.applyCss();
                root.layout();
                ((Button) root.lookup("#nextSearchResult")).fire();
                ((Button) root.lookup("#nextSearchResult")).fire();
                root.applyCss();
                root.layout();
                capture(stage, "drawer-local-search-light");

                var group = conversations.getItems().stream()
                        .filter(item -> item.id().equals("grp-01")).findFirst().orElseThrow();
                conversations.getSelectionModel().select(group);
                ((Button) root.lookup("#conversationInfo")).fire();
                navigator.themes().toggle();
                root.applyCss();
                root.layout();
                assertEquals(8, root.lookupAll(".chat-info-member").size());
                assertNull(root.lookup("#blockConversationPeer"));
                capture(stage, "drawer-group-dark");
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    private static void enterPreviewWorkspace(Parent root) {
        root.applyCss();
        root.layout();
        assertInstanceOf(Button.class, root.lookup("#authSignInSubmit")).fire();
    }

    private static void fireSetting(Parent root, String id) {
        ((Button) root.lookup("#workspaceSettings")).fire();
        ((Button) settingsPopup().getScene().getRoot().lookup("#" + id)).fire();
    }

    private static Window settingsPopup() {
        return Window.getWindows().stream()
                .filter(window -> window.getScene().getRoot().lookup("#settingsContent") != null)
                .findFirst().orElseThrow();
    }

    private static void capture(Window stage, String name) throws Exception {
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
