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
import com.chatsystem.user.control.ChatMessageRow;
import com.chatsystem.user.service.model.TargetUserOption;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.chart.BarChart;
import javafx.scene.image.WritableImage;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
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
                assertEquals("WhatsUp", stage.getTitle());
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
                navigator.navigate(MockDataPreviewController.VIEW);
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
                navigator.navigate(MockDataPreviewController.VIEW);
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
                assertNotNull(root.lookup("#addConversationMember"));
                assertNotNull(root.lookup("#renameConversationGroup"));
                assertNotNull(root.lookup("#resignConversationAdmin"));
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

    @Test
    @SuppressWarnings("unchecked")
    void groupManagementModalsValidateAndCaptureInBothThemes() throws Exception {
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

                ((Button) root.lookup("#createGroup")).fire();
                Window createModal = modalWindow(stage);
                var createRoot = createModal.getScene().getRoot();
                createRoot.applyCss();
                createRoot.layout();
                assertTrue(createRoot.lookup("#groupFormSubmit").isDisabled());
                ((TextField) createRoot.lookup("#groupNameField")).setText("Nhóm Ôn tập Java");
                assertTrue(createRoot.lookup("#groupFormSubmit").isDisabled());
                ((CheckBox) createRoot.lookup("#group-candidate-usr-02")).fire();
                assertFalse(createRoot.lookup("#groupFormSubmit").isDisabled());
                assertEquals("Chọn thành viên (1 đã chọn)",
                        ((Label) createRoot.lookup("#groupSelectionSummary")).getText());
                createRoot.applyCss();
                createRoot.layout();
                capture(createModal, "group-create-light");
                createModal.hide();

                var conversations = (ListView<MockConversation>) root.lookup("#conversationItems");
                var group = conversations.getItems().stream()
                        .filter(item -> item.id().equals("grp-01")).findFirst().orElseThrow();
                conversations.getSelectionModel().select(group);
                ((Button) root.lookup("#conversationInfo")).fire();
                root.applyCss();
                root.layout();

                ((Button) root.lookup("#addConversationMember")).fire();
                Window addModal = modalWindow(stage);
                assertTrue(addModal.getScene().getRoot().lookup("#groupFormSubmit").isDisabled());
                addModal.hide();

                ((Button) root.lookup("#renameConversationGroup")).fire();
                Window renameModal = modalWindow(stage);
                var renameRoot = renameModal.getScene().getRoot();
                assertFalse(renameRoot.lookup("#groupFormSubmit").isDisabled());
                ((TextField) renameRoot.lookup("#groupNameField")).clear();
                assertTrue(renameRoot.lookup("#groupFormSubmit").isDisabled());
                renameModal.hide();

                navigator.themes().toggle();
                ((Button) root.lookup("#resignConversationAdmin")).fire();
                Window successorModal = modalWindow(stage);
                var successorRoot = successorModal.getScene().getRoot();
                successorRoot.applyCss();
                successorRoot.layout();
                assertTrue(successorRoot.lookup("#groupFormSubmit").isDisabled());
                ((RadioButton) successorRoot.lookup("#group-candidate-usr-03")).fire();
                assertFalse(successorRoot.lookup("#groupFormSubmit").isDisabled());
                assertTrue(((Label) successorRoot.lookup("#groupSelectionSummary")).getText().startsWith("Đã chọn:"));
                successorRoot.applyCss();
                successorRoot.layout();
                capture(successorModal, "group-successor-leave-dark");
                successorModal.hide();
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
    void aiAssistantSuggestsRepliesAndRewritesDraftInBothThemes() throws Exception {
        var stageRef = new AtomicReference<Stage>();
        var navRef = new AtomicReference<com.chatsystem.desktop.navigation.ViewNavigator>();
        try {
            onFx(() -> {
                Stage stage = new Stage();
                var navigator = UserApplication.createNavigator();
                stageRef.set(stage);
                navRef.set(navigator);
                stage.setX(0);
                stage.setY(0);
                stage.setScene(new Scene(navigator.getRoot(), 1280, 720));
                stage.show();
                var root = navigator.getRoot();
                enterPreviewWorkspace(root);
                root.applyCss();
                root.layout();

                var conversations = (ListView<MockConversation>) root.lookup("#conversationItems");
                var direct = conversations.getItems().stream()
                        .filter(item -> item.id().equals("direct-01")).findFirst().orElseThrow();
                conversations.getSelectionModel().select(direct);
                navigator.themes().toggle();
                root.applyCss();
                root.layout();
                ((Button) root.lookup("#aiAssistantButton")).fire();
                return null;
            });

            onFx(() -> {
                Stage stage = stageRef.get();
                Window popup = aiPopupWindow();
                var popupRoot = popup.getScene().getRoot();
                assertEquals("Gợi ý trả lời", ((Label) popupRoot.lookup("#aiHeading")).getText());
                assertEquals(3, popupRoot.lookupAll(".chat-ai-option").size());
                captureWithPopover(stage, popup, "pr-52-ai-replies-dark");

                ((Button) popupRoot.lookup("#aiClose")).fire();
                assertFalse(popup.isShowing());

                navRef.get().themes().toggle();
                var root = navRef.get().getRoot();
                var draft = (TextArea) root.lookup("#draftField");
                draft.setText("mai mình gửi bản tổng hợp các màn hình. Bạn xem giúp mình phần Hội thoại trước nhé.");
                root.applyCss();
                root.layout();
                ((Button) root.lookup("#aiAssistantButton")).fire();
                return null;
            });

            onFx(() -> {
                Stage stage = stageRef.get();
                Window popup = aiPopupWindow();
                var popupRoot = popup.getScene().getRoot();
                assertEquals("Viết lại bản nháp", ((Label) popupRoot.lookup("#aiHeading")).getText());
                var options = popupRoot.lookupAll(".chat-ai-option").stream()
                        .map(Button.class::cast).toList();
                assertEquals(3, options.size());
                captureWithPopover(stage, popup, "pr-52-ai-rewrites-light");

                options.getFirst().fire();
                assertFalse(popup.isShowing());
                var draft = (TextArea) navRef.get().getRoot().lookup("#draftField");
                assertTrue(draft.getText().startsWith("Mai mình"));
                return null;
            });
        } finally {
            onFx(() -> {
                if (navRef.get() != null) {
                    navRef.get().close();
                }
                if (stageRef.get() != null) {
                    stageRef.get().close();
                }
                return null;
            });
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void spamReportDialogFlowsThroughPickPreviewAndAcceptedInBothThemes() throws Exception {
        var stageRef = new AtomicReference<Stage>();
        var navRef = new AtomicReference<com.chatsystem.desktop.navigation.ViewNavigator>();
        try {
            onFx(() -> {
                Stage stage = new Stage();
                var navigator = UserApplication.createNavigator();
                stageRef.set(stage);
                navRef.set(navigator);
                stage.setX(0);
                stage.setY(0);
                stage.setScene(new Scene(navigator.getRoot(), 1280, 720));
                stage.show();
                var root = navigator.getRoot();
                enterPreviewWorkspace(root);
                root.applyCss();
                root.layout();

                var conversations = (ListView<MockConversation>) root.lookup("#conversationItems");
                var group = conversations.getItems().stream()
                        .filter(item -> item.id().equals("grp-01")).findFirst().orElseThrow();
                conversations.getSelectionModel().select(group);
                ((Button) root.lookup("#conversationInfo")).fire();
                root.applyCss();
                root.layout();
                ((Button) root.lookup("#reportConversation")).fire();
                return null;
            });

            onFx(() -> {
                Window modal = reportModalWindow(stageRef.get());
                var modalRoot = modal.getScene().getRoot();
                assertFalse(modalRoot.lookupAll(".chat-report-excerpt-card").isEmpty());
                assertEquals("1 / 20 đã chọn", ((Label) modalRoot.lookup("#reportSelectionCount")).getText());
                var reason = (TextField) modalRoot.lookup("#reportReasonField");
                assertEquals("", reason.getText());
                capture(modal, "spam-report-pick-light");

                ((Button) modalRoot.lookup("#reportPreviewSubmit")).fire();
                return null;
            });

            onFx(() -> {
                Window modal = reportModalWindow(stageRef.get());
                var modalRoot = modal.getScene().getRoot();
                var reason = (TextField) modalRoot.lookup("#reportReasonField");
                PseudoClass invalid = PseudoClass.getPseudoClass("invalid");
                assertTrue(modalRoot.lookup("#reportPickFeedback").isVisible());
                assertTrue(reason.getPseudoClassStates().contains(invalid));
                capture(modal, "spam-report-pick-error-light");

                reason.setText("Nội dung quảng bá lặp lại trong nhóm.");
                assertFalse(reason.getPseudoClassStates().contains(invalid));
                ((Button) modalRoot.lookup("#reportPreviewSubmit")).fire();
                navRef.get().themes().toggle();
                modalRoot.applyCss();
                modalRoot.layout();
                return null;
            });

            onFx(() -> {
                Window modal = reportModalWindow(stageRef.get());
                var modalRoot = modal.getScene().getRoot();
                assertTrue(modalRoot.lookup("#reportSubmitConfirmed").isVisible());
                capture(modal, "spam-report-preview-dark");

                ((Button) modalRoot.lookup("#reportSubmitConfirmed")).fire();
                return null;
            });

            onFx(() -> {
                Window modal = reportModalWindow(stageRef.get());
                var modalRoot = modal.getScene().getRoot();
                assertTrue(modalRoot.lookup("#reportAcceptedDone").isVisible());
                assertEquals("Chờ xử lý", ((Label) modalRoot.lookup(".chat-report-status-text")).getText());
                capture(modal, "spam-report-accepted-dark");
                ((Button) modalRoot.lookup("#reportAcceptedDone")).fire();
                assertFalse(modal.isShowing());
                return null;
            });
        } finally {
            onFx(() -> {
                if (navRef.get() != null) {
                    navRef.get().close();
                }
                if (stageRef.get() != null) {
                    stageRef.get().close();
                }
                return null;
            });
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void messageHoverMenuOpensSpamReportWithPreselectedSenderAndMessage() throws Exception {
        var stageRef = new AtomicReference<Stage>();
        var navRef = new AtomicReference<com.chatsystem.desktop.navigation.ViewNavigator>();
        var targetMessageRef = new AtomicReference<MockMessage>();
        try {
            onFx(() -> {
                Stage stage = new Stage();
                var navigator = UserApplication.createNavigator();
                stageRef.set(stage);
                navRef.set(navigator);
                stage.setX(0);
                stage.setY(0);
                stage.setScene(new Scene(navigator.getRoot(), 1280, 720));
                stage.show();
                var root = navigator.getRoot();
                enterPreviewWorkspace(root);
                root.applyCss();
                root.layout();

                var data = MockData.create();
                String currentUserId = data.previewUser().id();
                var conversations = (ListView<MockConversation>) root.lookup("#conversationItems");
                var direct = conversations.getItems().stream()
                        .filter(item -> item.id().equals("direct-01")).findFirst().orElseThrow();
                conversations.getSelectionModel().select(direct);
                root.applyCss();
                root.layout();

                var directMessagesById = data.messagesFor("direct-01").stream()
                        .collect(java.util.stream.Collectors.toMap(MockMessage::id, m -> m));
                var directRows = root.lookupAll(".chat-message-row").stream()
                        .map(ChatMessageRow.class::cast).toList();
                assertTrue(directRows.stream()
                        .filter(row -> directMessagesById.get(row.messageId()).state() == MockMessage.State.RECALLED)
                        .allMatch(row -> row.lookup(".chat-message-more") == null),
                        "Recalled messages must not offer message actions");
                assertTrue(directRows.stream()
                        .filter(row -> directMessagesById.get(row.messageId()).senderId().equals(currentUserId))
                        .allMatch(row -> row.lookup(".chat-message-more") == null),
                        "Outgoing own messages must not offer Báo cáo");
                assertTrue(directRows.stream()
                        .filter(row -> !directMessagesById.get(row.messageId()).senderId().equals(currentUserId)
                                && directMessagesById.get(row.messageId()).acceptedByServer()
                                && directMessagesById.get(row.messageId()).state() != MockMessage.State.RECALLED)
                        .allMatch(row -> row.lookup(".chat-message-more") != null),
                        "Eligible incoming messages offer the hover menu");

                var group = conversations.getItems().stream()
                        .filter(item -> item.id().equals("grp-01")).findFirst().orElseThrow();
                conversations.getSelectionModel().select(group);
                root.applyCss();
                root.layout();

                String firstMemberId = group.participantIds().stream()
                        .filter(id -> !id.equals(currentUserId)).findFirst().orElseThrow();
                var groupMessages = data.messagesFor("grp-01");
                MockMessage targetMessage = null;
                for (int i = groupMessages.size() - 1; i >= 0; i--) {
                    MockMessage candidate = groupMessages.get(i);
                    if (candidate.acceptedByServer()
                            && candidate.state() != MockMessage.State.RECALLED
                            && !candidate.senderId().equals(currentUserId)
                            && !candidate.senderId().equals(firstMemberId)) {
                        targetMessage = candidate;
                        break;
                    }
                }
                assertNotNull(targetMessage);
                targetMessageRef.set(targetMessage);
                return null;
            });

            onFx(() -> {
                var root = navRef.get().getRoot();
                root.applyCss();
                root.layout();
                ((ScrollPane) root.lookup("#historyScroll")).setVvalue(1);
                root.applyCss();
                root.layout();

                String targetId = targetMessageRef.get().id();
                var targetRow = root.lookupAll(".chat-message-row").stream()
                        .map(ChatMessageRow.class::cast)
                        .filter(row -> row.messageId().equals(targetId))
                        .findFirst().orElseThrow();
                Button moreBtn = (Button) targetRow.lookup(".chat-message-more");
                assertNotNull(moreBtn);
                moreBtn.fire();
                return null;
            });

            onFx(() -> {
                Window popup = messageMenuPopup();
                assertTrue(popup.isShowing());
                Stage stage = stageRef.get();
                assertTrue(popup.getY() >= stage.getY() && popup.getY() < stage.getY() + stage.getHeight());
                var menuRoot = popup.getScene().getRoot();
                Button reportAction = (Button) menuRoot.lookup("#messageActionReport");
                assertNotNull(reportAction);
                assertEquals("Báo cáo", reportAction.getText());
                captureWithPopover(stage, popup, "user-message-menu-light");

                navRef.get().themes().toggle();
                navRef.get().getRoot().applyCss();
                navRef.get().getRoot().layout();
                menuRoot.applyCss();
                menuRoot.layout();
                captureWithPopover(stage, popup, "user-message-menu-dark");

                navRef.get().themes().toggle();
                reportAction.fire();
                return null;
            });

            onFx(() -> {
                Window modal = reportModalWindow(stageRef.get());
                var modalRoot = modal.getScene().getRoot();
                var combo = (ComboBox<TargetUserOption>) modalRoot.lookup(".chat-report-combo");
                assertNotNull(combo.getValue());
                assertEquals(targetMessageRef.get().senderId(), combo.getValue().id());
                assertEquals("1 / 20 đã chọn", ((Label) modalRoot.lookup("#reportSelectionCount")).getText());
                assertEquals("", ((TextField) modalRoot.lookup("#reportReasonField")).getText());
                capture(modal, "spam-report-from-message-light");

                ((Button) modalRoot.lookup("#reportPickCancel")).fire();
                assertFalse(modal.isShowing());
                return null;
            });
        } finally {
            onFx(() -> {
                if (navRef.get() != null) {
                    navRef.get().close();
                }
                if (stageRef.get() != null) {
                    stageRef.get().close();
                }
                return null;
            });
        }
    }

    private static Window messageMenuPopup() {
        return Window.getWindows().stream()
                .filter(window -> window.isShowing() && window.getScene() != null
                        && window.getScene().getRoot().lookup("#messageActionMenu") != null)
                .findFirst().orElseThrow();
    }

    private static Window reportModalWindow(Window owner) {
        return Window.getWindows().stream()
                .filter(window -> window != owner && window.isShowing()
                        && window.getScene().getRoot().lookup("#reportPreviewSubmit") != null)
                .findFirst().orElseThrow();
    }

    private static Window aiPopupWindow() {
        return Window.getWindows().stream()
                .filter(window -> window.isShowing() && window.getScene().getRoot().lookup("#aiAssistant") != null)
                .findFirst().orElseThrow();
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(20, TimeUnit.SECONDS);
    }

    private static Window modalWindow(Window owner) {
        return Window.getWindows().stream()
                .filter(window -> window != owner && window.isShowing()
                        && window.getScene().getRoot().lookup("#groupFormSubmit") != null)
                .findFirst().orElseThrow();
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
        BufferedImage output = toBufferedImage(stage.getScene().snapshot(null));
        Path directory = Path.of("target", "screenshots");
        Files.createDirectories(directory);
        ImageIO.write(output, "png", directory.resolve(name + ".png").toFile());
    }

    private static void captureWithPopover(Stage stage, Window popup, String name) throws Exception {
        if (!Boolean.getBoolean("chatsystem.capture")) {
            return;
        }
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        popup.getScene().getRoot().applyCss();
        popup.getScene().getRoot().layout();
        BufferedImage base = toBufferedImage(stage.getScene().snapshot(null));
        var transparent = new SnapshotParameters();
        transparent.setFill(Color.TRANSPARENT);
        BufferedImage overlay = toBufferedImage(popup.getScene().getRoot().snapshot(transparent, null));
        int x = (int) Math.round((popup.getX() + popup.getScene().getX())
                - (stage.getX() + stage.getScene().getX()));
        int y = (int) Math.round((popup.getY() + popup.getScene().getY())
                - (stage.getY() + stage.getScene().getY()));
        var graphics = base.createGraphics();
        try {
            graphics.drawImage(overlay, x, y, null);
        } finally {
            graphics.dispose();
        }
        Path directory = Path.of("target", "screenshots");
        Files.createDirectories(directory);
        ImageIO.write(base, "png", directory.resolve(name + ".png").toFile());
    }

    private static BufferedImage toBufferedImage(WritableImage image) {
        BufferedImage output = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < output.getHeight(); y++) {
            for (int x = 0; x < output.getWidth(); x++) {
                output.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        return output;
    }
}
