package com.chatsystem.user.messaging;

import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockUser;
import java.util.List;

public interface ConversationCatalog {
    List<MockConversation> conversations();

    List<MockMessage> messagesFor(String conversationId);

    MockUser user(String id);

    String currentUserId();

    default boolean isOnline(MockConversation conversation) {
        return conversation.participantIds().stream()
                .filter(id -> !currentUserId().equals(id))
                .map(this::user)
                .anyMatch(user -> user.presence() == MockUser.Presence.ONLINE);
    }

    default String statusText(MockConversation conversation) {
        if (conversation.encrypted()) {
            return "Mã hóa đầu cuối";
        }
        return isOnline(conversation) ? "Đang hoạt động" : "Ngoại tuyến";
    }
}
