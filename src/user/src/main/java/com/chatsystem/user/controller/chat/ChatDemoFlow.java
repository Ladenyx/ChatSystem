package com.chatsystem.user.controller.chat;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.ViewNavigator;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/** Offline workspace state owned by one User Client window. */
public final class ChatDemoFlow {
    private final MockData data = MockData.create();
    private final ReadOnlyObjectWrapper<MockConversation> selected = new ReadOnlyObjectWrapper<>();

    private ChatDemoFlow() {
    }

    public static ChatDemoFlow get(ViewNavigator navigator) {
        return (ChatDemoFlow) navigator.getRoot().getProperties().computeIfAbsent(
                ChatDemoFlow.class, key -> new ChatDemoFlow());
    }

    public static void clear(ViewNavigator navigator) {
        navigator.getRoot().getProperties().remove(ChatDemoFlow.class);
    }

    public MockData data() {
        return data;
    }

    public ReadOnlyObjectProperty<MockConversation> selectedProperty() {
        return selected.getReadOnlyProperty();
    }

    public void select(MockConversation conversation) {
        selected.set(conversation);
    }

    public boolean isOnline(MockConversation chat) {
        return chat.participantIds().stream().filter(id -> !id.equals(MockData.PREVIEW_USER_ID)).findFirst()
                .map(data::user).filter(peer -> peer.presence() == MockUser.Presence.ONLINE).isPresent();
    }

    public String statusText(MockConversation chat) {
        if (chat.encrypted()) {
            return "Mã hóa đầu cuối · Demo";
        }
        return isOnline(chat) ? "Đang hoạt động" : "Ngoại tuyến";
    }
}
