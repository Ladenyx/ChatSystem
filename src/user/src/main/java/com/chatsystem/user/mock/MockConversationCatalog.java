package com.chatsystem.user.mock;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.user.messaging.ConversationCatalog;
import java.util.List;

public final class MockConversationCatalog implements ConversationCatalog {
    private final MockData data = MockData.create();

    @Override
    public List<MockConversation> conversations() {
        return data.conversations();
    }

    @Override
    public List<MockMessage> messagesFor(String conversationId) {
        return data.messagesFor(conversationId);
    }

    @Override
    public MockUser user(String id) {
        return data.user(id);
    }

    @Override
    public String currentUserId() {
        return data.previewUser().id();
    }
}
