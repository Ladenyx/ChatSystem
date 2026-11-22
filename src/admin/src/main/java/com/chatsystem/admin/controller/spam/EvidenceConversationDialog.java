package com.chatsystem.admin.controller.spam;

import com.chatsystem.admin.mock.SpamModerationPreview;
import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockSpamReport;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.theme.ThemeManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/** Read-only evidence snapshot viewer styled as a structured conversation dialog. */
final class EvidenceConversationDialog {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm", Locale.ROOT)
            .withZone(MockData.DISPLAY_ZONE);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)
            .withZone(MockData.DISPLAY_ZONE);

    private EvidenceConversationDialog() {
    }

    static Dialog<ButtonType> create(Window owner, ThemeManager themes, SpamModerationPreview preview,
                                     MockSpamReport report, String focusMessageId) {
        Dialog<ButtonType> dialog = new Dialog<>();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(true);
        dialog.setTitle("Bằng chứng hội thoại · Báo cáo #" + report.id());

        var pane = dialog.getDialogPane();
        pane.setId("evidenceConversationDialog");
        pane.getStyleClass().add("evidence-dialog-pane");
        pane.setPrefSize(720, 600);
        pane.setMinSize(520, 420);

        themes.apply(pane);
        pane.getStylesheets().add(Objects.requireNonNull(
                EvidenceConversationDialog.class.getResource("/com/chatsystem/admin/css/spam.css")).toExternalForm());

        ButtonType closeType = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().setAll(closeType);
        Button closeButton = (Button) pane.lookupButton(closeType);
        closeButton.setId("closeEvidenceDialog");
        closeButton.getStyleClass().add("evidence-dialog-close");
        closeButton.setDefaultButton(true);
        ButtonBar.setButtonUniformSize(closeButton, false);

        MockUser reporter = preview.user(report.reporterId());
        MockUser reported = preview.user(report.reportedUserId());
        var conversation = preview.conversation(report.conversationId());
        boolean isGroup = conversation.isPresent() && conversation.get().kind() == MockConversation.Kind.GROUP;

        VBox chatList = new VBox(12);
        chatList.setId("evidenceConversationList");
        chatList.getStyleClass().add("evidence-chat-canvas");
        chatList.setMinWidth(0);

        List<Node> selectedRows = new ArrayList<>();
        Map<String, Node> rowsByMessageId = new HashMap<>();
        LocalDate previousDay = null;

        for (MockSpamReport.Evidence item : report.evidence()) {
            MockMessage msg = item.message();
            LocalDate day = msg.sentAt().atZone(MockData.DISPLAY_ZONE).toLocalDate();
            if (!day.equals(previousDay)) {
                chatList.getChildren().add(dateDivider(day));
                previousDay = day;
            }
            HBox row = messageRow(preview, report, item);
            chatList.getChildren().add(row);
            rowsByMessageId.put(msg.id(), row);
            if (item.selected()) {
                selectedRows.add(row);
            }
        }

        ScrollPane scroll = new ScrollPane(chatList);
        scroll.setId("evidenceConversationScroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFocusTraversable(false);
        scroll.getStyleClass().add("evidence-chat-scroll");

        Label title = new Label(isGroup
                ? conversation.get().title()
                : "Hội thoại với " + reported.fullName());
        title.getStyleClass().add("evidence-dialog-title");
        title.setWrapText(true);
        title.setMinWidth(0);

        Label kindPill = new Label(isGroup ? "Nhóm chat" : "Trò chuyện 1–1");
        kindPill.getStyleClass().add("evidence-dialog-kind");
        kindPill.setMinWidth(Region.USE_PREF_SIZE);

        HBox titleRow = new HBox(8, title, kindPill);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(title, Priority.SOMETIMES);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerTop = new HBox(12, titleRow, spacer);
        headerTop.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleRow, Priority.SOMETIMES);

        int[] activeIndex = {0};
        if (focusMessageId != null) {
            Node focused = rowsByMessageId.get(focusMessageId);
            int idx = selectedRows.indexOf(focused);
            if (idx >= 0) {
                activeIndex[0] = idx;
            }
        }

        if (!selectedRows.isEmpty()) {
            HBox jumpBar = buildJumpBar(scroll, chatList, selectedRows, activeIndex);
            headerTop.getChildren().add(jumpBar);
        }

        Label subtitle = new Label("Bản chụp chỉ đọc gồm " + report.evidence().size()
                + " tin nhắn do " + reporter.fullName() + " (@" + reporter.username()
                + ") gửi · Góc nhìn từ người báo cáo");
        subtitle.getStyleClass().add("caption");
        subtitle.setWrapText(true);
        subtitle.setMinWidth(0);

        VBox header = new VBox(8, headerTop, subtitle);
        header.getStyleClass().add("evidence-dialog-header");
        header.setMinWidth(0);

        FontIcon lockIcon = new FontIcon(Feather.LOCK);
        lockIcon.setIconSize(14);
        Label noticeText = new Label(
                "Bản chụp chỉ đọc — hệ thống không xác minh bản rõ bằng mật mã E2EE"
                        + " và không cấp quyền truy cập hội thoại trực tiếp");
        noticeText.getStyleClass().add("caption");
        noticeText.setWrapText(true);
        noticeText.setMinWidth(0);
        HBox.setHgrow(noticeText, Priority.ALWAYS);

        HBox noticeBar = new HBox(8, lockIcon, noticeText);
        noticeBar.setAlignment(Pos.CENTER_LEFT);
        noticeBar.getStyleClass().add("evidence-dialog-notice");

        BorderPane container = new BorderPane(scroll, header, null, noticeBar, null);
        container.setMinWidth(0);
        pane.setContent(container);

        Node initialTarget = focusMessageId != null && rowsByMessageId.containsKey(focusMessageId)
                ? rowsByMessageId.get(focusMessageId)
                : selectedRows.isEmpty() ? null : selectedRows.getFirst();

        if (initialTarget != null) {
            dialog.setOnShown(event -> {
                pane.applyCss();
                pane.layout();
                scrollToNode(scroll, chatList, initialTarget);
            });
            Platform.runLater(() -> {
                pane.applyCss();
                pane.layout();
                scrollToNode(scroll, chatList, initialTarget);
            });
        }

        return dialog;
    }

    private static HBox buildJumpBar(ScrollPane scroll, VBox chatList, List<Node> selectedRows, int[] activeIndex) {
        int count = selectedRows.size();
        Label counter = new Label();
        counter.setId("evidenceJumpCounter");
        counter.getStyleClass().add("evidence-jump-counter");
        counter.setMinWidth(Region.USE_PREF_SIZE);
        updateCounter(counter, activeIndex[0], count);

        HBox jumpBar = new HBox(6, counter);
        jumpBar.setAlignment(Pos.CENTER_LEFT);
        jumpBar.setMinWidth(Region.USE_PREF_SIZE);

        if (count > 1) {
            Button prev = new Button("", FontIcon.of(Feather.CHEVRON_UP, 14));
            prev.setId("prevReported");
            prev.setAccessibleText("Tin bị báo cáo trước");
            prev.getStyleClass().add("evidence-jump-btn");
            prev.setOnAction(event -> {
                activeIndex[0] = (activeIndex[0] - 1 + count) % count;
                updateCounter(counter, activeIndex[0], count);
                scrollToNode(scroll, chatList, selectedRows.get(activeIndex[0]));
            });

            Button next = new Button("", FontIcon.of(Feather.CHEVRON_DOWN, 14));
            next.setId("nextReported");
            next.setAccessibleText("Tin bị báo cáo tiếp theo");
            next.getStyleClass().add("evidence-jump-btn");
            next.setOnAction(event -> {
                activeIndex[0] = (activeIndex[0] + 1) % count;
                updateCounter(counter, activeIndex[0], count);
                scrollToNode(scroll, chatList, selectedRows.get(activeIndex[0]));
            });
            jumpBar.getChildren().addAll(prev, next);
        }

        Button jump = new Button("Tới tin bị báo cáo", FontIcon.of(Feather.FLAG, 12));
        jump.setId("jumpToReported");
        jump.getStyleClass().add("evidence-jump-btn");
        jump.setOnAction(event -> {
            updateCounter(counter, activeIndex[0], count);
            scrollToNode(scroll, chatList, selectedRows.get(activeIndex[0]));
        });
        jumpBar.getChildren().add(jump);
        return jumpBar;
    }

    private static void updateCounter(Label counter, int index, int total) {
        counter.setText(total == 1 ? "1 tin bị báo cáo" : "Tin bị báo cáo " + (index + 1) + " / " + total);
    }

    private static HBox dateDivider(LocalDate day) {
        Region left = new Region();
        left.getStyleClass().add("evidence-date-line");
        HBox.setHgrow(left, Priority.ALWAYS);

        Label chip = new Label(DATE.format(day));
        chip.getStyleClass().add("evidence-date-chip");
        chip.setMinWidth(Region.USE_PREF_SIZE);

        Region right = new Region();
        right.getStyleClass().add("evidence-date-line");
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox divider = new HBox(12, left, chip, right);
        divider.setAlignment(Pos.CENTER);
        divider.setMinWidth(0);
        return divider;
    }

    private static HBox messageRow(SpamModerationPreview preview, MockSpamReport report, MockSpamReport.Evidence item) {
        MockMessage msg = item.message();
        MockUser sender = preview.user(msg.senderId());
        boolean reporterOwn = msg.senderId().equals(report.reporterId()) && !item.selected();

        HBox row = new HBox(8);
        row.getStyleClass().add("evidence-message-row");
        row.setAlignment(Pos.TOP_LEFT);
        row.setMinWidth(0);

        VBox content = new VBox(4);
        content.setMinWidth(0);
        content.maxWidthProperty().bind(row.widthProperty().multiply(0.74));

        VBox bubble = new VBox(4);
        bubble.setMinWidth(0);
        bubble.setMaxWidth(Region.USE_PREF_SIZE);
        bubble.getStyleClass().add("evidence-msg-bubble");

        if (reporterOwn) {
            content.setAlignment(Pos.TOP_RIGHT);
            Label author = new Label("Người báo cáo · " + sender.fullName());
            author.getStyleClass().add("evidence-msg-author");
            Label time = new Label(TIME.format(msg.sentAt()));
            time.setAccessibleText(DATE_TIME.format(msg.sentAt()));
            time.getStyleClass().add("evidence-msg-time");
            HBox meta = new HBox(6, author, time);
            meta.setAlignment(Pos.CENTER_RIGHT);

            bubble.getStyleClass().add("evidence-msg-reporter");
            bubble.getChildren().add(wrappingLabel(msg.text(), "admin-bubble-text"));
            content.getChildren().addAll(meta, bubble);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().addAll(spacer, content);
        } else {
            content.setAlignment(Pos.TOP_LEFT);
            Label avatar = new Label(initials(sender.fullName()));
            avatar.getStyleClass().add("evidence-msg-avatar");
            if (sender.id().equals(report.reportedUserId())) {
                avatar.getStyleClass().add("avatar-reported");
            }

            Label author = new Label(sender.fullName());
            author.getStyleClass().add("evidence-msg-author");
            Label handle = new Label("@" + sender.username());
            handle.getStyleClass().add("evidence-msg-handle");
            Label time = new Label(TIME.format(msg.sentAt()));
            time.setAccessibleText(DATE_TIME.format(msg.sentAt()));
            time.getStyleClass().add("evidence-msg-time");
            FlowPane meta = new FlowPane(6, 2, author, handle, time);
            meta.setAlignment(Pos.CENTER_LEFT);
            meta.setMinWidth(0);

            if (item.selected()) {
                Label flag = new Label("Tin bị báo cáo", FontIcon.of(Feather.FLAG, 12));
                flag.getStyleClass().add("tag-selected");
                bubble.getStyleClass().add("bubble-selected");
                bubble.getChildren().add(flag);
            } else {
                bubble.getStyleClass().add("evidence-msg-other");
            }
            bubble.getChildren().add(wrappingLabel(msg.text(), "admin-bubble-text"));
            content.getChildren().addAll(meta, bubble);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().addAll(avatar, content, spacer);
        }

        return row;
    }

    private static Label wrappingLabel(String text, String style) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.getStyleClass().add(style);
        return label;
    }

    private static String initials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase(Locale.ROOT);
        }
        return (parts[parts.length - 2].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
                .toUpperCase(Locale.ROOT);
    }

    private static void scrollToNode(ScrollPane scroll, VBox content, Node target) {
        scroll.applyCss();
        scroll.layout();
        content.applyCss();
        content.layout();
        double viewportHeight = scroll.getViewportBounds().getHeight();
        double contentHeight = content.getBoundsInLocal().getHeight();
        double scrollable = contentHeight - viewportHeight;
        if (scrollable > 0 && target != null) {
            double targetY = target.getBoundsInParent().getMinY() - viewportHeight * 0.3;
            scroll.setVvalue(Math.clamp(targetY / scrollable, 0.0, 1.0));
        } else if (viewportHeight <= 0) {
            Platform.runLater(() -> scrollToNodeOnce(scroll, content, target));
        }
    }

    private static void scrollToNodeOnce(ScrollPane scroll, VBox content, Node target) {
        scroll.applyCss();
        scroll.layout();
        content.applyCss();
        content.layout();
        double viewportHeight = scroll.getViewportBounds().getHeight();
        double scrollable = content.getBoundsInLocal().getHeight() - viewportHeight;
        if (scrollable > 0 && target != null) {
            double targetY = target.getBoundsInParent().getMinY() - viewportHeight * 0.3;
            scroll.setVvalue(Math.clamp(targetY / scrollable, 0.0, 1.0));
        }
    }
}
