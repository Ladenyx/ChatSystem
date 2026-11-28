package com.chatsystem.desktop.skeleton;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.common.api.ApiError;
import com.chatsystem.common.api.ErrorCode;
import com.chatsystem.desktop.async.LoadFailure;
import com.chatsystem.desktop.async.ViewLoadState;
import com.chatsystem.desktop.client.ApiClientException;
import com.chatsystem.desktop.theme.ThemeManager;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "chatsystem.uiTests", matches = "true")
class StateContentPaneUiTest {
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
    void switchesAllFiveStatesThemesAndStatefulControlsOnFxThread() throws Exception {
        onFx(() -> {
            ThemeManager themes = new ThemeManager();
            Label content = new Label("Real rows");
            TableSkeleton skeleton = new TableSkeleton();
            StateContentPane pane = new StateContentPane(content, skeleton, themes.reducedMotionProperty());
            themes.apply(pane);

            Stage stage = new Stage();
            stage.setScene(new Scene(new VBox(pane), 800, 600));
            stage.show();

            AtomicInteger retries = new AtomicInteger();
            pane.setOnRetry(retries::incrementAndGet);

            pane.apply(ViewLoadState.loading());
            assertTrue(skeleton.isVisible());

            pane.apply(ViewLoadState.content("rows"));
            assertFalse(skeleton.isVisible());
            assertTrue(content.isVisible());

            pane.apply(ViewLoadState.refreshing("rows"));
            assertTrue(content.isVisible());

            pane.apply(ViewLoadState.empty());
            assertFalse(content.isVisible());

            LoadFailure offline = LoadFailure.fromThrowable(new IOException("down"));
            pane.apply(ViewLoadState.errorRetainingData(offline, "rows"));
            assertTrue(content.isVisible());

            LoadFailure forbidden = LoadFailure.fromThrowable(new ApiClientException(
                    ApiError.of(ErrorCode.FORBIDDEN, "Denied", Instant.now(), "/api/v1/admin/users", "r1"),
                    403
            ));
            pane.apply(ViewLoadState.errorRetainingData(forbidden, "rows"));
            assertFalse(content.isVisible());

            themes.setMode(ThemeManager.Mode.DARK);
            themes.setMode(ThemeManager.Mode.LIGHT);

            Button save = new Button("Lưu");
            BusyButtonBinding binding = new BusyButtonBinding(save, "Đang lưu...");
            binding.busyConsumer().accept(true);
            assertTrue(save.isDisabled());
            assertEquals("Đang lưu...", save.getText());
            binding.close();
            assertFalse(save.isDisabled());
            assertEquals("Lưu", save.getText());

            MessageSendStatusView sendStatus = new MessageSendStatusView();
            sendStatus.setStatus(MessageSendStatusView.Status.SENDING, null);
            assertTrue(sendStatus.isVisible());
            sendStatus.setStatus(MessageSendStatusView.Status.FAILED, retries::incrementAndGet);
            assertTrue(sendStatus.isVisible());

            E2eeSyncBanner e2ee = new E2eeSyncBanner();
            e2ee.showWaitingForKey("Chờ khóa");
            assertTrue(e2ee.isVisible());

            SearchPartialSyncBanner partial = new SearchPartialSyncBanner();
            partial.showWarning("Chưa đồng bộ hết", "Tải thêm", retries::incrementAndGet);
            assertTrue(partial.isVisible());

            pane.dispose();
            stage.close();
            return null;
        });
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return action.call();
        }
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
