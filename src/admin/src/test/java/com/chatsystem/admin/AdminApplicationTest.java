package com.chatsystem.admin;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.desktop.controller.MockDataPreviewController;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
            new AdminApplication().start(stage);
            try {
                var root = stage.getScene().getRoot();
                capture(stage, "admin-light");
                for (var view : AdminViews.PAGES) {
                    ToggleButton button = (ToggleButton) root.lookup("#nav-" + view.id());
                    button.fire();
                    assertTrue(button.isSelected());
                    assertEquals(view.title(), ((Label) root.lookup("#screenTitle")).getText());
                }
                ((Button) root.lookup("#goBack")).fire();
                assertTrue(((ToggleButton) root.lookup("#nav-A-SCR-08")).isSelected());
                ((Button) root.lookup("#toggleTheme")).fire();
                assertTrue(root.getPseudoClassStates().contains(javafx.css.PseudoClass.getPseudoClass("dark")));
                capture(stage, "admin-dark");
                ((Button) root.lookup("#openUserDialog")).fire();
                assertTrue(javafx.stage.Window.getWindows().size() > 1);
                ((ToggleButton) root.lookup("#nav-A-SCR-02")).fire();
                assertEquals(1, javafx.stage.Window.getWindows().size());
            } finally {
                stage.close();
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
                capture(stage, "admin-small");
                ((Button) navigator.getRoot().lookup("#openMockData")).fire();
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
