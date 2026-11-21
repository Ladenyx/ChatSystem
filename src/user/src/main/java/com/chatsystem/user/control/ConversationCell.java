package com.chatsystem.user.control;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.user.controller.chat.ChatDemoFlow;
import java.time.format.DateTimeFormatter;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Reusable Conversation item with Avatar, Presence and Badge states. */
public final class ConversationCell extends ListCell<MockConversation> {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm").withZone(MockData.DISPLAY_ZONE);

    private final ChatDemoFlow flow;
    private final ConversationAvatar avatar = new ConversationAvatar();
    private final Label name = new Label();
    private final Label preview = new Label();
    private final Label time = new Label();
    private final Label unread = new Label();
    private final HBox row;

    public ConversationCell(ChatDemoFlow flow) {
        this.flow = flow;
        getStyleClass().add("conversation-cell");
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        // Let the viewport size the cell so a long preview never creates a horizontal scrollbar.
        setPrefWidth(0);

        name.getStyleClass().add("chat-row-name");
        name.setWrapText(true);
        name.setMaxHeight(36);
        preview.getStyleClass().add("chat-caption");
        preview.setWrapText(true);
        preview.setMaxHeight(32);
        VBox text = new VBox(4, name, preview);
        text.setMinWidth(0);
        text.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(text, Priority.ALWAYS);

        time.getStyleClass().add("chat-caption");
        unread.getStyleClass().add("chat-unread");
        VBox metadata = new VBox(4, time, unread);
        metadata.setAlignment(Pos.CENTER_RIGHT);
        metadata.setMinWidth(36);

        row = new HBox(16, avatar, text, metadata);
        row.setMinWidth(0);
        row.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(0, getWidth() - snappedLeftInset() - snappedRightInset()),
                widthProperty(), paddingProperty()));
        row.setAlignment(Pos.CENTER_LEFT);
        unread.managedProperty().bind(unread.visibleProperty());
    }

    @Override
    protected void updateItem(MockConversation chat, boolean empty) {
        super.updateItem(chat, empty);
        if (empty || chat == null) {
            setGraphic(null);
            setAccessibleText(null);
            return;
        }
        avatar.show(chat, flow);
        name.setText(chat.title());
        name.getStyleClass().setAll("label", "chat-row-name");
        if (chat.unreadCount() > 0) {
            name.getStyleClass().add("chat-row-unread");
        }
        var messages = flow.data().messagesFor(chat.id());
        MockMessage last = messages.isEmpty() ? null : messages.getLast();
        preview.setText(last == null ? "Chưa có tin nhắn" : last.state() == MockMessage.State.RECALLED
                ? "Tin nhắn đã thu hồi" : last.text().replaceAll("\\s+", " "));
        time.setText(last == null ? "" : TIME.format(last.sentAt()));
        unread.setText(Integer.toString(chat.unreadCount()));
        unread.setVisible(chat.unreadCount() > 0);
        setAccessibleText(chat.title() + ", " + flow.statusText(chat)
                + (unread.isVisible() ? ", " + unread.getText() + " tin chưa đọc" : ""));
        setGraphic(row);
    }
}
