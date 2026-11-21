package com.chatsystem.admin.mock;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockLoginLog;
import com.chatsystem.desktop.mock.model.MockUser;
import java.util.List;
import java.util.Optional;

/** Read-only data for the Version 1 account views. */
public final class UserManagementFixtures {
    private static final MockData DATA = MockData.create();
    private static final List<Friendship> FRIENDSHIPS = List.of(
            new Friendship("usr-01", "usr-02"), new Friendship("usr-01", "usr-04"),
            new Friendship("usr-01", "usr-05"), new Friendship("usr-01", "usr-08"),
            new Friendship("usr-02", "usr-06"), new Friendship("usr-04", "usr-07"));

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
        return users().stream().filter(user -> FRIENDSHIPS.stream().anyMatch(pair -> pair.joins(id, user.id())))
                .toList();
    }

    private record Friendship(String firstId, String secondId) {
        boolean joins(String userId, String friendId) {
            return (firstId.equals(userId) && secondId.equals(friendId))
                    || (secondId.equals(userId) && firstId.equals(friendId));
        }
    }
}
