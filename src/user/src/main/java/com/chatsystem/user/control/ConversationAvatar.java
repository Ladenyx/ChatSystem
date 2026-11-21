package com.chatsystem.user.control;

import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.user.controller.chat.ChatDemoFlow;
import java.util.Locale;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/** The shared 40 px avatar from the conversation row, header and drawer. */
public final class ConversationAvatar extends StackPane {
    private final Label initials = new Label();
    private final StackPane presence = new StackPane();
    private final ChatIcon lock = new ChatIcon("lock");

    public ConversationAvatar() {
        initials.getStyleClass().add("chat-avatar");
        lock.getStyleClass().add("chat-lock");
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setAlignment(presence, Pos.BOTTOM_RIGHT);
        getChildren().setAll(initials, presence);
    }

    public void show(MockConversation chat, ChatDemoFlow flow) {
        initials.setText(initials(chat.title()));
        presence.getStyleClass().setAll("chat-presence");
        if (chat.encrypted()) {
            presence.getStyleClass().add("chat-presence-encrypted");
            presence.getChildren().setAll(lock);
        } else {
            presence.getStyleClass().add(flow.isOnline(chat) ? "chat-presence-online" : "chat-presence-offline");
            presence.getChildren().clear();
        }
    }

    private static String initials(String title) {
        String[] words = title.strip().split("\\s+");
        return (words[0].substring(0, 1) + (words.length > 1 ? words[words.length - 1].substring(0, 1) : ""))
                .toUpperCase(Locale.ROOT);
    }
}
