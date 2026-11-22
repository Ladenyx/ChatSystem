package com.chatsystem.user.mock;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.user.messaging.ConversationCatalog;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MockConversationCatalog implements ConversationCatalog {
    // Anchor slightly past 48h after direct-01's first message so U-16 demos both recallable and expired states.
    private static final Instant DEFAULT_REFERENCE_TIME =
            MockData.SNAPSHOT_AT.plus(Duration.ofHours(2).plusSeconds(30));

    private final MockData data = MockData.create();
    private final Instant referenceTime;
    private final Map<String, MockMessage> messagesById;
    private final Set<String> deletedForSelfIds = new LinkedHashSet<>();
    private final Set<String> recalledIds = new LinkedHashSet<>();
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();

    public MockConversationCatalog() {
        this(DEFAULT_REFERENCE_TIME);
    }

    public MockConversationCatalog(Instant referenceTime) {
        this.referenceTime = Objects.requireNonNull(referenceTime, "referenceTime");
        this.messagesById = data.messages().stream()
                .collect(Collectors.toUnmodifiableMap(MockMessage::id, Function.identity()));
    }

    @Override
    public List<MockConversation> conversations() {
        return data.conversations();
    }

    @Override
    public List<MockMessage> messagesFor(String conversationId) {
        return data.messagesFor(conversationId).stream()
                .filter(message -> !deletedForSelfIds.contains(message.id()))
                .map(message -> recalledIds.contains(message.id())
                        ? new MockMessage(message.id(), message.conversationId(), message.senderId(),
                                "", message.sentAt(), MockMessage.State.RECALLED)
                        : message)
                .toList();
    }

    @Override
    public MockUser user(String id) {
        return data.user(id);
    }

    @Override
    public String currentUserId() {
        return data.previewUser().id();
    }

    @Override
    public Instant referenceTime() {
        return referenceTime;
    }

    @Override
    public void recallForEveryone(String messageId) {
        if (messageId == null || !messagesById.containsKey(messageId)) {
            throw new IllegalArgumentException("Không tìm thấy tin nhắn.");
        }
        MockMessage original = messagesById.get(messageId);
        if (original.state() == MockMessage.State.RECALLED || recalledIds.contains(messageId)) {
            return;
        }
        if (!currentUserId().equals(original.senderId())) {
            throw new IllegalStateException("Chỉ người gửi mới có thể thu hồi tin nhắn này.");
        }
        if (!original.acceptedByServer()) {
            throw new IllegalStateException("Chỉ có thể thu hồi tin nhắn đã được xác nhận.");
        }
        if (!canRecallForEveryone(original)) {
            throw new IllegalStateException("Đã quá thời hạn 48 giờ để thu hồi với mọi người.");
        }
        if (recalledIds.add(messageId)) {
            notifyChanged();
        }
    }

    @Override
    public void deleteForSelf(Collection<String> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            throw new IllegalArgumentException("Cần chọn ít nhất một tin nhắn để xóa.");
        }
        Set<String> uniqueIds = new LinkedHashSet<>();
        for (String id : messageIds) {
            if (id == null || !messagesById.containsKey(id)) {
                throw new IllegalArgumentException("Không tìm thấy tin nhắn cần xóa.");
            }
            uniqueIds.add(id);
        }
        if (uniqueIds.size() > MAX_BULK_DELETE) {
            throw new IllegalArgumentException("Chỉ có thể xóa tối đa " + MAX_BULK_DELETE + " tin nhắn mỗi lần.");
        }
        String conversationId = null;
        for (String id : uniqueIds) {
            MockMessage message = messagesById.get(id);
            if (conversationId == null) {
                conversationId = message.conversationId();
            } else if (!conversationId.equals(message.conversationId())) {
                throw new IllegalArgumentException("Các tin nhắn được chọn phải thuộc cùng một hội thoại.");
            }
            if (!message.acceptedByServer()) {
                throw new IllegalStateException("Chỉ có thể xóa tin nhắn đã được xác nhận.");
            }
        }
        boolean changed = deletedForSelfIds.addAll(uniqueIds);
        if (changed) {
            notifyChanged();
        }
    }

    @Override
    public void addChangeListener(Runnable listener) {
        Objects.requireNonNull(listener, "listener");
        if (!changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    @Override
    public void removeChangeListener(Runnable listener) {
        if (listener != null) {
            changeListeners.remove(listener);
        }
    }

    public void reset() {
        if (deletedForSelfIds.isEmpty() && recalledIds.isEmpty()) {
            return;
        }
        deletedForSelfIds.clear();
        recalledIds.clear();
        notifyChanged();
    }

    private void notifyChanged() {
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }
}
