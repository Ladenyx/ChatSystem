package com.chatsystem.user.controller.contacts;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.theme.ThemeManager.Mode;
import com.chatsystem.user.UserApplication;
import com.chatsystem.user.UserViews;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

// Run separately from UserApplicationTest; JavaFX cannot be restarted in the same JVM.
@EnabledIfSystemProperty(named = "chatsystem.uiTests", matches = "true")
class ContactsUiIT {
    private static final List<String> TABS = List.of("All", "Online", "Requests", "Search", "Blocked");
    private static final ConcurrentLinkedQueue<Throwable> FX_ERRORS = new ConcurrentLinkedQueue<>();

    @BeforeAll
    static void startJavaFx() throws Exception {
        var ready = new CountDownLatch(1);
        Platform.startup(() -> {
            Thread.currentThread().setUncaughtExceptionHandler((thread, error) -> FX_ERRORS.add(error));
            Platform.setImplicitExit(false);
            ready.countDown();
        });
        assertTrue(ready.await(10, TimeUnit.SECONDS), "JavaFX did not start");
    }

    @AfterAll
    static void stopJavaFx() {
        Platform.exit();
    }

    @AfterEach
    void noUnhandledJavaFxErrors() {
        var errors = List.copyOf(FX_ERRORS);
        FX_ERRORS.clear();
        assertTrue(errors.isEmpty(), () -> "Unhandled JavaFX errors: " + errors);
    }

    @Test
    void allFiveTabsFitAtBothWindowSizesAndThemes() throws Exception {
        for (Mode mode : Mode.values()) {
            for (int width : List.of(1024, 1280)) {
                int height = width == 1024 ? 640 : 720;
                withContacts(width, height, mode, (stage, root) -> {
                    var scroll = require(root, "contactsScroll", ScrollPane.class);
                    for (String tab : TABS) {
                        select(root, tab);
                        if (tab.equals("Search")) {
                            assertNotNull(root.lookup("#contactsEmpty"), "Search starts with a hint");
                            if (width == 1024 && mode == Mode.DARK) {
                                capture(stage.getScene(), "empty-search-dark-1024x640");
                            }
                            filter(root).setText("haiphong");
                        }
                        root.applyCss();
                        root.layout();
                        assertEquals(width, stage.getScene().getWidth());
                        assertEquals(height, stage.getScene().getHeight());
                        assertTrue(scroll.getViewportBounds().getWidth() > 0, tab + " has room for results");
                        assertTrue(scroll.getContent().getLayoutBounds().getWidth()
                                <= scroll.getViewportBounds().getWidth() + 1, tab + " content must fit without clipping");
                        assertActionsFit(scroll, tab);
                        // Representative images at both sizes; no duplicate captures for the remaining combinations.
                        if (width == 1280 && mode == Mode.LIGHT) {
                            capture(stage.getScene(), tab.toLowerCase(Locale.ROOT) + "-light-1280x720");
                        } else if (width == 1024 && mode == Mode.DARK && tab.equals("All")) {
                            capture(stage.getScene(), "all-dark-1024x640");
                        }
                    }
                });
            }
        }
    }

    private static void assertActionsFit(ScrollPane scroll, String tab) {
        String peer = switch (tab) {
            case "All" -> "02";
            case "Online" -> "04";
            case "Requests" -> "07";
            case "Search" -> "12";
            case "Blocked" -> "09";
            default -> throw new AssertionError(tab);
        };
        var viewport = scroll.lookup(".viewport");
        assertNotNull(viewport);
        var viewportBounds = viewport.localToScene(viewport.getBoundsInLocal());
        var actions = require(scroll, "contact-usr-" + peer, Parent.class).lookupAll(".button");
        assertFalse(actions.isEmpty(), tab + " has visible row actions");
        for (Node action : actions) {
            var bounds = action.localToScene(action.getBoundsInLocal());
            assertTrue(bounds.getMinX() >= viewportBounds.getMinX() - 1
                            && bounds.getMaxX() <= viewportBounds.getMaxX() + 1,
                    action.getId() + " fits horizontally in the results viewport");
        }
    }

    @Test
    void filtersFriendsByNameAndSearchesVisibleAccountsByNameOrUsername() throws Exception {
        withContacts(1280, 720, Mode.LIGHT, (stage, root) -> {
            assertNotNull(root.lookup("#contact-usr-02"));
            assertNotNull(root.lookup("#contact-usr-04"));
            filter(root).setText("Bảo Châu");
            assertNotNull(root.lookup("#contact-usr-02"));
            assertNull(root.lookup("#contact-usr-04"));
            select(root, "Online");
            assertNotNull(root.lookup("#contact-usr-04"));
            assertNull(root.lookup("#contact-usr-02"));
            filter(root).setText("does-not-match");
            assertNotNull(root.lookup("#contactsEmpty"));

            select(root, "Search");
            filter(root).setText("  HAIPHONG  ");
            assertNotNull(root.lookup("#contact-usr-12"));
            assertNotNull(root.lookup("#contact-chat-usr-12"), "Non-friends can open direct chat");
            assertNull(root.lookup("#contact-group-usr-12"), "Group creation is offered only for friends");
            filter(root).setText("Hải Phong");
            assertNotNull(root.lookup("#contact-usr-12"));
            for (String hidden : List.of("usr-01", "usr-09", "usr-10")) {
                filter(root).setText(MockData.create().user(hidden).username());
                assertNull(root.lookup("#contact-" + hidden));
                assertNotNull(root.lookup("#contactsEmpty"));
            }
        });
    }

    @Test
    void friendRequestsUpdateTheOfflinePreviewAcrossTabs() throws Exception {
        withContacts(1280, 720, Mode.LIGHT, (stage, root) -> {
            select(root, "Requests");
            fire(root, "contact-accept-usr-07");
            assertNull(root.lookup("#contact-usr-07"));
            fire(root, "contact-decline-usr-08");
            assertNotNull(root.lookup("#contactsEmpty"));
            select(root, "All");
            assertNotNull(root.lookup("#contact-usr-07"));
            assertNull(root.lookup("#contact-usr-08"));
            select(root, "Search");
            filter(root).setText("haiphong");
            fire(root, "contact-invite-usr-12");
            assertNotNull(root.lookup("#contact-usr-12"));
            assertNull(root.lookup("#contact-invite-usr-12"), "A pending invitation cannot be sent again");
        });
    }

    @Test
    void compactConfirmationKeepsSafeDefaultAndEscapeInBothThemes() throws Exception {
        for (Mode mode : Mode.values()) {
            withContacts(1024, 640, mode, (stage, root) -> {
                fire(root, "contact-unfriend-usr-02");
                var confirmation = dialog(stage, "contactsConfirmation");
                var dialogStage = assertInstanceOf(Stage.class, confirmation.getScene().getWindow());
                assertSame(stage, dialogStage.getOwner());
                assertEquals(Modality.WINDOW_MODAL, dialogStage.getModality());
                assertEquals(StageStyle.UNDECORATED, dialogStage.getStyle());
                assertNull(confirmation.getHeaderText(), "A separate header repeats the in-dialog title");
                var heading = require(confirmation, "contactsConfirmationTitle", Label.class);
                assertTrue(heading.isVisible());
                assertTrue(heading.getText().contains("Hủy kết bạn"));
                assertTrue(heading.getText().contains(MockData.create().user("usr-02").fullName()));
                var cancel = require(confirmation, "cancel-contact-action", Button.class);
                var proceed = require(confirmation, "confirm-contact-action", Button.class);
                assertTrue(cancel.isDefaultButton());
                assertTrue(cancel.isCancelButton());
                assertSame(cancel, confirmation.getScene().getFocusOwner());
                assertFalse(proceed.isDefaultButton());
                assertFalse(proceed.isCancelButton());
                assertTrue(proceed.getStyleClass().contains("contacts-confirm-danger"));
                capture(confirmation.getScene(), "confirm-" + mode.name().toLowerCase(Locale.ROOT));
                Event.fireEvent(confirmation, new KeyEvent(KeyEvent.KEY_PRESSED,
                        "", "", KeyCode.ESCAPE, false, false, false, false));
                assertFalse(dialogStage.isShowing());
                assertNotNull(root.lookup("#contact-usr-02"));
            });
        }
    }

    @Test
    void cancelLeavesAFriendAndUnblockingDoesNotRestoreFriendship() throws Exception {
        withContacts(1024, 640, Mode.DARK, (stage, root) -> {
            fire(root, "contact-unfriend-usr-02");
            fire(dialog(stage, "contactsConfirmation"), "cancel-contact-action");
            assertNotNull(root.lookup("#contact-usr-02"));
            fire(root, "contact-unfriend-usr-02");
            fire(dialog(stage, "contactsConfirmation"), "confirm-contact-action");
            assertNull(root.lookup("#contact-usr-02"));

            fire(root, "contact-block-usr-04");
            var block = require(dialog(stage, "contactsConfirmation"), "confirm-contact-action", Button.class);
            assertTrue(block.getStyleClass().contains("contacts-confirm-danger"));
            block.fire();
            assertNull(root.lookup("#contact-usr-04"));
            select(root, "Blocked");
            assertNotNull(root.lookup("#contact-usr-04"));
            fire(root, "contact-unblock-usr-04");
            var unblock = require(dialog(stage, "contactsConfirmation"), "confirm-contact-action", Button.class);
            assertFalse(unblock.getStyleClass().contains("contacts-confirm-danger"));
            unblock.fire();
            assertNull(root.lookup("#contact-usr-04"));
            select(root, "All");
            assertNull(root.lookup("#contact-usr-04"), "Unblocking must not restore friendship");
        });
    }

    @Test
    void chatAndGroupActionsReachRegisteredDestinations() throws Exception {
        withContacts(1280, 720, Mode.LIGHT, (stage, root) -> {
            select(root, "Online");
            fire(root, "contact-group-usr-04");
            var modal = dialog(stage, "screenId");
            assertEquals(UserViews.CREATE_GROUP.id(), require(modal, "screenId", Label.class).getText());
            assertNotNull(root.lookup("#contactsRoot"), "Modal must not replace Contacts");
            fire(modal, modal.getButtonTypes().getFirst());
            select(root, "Search");
            filter(root).setText("haiphong");
            fire(root, "contact-chat-usr-12");
            assertTrue(require(root, "navChats", ToggleButton.class).isSelected());
            assertNull(root.lookup("#contactsRoot"));
            fire(root, "goBack");
            assertTrue(require(root, "navContacts", ToggleButton.class).isSelected());
            assertNotNull(root.lookup("#contact-usr-12"), "Returning preserves the cached search");
        });
    }

    private static void select(Parent root, String tab) {
        var toggle = require(root, "contactsTab" + tab, ToggleButton.class);
        toggle.fire();
        assertTrue(toggle.isSelected(), tab + " must be selected");
        assertEquals(1, TABS.stream().filter(id -> require(root, "contactsTab" + id, ToggleButton.class)
                .isSelected()).count());
    }

    private static TextField filter(Parent root) {
        return require(root, "contactsFilter", TextField.class);
    }

    private static void fire(Parent root, String id) {
        var button = require(root, id, ButtonBase.class);
        assertFalse(button.isDisabled(), id);
        button.fire();
    }

    private static void fire(DialogPane pane, javafx.scene.control.ButtonType type) {
        assertInstanceOf(ButtonBase.class, pane.lookupButton(type)).fire();
    }

    private static DialogPane dialog(Stage owner, String marker) {
        return Window.getWindows().stream().filter(window -> window != owner && window.isShowing())
                .map(window -> window.getScene().getRoot())
                .filter(DialogPane.class::isInstance).map(DialogPane.class::cast)
                .filter(pane -> marker.equals(pane.getId()) || pane.lookup("#" + marker) != null)
                .findFirst().orElseThrow(() -> new AssertionError("Missing dialog: " + marker));
    }

    private static <T extends Node> T require(Parent root, String id, Class<T> type) {
        return assertInstanceOf(type, root.lookup("#" + id), id);
    }

    private static void withContacts(int width, int height, Mode mode, WindowAction action) throws Exception {
        var task = new FutureTask<Void>(() -> {
            var navigator = UserApplication.createNavigator();
            var stage = new Stage();
            try {
                navigator.themes().setMode(mode);
                var root = navigator.getRoot();
                stage.setScene(new Scene(root, width, height));
                stage.show();
                fire(root, "authSignInSubmit");
                fire(root, "navContacts");
                assertEquals(UserViews.CONTACTS, navigator.currentViewProperty().get());
                root.applyCss();
                root.layout();
                action.run(stage, root);
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(30, TimeUnit.SECONDS);
    }

    @FunctionalInterface
    private interface WindowAction {
        void run(Stage stage, Parent root) throws Exception;
    }

    private static void capture(Scene scene, String name) throws Exception {
        if (!Boolean.getBoolean("chatsystem.capture")) {
            return;
        }
        // Wait for JavaFX to lay out the updated Scene on a pulse before each capture.
        var key = new Object();
        scene.snapshot(result -> {
            Platform.exitNestedEventLoop(key, result.getImage());
            return null;
        }, null);
        var image = (WritableImage) Platform.enterNestedEventLoop(key);
        var output = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < output.getHeight(); y++) {
            for (int x = 0; x < output.getWidth(); x++) {
                output.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        var directory = Path.of("target", "screenshots");
        Files.createDirectories(directory);
        assertTrue(ImageIO.write(output, "png", directory.resolve("U-SCR-08-" + name + ".png").toFile()));
    }
}
