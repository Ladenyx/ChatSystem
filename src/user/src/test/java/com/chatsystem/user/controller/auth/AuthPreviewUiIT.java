package com.chatsystem.user.controller.auth;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.desktop.navigation.View;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.desktop.theme.ThemeManager.Mode;
import com.chatsystem.user.UserApplication;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.navigation.UserViewInputs;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
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
import javafx.scene.control.TextInputControl;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

// Run explicitly in a separate JVM from UserApplicationTest; JavaFX cannot be restarted.
@EnabledIfSystemProperty(named = "chatsystem.uiTests", matches = "true")
class AuthPreviewUiIT {
    private static final ConcurrentLinkedQueue<Throwable> FX_ERRORS = new ConcurrentLinkedQueue<>();
    private static final List<Preview> PREVIEWS = List.of(
            new Preview(1280, 720, Mode.LIGHT, ""),
            new Preview(1280, 720, Mode.DARK, "-dark"),
            new Preview(1024, 640, Mode.LIGHT, "-small"),
            new Preview(1024, 640, Mode.DARK, "-small-dark"));

    @BeforeAll
    static void startJavaFx() throws InterruptedException {
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
        var errors = List.copyOf(FX_ERRORS);
        FX_ERRORS.clear();
        assertTrue(errors.isEmpty(), () -> "Unhandled JavaFX errors: " + errors);
    }

    @Test
    void startsOnOfflineSignInAndShowsRailInWorkspaceIncludingE2ee() throws Exception {
        for (var preview : PREVIEWS) {
            withAuthWindow(preview, (stage, navigator) -> {
                var root = navigator.getRoot();
                assertAuthRoute(navigator, UserViews.SIGN_IN);
                var offlineHint = assertInstanceOf(Label.class, shownNode(root, "authOfflinePreview"));
                assertTrue(offlineHint.getText().contains("Không nhập thông tin thật"));
                var submit = assertInstanceOf(Button.class, shownNode(root, "authSignInSubmit"));
                assertReadableSubmit(submit, preview);
                captureAuth(stage, preview.signInImage());

                var toggle = assertInstanceOf(Button.class, shownNode(root, "authSignInShowPassword"));
                var masked = assertInstanceOf(PasswordField.class, shownNode(root, "authSignInPassword"));
                var plain = assertInstanceOf(TextInputControl.class, requireNode(root, "authSignInPasswordVisible"));
                var closedEye = toggle.getGraphic();
                assertEquals("Hiện mật khẩu", toggle.getAccessibleText());
                assertFalse(plain.isVisible());
                masked.setText("sample-only");

                toggle.fire();
                assertEquals("Ẩn mật khẩu", toggle.getAccessibleText());
                assertNotSame(closedEye, toggle.getGraphic());
                assertTrue(plain.isVisible());
                assertFalse(masked.isVisible());
                assertEquals("sample-only", plain.getText());

                toggle.fire();
                assertSame(closedEye, toggle.getGraphic());
                assertFalse(plain.isVisible());
                assertTrue(masked.isVisible());
                masked.clear();
                assertEquals("", authField(root, "authSignInUsername").getText());
                assertEquals("", masked.getText());

                click(root, "authSignInSubmit");
                assertEquals(UserViews.CONVERSATIONS, navigator.currentViewProperty().get());
                assertRail(root, true);
                assertNotNull(requireNode(root, "workspaceSettings"));

                navigator.navigate(UserViews.E2EE_KEY_SETUP, UserViewInputs.E2eeKeySetup.RECOVER);
                assertEquals(UserViews.E2EE_KEY_SETUP, navigator.currentViewProperty().get());
                assertRail(root, true);
                assertTrue(navigator.goBack());
                assertEquals(UserViews.CONVERSATIONS, navigator.currentViewProperty().get());
                assertRail(root, true);
            });
        }
    }

    @Test
    void signUpPreviewShowsEachScreenWithoutVerifyingAnAccount() throws Exception {
        for (var preview : PREVIEWS) {
            withAuthWindow(preview, (stage, navigator) -> {
                var root = navigator.getRoot();
                click(root, "authOpenSignUp");
                assertAuthRoute(navigator, UserViews.SIGN_UP);
                captureAuth(stage, "U-SCR-02-sign-up-username" + preview.suffix());
                click(root, "authSignUpContinue");
                assertPasswordRulesShown(root);
                captureAuth(stage, "U-SCR-02-sign-up-password" + preview.suffix());
                click(root, "authSignUpContinue");
                authField(root, "authSignUpEmail").setText("preview@example.test");
                captureAuth(stage, "U-SCR-02-sign-up-email" + preview.suffix());
                click(root, "authSignUpContinue");
                assertAuthRoute(navigator, UserViews.VERIFY_EMAIL);
                assertEquals("p***@example.test", labelText(root, "maskedEmail"));
                assertPreviewResendDisabled(root, "authVerifyResend");
                captureAuth(stage, "U-SCR-03-verify-email" + preview.suffix());
                click(root, "authVerifyChangeEmail");
                assertAuthRoute(navigator, UserViews.SIGN_UP);
                assertEquals("preview@example.test", authField(root, "authSignUpEmail").getText());
                click(root, "authSignUpContinue");
                click(root, "authVerifySubmit");
                assertAuthRoute(navigator, UserViews.SIGN_UP);
                shownNode(root, "authSignUpFullName");
                captureAuth(stage, "U-SCR-02-sign-up-profile" + preview.suffix());
                click(root, "authSignUpContinue");
                assertAuthRoute(navigator, UserViews.SIGN_IN);
            });
        }
    }

    @Test
    void passwordRecoveryPreviewShowsEachScreenWithoutResettingAPassword() throws Exception {
        for (var preview : PREVIEWS) {
            withAuthWindow(preview, (stage, navigator) -> {
                var root = navigator.getRoot();
                click(root, "authOpenForgotPassword");
                assertAuthRoute(navigator, UserViews.FORGOT_PASSWORD);
                captureAuth(stage, "U-SCR-04-forgot-username" + preview.suffix());
                click(root, "authForgotContinue");
                shownNode(root, "authForgotEmail");
                captureAuth(stage, "U-SCR-04-forgot-email" + preview.suffix());
                click(root, "authForgotContinue");
                shownNode(root, "checkEmailStep");
                assertPreviewResendDisabled(root, "authForgotResend");
                captureAuth(stage, "U-SCR-04-check-email" + preview.suffix());
                click(root, "authForgotContinue");
                assertAuthRoute(navigator, UserViews.FORCE_CHANGE_PASSWORD);
                captureAuth(stage, "U-SCR-05-temporary-password" + preview.suffix());
                click(root, "authForceContinue");
                shownNode(root, "authNewPassword");
                assertPasswordRulesShown(root);
                captureAuth(stage, "U-SCR-05-new-password" + preview.suffix());
                click(root, "authForceContinue");
                assertAuthRoute(navigator, UserViews.SIGN_IN);
            });
        }
    }

    private static void assertAuthRoute(ViewNavigator navigator, View expected) {
        assertEquals(expected, navigator.currentViewProperty().get());
        assertRail(navigator.getRoot(), false);
    }

    private static void assertRail(Parent root, boolean shown) {
        var rail = requireNode(root, "workspaceRail");
        assertEquals(shown, rail.isVisible(), "Rail visibility");
        assertEquals(shown, rail.isManaged(), "Hidden rail must not occupy layout width");
    }

    private static void assertReadableSubmit(Button button, Preview preview) {
        assertFalse(button.isDisabled());
        var background = button.getBackground();
        assertNotNull(background, preview + ": sign-in button must have a resolved background");
        assertFalse(background.getFills().isEmpty(), preview + ": sign-in button must have a background fill");
        var fill = assertInstanceOf(Color.class, background.getFills().getLast().getFill());
        var text = assertInstanceOf(Color.class, button.getTextFill());
        assertEquals(1.0, fill.getOpacity(), preview + ": sign-in button fill must be opaque");
        assertEquals(1.0, text.getOpacity(), preview + ": sign-in button text must be opaque");
        double fillLuminance = luminance(fill);
        double textLuminance = luminance(text);
        double contrast = (Math.max(fillLuminance, textLuminance) + 0.05)
                / (Math.min(fillLuminance, textLuminance) + 0.05);
        assertTrue(contrast >= 4.5, () -> preview + ": sign-in text contrast was " + contrast);
    }

    private static double luminance(Color color) {
        return 0.2126 * linearChannel(color.getRed()) + 0.7152 * linearChannel(color.getGreen())
                + 0.0722 * linearChannel(color.getBlue());
    }

    private static double linearChannel(double channel) {
        return channel <= 0.04045 ? channel / 12.92 : Math.pow((channel + 0.055) / 1.055, 2.4);
    }

    private static void assertPreviewResendDisabled(Parent root, String id) {
        var button = assertInstanceOf(Button.class, shownNode(root, id));
        assertTrue(button.isDisabled(), id + " must not offer to resend in the offline preview");
    }

    private static void click(Parent root, String id) {
        var button = assertInstanceOf(Button.class, shownNode(root, id), id);
        assertFalse(button.isDisabled(), id + " should be enabled");
        button.fire();
    }

    private static TextInputControl authField(Parent root, String id) {
        return assertInstanceOf(TextInputControl.class, shownNode(root, id), id);
    }

    private static Node shownNode(Parent root, String id) {
        var node = requireNode(root, id);
        for (Node ancestor = node; ancestor != null; ancestor = ancestor.getParent()) {
            assertTrue(ancestor.isVisible(), id + " should be visible");
        }
        return node;
    }

    private static Node requireNode(Parent root, String id) {
        root.applyCss();
        root.layout();
        var node = root.lookup("#" + id);
        assertNotNull(node, id);
        return node;
    }

    private static String labelText(Parent root, String id) {
        return assertInstanceOf(Label.class, shownNode(root, id)).getText();
    }

    private static void assertPasswordRulesShown(Parent root) {
        for (String id : List.of("ruleLength", "ruleUpper", "ruleLower", "ruleDigit", "ruleSymbol")) {
            assertFalse(labelText(root, id).isBlank(), id);
        }
    }

    private static void withAuthWindow(Preview preview, AuthWindowAction action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            var navigator = UserApplication.createNavigator();
            Stage stage = new Stage();
            try {
                navigator.themes().setMode(preview.mode());
                stage.setScene(new Scene(navigator.getRoot(), preview.width(), preview.height()));
                stage.show();
                action.run(stage, navigator);
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(30, TimeUnit.SECONDS);
    }

    private record Preview(int width, int height, Mode mode, String suffix) {
        private String signInImage() {
            String name = width == 1024 ? "user-auth-small" : "U-SCR-01-sign-in";
            return mode == Mode.DARK ? name + "-dark" : name;
        }
    }

    @FunctionalInterface
    private interface AuthWindowAction {
        void run(Stage stage, ViewNavigator navigator) throws Exception;
    }

    private static void captureAuth(Stage stage, String name) throws Exception {
        if (!Boolean.getBoolean("chatsystem.capture")) {
            return;
        }
        // Auth steps can mutate an attached ScrollPane. Capture on the next JavaFX pulse.
        var key = new Object();
        stage.getScene().snapshot(result -> {
            Platform.exitNestedEventLoop(key, result.getImage());
            return null;
        }, null);
        var image = (WritableImage) Platform.enterNestedEventLoop(key);
        BufferedImage output = new BufferedImage((int) image.getWidth(), (int) image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < output.getHeight(); y++) {
            for (int x = 0; x < output.getWidth(); x++) {
                output.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        Path directory = Path.of("target", "screenshots");
        Files.createDirectories(directory);
        assertTrue(ImageIO.write(output, "png", directory.resolve(name + ".png").toFile()));
    }
}
