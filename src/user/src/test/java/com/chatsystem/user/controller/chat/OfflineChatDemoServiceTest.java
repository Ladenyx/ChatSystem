package com.chatsystem.user.controller.chat;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockMessage.State;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OfflineChatDemoServiceTest {
    private final MockData fixture = MockData.create();
    private final OfflineChatDemoService chat = new OfflineChatDemoService(fixture);

    @Test
    void latestPageContainsFiftyMessagesAndOlderPageKeepsOrder() {
        var latest = chat.page("grp-01", 50);
        var expanded = chat.page("grp-01", 100);
        assertEquals(50, latest.messages().size());
        assertTrue(latest.hasOlder());
        assertEquals(56, expanded.messages().size());
        assertFalse(expanded.hasOlder());
        assertEquals(latest.messages(), expanded.messages().subList(6, 56));
        assertEquals(fixture.messagesFor("grp-01"), expanded.messages());
    }

    @Test
    void retryDoesNotDuplicateAndDeliveryRequiresAcknowledgement() {
        String id = "direct-03";
        int before = chat.page(id, 50).messages().size();
        assertNull(chat.send(id, "   "));
        var pending = chat.send(id, "Nội dung gửi thử");
        assertEquals(pending, chat.latest(id));
        assertEquals(id, chat.conversations().getFirst().id());
        assertEquals(State.PENDING, pending.state());
        assertFalse(chat.retry(id, pending.id()));
        assertFalse(chat.advanceReceipt(id, pending.id(), State.DELIVERED));
        assertFalse(chat.acknowledge(id, pending.id(), false));
        assertEquals(State.FAILED, chat.latest(id).state());
        assertTrue(chat.retry(id, pending.id()));
        assertFalse(chat.retry(id, pending.id()));
        assertEquals(State.PENDING, chat.latest(id).state());
        assertTrue(chat.acknowledge(id, pending.id(), true));
        assertEquals(State.STORED, chat.latest(id).state());
        assertFalse(chat.advanceReceipt(id, pending.id(), State.READ));
        assertTrue(chat.advanceReceipt(id, pending.id(), State.DELIVERED));
        assertTrue(chat.advanceReceipt(id, pending.id(), State.READ));
        assertEquals(pending.id(), chat.latest(id).id());
        assertEquals(before + 1, chat.page(id, 50).messages().size());
    }

    @Test
    void recallRetainsTombstoneAndOnlyWorksForAnAcceptedOwnMessageWithin48Hours() {
        String id = "direct-01";
        var messages = chat.page(id, 50).messages();
        assertFalse(chat.recall(id, "demo-expired-recall"));
        var boundary = new MockMessage("boundary", id, MockData.PREVIEW_USER_ID, "at 48 hours",
                MockData.SNAPSHOT_AT.minus(Duration.ofHours(48)), State.READ);
        assertFalse(chat.canRecall(boundary));
        var incoming = messages.stream().filter(m -> !m.senderId().equals(MockData.PREVIEW_USER_ID)).findFirst().orElseThrow();
        assertFalse(chat.recall(id, incoming.id()));
        var eligible = messages.stream().filter(chat::canRecall).findFirst().orElseThrow();
        assertTrue(chat.recall(id, eligible.id()));
        var tombstone = chat.page(id, 50).messages().stream().filter(m -> m.id().equals(eligible.id()))
                .findFirst().orElseThrow();
        assertEquals(State.RECALLED, tombstone.state());
        assertEquals("", tombstone.text());
        assertEquals(messages.size(), chat.page(id, 50).messages().size());
        assertFalse(chat.recall(id, eligible.id()));
    }

    @Test
    void localDeleteUpdatesPreviewAndCancelsAnAttemptWithoutChangingFixture() {
        String id = "direct-02";
        var original = fixture.messagesFor(id);
        var pending = chat.send(id, "Xóa khi đang chờ");
        chat.deleteForSelf(id, Set.of(pending.id()));
        assertFalse(chat.acknowledge(id, pending.id(), false));
        var baseline = chat.page(id, 50).messages();
        chat.deleteForSelf(id, Set.of(baseline.getLast().id(), baseline.get(1).id()));
        assertEquals(baseline.get(baseline.size() - 2), chat.latest(id));
        assertEquals(baseline.size() - 2, chat.page(id, 50).messages().size());
        assertEquals(original, fixture.messagesFor(id));
    }

    @Test
    void blockedThreadsRetainHistoryAndLockedHistoryNeverExposesPlaintext() {
        assertEquals(OfflineChatDemoService.Access.BLOCKED_BY_SELF, chat.access("direct-04"));
        assertEquals(OfflineChatDemoService.Access.BLOCKED_BY_PEER, chat.access("direct-05"));
        assertNull(chat.send("direct-04", "Không thể gửi"));
        assertNull(chat.send("direct-05", "Không thể gửi"));
        assertFalse(chat.latest("direct-04").text().isEmpty());
        assertNull(chat.send("grp-03", "Không thể gửi"));
        assertFalse(fixture.messagesFor("grp-03").getLast().text().isEmpty());
        var locked = chat.page("grp-03", 50).messages();
        assertFalse(locked.isEmpty());
        assertTrue(locked.stream().allMatch(message -> message.text().isEmpty()));
        assertEquals("", chat.latest("grp-03").text());

        assertTrue(chat.conversations().stream().anyMatch(thread -> thread.id().equals("grp-01") && thread.unreadCount() > 0));
        chat.markRead("grp-01");
        assertTrue(chat.conversations().stream().anyMatch(thread -> thread.id().equals("grp-01") && thread.unreadCount() == 0));
    }
}
