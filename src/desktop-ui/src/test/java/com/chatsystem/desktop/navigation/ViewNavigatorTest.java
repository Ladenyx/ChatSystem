package com.chatsystem.desktop.navigation;

import static org.junit.jupiter.api.Assertions.*;

import atlantafx.base.controls.Popover;
import com.chatsystem.desktop.controller.DisplayOptionsController;
import com.chatsystem.desktop.theme.ThemeManager;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "chatsystem.uiTests", matches = "true")
class ViewNavigatorTest {
    private static final View FIRST = new View("first", "First", ViewNavigatorTest.class,
            "/com/chatsystem/desktop/test-page.fxml");
    private static final View SECOND = View.placeholder("second", "Second");

    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        });
        assertTrue(ready.await(10, TimeUnit.SECONDS), "JavaFX did not start");
    }

    @AfterAll
    static void stopJavaFx() {
        Platform.exit();
    }

    @Test
    void backRestoresCachedPageAndDoesNotDuplicateCurrentRoute() throws Exception {
        onFx(() -> {
            try (Fixture fixture = new Fixture()) {
                ViewNavigator nav = fixture.nav;
                TextField field = (TextField) nav.getRoot().lookup("#draft");
                field.setText("Bản nháp cần giữ");
                nav.navigate(FIRST);
                assertFalse(nav.canGoBackProperty().get());
                nav.navigate(SECOND);
                assertTrue(nav.goBack());
                assertSame(field, nav.getRoot().lookup("#draft"));
                assertEquals("Bản nháp cần giữ", field.getText());
                assertFalse(nav.goBack());
            }
            return null;
        });
    }

    @Test
    void missingAndMalformedFxmlLeaveCurrentPageAndHistoryIntact() throws Exception {
        onFx(() -> {
            try (Fixture fixture = new Fixture()) {
                fixture.nav.navigate(SECOND);
                for (String resource : new String[]{"/missing.fxml", "/com/chatsystem/desktop/broken.fxml"}) {
                    View invalid = new View("broken", "Broken", getClass(), resource);
                    ViewLoadException error = assertThrows(ViewLoadException.class,
                            () -> fixture.nav.navigate(invalid));
                    assertTrue(error.getMessage().contains(resource));
                    assertEquals(SECOND, fixture.nav.currentViewProperty().get());
                    assertTrue(fixture.nav.canGoBackProperty().get());
                }
                assertTrue(fixture.nav.goBack());
                assertEquals(FIRST, fixture.nav.currentViewProperty().get());
            }
            return null;
        });
    }

    @Test
    void resetDiscardsPreviousSessionStateAndHistory() throws Exception {
        onFx(() -> {
            try (Fixture fixture = new Fixture()) {
                TextField old = (TextField) fixture.nav.getRoot().lookup("#draft");
                old.setText("Private draft");
                fixture.nav.navigate(SECOND);
                fixture.nav.reset(FIRST);
                TextField fresh = (TextField) fixture.nav.getRoot().lookup("#draft");
                assertNotSame(old, fresh);
                assertEquals("", fresh.getText());
                assertFalse(fixture.nav.goBack());
            }
            return null;
        });
    }

    @Test
    void modalHasAnOwnerAndClosesWhenNavigating() throws Exception {
        onFx(() -> {
            try (Fixture fixture = new Fixture()) {
                Dialog<?> dialog = fixture.nav.showModal(SECOND);
                assertSame(fixture.stage, dialog.getOwner());
                assertEquals(Modality.WINDOW_MODAL, dialog.getModality());
                assertTrue(dialog.isShowing());
                fixture.nav.navigate(SECOND);
                assertFalse(dialog.isShowing());
            }
            return null;
        });
    }

    @Test
    void drawerClosesWithEscapeAndRestoresFocus() throws Exception {
        onFx(() -> {
            try (Fixture fixture = new Fixture()) {
                Button trigger = (Button) fixture.nav.getRoot().lookup("#anchor");
                trigger.requestFocus();
                fixture.nav.showDrawer(SECOND);
                assertNotNull(fixture.nav.getRoot().lookup("#drawer"));
                Event.fireEvent(fixture.nav.getRoot(), new KeyEvent(KeyEvent.KEY_PRESSED,
                        "", "", KeyCode.ESCAPE, false, false, false, false));
                assertNull(fixture.nav.getRoot().lookup("#drawer"));
                assertSame(trigger, fixture.stage.getScene().getFocusOwner());
            }
            return null;
        });
    }

    @Test
    void themeUpdatesOpenOverlaysWithoutLosingPageState() throws Exception {
        onFx(() -> {
            try (Fixture fixture = new Fixture()) {
                ThemeManager themes = fixture.nav.themes();
                TextField draft = (TextField) fixture.nav.getRoot().lookup("#draft");
                draft.setText("Giữ nguyên khi đổi theme");
                Dialog<?> dialog = fixture.nav.showModal(SECOND);
                themes.setMode(ThemeManager.Mode.DARK);
                PseudoClass dark = PseudoClass.getPseudoClass("dark");
                assertTrue(dialog.getDialogPane().getPseudoClassStates().contains(dark));
                assertTrue(fixture.nav.getRoot().getPseudoClassStates().contains(dark));
                dialog.close();
                Button anchor = (Button) fixture.nav.getRoot().lookup("#anchor");
                Popover popup = fixture.nav.showPopover(DisplayOptionsController.VIEW, anchor);
                assertTrue(popup.isShowing());
                assertTrue(popup.getRoot().isVisible());
                popup.getRoot().applyCss();
                assertTrue(popup.getRoot().getStyleClass().contains("cs-root"));
                assertTrue(popup.getRoot().getStyleClass().contains("root"));
                assertTrue(popup.getRoot().getPseudoClassStates().contains(dark));
                ((javafx.scene.control.ToggleButton) popup.getRoot().lookup("#lightTheme")).fire();
                assertEquals(ThemeManager.Mode.LIGHT, themes.getMode());
                assertFalse(popup.getRoot().getPseudoClassStates().contains(dark));
                Event.fireEvent(fixture.nav.getRoot(), new KeyEvent(KeyEvent.KEY_PRESSED,
                        "", "", KeyCode.ESCAPE, false, false, false, false));
                assertFalse(popup.isShowing());
                popup = fixture.nav.showPopover(DisplayOptionsController.VIEW, anchor);
                themes.setMode(ThemeManager.Mode.LIGHT);
                assertFalse(popup.getRoot().getPseudoClassStates().contains(dark));
                fixture.nav.navigate(SECOND);
                assertFalse(popup.isShowing());
                fixture.nav.goBack();
                assertEquals("Giữ nguyên khi đổi theme", draft.getText());
            }
            return null;
        });
    }

    private static <T> T onFx(Callable<T> operation) throws Exception {
        FutureTask<T> task = new FutureTask<>(operation);
        Platform.runLater(task);
        return task.get(15, TimeUnit.SECONDS);
    }

    private static final class Fixture implements AutoCloseable {
        private final Stage stage = new Stage();
        private final ViewNavigator nav;

        private Fixture() {
            StackPane outlet = new StackPane();
            nav = new ViewNavigator(new BorderPane(outlet), outlet, new ThemeManager());
            nav.themes().reducedMotionProperty().set(true);
            nav.reset(FIRST);
            stage.setScene(new Scene(nav.getRoot(), 1024, 640));
            stage.show();
            nav.getRoot().applyCss();
            nav.getRoot().layout();
        }

        @Override
        public void close() {
            nav.close();
            stage.close();
        }
    }
}
