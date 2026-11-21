package com.chatsystem.user.control;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockMessage.State;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** The reusable history row, with incoming, outgoing and selection variants. */
public final class ChatMessageRow extends HBox {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm").withZone(MockData.DISPLAY_ZONE);

    private final MockMessage message;

    public record Actions(Runnable retry, Runnable select, Runnable delete, Runnable recall) {}

    public ChatMessageRow(MockMessage message, String sender, boolean locked, boolean selecting,
                          boolean selected, Actions actions) {
        this.message = message;
        setMinWidth(0);
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("chat-message-row");
        boolean outgoing = message.senderId().equals(MockData.PREVIEW_USER_ID);
        boolean recalled = message.state() == State.RECALLED;
        VBox content = new VBox(4);
        content.setMinWidth(0);
        content.maxWidthProperty().bind(widthProperty().multiply(0.72));
        content.setAlignment(outgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        if (!outgoing && !locked && !recalled) {
            Label author = new Label(sender);
            author.getStyleClass().addAll("chat-caption", "chat-message-author");
            content.getChildren().add(author);
        }
        Label bubble = new Label(recalled ? "Tin nhắn đã được thu hồi"
                : locked ? "Tin nhắn chưa thể giải mã" : message.text());
        bubble.setWrapText(true);
        bubble.setMinWidth(0);
        bubble.getStyleClass().add("chat-bubble");
        bubble.getStyleClass().add(recalled || locked ? "chat-bubble-unavailable"
                : outgoing ? "chat-bubble-outgoing" : "chat-bubble-incoming");
        String status = outgoing && !locked && !recalled
                ? "  " + deliveryLabel(message.state(), actions.retry() != null) : "";
        Label metadata = new Label(TIME.format(message.sentAt()) + status);
        metadata.setWrapText(true);
        metadata.getStyleClass().add("chat-caption");
        if (outgoing && message.state() == State.READ && !locked) {
            metadata.getStyleClass().add("chat-receipt-read");
        }
        content.getChildren().addAll(bubble, metadata);
        if (actions.retry() != null && !selecting) {
            Button retry = new Button("Thử lại");
            retry.setId("retry-" + message.id());
            retry.getStyleClass().add("chat-outline");
            retry.setOnAction(event -> actions.retry().run());
            content.getChildren().add(retry);
        }

        if (selecting) {
            CheckBox check = new CheckBox();
            check.setSelected(selected);
            check.setId("select-" + message.id());
            check.setAccessibleText("Chọn tin nhắn lúc " + TIME.format(message.sentAt()));
            check.setOnAction(event -> actions.select().run());
            getChildren().add(check);
        }

        boolean showRecall = outgoing && !locked && !recalled && message.acceptedByServer();
        Button more = selecting ? null : messageMenu(actions, showRecall);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        if (outgoing) {
            getChildren().add(spacer);
            if (more != null) {
                getChildren().add(more);
            }
            getChildren().add(content);
        } else {
            getChildren().add(content);
            if (more != null) {
                getChildren().add(more);
            }
            getChildren().add(spacer);
        }
    }

    public String messageId() {
        return message.id();
    }

    private Button messageMenu(Actions actions, boolean showRecall) {
        ContextMenu menu = new ContextMenu();
        addItem(menu, "Xóa phía mình", actions.delete());
        if (showRecall && actions.recall() == null) {
            MenuItem expired = new MenuItem("Thu hồi với mọi người (quá 48 giờ)");
            expired.setDisable(true);
            menu.getItems().add(expired);
        } else {
            addItem(menu, "Thu hồi với mọi người", actions.recall());
        }
        addItem(menu, "Chọn nhiều tin nhắn", actions.select());
        Button more = new Button();
        more.setId("actions-" + message.id());
        more.setAccessibleText("Thao tác tin nhắn lúc " + TIME.format(message.sentAt()));
        more.getStyleClass().add("chat-message-more");
        more.setGraphic(new ChatIcon("message-more"));
        more.setContextMenu(menu);
        more.setOnAction(event -> menu.show(more, Side.BOTTOM, 0, 0));
        setOnContextMenuRequested(event -> {
            if (event.isKeyboardTrigger()) {
                more.fire();
            } else {
                menu.show(more, event.getScreenX(), event.getScreenY());
            }
            event.consume();
        });
        more.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F10 && event.isShiftDown()) {
                more.fire();
                event.consume();
            }
        });
        return more;
    }

    private static void addItem(ContextMenu menu, String label, Runnable action) {
        if (action != null) {
            MenuItem item = new MenuItem(label);
            item.setOnAction(event -> action.run());
            menu.getItems().add(item);
        }
    }

    private static String deliveryLabel(State state, boolean canRetry) {
        return switch (state) {
            case PENDING -> "Đang chờ";
            case FAILED -> canRetry ? "Đang chờ / Thử lại" : "Không gửi được";
            case STORED -> "Server đã lưu";
            case DELIVERED -> "Đã nhận";
            case READ -> "Đã xem";
            case RECALLED -> "";
        };
    }
}
