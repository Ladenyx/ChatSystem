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
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
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
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
                enterDemoWorkspace(root);
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
                capture(stage, "user-auth-small");
                enterDemoWorkspace(navigator.getRoot());
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
    void signInStartsSignedOutRejectsWrongPasswordAndClearsAfterSignOut() throws Exception {
        withAuthWindow((stage, navigator) -> {
            var root = navigator.getRoot();
            assertEquals(UserViews.SIGN_IN, navigator.currentViewProperty().get());
            assertEquals("", authField(root, "authSignInUsername").getText());
            assertEquals("", assertInstanceOf(PasswordField.class,
                    authField(root, "authSignInPassword")).getText());
            var reveal = requireNode(root, "authSignInShowPassword");
            assertEquals("Hiện mật khẩu", reveal.getAccessibleText());
            var workspaceRail = requireNode(root, "workspaceRail");
            assertFalse(workspaceRail.isVisible());
            assertFalse(workspaceRail.isManaged());
            var workspaceToolbar = requireNode(root, "workspaceToolbar");
            assertFalse(workspaceToolbar.isVisible());
            assertFalse(workspaceToolbar.isManaged());
            capture(stage, "U-SCR-01-sign-in");

            click(root, "authSignInSubmit");
            assertEquals(UserViews.SIGN_IN, navigator.currentViewProperty().get());
            assertFalse(labelText(root, "authSignInUsernameError").isBlank());
            assertFalse(labelText(root, "authSignInPasswordError").isBlank());
            enterText(root, "authSignInUsername", "demo");
            enterText(root, "authSignInPassword", "Incorrect!");
            click(root, "authSignInShowPassword");
            assertEquals("Ẩn mật khẩu", reveal.getAccessibleText());
            assertFalse(requireNode(root, "authSignInPassword").isVisible());
            assertEquals("Incorrect!", authField(root, "authSignInPasswordVisible").getText());
            click(root, "authSignInShowPassword");
            assertFalse(requireNode(root, "authSignInPasswordVisible").isVisible());
            assertEquals("Incorrect!", authField(root, "authSignInPassword").getText());
            click(root, "authSignInSubmit");
            assertEquals(UserViews.SIGN_IN, navigator.currentViewProperty().get());
            assertEquals("Tên đăng nhập hoặc mật khẩu không đúng.", labelText(root, "authSignInFeedback"));
            assertEquals("demo", authField(root, "authSignInUsername").getText());
            assertEquals("", authField(root, "authSignInPassword").getText());

            enterText(root, "authSignInUsername", "other");
            assertInlineClear(root, "authSignInFeedback");
            enterText(root, "authSignInPassword", "Incorrect!");
            click(root, "authSignInSubmit");
            assertInlineError(root, "authSignInFeedback", "Tên đăng nhập hoặc mật khẩu không đúng.");
            enterText(root, "authSignInPassword", "AnotherIncorrect!");
            assertInlineClear(root, "authSignInFeedback");
            click(root, "authSignInSubmit");
            assertInlineError(root, "authSignInFeedback", "Tên đăng nhập hoặc mật khẩu không đúng.");
            click(root, "authOpenForgotPassword");
            click(root, "authForgotCancel");
            assertInlineClear(root, "authSignInFeedback");

            enterDemoWorkspace(root);
            assertEquals(UserViews.CONVERSATIONS, navigator.currentViewProperty().get());
            assertTrue(workspaceRail.isVisible());
            assertTrue(workspaceToolbar.isVisible());
            assertFalse(navigator.canGoBackProperty().get(), "auth routes should not remain in workspace history");
            click(root, "signOut");
            assertEquals(UserViews.SIGN_IN, navigator.currentViewProperty().get());
            assertEquals("", authField(root, "authSignInUsername").getText());
            assertEquals("", authField(root, "authSignInPassword").getText());
            assertFalse(workspaceRail.isVisible());
            assertFalse(navigator.canGoBackProperty().get(), "sign-out must not leave the workspace in history");
        });
    }

    @Test
    void signUpRequiresEmailVerificationBeforePersonalInfo() throws Exception {
        withAuthWindow((stage, navigator) -> {
            var root = navigator.getRoot();
            click(root, "authOpenSignUp");
            assertEquals(UserViews.SIGN_UP, navigator.currentViewProperty().get());
            capture(stage, "U-SCR-02-sign-up");

            var usernameError = "authSignUpUsernameError";
            click(root, "authSignUpContinue");
            assertInlineError(root, usernameError, "Vui lòng nhập tên đăng nhập.");
            enterText(root, "authSignUpUsername", "short");
            assertInlineError(root, usernameError, "Tên đăng nhập phải có từ 6 đến 30 ký tự.");
            enterText(root, "authSignUpUsername", "bad!name");
            assertInlineError(root, usernameError, "Chỉ dùng chữ cái không dấu, chữ số, dấu _ và dấu -.");
            enterText(root, "authSignUpUsername", "demo01");
            assertInlineClear(root, usernameError);
            click(root, "authSignUpContinue");
            assertInlineError(root, usernameError, "Tên đăng nhập đã được đăng ký.");
            enterText(root, "authSignUpUsername", "new_demo");
            assertInlineClear(root, usernameError);
            click(root, "authSignUpContinue");

            var passwordError = "authSignUpPasswordError";
            var confirmationError = "authSignUpPasswordConfirmError";
            enterText(root, "authSignUpPassword", "weak");
            assertInlineError(root, passwordError, "Mật khẩu chưa đáp ứng đủ điều kiện.");
            enterText(root, "authSignUpPassword", "Demo!1234");
            assertInlineClear(root, passwordError);
            enterText(root, "authSignUpPasswordConfirm", "Wrong!1234");
            assertInlineError(root, confirmationError, "Mật khẩu nhập lại chưa khớp.");
            enterText(root, "authSignUpPasswordConfirm", "Demo!1234");
            assertInlineClear(root, confirmationError);
            enterText(root, "authSignUpPassword", "Other!1234");
            assertInlineError(root, confirmationError, "Mật khẩu nhập lại chưa khớp.");
            enterText(root, "authSignUpPasswordConfirm", "Other!1234");
            assertInlineClear(root, confirmationError);
            click(root, "authSignUpContinue");

            var emailError = "authSignUpEmailError";
            click(root, "authSignUpContinue");
            assertInlineError(root, emailError, "Vui lòng nhập email.");
            enterText(root, "authSignUpEmail", "missing-at.example.test");
            assertInlineError(root, emailError, "Vui lòng nhập email hợp lệ.");
            enterText(root, "authSignUpEmail", "banmoi@-example.test");
            assertInlineError(root, emailError, "Vui lòng nhập email hợp lệ.");
            enterText(root, "authSignUpEmail", "a".repeat(250) + "@example.test");
            assertInlineError(root, emailError, "Email không được vượt quá 254 ký tự.");
            enterText(root, "authSignUpEmail", "demo@example.test");
            assertInlineClear(root, emailError);
            click(root, "authSignUpContinue");
            assertInlineError(root, emailError, "Email đã được đăng ký.");
            enterText(root, "authSignUpEmail", "new_demo@example.test");
            assertInlineClear(root, emailError);
            click(root, "authSignUpContinue");
            assertEquals(UserViews.VERIFY_EMAIL, navigator.currentViewProperty().get());
            assertNull(root.lookup("#authSignUpFullName"));
            var verify = assertInstanceOf(Button.class, requireNode(root, "authVerifySubmit"));
            assertTrue(verify.isDisabled(), "empty verification form must not submit");
            assertTrue(requireNode(root, "authVerifyResend").isDisabled());
            capture(stage, "U-SCR-03-verify-email");

            enterVerificationCode(root, "12345");
            assertTrue(verify.isDisabled(), "incomplete verification form must not submit");
            enterVerificationCode(root, "123450");
            assertFalse(verify.isDisabled());
            click(root, "authVerifySubmit");
            assertEquals(UserViews.VERIFY_EMAIL, navigator.currentViewProperty().get());
            assertNull(root.lookup("#authSignUpFullName"));
            enterVerificationCode(root, "123456");
            click(root, "authVerifySubmit");
            assertEquals(UserViews.SIGN_UP, navigator.currentViewProperty().get());

            capture(stage, "U-SCR-02-sign-up-personal-info");
            click(root, "authSignUpContinue");
            assertInlineError(root, "authSignUpFullNameError", "Vui lòng nhập họ tên.");
            enterText(root, "authSignUpFullName", "a".repeat(101));
            assertInlineError(root, "authSignUpFullNameError", "Họ tên không được vượt quá 100 ký tự.");
            enterText(root, "authSignUpFullName", "Nguyễn Văn Mẫu");
            assertInlineClear(root, "authSignUpFullNameError");
            enterText(root, "authSignUpAddress", "a".repeat(256));
            assertInlineError(root, "authSignUpAddressError", "Địa chỉ không được vượt quá 255 ký tự.");
            enterText(root, "authSignUpAddress", "");
            assertInlineClear(root, "authSignUpAddressError");
            var birthDate = assertInstanceOf(DatePicker.class, requireNode(root, "authSignUpBirthDate"));
            birthDate.getEditor().setText("not-a-date");
            assertInlineError(root, "authSignUpBirthDateError", "Ngày sinh không hợp lệ.");
            birthDate.getEditor().setText("31/02/2026");
            birthDate.commitValue(); // The control also attempts to commit when it loses focus.
            assertEquals("31/02/2026", birthDate.getEditor().getText());
            pressAndReleaseEnter(birthDate);
            assertEquals("31/02/2026", birthDate.getEditor().getText());
            assertInlineError(root, "authSignUpBirthDateError", "Ngày sinh không hợp lệ.");
            click(root, "authSignUpContinue");
            assertEquals(UserViews.SIGN_UP, navigator.currentViewProperty().get());
            assertEquals("31/02/2026", birthDate.getEditor().getText());
            assertInlineError(root, "authSignUpBirthDateError", "Ngày sinh không hợp lệ.");
            var tomorrow = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1);
            birthDate.getEditor().setText(birthDate.getConverter().toString(tomorrow));
            assertInlineError(root, "authSignUpBirthDateError", "Ngày sinh không được ở tương lai.");
            birthDate.getEditor().setText("");
            assertInlineClear(root, "authSignUpBirthDateError");
            assertOptionalGenderChoices(root);
            click(root, "authSignUpContinue");
            assertEquals(UserViews.SIGN_IN, navigator.currentViewProperty().get());
            assertEquals("", authField(root, "authSignInUsername").getText());
            assertEquals("", authField(root, "authSignInPassword").getText());
        });
    }

    @Test
    void passwordResetRequiresCorrectEmailAndTemporaryPasswordBeforeSaving() throws Exception {
        withAuthWindow((stage, navigator) -> {
            var root = navigator.getRoot();
            click(root, "authOpenForgotPassword");
            assertEquals(UserViews.FORGOT_PASSWORD, navigator.currentViewProperty().get());
            capture(stage, "U-SCR-04-forgot-password");
            click(root, "authForgotContinue");
            assertInlineError(root, "authForgotUsernameError", "Nhập tên đăng nhập.");
            enterText(root, "authForgotUsername", "unknown_demo");
            click(root, "authForgotContinue");
            assertInlineError(root, "authForgotUsernameError", "Thông tin chưa đúng.");
            enterText(root, "authForgotUsername", "demo");
            assertInlineClear(root, "authForgotUsernameError");
            click(root, "authForgotContinue");
            assertTrue(labelText(root, "authForgotMaskedEmail").contains("d***@example.test"));

            var emailError = "authForgotEmailError";
            enterText(root, "authForgotEmail", "demo");
            assertInlineError(root, emailError, "Nhập email hợp lệ.");
            enterText(root, "authForgotEmail", "someone@example.test");
            assertInlineError(root, emailError, "Email không khớp với gợi ý.");
            enterText(root, "authForgotEmail", "demo@example.invalid");
            assertInlineError(root, emailError, "Email không khớp với gợi ý.");
            enterText(root, "authForgotEmail", "diana@example.test");
            assertInlineClear(root, emailError);
            click(root, "authForgotContinue");
            assertInlineError(root, emailError, "Thông tin chưa đúng.");
            assertEquals(UserViews.FORGOT_PASSWORD, navigator.currentViewProperty().get());
            assertTrue(authField(root, "authForgotEmail").isVisible());
            enterText(root, "authForgotEmail", "demo@example.test");
            assertInlineClear(root, emailError);
            click(root, "authForgotContinue");
            assertTrue(requireNode(root, "authForgotResend").isDisabled());
            click(root, "authForgotContinue");
            assertEquals(UserViews.FORCE_CHANGE_PASSWORD, navigator.currentViewProperty().get());
            capture(stage, "U-SCR-05-force-change-password");

            click(root, "authForceContinue");
            assertInlineError(root, "authTemporaryPasswordError", "Nhập mật khẩu tạm.");
            enterText(root, "authTemporaryPassword", "Wrong!1234");
            click(root, "authForceContinue");
            assertInlineError(root, "authTemporaryPasswordError", "Mật khẩu tạm không hợp lệ.");
            assertEquals(UserViews.FORCE_CHANGE_PASSWORD, navigator.currentViewProperty().get());
            assertTrue(authField(root, "authTemporaryPassword").isVisible());
            enterText(root, "authTemporaryPassword", "Tam!1234");
            assertInlineClear(root, "authTemporaryPasswordError");
            click(root, "authForceContinue");
            capture(stage, "U-SCR-05-new-password");
            enterText(root, "authNewPassword", "weak");
            assertInlineError(root, "authNewPasswordError", "Mật khẩu mới chưa đáp ứng đủ điều kiện.");
            enterText(root, "authNewPassword", "Updated!1234");
            assertInlineClear(root, "authNewPasswordError");
            enterText(root, "authConfirmNewPassword", "Mismatch!1234");
            assertInlineError(root, "authConfirmNewPasswordError", "Mật khẩu chưa khớp.");
            click(root, "authForceContinue");
            assertEquals(UserViews.FORCE_CHANGE_PASSWORD, navigator.currentViewProperty().get());
            enterText(root, "authConfirmNewPassword", "Updated!1234");
            assertInlineClear(root, "authConfirmNewPasswordError");
            enterText(root, "authNewPassword", "Different!1234");
            assertInlineError(root, "authConfirmNewPasswordError", "Mật khẩu chưa khớp.");
            enterText(root, "authConfirmNewPassword", "Different!1234");
            assertInlineClear(root, "authConfirmNewPasswordError");
            click(root, "authForceContinue");
            assertEquals(UserViews.SIGN_IN, navigator.currentViewProperty().get());
            assertEquals("", authField(root, "authSignInUsername").getText());
            assertEquals("", authField(root, "authSignInPassword").getText());
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
                enterDemoWorkspace(root);
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

    private static void enterDemoWorkspace(Parent root) {
        enterText(root, "authSignInUsername", "demo");
        enterText(root, "authSignInPassword", "Demo!1234");
        click(root, "authSignInSubmit");
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

    private static void assertInlineError(Parent root, String id, String message) {
        var label = assertInstanceOf(Label.class, requireNode(root, id));
        assertShowing(label);
        assertEquals(message, label.getText());
    }

    private static void assertInlineClear(Parent root, String id) {
        var label = assertInstanceOf(Label.class, requireNode(root, id));
        assertFalse(label.isVisible(), id + " should be hidden when valid");
        assertTrue(label.getText().isBlank(), id + " should clear stale errors");
    }

    private static void assertOptionalGenderChoices(Parent root) {
        var male = assertInstanceOf(RadioButton.class, requireNode(root, "authSignUpGenderMale"));
        var female = assertInstanceOf(RadioButton.class, requireNode(root, "authSignUpGenderFemale"));
        var other = assertInstanceOf(RadioButton.class, requireNode(root, "authSignUpGenderOther"));
        var options = List.of(male, female, other);
        assertEquals(List.of("Nam", "Nữ", "Khác"), options.stream().map(RadioButton::getText).toList());
        var group = male.getToggleGroup();
        assertNotNull(group);
        options.forEach(option -> {
            assertShowing(option);
            assertSame(group, option.getToggleGroup());
        });
        assertNull(group.getSelectedToggle());
        male.fire();
        assertSame(male, group.getSelectedToggle());
        other.fire();
        assertSame(other, group.getSelectedToggle());
        assertFalse(male.isSelected(), "Selecting another option must clear the previous one");
        click(root, "clearGenderButton");
        assertNull(group.getSelectedToggle(), "An optional field can return to no selection");
    }

    private static void pressAndReleaseEnter(DatePicker datePicker) {
        var editor = datePicker.getEditor();
        Event.fireEvent(editor,
                new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false));
        Event.fireEvent(editor,
                new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.ENTER, false, false, false, false));
    }

    private static void enterVerificationCode(Parent root, String code) {
        for (int i = 0; i < code.length(); i++) {
            enterText(root, "authVerifyDigit" + (i + 1), code.substring(i, i + 1));
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
