package com.chatsystem.admin.mock;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockLoginLog;
import com.chatsystem.desktop.mock.model.MockUser;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Read-only data for the Version 1 account views. */
public final class UserManagementFixtures {
    private static final MockData DATA = MockData.create();
    private static final List<Friendship> FRIENDSHIPS = List.of(
            new Friendship("usr-01", "usr-02", YearMonth.of(2025, 2)),
            new Friendship("usr-01", "usr-04", YearMonth.of(2025, 5)),
            new Friendship("usr-01", "usr-05", YearMonth.of(2025, 7)),
            new Friendship("usr-02", "usr-06", YearMonth.of(2025, 8)),
            new Friendship("usr-04", "usr-07", YearMonth.of(2025, 10)),
            new Friendship("usr-01", "usr-08", YearMonth.of(2025, 12)),
            new Friendship("usr-02", "usr-09", YearMonth.of(2026, 1)),
            new Friendship("usr-04", "usr-10", YearMonth.of(2026, 2)),
            new Friendship("usr-08", "usr-11", YearMonth.of(2026, 3)),
            // A second acceptance counts in the chart without adding a second direct friend.
            new Friendship("usr-02", "usr-06", YearMonth.of(2026, 4)),
            new Friendship("usr-05", "usr-13", YearMonth.of(2026, 5)),
            new Friendship("usr-06", "usr-14", YearMonth.of(2026, 7)),
            new Friendship("usr-04", "usr-15", YearMonth.of(2026, 8)),
            new Friendship("usr-07", "usr-16", YearMonth.of(2026, 8)),
            new Friendship("usr-13", "usr-17", YearMonth.of(2026, 9)),
            new Friendship("usr-16", "usr-18", YearMonth.of(2026, 10)),
            new Friendship("usr-10", "usr-18", YearMonth.of(2026, 10)));
    private static final Set<String> BLOCKED_PAIR = Set.of("usr-02", "usr-04");

    private UserManagementFixtures() {
    }

    public static List<MockUser> users() {
        return DATA.users();
    }

    public static Optional<MockUser> findUser(String id) {
        return users().stream().filter(user -> user.id().equals(id)).findFirst();
    }

    public static List<MockLoginLog> loginHistory(String id) {
        return DATA.loginLogs().stream().filter(login -> login.userId().equals(id)).toList();
    }

    public static List<MockUser> friends(String id) {
        return users().stream()
                .filter(user -> user.status() != MockUser.Status.DELETED && !hasBlockBetween(id, user.id()))
                .filter(user -> FRIENDSHIPS.stream().anyMatch(pair -> pair.joins(id, user.id())))
                .toList();
    }

    public static List<YearMonth> friendshipAcceptances() {
        return FRIENDSHIPS.stream().map(Friendship::acceptedMonth).toList();
    }

    public static boolean hasBlockBetween(String firstId, String secondId) {
        return !firstId.equals(secondId) && BLOCKED_PAIR.contains(firstId) && BLOCKED_PAIR.contains(secondId);
    }

    private record Friendship(String firstId, String secondId, YearMonth acceptedMonth) {
        boolean joins(String userId, String friendId) {
            return (firstId.equals(userId) && secondId.equals(friendId))
                    || (secondId.equals(userId) && firstId.equals(friendId));
        }
    }
}
