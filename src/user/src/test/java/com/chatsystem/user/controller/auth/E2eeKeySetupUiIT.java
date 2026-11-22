package com.chatsystem.user.controller.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.desktop.theme.ThemeManager.Mode;
import com.chatsystem.user.UserApplication;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.navigation.UserViewInputs.E2eeKeySetup;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Run separately from UserApplicationTest, which shuts down the JavaFX toolkit. */
@EnabledIfSystemProperty(named = "chatsystem.uiTests", matches = "true")
class E2eeKeySetupUiIT {
    @BeforeAll
    static void startJavaFx() throws Exception {
        var ready = new CompletableFuture<Void>();
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.complete(null);
        });
        ready.get(10, TimeUnit.SECONDS);
    }

    @AfterAll
    static void stopJavaFx() {
        Platform.exit();
    }

    @Test
    void chooserOpensBothModesAndSkipOrCancelPreservesThePreviousPage() throws Exception {
        try (var window = openWindow(1024, 640)) {
            onFx(() -> {
                var navigator = window.navigator();
                var root = navigator.getRoot();
                navigator.navigate(UserViews.CONTACTS);
                var host = assertInstanceOf(StackPane.class, node(root, "viewHost"));
                Node contactsPage = host.getChildren().getFirst();

                navigator.navigate(UserViews.E2EE_KEY_SETUP);
                assertShowing(root, "choices");
                button(root, "keyCreate").fire();
                assertShowing(root, "codeForm");
                assertShowing(root, "confirmationGroup");
                button(root, "keyCancel").fire();
                assertEquals(UserViews.CONTACTS, navigator.currentViewProperty().get());
                assertSame(contactsPage, host.getChildren().getFirst());

                navigator.navigate(UserViews.E2EE_KEY_SETUP);
                button(root, "keyRestore").fire();
                assertShowing(root, "codeForm");
                assertFalse(node(root, "confirmationGroup").isVisible());
                assertFalse(node(root, "confirmationGroup").isManaged());
                button(root, "keyCancel").fire();
                assertEquals(UserViews.CONTACTS, navigator.currentViewProperty().get());

                navigator.navigate(UserViews.E2EE_KEY_SETUP);
                button(root, "keySkip").fire();
                assertEquals(UserViews.CONTACTS, navigator.currentViewProperty().get());
                assertTrue(navigator.canGoBackProperty().get());
                return null;
            });
        }
    }

    @Test
    void setupFiltersMaskedInputAndOnlyShowsOfflineFeedback() throws Exception {
        try (var window = openWindow(1280, 720)) {
            onFx(() -> {
                var navigator = window.navigator();
                var root = navigator.getRoot();
                navigator.navigate(UserViews.E2EE_KEY_SETUP, E2eeKeySetup.SET_UP);
                assertShowing(root, "confirmationGroup");
                var submit = button(root, "keySubmit");
                assertTrue(submit.isDisabled());

                var first = field(root, "keyDigit1");
                first.replaceText(0, 0, "x");
                assertEquals("", first.getText());
                first.replaceText(0, 0, "١");
                assertEquals("", first.getText());
                fill(root, "keyDigit", "012345");
                assertTrue(submit.isDisabled(), "Setup requires both rows to be complete");
                fill(root, "keyConfirm", "012346");
                assertFalse(submit.isDisabled());
                submit.fire();
                String mismatch = notice(root);
                assertFalse(mismatch.isBlank(), "The preview should illustrate a mismatch");

                var last = field(root, "keyConfirm6");
                last.replaceText(0, last.getLength(), "5");
                submit.fire();
                assertEquals(UserViews.E2EE_KEY_SETUP, navigator.currentViewProperty().get());
                assertSuccessNotice(root, mismatch, "Đã thiết lập mã khôi phục.");
                assertCleared(root);
                assertTrue(submit.isDisabled());
                return null;
            });
        }
    }

    @Test
    void typedRecoveryUsesOneRowAndCancelWithoutHistoryFallsBackToConversations() throws Exception {
        try (var window = openWindow(1024, 640)) {
            onFx(() -> {
                var navigator = window.navigator();
                var root = navigator.getRoot();
                navigator.reset(UserViews.E2EE_KEY_SETUP, E2eeKeySetup.RECOVER);
                assertFalse(navigator.canGoBackProperty().get());
                assertShowing(root, "codeForm");
                assertFalse(node(root, "confirmationGroup").isManaged());
                var submit = button(root, "keySubmit");
                fill(root, "keyDigit", "65432");
                assertTrue(submit.isDisabled());
                fill(root, "keyDigit", "654321");
                assertFalse(submit.isDisabled());
                submit.fire();
                assertSuccessNotice(root, "", "Đã khôi phục khóa thành công.");
                assertCleared(root);
                assertTrue(submit.isDisabled());
                button(root, "keyCancel").fire();
                assertEquals(UserViews.CONVERSATIONS, navigator.currentViewProperty().get());
                return null;
            });
        }
    }

    @Test
    void queuedPasteFillsTheCurrentModeButCannotRepopulateAfterRouteChanges() throws Exception {
        try (var window = openWindow(1024, 640)) {
            var navigator = window.navigator();
            var root = navigator.getRoot();
            onFx(() -> {
                navigator.navigate(UserViews.E2EE_KEY_SETUP, E2eeKeySetup.SET_UP);
                field(root, "keyDigit1").replaceText(0, 0, "012345");
                return null;
            });
            onFx(() -> {
                assertEquals("012345", digits(root, "keyDigit"));
                assertTrue(button(root, "keySubmit").isDisabled());
                var detachedDigit = field(root, "keyDigit2");
                assertTrue(navigator.goBack());
                assertEquals(UserViews.CONVERSATIONS, navigator.currentViewProperty().get());
                assertEquals("", detachedDigit.getText(), "Leaving must clear even a cached, detached form");

                navigator.navigate(UserViews.E2EE_KEY_SETUP, E2eeKeySetup.SET_UP);
                assertCleared(root);
                field(root, "keyDigit1").replaceText(0, 0, "654321");
                navigator.navigate(UserViews.E2EE_KEY_SETUP, E2eeKeySetup.RECOVER);
                return null;
            });
            onFx(() -> {
                assertCleared(root);
                assertFalse(node(root, "confirmationGroup").isManaged());

                field(root, "keyDigit1").replaceText(0, 0, "345678");
                navigator.navigate(UserViews.CONTACTS);
                navigator.navigate(UserViews.E2EE_KEY_SETUP, E2eeKeySetup.RECOVER);
                return null;
            });
            onFx(() -> {
                assertCleared(root);
                assertTrue(button(root, "keySubmit").isDisabled());
                return null;
            });
        }
    }

    @Test
    void previewsLayOutInBothThemesAtDesktopAndCompactSizes() throws Exception {
        for (int width : new int[]{1280, 1024}) {
            int height = width == 1280 ? 720 : 640;
            try (var window = openWindow(width, height)) {
                for (Mode theme : Mode.values()) {
                    var navigator = window.navigator();
                    var root = navigator.getRoot();
                    String variant = width + "x" + height + "-" + (theme == Mode.LIGHT ? "light" : "dark");

                    onFx(() -> {
                        navigator.themes().setMode(theme);
                        navigator.navigate(UserViews.CONVERSATIONS);
                        navigator.navigate(UserViews.E2EE_KEY_SETUP);
                        assertLayout(root, "keyCreate", "keyRestore", "keySkip");
                        return null;
                    });
                    capture(window.stage(), "U-SCR-06-choose-" + variant);
                    onFx(() -> {
                        navigator.navigate(UserViews.E2EE_KEY_SETUP, E2eeKeySetup.SET_UP);
                        assertLayout(root, "keyDigit1", "keyDigit6", "keyConfirm6", "keySubmit", "keyCancel");
                        return null;
                    });
                    capture(window.stage(), "U-SCR-06-setup-" + variant);
                    onFx(() -> {
                        navigator.navigate(UserViews.E2EE_KEY_SETUP, E2eeKeySetup.RECOVER);
                        assertLayout(root, "keyDigit1", "keyDigit6", "keySubmit", "keyCancel");
                        return null;
                    });
                    capture(window.stage(), "U-SCR-06-recovery-" + variant);
                }
            }
        }
    }

    private static void assertLayout(Parent root, String... ids) {
        root.applyCss();
        root.layout();
        node(root, "keySetupView");
        for (String id : ids) {
            var node = assertShowing(root, id);
            var bounds = node.localToScene(node.getBoundsInLocal());
            assertTrue(bounds.getWidth() > 0 && bounds.getHeight() > 0, "Empty layout for " + id);
            assertTrue(bounds.getMinX() >= -1 && bounds.getMaxX() <= root.getScene().getWidth() + 1,
                    () -> id + " is outside the horizontal viewport: " + bounds);
        }
    }

    private static void assertSuccessNotice(Parent root, String previous, String expected) {
        String notice = notice(root);
        assertNotEquals(previous, notice);
        assertEquals(expected, notice);
        assertShowing(root, "notice");
    }

    private static String notice(Parent root) {
        return assertInstanceOf(Label.class, node(root, "notice")).getText();
    }

    private static void fill(Parent root, String prefix, String value) {
        for (int i = 0; i < value.length(); i++) {
            var field = field(root, prefix + (i + 1));
            field.replaceText(0, field.getLength(), value.substring(i, i + 1));
        }
    }

    private static String digits(Parent root, String prefix) {
        var result = new StringBuilder();
        for (int i = 1; i <= 6; i++) {
            result.append(field(root, prefix + i).getText());
        }
        return result.toString();
    }

    private static void assertCleared(Parent root) {
        for (String prefix : new String[]{"keyDigit", "keyConfirm"}) {
            for (int i = 1; i <= 6; i++) {
                assertEquals("", field(root, prefix + i).getText(), prefix + i);
            }
        }
    }

    private static Node assertShowing(Parent root, String id) {
        Node node = node(root, id);
        for (Node current = node; current != null; current = current.getParent()) {
            assertTrue(current.isVisible(), id + " must be showing");
        }
        return node;
    }

    private static Node node(Parent root, String id) {
        var node = root.lookup("#" + id);
        if (node == null) {
            // A newly attached ScrollPane needs its skin before its content is in the scene graph.
            root.applyCss();
            root.layout();
            node = root.lookup("#" + id);
        }
        assertNotNull(node, id);
        return node;
    }

    private static Button button(Parent root, String id) {
        return assertInstanceOf(Button.class, assertShowing(root, id), id);
    }

    private static PasswordField field(Parent root, String id) {
        return assertInstanceOf(PasswordField.class, node(root, id), id);
    }

    private static TestWindow openWindow(int width, int height) throws Exception {
        return onFx(() -> {
            var navigator = UserApplication.createNavigator();
            var stage = new Stage();
            stage.setScene(new Scene(navigator.getRoot(), width, height));
            stage.show();
            button(navigator.getRoot(), "authSignInSubmit").fire();
            assertEquals(UserViews.CONVERSATIONS, navigator.currentViewProperty().get());
            return new TestWindow(navigator, stage);
        });
    }

    private static void capture(Stage stage, String name) throws Exception {
        if (!Boolean.getBoolean("chatsystem.capture")) {
            return;
        }
        var rendered = new CompletableFuture<WritableImage>();
        onFx(() -> {
            stage.getScene().snapshot(result -> {
                rendered.complete(result.getImage());
                return null;
            }, null);
            return null;
        });
        var image = rendered.get(10, TimeUnit.SECONDS);
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        var pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        var output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        output.setRGB(0, 0, width, height, pixels, 0, width);
        Path directory = Path.of("target", "screenshots");
        Files.createDirectories(directory);
        assertTrue(ImageIO.write(output, "png", directory.resolve(name + ".png").toFile()));
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        var task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(15, TimeUnit.SECONDS);
    }

    private record TestWindow(ViewNavigator navigator, Stage stage) implements AutoCloseable {
        @Override
        public void close() throws Exception {
            onFx(() -> {
                try {
                    navigator.close();
                } finally {
                    stage.close();
                }
                return null;
            });
        }
    }
}
