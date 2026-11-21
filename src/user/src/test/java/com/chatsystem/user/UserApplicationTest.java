package com.chatsystem.user;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.desktop.controller.MockDataPreviewController;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockGroup;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockMessage.State;
import com.chatsystem.desktop.mock.model.MockMonthlyStatistics;
import com.chatsystem.desktop.mock.model.MockSpamReport;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.user.control.ChatMessageRow;
import com.chatsystem.user.controller.ai.OfflineAiAssistantService.Scenario;
import com.chatsystem.user.controller.chat.ChatDemoFlow;
import com.chatsystem.user.controller.chat.OfflineChatDemoService;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
                assertTrue(((ToggleButton) root.lookup("#navChats")).isSelected());
                assertTrue(root.lookup("#emptyWorkspace").isVisible());
                capture(stage, "user-light");
                ((ToggleButton) root.lookup("#navContacts")).fire();
                assertTrue(((ToggleButton) root.lookup("#navContacts")).isSelected());
                assertFalse(((Button) root.lookup("#goBack")).isDisabled());
                ((Button) root.lookup("#goBack")).fire();
                assertTrue(((ToggleButton) root.lookup("#navChats")).isSelected());
                var settings = (MenuButton) root.lookup("#workspaceSettings");
                settings.show();
                assertTrue(settings.isShowing());
                settings.hide();
                fireSetting(root, "toggleTheme");
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
                ((Button) root.lookup("#conversationInfo")).fire();
                assertNotNull(root.lookup("#drawer"));
                capture(stage, "user-drawer");
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
    void shellFitsTheMinimumContentSize() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();
            var navigator = UserApplication.createNavigator();
            try {
                var root = navigator.getRoot();
                var scene = new Scene(root, 1024, 640);
                stage.setScene(scene);
                root.resize(1024, 640);
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
    void chatHistoryLoadsOlderMessagesWithoutMovingTheAnchor() throws Exception {
        Stage stage = onFx(() -> {
            Stage window = new Stage();
            new UserApplication().start(window);
            selectChat(window.getScene().getRoot(), "grp-01");
            return window;
        });
        try {
            double before = onFx(() -> {
                var root = stage.getScene().getRoot();
                var scroll = (ScrollPane) root.lookup("#historyScroll");
                assertEquals(1, scroll.getVvalue(), 0.001);
                assertEquals(50, rowCount(root));
                scroll.setVvalue(0);
                root.layout();
                double y = messageY(root, "msg-grp-01-7");
                ((Button) root.lookup("#loadOlderMessages")).fire();
                return y;
            });
            onFx(() -> {
                var root = stage.getScene().getRoot();
                assertEquals(56, rowCount(root));
                assertEquals(before, messageY(root, "msg-grp-01-7"), 2);
                return null;
            });
        } finally {
            onFx(() -> { stage.close(); return null; });
        }
    }

    @Test
    void chatPreservesDraftsAndWiresSendAndBulkDelete() throws Exception {
        onFx(() -> {
            Stage stage = new Stage();
            var navigator = UserApplication.createNavigator();
            stage.setScene(new Scene(navigator.getRoot(), 1280, 720));
            stage.show();
            try {
                var root = navigator.getRoot();
                selectChat(root, "direct-01");
                var draft = (TextArea) root.lookup("#draftField");
                draft.setText("Bản nháp hội thoại thứ nhất");
                selectChat(root, "direct-02");
                assertEquals("", draft.getText());
                draft.setText("Mình đã cập nhật tài liệu.");
                selectChat(root, "direct-01");
                assertEquals("Bản nháp hội thoại thứ nhất", draft.getText());
                selectChat(root, "direct-02");
                assertEquals("Mình đã cập nhật tài liệu.", draft.getText());
                capture(stage, "chat-direct-light");
                ((Button) root.lookup("#sendMessage")).fire();
                assertEquals("", draft.getText());
                var chat = ChatDemoFlow.get(navigator).chat();
                var sent = chat.latest("direct-02");
                assertEquals("Mình đã cập nhật tài liệu.", sent.text());
                assertEquals(MockMessage.State.PENDING, sent.state());
                assertEquals("direct-02", ((MockConversation) ((ListView<?>) root.lookup("#conversationItems"))
                        .getItems().getFirst()).id());
                var more = (Button) root.lookup("#actions-" + sent.id());
                more.getContextMenu().getItems().stream().filter(item -> item.getText().equals("Chọn nhiều tin nhắn"))
                        .findFirst().orElseThrow().fire();
                ((CheckBox) root.lookup("#select-msg-direct-02-8")).fire();
                assertFalse(draft.getParent().isVisible());
                capture(stage, "chat-selection-light");
                ((Button) root.lookup("#deleteSelectedButton")).fire();
                confirmDialog(stage, "Xóa phía mình");
                assertEquals(7, rowCount(root));
                assertFalse(chat.page("direct-02", 50).messages().stream().anyMatch(message -> message.id().equals(sent.id())));
                assertTrue(draft.getParent().isVisible());

                selectChat(root, "direct-04");
                assertFalse(draft.getParent().isVisible());
                assertTrue(rowCount(root) > 0);
                selectChat(root, "grp-03");
                assertTrue(draft.isDisabled());
                assertTrue(((Button) root.lookup("#sendMessage")).isDisabled());
                navigator.themes().toggle();
                capture(stage, "chat-missing-key-dark");
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
    }

    @Test
    void chatShowsFailedSendingThenRetriesInPlaceAndAllowsRecall() throws Exception {
        var navigator = onFx(UserApplication::createNavigator);
        Stage stage = onFx(() -> {
            Stage window = new Stage();
            window.setScene(new Scene(navigator.getRoot(), 1280, 720));
            window.show();
            return window;
        });
        try {
            var chat = onFx(() -> ChatDemoFlow.get(navigator).chat());
            var sent = onFx(() -> {
                var root = navigator.getRoot();
                selectChat(root, "direct-03");
                ((TextArea) root.lookup("#draftField")).setText("Tin đang thử gửi.");
                ((Button) root.lookup("#sendMessage")).fire();
                return chat.latest("direct-03");
            });
            assertEquals(State.PENDING, sent.state());
            awaitState(chat, "direct-03", State.FAILED);
            onFx(() -> {
                var root = navigator.getRoot();
                capture(stage, "chat-retry-light");
                ((Button) root.lookup("#retry-" + sent.id())).fire();
                assertEquals(sent.id(), chat.latest("direct-03").id());
                assertEquals(State.PENDING, chat.latest("direct-03").state());
                return null;
            });
            awaitState(chat, "direct-03", State.STORED);
            onFx(() -> {
                var root = navigator.getRoot();
                assertEquals(9, rowCount(root));
                var more = (Button) root.lookup("#actions-" + sent.id());
                more.getContextMenu().getItems().stream().filter(item -> item.getText().equals("Thu hồi với mọi người"))
                        .findFirst().orElseThrow().fire();
                confirmDialog(stage, "Thu hồi với mọi người");
                assertEquals(State.RECALLED, chat.latest("direct-03").state());
                assertEquals(9, rowCount(root));
                selectChat(root, "direct-01");
                var expired = (Button) root.lookup("#actions-demo-expired-recall");
                assertTrue(expired.getContextMenu().getItems().stream()
                        .anyMatch(item -> item.getText().contains("quá 48 giờ") && item.isDisable()));
                expired.getContextMenu().getItems().stream().filter(item -> item.getText().equals("Xóa phía mình"))
                        .findFirst().orElseThrow().fire();
                confirmDialog(stage, "Xóa phía mình");
                assertNull(root.lookup("#actions-demo-expired-recall"));
                return null;
            });
        } finally {
            onFx(() -> { navigator.close(); stage.close(); return null; });
        }
    }

    @Test
    void globalSearchShowsPartialResultsAndJumpsToAnOlderMessage() throws Exception {
        var navigator = onFx(UserApplication::createNavigator);
        Stage stage = onFx(() -> shownStage(navigator.getRoot(), 1280, 720));
        try {
            var root = navigator.getRoot();
            onFx(() -> {
                var filter = (TextField) root.lookup("#conversationFilter");
                filter.setText("giao dien");
                assertNotNull(root.lookup("#globalConversationHeading"));
                assertNotNull(root.lookup("#globalMessageHeading"));
                assertNotNull(root.lookup("#global-conversation-grp-02"));
                assertTrue(root.lookup("#globalSearchSyncing").isVisible());
                capture(stage, "drawer-global-search-light");
                filter.setText("Chào mọi người");
                assertNull(root.lookup("#global-message-msg-grp-01-1"));
                return null;
            });
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(4);
            while (onFx(() -> root.lookup("#global-message-msg-grp-01-1") == null) && System.nanoTime() < deadline) {
                Thread.sleep(25);
            }
            onFx(() -> {
                Button hit = (Button) root.lookup("#global-message-msg-grp-01-1");
                assertNotNull(hit);
                assertFalse(root.lookup("#globalSearchSyncing").isVisible());
                hit.fire();
                assertEquals(56, rowCount(root));
                var highlighted = (ChatMessageRow) root.lookup("#search-highlighted-message");
                assertEquals("msg-grp-01-1", highlighted.messageId());
                return null;
            });
            onFx(() -> {
                var scroll = (ScrollPane) root.lookup("#historyScroll");
                double top = scroll.localToScene(0, 0).getY();
                double messageY = messageY(root, "msg-grp-01-1");
                assertTrue(messageY >= top - 1 && messageY < top + 100);
                var highlighted = (ChatMessageRow) root.lookup("#search-highlighted-message");
                assertNotNull(highlighted.getBackground());
                assertEquals(Color.web("#ebf3fc"), highlighted.getBackground().getFills().getFirst().getFill());
                capture(stage, "drawer-jump-light");
                var stale = (Button) root.lookup("#global-message-msg-grp-01-1");
                ChatDemoFlow.get(navigator).chat().deleteForSelf("grp-01", java.util.Set.of("msg-grp-01-1"));
                stale.fire();
                assertNull(root.lookup("#global-message-msg-grp-01-1"));
                assertNotNull(root.lookup("#unavailableSearchResult"));
                assertEquals("Chào mọi người", ((TextField) root.lookup("#conversationFilter")).getText());
                capture(stage, "drawer-stale-search-light");
                ((TextField) root.lookup("#conversationFilter")).setText("không có kết quả nào");
                assertNotNull(root.lookup("#globalSearchEmpty"));
                return null;
            });
        } finally {
            onFx(() -> { navigator.close(); stage.close(); return null; });
        }
    }

    @Test
    void drawerNavigatesScopedResultsAndConfirmsHistoryClearAndBlock() throws Exception {
        onFx(() -> {
            var navigator = UserApplication.createNavigator();
            var root = navigator.getRoot();
            Stage stage = shownStage(root, 1024, 640);
            try {
                selectChat(root, "direct-01");
                var draft = (TextArea) root.lookup("#draftField");
                draft.setText("Giữ lại bản nháp");
                ((Button) root.lookup("#conversationInfo")).fire();
                assertEquals("Thông tin đoạn chat", ((Label) root.lookup("#drawerHeading")).getText());
                capture(stage, "drawer-direct-light");
                ((Button) root.lookup("#searchInConversation")).fire();
                var local = (TextField) root.lookup("#conversationSearchField");
                local.setText("không có kết quả");
                root.applyCss();
                root.layout();
                assertNotNull(root.lookup("#localSearchEmpty"));
                local.setText("mình");
                Button next = (Button) root.lookup("#nextSearchResult");
                Button previous = (Button) root.lookup("#previousSearchResult");
                assertTrue(previous.isDisabled());
                next.fire();
                String first = ((ChatMessageRow) root.lookup("#search-highlighted-message")).messageId();
                assertTrue(first.contains("direct-01") || first.equals("demo-expired-recall"));
                assertTrue(previous.isDisabled());
                next.fire();
                assertNotEquals(first, ((ChatMessageRow) root.lookup("#search-highlighted-message")).messageId());
                assertFalse(previous.isDisabled());
                capture(stage, "drawer-local-search-light");
                previous.fire();
                assertEquals(first, ((ChatMessageRow) root.lookup("#search-highlighted-message")).messageId());
                ((Button) root.lookup("#drawerBack")).fire();
                var global = (TextField) root.lookup("#conversationFilter");
                global.setText("lần trước");
                assertNotNull(root.lookup("#global-message-demo-expired-recall"));
                ((Button) root.lookup("#clearConversationHistory")).fire();
                confirmDialog(stage, ButtonType.CANCEL.getText());
                assertTrue(rowCount(root) > 0);
                ((Button) root.lookup("#clearConversationHistory")).fire();
                confirmDialog(stage, "Xóa lịch sử phía mình");
                assertEquals(0, rowCount(root));
                assertNull(root.lookup("#global-message-demo-expired-recall"));
                assertEquals("Giữ lại bản nháp", draft.getText());
                ((Button) root.lookup("#blockConversationPeer")).fire();
                confirmDialog(stage, "Chặn người dùng");
                assertFalse(draft.getParent().isVisible());
                ((Button) root.lookup("#blockConversationPeer")).fire();
                confirmDialog(stage, "Bỏ chặn người dùng");
                assertTrue(draft.getParent().isVisible());
                assertEquals("Giữ lại bản nháp", draft.getText());
                global.clear();
                selectChat(root, "grp-01");
                ((Button) root.lookup("#conversationInfo")).fire();
                assertEquals("Thông tin nhóm", ((Label) root.lookup("#drawerHeading")).getText());
                root.applyCss();
                root.layout();
                assertEquals(8, root.lookupAll(".chat-member-row").size());
                assertNull(root.lookup("#blockConversationPeer"));
                navigator.themes().toggle();
                capture(stage, "drawer-group-dark");
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
    }

    private static Stage shownStage(Parent root, int width, int height) {
        Stage stage = new Stage();
        stage.setScene(new Scene(root, width, height));
        stage.show();
        root.applyCss();
        root.layout();
        return stage;
    }

    @Test
    void createGroupAndManageMembersFromTheDrawer() throws Exception {
        onFx(() -> {
            var navigator = UserApplication.createNavigator();
            var root = navigator.getRoot();
            Stage stage = shownStage(root, 1280, 720);
            try {
                var flow = ChatDemoFlow.get(navigator);
                var groups = flow.chat().groups();
                ((Button) root.lookup("#createGroup")).fire();
                var create = openDialogPane(stage);
                var createSubmit = (Button) create.lookup("#groupFormSubmit");
                assertTrue(createSubmit.isDisabled());
                ((TextField) create.lookup("#groupNameField")).setText("Nhóm Ôn tập Java");
                assertTrue(createSubmit.isDisabled());
                assertNull(create.lookup("#group-candidate-usr-05"));
                assertNull(create.lookup("#group-candidate-usr-06"));
                ((TextField) create.lookup("#memberSearchField")).setText("bao chau");
                ((CheckBox) create.lookup("#group-candidate-usr-02")).fire();
                assertFalse(createSubmit.isDisabled());
                ((TextField) create.lookup("#memberSearchField")).clear();
                capture(create.getScene().getWindow(), "group-create-light");
                createSubmit.fire();
                var created = flow.selectedProperty().get();
                assertEquals("Nhóm Ôn tập Java", created.title());
                assertTrue(created.encrypted());
                assertTrue(groups.isAdmin(created.id()));
                assertEquals(0, rowCount(root));

                ((Button) root.lookup("#conversationInfo")).fire();
                root.applyCss();
                root.layout();
                ((Button) root.lookup("#renameConversationGroup")).fire();
                var rename = openDialogPane(stage);
                ((TextField) rename.lookup("#groupNameField")).setText("Java buổi tối");
                ((Button) rename.lookup("#groupFormSubmit")).fire();
                assertEquals("Java buổi tối", flow.selectedProperty().get().title());
                assertEquals("Java buổi tối", ((Label) root.lookup("#conversationTitle")).getText());
                assertNotNull(root.lookup("#drawerClose"));
                ((Button) root.lookup("#addConversationMember")).fire();
                var add = openDialogPane(stage);
                assertNull(add.lookup("#group-candidate-usr-02"));
                ((CheckBox) add.lookup("#group-candidate-usr-03")).fire();
                ((Button) add.lookup("#groupFormSubmit")).fire();
                assertEquals(3, groups.group(created.id()).members().size());

                fireMemberAction(root, "usr-03", "Cấp quyền Admin");
                confirmDialog(stage, "Cấp quyền Admin");
                assertFalse(groups.needsSuccessor(created.id()));
                fireMemberAction(root, "usr-03", "Thu hồi quyền Admin");
                confirmDialog(stage, "Thu hồi quyền Admin");
                assertTrue(groups.needsSuccessor(created.id()));
                fireMemberAction(root, "usr-03", "Xóa thành viên");
                confirmDialog(stage, "Xóa thành viên");
                assertEquals(2, groups.group(created.id()).members().size());
                assertNull(root.lookup("#group-member-menu-usr-03"));

                ((Button) root.lookup("#resignConversationAdmin")).fire();
                var successor = openDialogPane(stage);
                assertTrue(((Button) successor.lookup("#groupFormSubmit")).isDisabled());
                ((CheckBox) successor.lookup("#group-candidate-usr-02")).fire();
                ((Button) successor.lookup("#groupFormCancel")).fire();
                assertTrue(groups.isAdmin(created.id()));
                assertNotNull(root.lookup("#drawerClose"));

                ((Button) root.lookup("#resignConversationAdmin")).fire();
                successor = openDialogPane(stage);
                ((CheckBox) successor.lookup("#group-candidate-usr-02")).fire();
                capture(successor.getScene().getWindow(), "group-successor-step-down");
                ((Button) successor.lookup("#groupFormSubmit")).fire();
                assertFalse(groups.isAdmin(created.id()));
                assertNull(root.lookup("#renameConversationGroup"));
                assertNull(root.lookup("#group-member-menu-usr-02"));
                assertFalse(root.lookup("#addConversationMember").isDisabled());
                ((Button) root.lookup("#addConversationMember")).fire();
                add = openDialogPane(stage);
                ((CheckBox) add.lookup("#group-candidate-usr-04")).fire();
                ((Button) add.lookup("#groupFormSubmit")).fire();
                assertEquals(3, groups.group(created.id()).members().size());
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
    }

    @Test
    void groupLeaveUsesSuccessorWhenNeededAndShowsReadOnlyHistory() throws Exception {
        onFx(() -> {
            var navigator = UserApplication.createNavigator();
            var root = navigator.getRoot();
            Stage stage = shownStage(root, 1024, 640);
            try {
                var flow = ChatDemoFlow.get(navigator);
                var chat = flow.chat();
                var created = chat.createGroup("Java bàn giao", java.util.List.of("usr-02", "usr-03"));
                var message = chat.send(created.id(), "Tin cũ vẫn đọc được.");
                chat.acknowledge(created.id(), message.id(), false);
                flow.openCreatedGroup(created);
                ((TextArea) root.lookup("#draftField")).setText("Bản nháp chưa gửi");
                ((Button) root.lookup("#conversationInfo")).fire();
                root.applyCss();
                root.layout();
                navigator.themes().toggle();
                ((Button) root.lookup("#leaveConversationGroup")).fire();
                var leave = openDialogPane(stage);
                var first = (CheckBox) leave.lookup("#group-candidate-usr-02");
                first.fire();
                ((CheckBox) leave.lookup("#group-candidate-usr-03")).fire();
                assertFalse(first.isSelected());
                capture(leave.getScene().getWindow(), "group-successor-leave-dark");
                ((Button) leave.lookup("#groupFormSubmit")).fire();
                assertEquals(OfflineChatDemoService.Access.GROUP_LEFT, chat.access(created.id()));
                assertEquals(1, rowCount(root));
                assertTrue(root.lookup("#readOnlyGroupFooter").isVisible());
                assertNull(root.lookup("#addConversationMember"));
                assertEquals("Bản nháp chưa gửi", flow.draft(created.id()));
                assertNull(chat.send(created.id(), "Tin sau khi rời"));
                capture(stage, "group-left-read-only-dark");

                selectChat(root, "grp-04");
                assertEquals(OfflineChatDemoService.Access.GROUP_REMOVED, chat.access("grp-04"));
                assertEquals(2, rowCount(root));
                assertTrue(root.lookup("#readOnlyGroupFooter").isVisible());

                var last = chat.createGroup("Một thành viên", java.util.List.of("usr-02"));
                chat.groups().removeMember(last.id(), "usr-02");
                flow.openCreatedGroup(last);
                ((Button) root.lookup("#conversationInfo")).fire();
                root.applyCss();
                root.layout();
                ((Button) root.lookup("#leaveConversationGroup")).fire();
                assertNull(openDialogPane(stage).lookup("#groupFormSubmit"));
                confirmDialog(stage, "Rời nhóm");
                assertTrue(chat.groups().group(last.id()).members().isEmpty());
                assertTrue(root.lookup("#readOnlyGroupFooter").isVisible());
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
    }

    @Test
    void refineRequiresConsentAndApplyUpdatesOnlyTheDraft() throws Exception {
        var navigator = onFx(UserApplication::createNavigator);
        Stage stage = onFx(() -> shownStage(navigator.getRoot(), 1280, 720));
        try {
            onFx(() -> {
                var root = navigator.getRoot();
                selectChat(root, "direct-01");
                var draft = (TextArea) root.lookup("#draftField");
                var refine = (Button) root.lookup("#refineDraft");
                assertTrue(refine.isDisabled());
                draft.setText("Nháp gốc của mình");
                refine.fire();
                var popup = aiWindow(stage);
                var panel = popup.getScene().getRoot();
                var copy = (TextArea) panel.lookup("#aiRefineInput");
                assertEquals(draft.getText(), copy.getText());
                assertNull(panel.lookup("#aiOption-1"));
                copy.setText("mình đã cập nhật tài liệu.");
                ((ToggleButton) panel.lookup("#aiTone-POLITE")).fire();
                assertEquals("Nháp gốc của mình", draft.getText());
                capture(popup, "ai-refine-preview-light", "#aiAssistant");
                ((Button) panel.lookup("#aiConfirm")).fire();
                assertNotNull(panel.lookup("#aiLoading"));
                return null;
            });
            awaitAiNode(stage, "#aiOption-3");
            onFx(() -> {
                var root = navigator.getRoot();
                var popup = aiWindow(stage);
                var panel = popup.getScene().getRoot();
                var chat = ChatDemoFlow.get(navigator).chat();
                var latest = chat.latest("direct-01");
                var apply = (Button) panel.lookup("#aiApply");
                assertTrue(apply.isDisabled());
                var option = (CheckBox) panel.lookup("#aiOption-1");
                option.fire();
                capture(popup, "ai-results-light", "#aiAssistant");
                apply.fire();
                assertEquals(option.getUserData(), ((TextArea) root.lookup("#draftField")).getText());
                assertEquals(latest, chat.latest("direct-01"));
                assertNull(openAiWindow(stage));
                return null;
            });
        } finally {
            onFx(() -> { navigator.close(); stage.close(); return null; });
        }
    }

    @Test
    void recommendCapsTheSelectionAndPreviewsEditedCopiesWithoutMutatingMessages() throws Exception {
        onFx(() -> {
            var navigator = UserApplication.createNavigator();
            var root = navigator.getRoot();
            Stage stage = shownStage(root, 1024, 640);
            try {
                selectChat(root, "direct-01");
                navigator.themes().toggle();
                var draft = (TextArea) root.lookup("#draftField");
                assertTrue(draft.getText().isEmpty());
                var chat = ChatDemoFlow.get(navigator).chat();
                var original = chat.page("direct-01", 50).messages();
                var eligible = original.stream().filter(message -> message.state() != State.RECALLED).toList();
                var recommend = (Button) root.lookup("#recommendReply");
                assertFalse(recommend.isDisabled());
                recommend.fire();
                var popup = aiWindow(stage);
                var panel = popup.getScene().getRoot();
                assertTrue(panel.lookup("#aiConfirm").isDisabled());
                assertTrue(original.stream().filter(message -> message.state() == State.RECALLED)
                        .noneMatch(message -> panel.lookup("#aiContext-" + message.id()) != null));
                for (int i = 0; i < 5; i++) {
                    ((CheckBox) panel.lookup("#aiContext-" + eligible.get(i).id())).fire();
                }
                String first = eligible.getFirst().id();
                assertTrue(panel.lookup("#aiContext-" + eligible.get(5).id()).isDisabled());
                ((Button) panel.lookup("#aiEditContext")).fire();
                var editor = (TextArea) panel.lookup("#aiContextEdit-" + first);
                editor.setText("Bản sao ngữ cảnh đã sửa.");
                ((Button) panel.lookup("#aiReviewContext")).fire();
                panel.applyCss();
                panel.layout();
                assertTrue(panel.lookup("#aiContext-" + first).getAccessibleText().contains("Bản sao ngữ cảnh đã sửa."));
                assertEquals(original, chat.page("direct-01", 50).messages());
                assertEquals("", draft.getText());
                capture(popup, "ai-recommend-preview-dark", "#aiAssistant");
                chat.deleteForSelf("direct-01", java.util.Set.of(first));
                ((Button) panel.lookup("#aiConfirm")).fire();
                assertNull(panel.lookup("#aiLoading"));
                assertTrue(((Label) panel.lookup("#aiContextSummary")).getText().contains("4 / 5"));
                ((Button) panel.lookup("#aiCancel")).fire();
                assertEquals("", draft.getText());
                assertNull(openAiWindow(stage));
            } finally {
                navigator.close();
                stage.close();
            }
            return null;
        });
    }

    @Test
    void aiErrorsRequireAnotherConsentAndLateResultsDoNotReplaceChangedDrafts() throws Exception {
        var navigator = onFx(UserApplication::createNavigator);
        Stage stage = onFx(() -> shownStage(navigator.getRoot(), 1280, 720));
        try {
            var root = navigator.getRoot();
            onFx(() -> {
                selectChat(root, "direct-01");
                ((TextArea) root.lookup("#draftField")).setText("Giữ bản nháp");
                ((Button) root.lookup("#refineDraft")).fire();
                return null;
            });
            for (var scenario : java.util.List.of(Scenario.ERROR, Scenario.TIMEOUT, Scenario.QUOTA)) {
                onFx(() -> { submitAi(stage, scenario); return null; });
                awaitAiNode(stage, "#aiError");
                onFx(() -> {
                    var panel = aiWindow(stage).getScene().getRoot();
                    assertTrue(((Label) panel.lookup("#aiError")).getText().contains(scenario == Scenario.ERROR ? "kết nối"
                            : scenario == Scenario.TIMEOUT ? "15 giây" : "hạn mức"));
                    assertEquals("Giữ bản nháp", ((TextArea) root.lookup("#draftField")).getText());
                    ((Button) panel.lookup("#aiReviewInput")).fire();
                    assertNull(panel.lookup("#aiLoading"));
                    assertNotNull(panel.lookup("#aiConfirm"));
                    return null;
                });
            }
            onFx(() -> {
                submitAi(stage, Scenario.SUCCESS);
                var draft = (TextArea) root.lookup("#draftField");
                draft.setText("Nháp đã đổi");
                draft.setText("Giữ bản nháp");
                return null;
            });
            awaitAiNode(stage, "#aiStale");
            onFx(() -> {
                var panel = aiWindow(stage).getScene().getRoot();
                assertNull(panel.lookup("#aiOption-1"));
                assertEquals("Giữ bản nháp", ((TextArea) root.lookup("#draftField")).getText());
                ((Button) panel.lookup("#aiContinue")).fire();
                ((Button) root.lookup("#refineDraft")).fire();
                submitAi(stage, Scenario.SUCCESS);
                ((Button) aiWindow(stage).getScene().getRoot().lookup("#aiStop")).fire();
                assertNull(openAiWindow(stage));
                ((Button) root.lookup("#refineDraft")).fire();
                submitAi(stage, Scenario.SUCCESS);
                selectChat(root, "direct-02");
                assertNull(openAiWindow(stage));
                assertEquals("", ((TextArea) root.lookup("#draftField")).getText());
                ((Button) root.lookup("#recommendReply")).fire();
                var recommendation = aiWindow(stage).getScene().getRoot();
                ((CheckBox) recommendation.lookup("#aiContext-msg-direct-02-1")).fire();
                ((Button) recommendation.lookup("#aiConfirm")).fire();
                return null;
            });
            awaitAiNode(stage, "#aiOption-3");
            onFx(() -> {
                var panel = aiWindow(stage).getScene().getRoot();
                var chat = ChatDemoFlow.get(navigator).chat();
                var messages = chat.page("direct-02", 50).messages();
                var option = (CheckBox) panel.lookup("#aiOption-3");
                option.fire();
                ((Button) panel.lookup("#aiApply")).fire();
                assertEquals(option.getUserData(), ((TextArea) root.lookup("#draftField")).getText());
                assertEquals(messages, chat.page("direct-02", 50).messages());
                return null;
            });
        } finally {
            onFx(() -> { navigator.close(); stage.close(); return null; });
        }
    }

    @SuppressWarnings("unchecked")
    private static void submitAi(Stage owner, Scenario scenario) {
        var panel = aiWindow(owner).getScene().getRoot();
        ((ComboBox<Scenario>) panel.lookup("#aiScenario")).setValue(scenario);
        ((Button) panel.lookup("#aiConfirm")).fire();
    }

    private static void awaitAiNode(Stage owner, String id) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(4);
        TimeUnit.MILLISECONDS.sleep(25);
        while (onFx(() -> aiWindow(owner).getScene().getRoot().lookup(id) == null)) {
            assertTrue(System.nanoTime() < deadline, "Missing AI state " + id);
            TimeUnit.MILLISECONDS.sleep(25);
        }
    }

    private static Window openAiWindow(Stage owner) {
        return Window.getWindows().stream().filter(window -> window != owner && window.isShowing())
                .filter(window -> window.getScene().getRoot().lookup("#aiAssistant") != null).findFirst().orElse(null);
    }

    private static Window aiWindow(Stage owner) {
        Window window = openAiWindow(owner);
        assertNotNull(window);
        window.getScene().getRoot().applyCss();
        window.getScene().getRoot().layout();
        return window;
    }

    private static void fireMemberAction(Parent root, String memberId, String label) {
        var menu = (Button) root.lookup("#group-member-menu-" + memberId);
        menu.getContextMenu().getItems().stream().filter(item -> item.getText().equals(label))
                .findFirst().orElseThrow().fire();
    }

    private static void awaitState(OfflineChatDemoService chat, String chatId, State expected) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(4);
        State observed;
        do {
            observed = onFx(() -> chat.latest(chatId).state());
            if (observed == expected) {
                return;
            }
            Thread.sleep(25);
        } while (System.nanoTime() < deadline);
        assertEquals(expected, observed);
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(20, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    private static void selectChat(Parent root, String id) {
        var list = (ListView<MockConversation>) root.lookup("#conversationItems");
        var chat = list.getItems().stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
        list.getSelectionModel().select(chat);
        if (list.getSelectionModel().getSelectedIndex() >= 0) {
            list.scrollTo(list.getSelectionModel().getSelectedIndex());
        }
        root.applyCss();
        root.layout();
    }

    private static long rowCount(Parent root) {
        return ((VBox) root.lookup("#messageRows")).getChildren().stream().filter(ChatMessageRow.class::isInstance).count();
    }

    private static double messageY(Parent root, String messageId) {
        var row = ((VBox) root.lookup("#messageRows")).getChildren().stream()
                .filter(ChatMessageRow.class::isInstance).map(ChatMessageRow.class::cast)
                .filter(item -> item.messageId().equals(messageId)).findFirst().orElseThrow();
        return row.localToScene(0, 0).getY();
    }

    private static void confirmDialog(Stage owner, String action) {
        DialogPane pane = openDialogPane(owner);
        ButtonType button = pane.getButtonTypes().stream().filter(type -> type.getText().equals(action))
                .findFirst().orElseThrow();
        ((Button) pane.lookupButton(button)).fire();
    }

    private static DialogPane openDialogPane(Stage owner) {
        var pane = Window.getWindows().stream().filter(window -> window != owner && window.isShowing())
                .map(window -> window.getScene().getRoot()).filter(DialogPane.class::isInstance)
                .map(DialogPane.class::cast).findFirst().orElseThrow();
        pane.applyCss();
        pane.layout();
        return pane;
    }

    private static void fireSetting(Parent root, String id) {
        var settings = (MenuButton) root.lookup("#workspaceSettings");
        settings.getItems().stream().filter(item -> id.equals(item.getId())).findFirst().orElseThrow().fire();
    }

    private static void capture(Window stage, String name) throws Exception {
        capture(stage, name, null);
    }

    private static void capture(Window stage, String name, String selector) throws Exception {
        if (!Boolean.getBoolean("chatsystem.capture")) {
            return;
        }
        var scene = stage.getScene();
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        WritableImage image = selector == null ? scene.snapshot(null) : scene.getRoot().lookup(selector).snapshot(null, null);
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
