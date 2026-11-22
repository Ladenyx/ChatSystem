package com.chatsystem.user.messaging;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockUser;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface ConversationCatalog {
    Duration RECALL_WINDOW = Duration.ofHours(48);
    int MAX_BULK_DELETE = 50;

    List<MockConversation> conversations();

    List<MockMessage> messagesFor(String conversationId);

    MockUser user(String id);

    String currentUserId();

    default Instant referenceTime() {
        return MockData.SNAPSHOT_AT;
    }

    default boolean canDeleteForSelf(MockMessage message) {
        return message != null
                && message.acceptedByServer()
                && message.state() != MockMessage.State.RECALLED;
    }

    default boolean canRecallForEveryone(MockMessage message) {
        return canDeleteForSelf(message)
                && currentUserId().equals(message.senderId())
                && !referenceTime().isBefore(message.sentAt())
                && !referenceTime().isAfter(message.sentAt().plus(RECALL_WINDOW));
    }

    default void recallForEveryone(String messageId) {
        throw new UnsupportedOperationException("Recall is not supported");
    }

    default void deleteForSelf(Collection<String> messageIds) {
        throw new UnsupportedOperationException("Delete for self is not supported");
    }

    default void addChangeListener(Runnable listener) {
    }

    default void removeChangeListener(Runnable listener) {
    }

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
