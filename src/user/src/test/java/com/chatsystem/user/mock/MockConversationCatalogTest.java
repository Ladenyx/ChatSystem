package com.chatsystem.user.mock;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.user.messaging.ConversationCatalog;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class MockConversationCatalogTest {
    @Test
    void defaultReferenceTimeDemonstratesBothExpiredAndRecallableOwnMessages() {
        var catalog = new MockConversationCatalog();
        var messages = catalog.messagesFor("direct-01");
        MockMessage firstOwn = messages.stream()
                .filter(m -> m.id().equals("msg-direct-01-1")).findFirst().orElseThrow();
        MockMessage laterOwn = messages.stream()
                .filter(m -> m.id().equals("msg-direct-01-3")).findFirst().orElseThrow();
        MockMessage incoming = messages.stream()
                .filter(m -> m.id().equals("msg-direct-01-2")).findFirst().orElseThrow();

        assertFalse(catalog.canRecallForEveryone(firstOwn), "48h00m30s old message exceeds 48h window");
        assertTrue(catalog.canRecallForEveryone(laterOwn), "47h58m30s old message stays inside 48h window");
        assertFalse(catalog.canRecallForEveryone(incoming), "Cannot recall another participant's message");
        assertTrue(catalog.canDeleteForSelf(firstOwn));
        assertTrue(catalog.canDeleteForSelf(incoming));
    }

    @Test
    void enforcesExact48HourBoundaryAndIdempotentRecall() {
        var base = MockData.create().messagesFor("direct-01").getFirst();
        var atExactBoundary = new MockConversationCatalog(base.sentAt().plus(ConversationCatalog.RECALL_WINDOW));
        assertTrue(atExactBoundary.canRecallForEveryone(base));
        atExactBoundary.recallForEveryone(base.id());
        atExactBoundary.recallForEveryone(base.id()); // idempotent (UC-USR-MSG-06 A1)

        var updated = atExactBoundary.messagesFor("direct-01").stream()
                .filter(m -> m.id().equals(base.id())).findFirst().orElseThrow();
        assertEquals(MockMessage.State.RECALLED, updated.state());
        assertEquals("", updated.text());

        var pastBoundary = new MockConversationCatalog(
                base.sentAt().plus(ConversationCatalog.RECALL_WINDOW).plusSeconds(1));
        assertFalse(pastBoundary.canRecallForEveryone(base));
        assertThrows(IllegalStateException.class, () -> pastBoundary.recallForEveryone(base.id()));
    }

    @Test
    void rejectsRecallForOthersOrUnconfirmedMessages() {
        var catalog = new MockConversationCatalog(MockData.SNAPSHOT_AT);
        String otherMsgId = catalog.messagesFor("direct-01").stream()
                .filter(m -> !catalog.currentUserId().equals(m.senderId()))
                .findFirst().orElseThrow().id();
        String pendingId = catalog.messagesFor("direct-02").stream()
                .filter(m -> m.state() == MockMessage.State.PENDING)
                .findFirst().orElseThrow().id();
        String failedId = catalog.messagesFor("direct-03").stream()
                .filter(m -> m.state() == MockMessage.State.FAILED)
                .findFirst().orElseThrow().id();

        assertThrows(IllegalStateException.class, () -> catalog.recallForEveryone(otherMsgId));
        assertThrows(IllegalStateException.class, () -> catalog.recallForEveryone(pendingId));
        assertThrows(IllegalStateException.class, () -> catalog.recallForEveryone(failedId));
    }

    @Test
    void deleteForSelfValidatesLimitScopeConfirmationAndNotifiesListeners() {
        var catalog = new MockConversationCatalog();
        AtomicInteger notifications = new AtomicInteger();
        catalog.addChangeListener(notifications::incrementAndGet);

        List<String> twoIds = catalog.messagesFor("direct-04").stream()
                .limit(2).map(MockMessage::id).toList();
        catalog.deleteForSelf(twoIds);
        assertEquals(1, notifications.get());
        assertEquals(6, catalog.messagesFor("direct-04").size());

        catalog.deleteForSelf(twoIds); // idempotent repeat (UC-USR-MSG-04 A2)
        assertEquals(1, notifications.get());

        List<String> overLimit = catalog.messagesFor("grp-01").stream()
                .filter(MockMessage::acceptedByServer)
                .limit(ConversationCatalog.MAX_BULK_DELETE + 1)
                .map(MockMessage::id).toList();
        assertEquals(ConversationCatalog.MAX_BULK_DELETE + 1, overLimit.size());
        assertThrows(IllegalArgumentException.class, () -> catalog.deleteForSelf(overLimit));

        String directOneId = catalog.messagesFor("direct-01").getFirst().id();
        String directFourRemainingId = catalog.messagesFor("direct-04").getFirst().id();
        assertThrows(IllegalArgumentException.class,
                () -> catalog.deleteForSelf(List.of(directOneId, directFourRemainingId)));

        String pendingId = catalog.messagesFor("direct-02").stream()
                .filter(m -> m.state() == MockMessage.State.PENDING)
                .findFirst().orElseThrow().id();
        assertThrows(IllegalStateException.class, () -> catalog.deleteForSelf(List.of(pendingId)));

        catalog.reset();
        assertEquals(2, notifications.get());
        assertEquals(8, catalog.messagesFor("direct-04").size());
    }
}
