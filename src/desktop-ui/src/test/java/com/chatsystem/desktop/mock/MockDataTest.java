package com.chatsystem.desktop.mock;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockGroup;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockMonthlyStatistics;
import com.chatsystem.desktop.mock.model.MockSpamReport;
import com.chatsystem.desktop.mock.model.MockUser;
import java.time.Duration;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class MockDataTest {
    private final MockData data = MockData.create();

    @Test
    void snapshotsAreDeterministicAndCoverDisplayStates() {
        MockData other = MockData.create();
        assertEquals(data.users(), other.users());
        assertEquals(data.groups(), other.groups());
        assertEquals(data.conversations(), other.conversations());
        assertEquals(data.messages(), other.messages());
        assertEquals(data.spamReports(), other.spamReports());
        assertEquals(data.loginLogs(), other.loginLogs());
        assertEquals(data.activities(), other.activities());
        assertEquals(20, data.users().size());
        assertEquals(4, data.groups().size());
        assertEquals(9, data.conversations().size());
        assertEquals(126, data.messages().size());
        assertEquals(6, data.spamReports().size());
        assertEquals(java.util.Set.of(MockUser.Status.values()),
                new HashSet<>(data.users().stream().map(MockUser::status).toList()));
        assertEquals(java.util.Set.of(MockUser.Presence.values()),
                new HashSet<>(data.users().stream().map(MockUser::presence).toList()));
        assertEquals(java.util.Set.of(MockMessage.State.values()),
                new HashSet<>(data.messages().stream().map(MockMessage::state).toList()));
        assertTrue(data.users().stream().allMatch(user -> user.email().endsWith("@example.test")));
        assertTrue(data.users().stream().anyMatch(user -> user.birthDate().isEmpty()));
        assertTrue(data.messages().stream().anyMatch(message -> message.text().contains("\n")));
        assertTrue(data.messages().stream().anyMatch(message -> message.text().length() > 150));
        assertTrue(data.messages().stream().filter(message -> message.state() == MockMessage.State.RECALLED)
                .allMatch(message -> message.text().isEmpty()));
        assertTrue(data.messages().stream().filter(message -> !message.acceptedByServer())
                .allMatch(message -> message.senderId().equals(MockData.PREVIEW_USER_ID)));
    }

    @Test
    void allReferencesAndTimestampsBelongToTheSameSnapshot() {
        assertUnique(data.users(), MockUser::id);
        assertUnique(data.users(), MockUser::username);
        assertUnique(data.groups(), MockGroup::id);
        assertUnique(data.conversations(), MockConversation::id);
        assertUnique(data.messages(), MockMessage::id);
        assertUnique(data.spamReports(), MockSpamReport::id);
        assertUnique(data.loginLogs(), log -> log.id());
        assertEquals(MockUser.Status.ACTIVE, data.previewUser().status());
        for (MockUser user : data.users()) {
            assertFalse(user.createdAt().isAfter(MockData.SNAPSHOT_AT));
            assertEquals(user.status() == MockUser.Status.DELETED, user.deletedAt().isPresent());
            user.deletedAt().ifPresent(at -> {
                assertFalse(at.isBefore(user.createdAt()));
                assertFalse(at.isAfter(MockData.SNAPSHOT_AT));
                assertTrue(Duration.between(at, MockData.SNAPSHOT_AT).toDays() < 30);
            });
            if (user.status() != MockUser.Status.ACTIVE) {
                assertEquals(MockUser.Presence.OFFLINE, user.presence());
            }
        }
        for (MockConversation chat : data.conversations()) {
            assertTrue(chat.participantIds().contains(MockData.PREVIEW_USER_ID));
            assertEquals(chat.participantIds().size(), new HashSet<>(chat.participantIds()).size());
            chat.participantIds().forEach(data::user);
            List<MockMessage> messages = data.messagesFor(chat.id());
            assertFalse(messages.isEmpty());
            assertEquals(messages.stream().sorted(Comparator.comparing(MockMessage::sentAt)).toList(), messages);
            assertTrue(chat.unreadCount() <= messages.stream()
                    .filter(message -> !message.senderId().equals(MockData.PREVIEW_USER_ID)).count());
            if (chat.kind() == MockConversation.Kind.DIRECT) {
                assertEquals(2, chat.participantIds().size());
                assertFalse(chat.encrypted());
            } else {
                MockGroup group = data.groups().stream().filter(item -> item.id().equals(chat.id())).findFirst().orElseThrow();
                assertEquals(group.name(), chat.title());
                assertEquals(group.members().stream().map(MockGroup.Member::userId).toList(), chat.participantIds());
                assertTrue(chat.encrypted());
                assertTrue(group.members().stream().anyMatch(member -> member.role() == MockGroup.Role.ADMIN));
                assertTrue(messages.stream().noneMatch(message -> message.sentAt().isBefore(group.createdAt())));
                assertTrue(group.members().stream().noneMatch(member -> data.user(member.userId()).status() == MockUser.Status.DELETED));
            }
        }
        assertTrue(data.messagesFor("grp-01").size() > 50);
        assertTrue(data.messagesFor("missing-conversation").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> data.user("missing-user"));
        for (MockMessage message : data.messages()) {
            MockConversation chat = data.conversations().stream()
                    .filter(item -> item.id().equals(message.conversationId())).findFirst().orElseThrow();
            assertTrue(chat.participantIds().contains(message.senderId()));
            assertFalse(message.sentAt().isBefore(data.user(message.senderId()).createdAt()));
            assertFalse(message.sentAt().isAfter(MockData.SNAPSHOT_AT));
        }
        for (var login : data.loginLogs()) {
            assertFalse(login.loggedInAt().isBefore(data.user(login.userId()).createdAt()));
            assertFalse(login.loggedInAt().isAfter(MockData.SNAPSHOT_AT));
            data.user(login.userId()).deletedAt().ifPresent(at -> assertTrue(login.loggedInAt().isBefore(at)));
        }
        assertEquals(data.loginLogs().stream().sorted(Comparator.comparing(
                com.chatsystem.desktop.mock.model.MockLoginLog::loggedInAt).reversed()
                .thenComparing(com.chatsystem.desktop.mock.model.MockLoginLog::id)).toList(), data.loginLogs());
    }

    @Test
    void reportEvidenceIsAnOrderedDeduplicatedSnapshotWithSelectedAndContextLabels() {
        boolean foundOverlap = false;
        boolean foundSelectedOutsideContext = false;
        for (MockSpamReport report : data.spamReports()) {
            data.user(report.reporterId());
            data.user(report.reportedUserId());
            assertFalse(report.reporterId().equals(report.reportedUserId()));
            assertFalse(report.createdAt().isAfter(MockData.SNAPSHOT_AT));
            assertUnique(report.evidence(), item -> item.message().id());
            assertTrue(report.evidence().stream().anyMatch(MockSpamReport.Evidence::selected));
            List<MockMessage> eligible = data.messagesFor(report.conversationId()).stream()
                    .filter(message -> message.acceptedByServer() && message.state() != MockMessage.State.RECALLED)
                    .filter(message -> !message.sentAt().isAfter(report.createdAt())).toList();
            assertEquals(eligible.subList(Math.max(0, eligible.size() - 30), eligible.size()),
                    report.evidence().stream().filter(MockSpamReport.Evidence::context)
                            .map(MockSpamReport.Evidence::message).toList());
            for (var item : report.evidence()) {
                assertTrue(item.selected() || item.context());
                assertEquals(report.conversationId(), item.message().conversationId());
                assertTrue(eligible.contains(item.message()));
                if (item.selected()) {
                    assertEquals(report.reportedUserId(), item.message().senderId());
                }
                foundOverlap |= item.selected() && item.context();
                foundSelectedOutsideContext |= item.selected() && !item.context();
            }
        }
        assertTrue(foundOverlap, "Direct report keeps both labels on overlapping evidence");
        assertTrue(foundSelectedOutsideContext, "Long group history keeps the older Selected message");
        assertThrows(IllegalArgumentException.class,
                () -> new MockSpamReport.Evidence(data.messages().getFirst(), false, false));
    }

    @Test
    void twelveMonthTotalsMatchTheHandCountedFixtureIncludingEmptyAndFutureMonths() {
        assertMonthly(2025, new int[]{1, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1},
                new int[]{1, 2, 3, 3, 4, 4, 5, 6, 6, 7, 7, 8});
        assertMonthly(2026, new int[]{1, 1, 2, 0, 1, 0, 1, 2, 1, 2, 1, 0},
                new int[]{9, 10, 12, 12, 13, 13, 14, 16, 17, 19, 19, 0});
        assertMonthly(2027, new int[12], new int[12]);
        assertEquals(data.users().size(), data.availableYears().stream().flatMap(year -> data.monthlyStatistics(year).stream())
                .mapToInt(MockMonthlyStatistics::registrations).sum());
        // Soft-deleted profiles remain counted in their original registration month.
        assertEquals(2, data.monthlyStatistics(2026).get(9).registrations());
    }

    @Test
    void activityCountsLaunchesAndDistinctAcceptedDestinationsRatherThanMessages() {
        var mine = data.activities().stream().filter(row -> row.userId().equals(MockData.PREVIEW_USER_ID)).findFirst().orElseThrow();
        assertEquals(16, mine.appOpens()); // 11 first launches + 5 second launches, Jan–Nov.
        assertEquals(5, mine.peopleChattedWith());
        assertEquals(4, mine.groupsChattedIn());
        var nonParticipant = data.activities().stream().filter(row -> row.userId().equals("usr-17")).findFirst().orElseThrow();
        assertEquals(4, nonParticipant.appOpens());
        assertEquals(0, nonParticipant.peopleChattedWith());
        assertEquals(0, nonParticipant.groupsChattedIn());
        assertEquals(data.users().size(), data.activities().size());
    }

    @Test
    void callersCannotMutateSharedFixturesOrNestedLists() {
        assertThrows(UnsupportedOperationException.class, () -> data.users().clear());
        assertThrows(UnsupportedOperationException.class, () -> data.groups().clear());
        assertThrows(UnsupportedOperationException.class, () -> data.messages().clear());
        assertThrows(UnsupportedOperationException.class, () -> data.conversations().clear());
        assertThrows(UnsupportedOperationException.class, () -> data.spamReports().clear());
        assertThrows(UnsupportedOperationException.class, () -> data.loginLogs().clear());
        assertThrows(UnsupportedOperationException.class, () -> data.activities().clear());
        assertThrows(UnsupportedOperationException.class, () -> data.availableYears().clear());
        assertThrows(UnsupportedOperationException.class, () -> data.monthlyStatistics(2026).clear());
        assertThrows(UnsupportedOperationException.class, () -> data.messagesFor("grp-01").clear());
        assertThrows(UnsupportedOperationException.class, () -> data.groups().getFirst().members().clear());
        assertThrows(UnsupportedOperationException.class, () -> data.conversations().getFirst().participantIds().clear());
        assertThrows(UnsupportedOperationException.class, () -> data.spamReports().getFirst().evidence().clear());
        var members = new ArrayList<>(data.groups().getFirst().members());
        MockGroup group = new MockGroup("copy", "Copy", MockData.SNAPSHOT_AT, members);
        members.clear();
        assertFalse(group.members().isEmpty());
        var evidence = new ArrayList<>(data.spamReports().getFirst().evidence());
        MockSpamReport report = new MockSpamReport("copy", "usr-01", "usr-02", "direct-01", "Copy",
                MockData.SNAPSHOT_AT, MockSpamReport.Status.PENDING, evidence);
        evidence.clear();
        assertFalse(report.evidence().isEmpty());
    }

    private void assertMonthly(int year, int[] registrations, int[] activeUsers) {
        List<MockMonthlyStatistics> months = data.monthlyStatistics(year);
        assertEquals(12, months.size());
        for (int index = 0; index < 12; index++) {
            assertEquals(YearMonth.of(year, index + 1), months.get(index).month());
            assertEquals(registrations[index], months.get(index).registrations(), "Registrations " + months.get(index).month());
            assertEquals(activeUsers[index], months.get(index).activeUsers(), "Active users " + months.get(index).month());
        }
    }

    private static <T> void assertUnique(List<T> rows, Function<T, String> id) {
        assertEquals(rows.size(), rows.stream().map(id).distinct().count());
    }
}
