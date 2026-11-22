package com.chatsystem.user.control;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockMessage.State;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/** The reusable history row, with incoming, outgoing and selection variants. */
public final class ChatMessageRow extends HBox {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm").withZone(MockData.DISPLAY_ZONE);
    private static final PseudoClass MENU_OPEN = PseudoClass.getPseudoClass("menu-open");

    private final MockMessage message;

    public ChatMessageRow(MockMessage message, String sender, boolean outgoing) {
        this(message, sender, outgoing, null);
    }

    public ChatMessageRow(MockMessage message, String sender, boolean outgoing, Consumer<Button> onMoreAction) {
        this.message = message;
        setMinWidth(0);
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().addAll("chat-message-row", outgoing ? "is-outgoing" : "is-incoming");
        boolean recalled = message.state() == State.RECALLED;
        VBox content = new VBox(4);
        content.setMinWidth(0);
        content.maxWidthProperty().bind(widthProperty().multiply(0.80));
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
        bubble.setMinHeight(Region.USE_PREF_SIZE);
        bubble.maxWidthProperty().bind(widthProperty().multiply(0.66));
        bubble.getStyleClass().add("chat-bubble");
        bubble.getStyleClass().add(recalled ? "chat-bubble-unavailable"
                : outgoing ? "chat-bubble-outgoing" : "chat-bubble-incoming");

        javafx.scene.Node messageContent = bubble;
        if (onMoreAction != null) {
            Button moreButton = new Button();
            moreButton.getStyleClass().add("chat-message-more");
            moreButton.setGraphic(FontIcon.of(Feather.MORE_VERTICAL, 15));
            moreButton.setMinWidth(Region.USE_PREF_SIZE);
            moreButton.setAccessibleText("Thao tác tin nhắn");
            moreButton.setTooltip(new Tooltip("Thao tác tin nhắn"));
            moreButton.setOnAction(event -> onMoreAction.accept(moreButton));

            HBox bubbleWithMore = outgoing ? new HBox(8, moreButton, bubble) : new HBox(8, bubble, moreButton);
            bubbleWithMore.setAlignment(outgoing ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            bubbleWithMore.setMinWidth(0);
            messageContent = bubbleWithMore;
        }
        if (outgoing) {
            content.getChildren().add(messageContent);
        } else {
            Label avatar = new Label(ConversationAvatar.initials(sender));
            avatar.getStyleClass().add("chat-message-avatar");
            avatar.setMinWidth(Region.USE_PREF_SIZE);
            avatar.setAccessibleText(sender);
            HBox incomingRow = new HBox(8, avatar, messageContent);
            incomingRow.setAlignment(Pos.BOTTOM_LEFT);
            incomingRow.setMinWidth(0);
            content.getChildren().add(incomingRow);
        }
        String status = outgoing && !recalled ? " · " + deliveryLabel(message.state()) : "";
        Label metadata = new Label(TIME.format(message.sentAt()) + status);
        metadata.setWrapText(true);
        metadata.getStyleClass().addAll("chat-caption", "chat-message-meta");
        if (outgoing && message.state() == State.READ) {
            metadata.getStyleClass().add("chat-receipt-read");
        }
        content.getChildren().add(metadata);
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

    public void setMenuOpen(boolean open) {
        pseudoClassStateChanged(MENU_OPEN, open);
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
