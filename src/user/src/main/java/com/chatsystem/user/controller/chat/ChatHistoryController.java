package com.chatsystem.user.controller.chat;

import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.navigation.ViewNavigator;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.control.ChatMessageRow;
import com.chatsystem.user.control.ConversationAvatar;
import com.chatsystem.user.messaging.ConversationCatalog;
import com.chatsystem.user.navigation.UserViewInputs;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public final class ChatHistoryController {
    @FXML private Label conversationTitle;
    @FXML private Label conversationStatus;
    @FXML private ConversationAvatar conversationAvatar;
    @FXML private ScrollPane historyScroll;
    @FXML private VBox messageRows;
    @FXML private TextArea draftField;

    private ViewNavigator navigator;
    private MockConversation conversation;

    public void show(ViewNavigator navigator, ConversationCatalog catalog, MockConversation conversation) {
        this.navigator = navigator;
        boolean changed = !conversation.equals(this.conversation);
        this.conversation = conversation;
        conversationTitle.setText(conversation.title());
        conversationStatus.setText(catalog.statusText(conversation));
        conversationAvatar.show(conversation, catalog);
        if (changed) {
            draftField.clear();
            messageRows.getChildren().setAll(catalog.messagesFor(conversation.id()).stream()
                    .map(message -> new ChatMessageRow(message, catalog.user(message.senderId()).fullName(),
                            catalog.currentUserId().equals(message.senderId()))).toList());
            historyScroll.setVvalue(1);
        }
    }

    @FXML
    private void openInfo() {
        if (navigator.isDrawerOpen()) {
            navigator.closeDrawer();
        } else {
            navigator.showDrawer(UserViews.CONVERSATION_INFO, new UserViewInputs.Conversation(conversation.id()));
        }
    }
}
