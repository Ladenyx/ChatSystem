package com.chatsystem.user.control;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockMessage.State;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** The reusable history row, with incoming, outgoing and selection variants. */
public final class ChatMessageRow extends HBox {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm").withZone(MockData.DISPLAY_ZONE);

    private final MockMessage message;

    public ChatMessageRow(MockMessage message, String sender, boolean outgoing) {
        this.message = message;
        setMinWidth(0);
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("chat-message-row");
        boolean recalled = message.state() == State.RECALLED;
        VBox content = new VBox(4);
        content.setMinWidth(0);
        content.maxWidthProperty().bind(widthProperty().multiply(0.72));
        content.setAlignment(outgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        if (!outgoing && !recalled) {
            Label author = new Label(sender);
            author.getStyleClass().addAll("chat-caption", "chat-message-author");
            content.getChildren().add(author);
        }
        Label bubble = new Label(recalled ? "Tin nhắn đã được thu hồi"
                : message.text());
        bubble.setWrapText(true);
        bubble.setMinWidth(0);
        bubble.getStyleClass().add("chat-bubble");
        bubble.getStyleClass().add(recalled ? "chat-bubble-unavailable"
                : outgoing ? "chat-bubble-outgoing" : "chat-bubble-incoming");
        String status = outgoing && !recalled ? " · " + deliveryLabel(message.state()) : "";
        Label metadata = new Label(TIME.format(message.sentAt()) + status);
        metadata.setWrapText(true);
        metadata.getStyleClass().addAll("chat-caption", "chat-message-meta");
        if (outgoing && message.state() == State.READ) {
            metadata.getStyleClass().add("chat-receipt-read");
        }
        content.getChildren().addAll(bubble, metadata);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        if (outgoing) {
            getChildren().add(spacer);
            getChildren().add(content);
        } else {
            getChildren().add(content);
            getChildren().add(spacer);
        }
    }

    public String messageId() {
        return message.id();
    }

    private static String deliveryLabel(State state) {
        return switch (state) {
            case PENDING -> "Đang chờ";
            case FAILED -> "Đang chờ / Thử lại";
            case STORED -> "Server đã lưu";
            case DELIVERED -> "Đã nhận";
            case READ -> "Đã xem";
            case RECALLED -> "";
        };
    }
}
