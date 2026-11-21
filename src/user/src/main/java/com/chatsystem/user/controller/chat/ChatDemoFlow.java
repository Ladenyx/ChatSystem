package com.chatsystem.user.controller.chat;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.desktop.navigation.ViewNavigator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.util.Duration;

/** Offline workspace state owned by one User Client window. */
public final class ChatDemoFlow {
    private final MockData data = MockData.create();
    private final OfflineChatDemoService chat = new OfflineChatDemoService(data);
    private final Map<String, String> drafts = new HashMap<>();
    private final ReadOnlyObjectWrapper<MockConversation> selected = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyLongWrapper searchRevision = new ReadOnlyLongWrapper();
    private final ReadOnlyLongWrapper conversationRevision = new ReadOnlyLongWrapper();
    private BiPredicate<String, String> messageNavigator;
    private Consumer<MockConversation> reloadConversation;
    private Consumer<MockConversation> openCreatedGroup;
    private PauseTransition searchSync;

    private ChatDemoFlow() {
    }

    public static ChatDemoFlow get(ViewNavigator navigator) {
        return (ChatDemoFlow) navigator.getRoot().getProperties().computeIfAbsent(
                ChatDemoFlow.class, key -> new ChatDemoFlow());
    }

    public static void clear(ViewNavigator navigator) {
        navigator.getRoot().getProperties().remove(ChatDemoFlow.class);
    }

    public boolean isCurrent(ViewNavigator navigator) {
        return navigator.currentViewProperty().get() != null
                && navigator.getRoot().getProperties().get(ChatDemoFlow.class) == this;
    }

    public MockData data() {
        return data;
    }

    public OfflineChatDemoService chat() {
        return chat;
    }

    public void connectWorkspace(BiPredicate<String, String> messageNavigator, Consumer<MockConversation> reloadConversation,
                                 Consumer<MockConversation> openCreatedGroup) {
        this.messageNavigator = messageNavigator;
        this.reloadConversation = reloadConversation;
        this.openCreatedGroup = openCreatedGroup;
    }

    public void disconnectWorkspace() {
        messageNavigator = null;
        reloadConversation = null;
        openCreatedGroup = null;
    }

    public boolean jumpToMessage(String conversationId, String messageId) {
        return messageNavigator != null && messageNavigator.test(conversationId, messageId);
    }

    public void conversationChanged() {
        if (reloadConversation != null && selected.get() != null) {
            MockConversation updated = chat.conversation(selected.get().id());
            selected.set(updated);
            reloadConversation.accept(updated);
        }
        conversationRevision.set(conversationRevision.get() + 1);
        searchChanged();
    }

    public ReadOnlyLongProperty conversationRevisionProperty() {
        return conversationRevision.getReadOnlyProperty();
    }

    public void openCreatedGroup(MockConversation group) {
        if (openCreatedGroup != null) {
            openCreatedGroup.accept(group);
        }
    }

    public ReadOnlyLongProperty searchRevisionProperty() {
        return searchRevision.getReadOnlyProperty();
    }

    public void searchChanged() {
        searchRevision.set(searchRevision.get() + 1);
    }

    public OfflineChatDemoService.SearchResults search(String conversationId, String query) {
        var result = chat.search(conversationId, query);
        if (result.syncing() && searchSync == null) {
            searchSync = new PauseTransition(Duration.millis(900));
            searchSync.setOnFinished(event -> {
                chat.finishHistorySync();
                searchSync = null;
                searchChanged();
            });
            searchSync.play();
        }
        return result;
    }

    public String draft(String conversationId) {
        return drafts.getOrDefault(conversationId, "");
    }

    public void saveDraft(String conversationId, String draft) {
        if (draft.isEmpty()) {
            drafts.remove(conversationId);
        } else {
            drafts.put(conversationId, draft);
        }
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
            return switch (this.chat.access(chat.id())) {
                case GROUP_LEFT -> "Đã rời nhóm · Chỉ đọc";
                case GROUP_REMOVED -> "Đã bị xóa khỏi nhóm · Chỉ đọc";
                default -> "Mã hóa đầu cuối · Demo";
            };
        }
        return isOnline(chat) ? "Đang hoạt động" : "Ngoại tuyến";
    }
}
