package com.chatsystem.user;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.desktop.controller.MockDataPreviewController;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockGroup;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockMonthlyStatistics;
import com.chatsystem.desktop.mock.model.MockSpamReport;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.desktop.theme.ThemeManager.Mode;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
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
                root.applyCss();
                root.layout();
                enterPreviewWorkspace(root);
                assertTrue(((ToggleButton) root.lookup("#navChats")).isSelected());
                capture(stage, "user-light");
                ((ToggleButton) root.lookup("#navContacts")).fire();
                assertTrue(((ToggleButton) root.lookup("#navContacts")).isSelected());
                assertFalse(((Button) root.lookup("#goBack")).isDisabled());
                ((Button) root.lookup("#goBack")).fire();
                assertTrue(((ToggleButton) root.lookup("#navChats")).isSelected());
                ((Button) root.lookup("#toggleTheme")).fire();
                assertTrue(root.getPseudoClassStates().contains(javafx.css.PseudoClass.getPseudoClass("dark")));
                capture(stage, "user-dark");
                ((Button) root.lookup("#conversationInfo")).fire();
                assertNotNull(root.lookup("#drawer"));
                capture(stage, "user-drawer");
                ((Button) root.lookup("#drawerClose")).fire();
                assertNull(root.lookup("#drawer"));
                ((Button) root.lookup("#openProfile")).fire();
                assertTrue(javafx.stage.Window.getWindows().size() > 1);
                capture(Window.getWindows().stream().filter(window -> window != stage).findFirst().orElseThrow(),
                        "user-modal");
                ((ToggleButton) root.lookup("#navContacts")).fire();
                assertEquals(1, javafx.stage.Window.getWindows().size());
                ((Button) root.lookup("#displayOptions")).fire();
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
                stage.setScene(new Scene(navigator.getRoot(), 1024, 640));
                stage.show();
                navigator.getRoot().applyCss();
                navigator.getRoot().layout();
                assertEquals(1024, stage.getScene().getWidth());
                assertEquals(640, stage.getScene().getHeight());
                var signIn = requireNode(navigator.getRoot(), "authSignIn");
                assertTrue(signIn.getBoundsInLocal().getWidth() > 0);
                assertReadableSignInInBothThemes(stage, navigator, "user-auth-small");
                enterPreviewWorkspace(navigator.getRoot());
                capture(stage, "user-small");
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
    void signInPreviewTogglesPasswordAndNavigatesWithoutCredentials() throws Exception {
        withAuthWindow((stage, navigator) -> {
            var root = navigator.getRoot();
            assertEquals(UserViews.SIGN_IN, navigator.currentViewProperty().get());
            var rail = requireNode(root, "workspaceRail");
            assertFalse(rail.isVisible());
            assertFalse(rail.isManaged());
            assertReadableSignInInBothThemes(stage, navigator, "U-SCR-01-sign-in");

            var toggle = assertInstanceOf(Button.class, requireNode(root, "authSignInShowPassword"));
            var masked = assertInstanceOf(PasswordField.class, requireNode(root, "authSignInPassword"));
            var plain = requireNode(root, "authSignInPasswordVisible");
            var closedEye = toggle.getGraphic();
            assertEquals("Hiện mật khẩu", toggle.getAccessibleText());
            assertFalse(plain.isVisible());
            toggle.fire();
            assertEquals("Ẩn mật khẩu", toggle.getAccessibleText());
            assertNotSame(closedEye, toggle.getGraphic());
            assertTrue(plain.isVisible());
            assertFalse(masked.isVisible());
            toggle.fire();
            assertSame(closedEye, toggle.getGraphic());
            assertFalse(plain.isVisible());
            assertTrue(masked.isVisible());

            enterPreviewWorkspace(root);
            assertEquals(UserViews.CONVERSATIONS, navigator.currentViewProperty().get());
            click(root, "signOut");
            assertEquals(UserViews.SIGN_IN, navigator.currentViewProperty().get());
            assertEquals("", authField(root, "authSignInUsername").getText());
            assertEquals("", authField(root, "authSignInPassword").getText());
        });
    }

    @Test
    void signUpPreviewShowsEachScreenWithoutVerifyingAnAccount() throws Exception {
        withAuthWindow((stage, navigator) -> {
            var root = navigator.getRoot();
            click(root, "authOpenSignUp");
            assertEquals(UserViews.SIGN_UP, navigator.currentViewProperty().get());
            captureAuth(stage, "U-SCR-02-sign-up-username");
            click(root, "authSignUpContinue");
            assertPasswordRulesShown(root);
            captureAuth(stage, "U-SCR-02-sign-up-password");
            click(root, "authSignUpContinue");
            enterText(root, "authSignUpEmail", "preview@example.test");
            captureAuth(stage, "U-SCR-02-sign-up-email");
            click(root, "authSignUpContinue");
            assertEquals(UserViews.VERIFY_EMAIL, navigator.currentViewProperty().get());
            assertEquals("p***@example.test", labelText(root, "maskedEmail"));
            assertPreviewResendDisabled(root, "authVerifyResend");
            captureAuth(stage, "U-SCR-03-verify-email");
            click(root, "authVerifyChangeEmail");
            assertEquals("preview@example.test", authField(root, "authSignUpEmail").getText());
            click(root, "authSignUpContinue");
            click(root, "authVerifySubmit");
            assertEquals(UserViews.SIGN_UP, navigator.currentViewProperty().get());
            assertShowing(requireNode(root, "authSignUpFullName"));
            captureAuth(stage, "U-SCR-02-sign-up-profile");
            click(root, "authSignUpContinue");
            assertEquals(UserViews.SIGN_IN, navigator.currentViewProperty().get());
        });
    }

    @Test
    void passwordRecoveryPreviewShowsEachScreenWithoutResettingAPassword() throws Exception {
        withAuthWindow((stage, navigator) -> {
            var root = navigator.getRoot();
            click(root, "authOpenForgotPassword");
            assertEquals(UserViews.FORGOT_PASSWORD, navigator.currentViewProperty().get());
            captureAuth(stage, "U-SCR-04-forgot-username");
            click(root, "authForgotContinue");
            assertShowing(requireNode(root, "authForgotEmail"));
            captureAuth(stage, "U-SCR-04-forgot-email");
            click(root, "authForgotContinue");
            assertShowing(requireNode(root, "checkEmailStep"));
            assertPreviewResendDisabled(root, "authForgotResend");
            captureAuth(stage, "U-SCR-04-check-email");
            click(root, "authForgotContinue");
            assertEquals(UserViews.FORCE_CHANGE_PASSWORD, navigator.currentViewProperty().get());
            captureAuth(stage, "U-SCR-05-temporary-password");
            click(root, "authForceContinue");
            assertShowing(requireNode(root, "authNewPassword"));
            assertPasswordRulesShown(root);
            captureAuth(stage, "U-SCR-05-new-password");
            click(root, "authForceContinue");
            assertEquals(UserViews.SIGN_IN, navigator.currentViewProperty().get());
        });
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
                ((Button) root.lookup("#openMockData")).fire();
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
                ((Button) root.lookup("#openMockData")).fire();
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

    private static void enterPreviewWorkspace(Parent root) {
        click(root, "authSignInSubmit");
    }

    private static void assertReadableSignInInBothThemes(Stage stage, ViewNavigator navigator, String imageName)
            throws Exception {
        var themes = navigator.themes();
        var original = themes.getMode();
        try {
            for (var mode : Mode.values()) {
                themes.setMode(mode);
                var button = assertInstanceOf(Button.class, requireNode(navigator.getRoot(), "authSignInSubmit"));
                assertShowing(button);
                assertFalse(button.isDisabled());
                var background = button.getBackground();
                assertNotNull(background, mode + ": the sign-in button must have a resolved background");
                assertFalse(background.getFills().isEmpty(), mode + ": the sign-in button must have a background fill");
                var fill = assertInstanceOf(Color.class, background.getFills().getLast().getFill());
                var text = assertInstanceOf(Color.class, button.getTextFill());
                assertEquals(1.0, fill.getOpacity(), mode + ": sign-in button fill must be opaque");
                assertEquals(1.0, text.getOpacity(), mode + ": sign-in button text must be opaque");
                double fillLuminance = luminance(fill);
                double textLuminance = luminance(text);
                double contrast = (Math.max(fillLuminance, textLuminance) + 0.05)
                        / (Math.min(fillLuminance, textLuminance) + 0.05);
                assertTrue(contrast >= 4.5, () -> mode + ": sign-in text contrast was " + contrast);
                captureAuth(stage, mode == Mode.DARK ? imageName + "-dark" : imageName);
            }
        } finally {
            themes.setMode(original);
        }
    }

    private static double luminance(Color color) {
        return 0.2126 * linearChannel(color.getRed()) + 0.7152 * linearChannel(color.getGreen())
                + 0.0722 * linearChannel(color.getBlue());
    }

    private static double linearChannel(double channel) {
        return channel <= 0.04045 ? channel / 12.92 : Math.pow((channel + 0.055) / 1.055, 2.4);
    }

    private static void assertPreviewResendDisabled(Parent root, String id) {
        var button = assertInstanceOf(Button.class, requireNode(root, id));
        assertShowing(button);
        assertTrue(button.isDisabled(), id + " must not offer to resend in the offline preview");
    }

    private static void click(Parent root, String id) {
        var button = assertInstanceOf(Button.class, requireNode(root, id), id);
        assertShowing(button);
        assertFalse(button.isDisabled(), id + " should be enabled");
        button.fire();
    }

    private static void enterText(Parent root, String id, String value) {
        authField(root, id).setText(value);
    }

    private static TextInputControl authField(Parent root, String id) {
        var field = assertInstanceOf(TextInputControl.class, requireNode(root, id), id);
        assertShowing(field);
        return field;
    }

    private static void assertShowing(Node node) {
        for (Node current = node; current != null; current = current.getParent()) {
            assertTrue(current.isVisible(), node.getId() + " should be visible");
        }
    }

    private static Node requireNode(Parent root, String id) {
        root.applyCss();
        root.layout();
        var node = root.lookup("#" + id);
        assertNotNull(node, id);
        return node;
    }

    private static String labelText(Parent root, String id) {
        return assertInstanceOf(Label.class, requireNode(root, id)).getText();
    }

    private static void assertPasswordRulesShown(Parent root) {
        for (String id : List.of("ruleLength", "ruleUpper", "ruleLower", "ruleDigit", "ruleSymbol")) {
            var label = assertInstanceOf(Label.class, requireNode(root, id));
            assertShowing(label);
            assertFalse(label.getText().isBlank(), id);
        }
    }

    private static void withAuthWindow(AuthWindowAction action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            var navigator = UserApplication.createNavigator();
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(navigator.getRoot(), 1280, 720));
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

    @FunctionalInterface
    private interface AuthWindowAction {
        void run(Stage stage, ViewNavigator navigator) throws Exception;
    }

    private static void captureAuth(Stage stage, String name) throws Exception {
        if (!Boolean.getBoolean("chatsystem.capture")) {
            return;
        }
        // Auth steps share a route and mutate an attached ScrollPane in one test task.
        // Wait for a new JavaFX frame so the screenshot includes the updated render tree.
        var key = new Object();
        stage.getScene().snapshot(result -> {
            Platform.exitNestedEventLoop(key, result.getImage());
            return null;
        }, null);
        var image = (WritableImage) Platform.enterNestedEventLoop(key);
        writeCapture(image, name);
    }

    private static void capture(Window stage, String name) throws Exception {
        if (!Boolean.getBoolean("chatsystem.capture")) {
            return;
        }
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        writeCapture(stage.getScene().snapshot(null), name);
    }

    private static void writeCapture(WritableImage image, String name) throws Exception {
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
