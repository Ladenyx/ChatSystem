package com.chatsystem.user.controller.chat;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockMessage.State;
import java.time.Duration;
import java.time.Instant;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Session-local chat demo. Each send attempt advances only through simulated acknowledgments. */
public final class OfflineChatDemoService {
    public static final int PAGE_SIZE = 50;
    private static final Instant DEMO_TIME = MockData.SNAPSHOT_AT;
    private static final Duration RECALL_WINDOW = Duration.ofHours(48);

    public enum Access {
        AVAILABLE, BLOCKED_BY_SELF, BLOCKED_BY_PEER, MISSING_KEY
    }

    public record HistoryPage(List<MockMessage> messages, boolean hasOlder) {}
    public record SearchResults(List<MockConversation> conversations, List<MockMessage> messages,
                                boolean syncing, boolean missingKey) {}

    private final List<MockConversation> conversations;
    private final Map<String, List<MockMessage>> history = new HashMap<>();
    private final Map<String, Integer> unread = new HashMap<>();
    private final Set<String> activeAttempts = new HashSet<>();
    private final Set<String> selfBlocked = new HashSet<>(Set.of("direct-04"));
    private final Set<String> unsynced = new HashSet<>();
    private long sequence;
    private boolean nextDirectThreeSendFails = true;

    public OfflineChatDemoService(MockData data) {
        conversations = data.conversations();
        for (MockConversation conversation : conversations) {
            String id = conversation.id();
            history.put(id, new ArrayList<>(data.messagesFor(id)));
            unread.put(id, conversation.unreadCount());
        }
        history.get("direct-01").addFirst(new MockMessage("demo-expired-recall", "direct-01",
                MockData.PREVIEW_USER_ID, "Mình gửi thông tin buổi họp lần trước.",
                DEMO_TIME.minus(RECALL_WINDOW).minusSeconds(1), State.READ));
        var groupHistory = history.get("grp-01");
        groupHistory.stream().limit(Math.max(0, groupHistory.size() - PAGE_SIZE))
                .map(MockMessage::id).forEach(unsynced::add);
    }

    public List<MockConversation> conversations() {
        return conversations.stream()
                .sorted(Comparator.comparing(this::latestTimestamp).reversed())
                .map(chat -> new MockConversation(chat.id(), chat.title(), chat.kind(),
                        chat.participantIds(), unread.get(chat.id())))
                .toList();
    }

    private Instant latestTimestamp(MockConversation chat) {
        var messages = history.get(chat.id());
        return messages.isEmpty() ? Instant.MIN : messages.getLast().sentAt();
    }

    public MockMessage latest(String conversationId) {
        var messages = history.get(conversationId);
        return messages.isEmpty() ? null : visible(messages.getLast());
    }

    public void markRead(String conversationId) {
        unread.put(conversationId, 0);
    }

    public HistoryPage page(String conversationId, int limit) {
        var messages = history.get(conversationId);
        int first = Math.max(0, messages.size() - limit);
        return new HistoryPage(messages.subList(first, messages.size()).stream().map(this::visible).toList(), first > 0);
    }

    private MockMessage visible(MockMessage message) {
        if (access(message.conversationId()) == Access.MISSING_KEY) {
            return new MockMessage(message.id(), message.conversationId(), message.senderId(),
                    "", message.sentAt(), message.state());
        }
        return message;
    }

    public Access access(String conversationId) {
        if (selfBlocked.contains(conversationId)) {
            return Access.BLOCKED_BY_SELF;
        }
        return switch (conversationId) {
            case "direct-05" -> Access.BLOCKED_BY_PEER;
            case "grp-03" -> Access.MISSING_KEY;
            default -> Access.AVAILABLE;
        };
    }

    public boolean setBlocked(String conversationId, boolean blocked) {
        boolean direct = conversations.stream().anyMatch(chat -> chat.id().equals(conversationId) && !chat.encrypted());
        return direct && (blocked ? selfBlocked.add(conversationId) : selfBlocked.remove(conversationId));
    }

    public void clearHistory(String conversationId) {
        var messages = history.get(conversationId);
        for (var message : messages) {
            activeAttempts.remove(message.id());
            unsynced.remove(message.id());
        }
        messages.clear();
        markRead(conversationId);
    }

    public SearchResults search(String conversationId, String query) {
        String needle = searchKey(query);
        boolean missingKey = conversationId != null && access(conversationId) == Access.MISSING_KEY;
        if (needle.isBlank()) {
            return new SearchResults(List.of(), List.of(), false, missingKey);
        }
        var scope = conversationId == null ? conversations()
                : conversations.stream().filter(chat -> chat.id().equals(conversationId)).toList();
        var names = conversationId == null ? scope.stream()
                .filter(chat -> searchKey(chat.title()).contains(needle))
                .toList() : List.<MockConversation>of();
        var available = scope.stream().filter(chat -> access(chat.id()) != Access.MISSING_KEY)
                .flatMap(chat -> history.get(chat.id()).stream()).toList();
        var order = Comparator.comparing(MockMessage::sentAt).thenComparing(MockMessage::id);
        var matches = available.stream().filter(message -> canFind(message) && !unsynced.contains(message.id()))
                .filter(message -> searchKey(message.text()).contains(needle))
                .sorted(conversationId == null ? order.reversed() : order).toList();
        return new SearchResults(names, matches, available.stream().anyMatch(m -> unsynced.contains(m.id())), missingKey);
    }

    public void finishHistorySync() {
        unsynced.clear();
    }

    /** Returns the window size needed to display a still-searchable hit, or zero for a stale hit. */
    public int historyLimitFor(String conversationId, String messageId) {
        var messages = history.getOrDefault(conversationId, List.of());
        int index = indexOf(messages, messageId);
        if (index < 0 || !canFind(messages.get(index))) {
            return 0;
        }
        return Math.ceilDiv(messages.size() - index, PAGE_SIZE) * PAGE_SIZE;
    }

    private boolean canFind(MockMessage message) {
        return message.state() != State.RECALLED && access(message.conversationId()) != Access.MISSING_KEY
                && !message.text().isBlank();
    }

    private static String searchKey(String text) {
        return Normalizer.normalize(text.strip().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ', 'd');
    }

    public MockMessage send(String conversationId, String draft) {
        if (access(conversationId) != Access.AVAILABLE || draft.isBlank()) {
            return null;
        }
        long number = ++sequence;
        String id = "demo-send-" + number;
        var message = new MockMessage(id, conversationId, MockData.PREVIEW_USER_ID,
                draft.strip(), DEMO_TIME.plusSeconds(number), State.PENDING);
        history.get(conversationId).add(message);
        activeAttempts.add(id);
        return message;
    }

    public boolean canRetry(MockMessage message) {
        return access(message.conversationId()) == Access.AVAILABLE
                && message.senderId().equals(MockData.PREVIEW_USER_ID)
                && (message.state() == State.PENDING || message.state() == State.FAILED)
                && !activeAttempts.contains(message.id());
    }

    public boolean retry(String conversationId, String messageId) {
        var messages = history.get(conversationId);
        int index = indexOf(messages, messageId);
        if (index < 0 || !canRetry(messages.get(index))) {
            return false;
        }
        activeAttempts.add(messageId);
        replaceState(messages, index, State.PENDING);
        return true;
    }

    public boolean acknowledge(String conversationId, String messageId, boolean retry) {
        if (!activeAttempts.remove(messageId)) {
            return false;
        }
        var messages = history.get(conversationId);
        int index = indexOf(messages, messageId);
        if (index < 0 || messages.get(index).state() != State.PENDING) {
            return false;
        }
        boolean simulateFailure = !retry && conversationId.equals("direct-03") && nextDirectThreeSendFails;
        if (simulateFailure) {
            nextDirectThreeSendFails = false;
        }
        if (simulateFailure || access(conversationId) != Access.AVAILABLE) {
            replaceState(messages, index, State.FAILED);
            return false;
        }
        replaceState(messages, index, State.STORED);
        return true;
    }

    public boolean advanceReceipt(String conversationId, String messageId, State next) {
        var messages = history.get(conversationId);
        int index = indexOf(messages, messageId);
        if (index < 0) {
            return false;
        }
        State previous = messages.get(index).state();
        if ((previous == State.STORED && next == State.DELIVERED)
                || (previous == State.DELIVERED && next == State.READ)) {
            replaceState(messages, index, next);
            return true;
        }
        return false;
    }

    public void deleteForSelf(String conversationId, Set<String> ids) {
        history.get(conversationId).removeIf(message -> {
            if (!ids.contains(message.id())) {
                return false;
            }
            activeAttempts.remove(message.id());
            unsynced.remove(message.id());
            return true;
        });
    }

    public boolean canRecall(MockMessage message) {
        var age = Duration.between(message.sentAt(), DEMO_TIME.plusSeconds(sequence));
        return access(message.conversationId()) != Access.MISSING_KEY
                && message.senderId().equals(MockData.PREVIEW_USER_ID)
                && message.acceptedByServer() && message.state() != State.RECALLED
                && !age.isNegative() && age.compareTo(RECALL_WINDOW) < 0;
    }

    public boolean recall(String conversationId, String messageId) {
        var messages = history.get(conversationId);
        int index = indexOf(messages, messageId);
        if (index < 0 || !canRecall(messages.get(index))) {
            return false;
        }
        replaceState(messages, index, State.RECALLED);
        return true;
    }

    private static int indexOf(List<MockMessage> messages, String messageId) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).id().equals(messageId)) {
                return i;
            }
        }
        return -1;
    }

    private static void replaceState(List<MockMessage> messages, int index, State next) {
        var old = messages.get(index);
        messages.set(index, new MockMessage(old.id(), old.conversationId(), old.senderId(),
                next == State.RECALLED ? "" : old.text(), old.sentAt(), next));
    }
}
