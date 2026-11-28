package com.chatsystem.admin.service;

import com.chatsystem.admin.mock.UserManagementFixtures;
import com.chatsystem.common.api.OffsetPage;
import com.chatsystem.common.api.SortDirection;
import com.chatsystem.common.domain.AccountStatus;
import com.chatsystem.common.domain.UserRole;
import com.chatsystem.common.dto.admin.AdminUserListItemDto;
import com.chatsystem.desktop.mock.model.MockUser;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Deterministic offline mock implementation backed by {@link UserManagementFixtures}. */
public final class MockAdminUserDirectoryService implements AdminUserDirectoryService {
    private static final Locale VI_LOCALE = Locale.of("vi", "VN");

    private final List<AdminUserListItemDto> allUsers;
    private final Map<UUID, MockUser> mockUsersById;

    public MockAdminUserDirectoryService() {
        Map<UUID, MockUser> map = new LinkedHashMap<>();
        List<MockUser> fixtures = UserManagementFixtures.users();
        for (MockUser user : fixtures) {
            map.put(idForMockUser(user.id()), user);
        }
        this.mockUsersById = Map.copyOf(map);
        this.allUsers = fixtures.stream().map(MockAdminUserDirectoryService::toListItem).toList();
    }

    @Override
    public CompletableFuture<OffsetPage<AdminUserListItemDto>> listUsers(AdminUserFilter filter) {
        Objects.requireNonNull(filter, "filter");
        String nameTerm = normalize(filter.name());
        String usernameTerm = normalize(filter.username());
        List<AdminUserListItemDto> matched = allUsers.stream()
                .filter(user -> filter.status() == null || user.status() == filter.status())
                .filter(user -> nameTerm.isEmpty() || normalize(user.fullName()).contains(nameTerm))
                .filter(user -> usernameTerm.isEmpty() || normalize(user.username()).contains(usernameTerm))
                .sorted(comparatorFor(filter))
                .toList();

        int fromIndex = Math.min(filter.page() * filter.size(), matched.size());
        int toIndex = Math.min(fromIndex + filter.size(), matched.size());
        return CompletableFuture.completedFuture(
                OffsetPage.of(matched.subList(fromIndex, toIndex), filter.page(), filter.size(), matched.size())
        );
    }

    @Override
    public boolean isLiveBackend() {
        return false;
    }

    public Optional<MockUser> findMockUser(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mockUsersById.get(id));
    }

    public static UUID idForMockUser(String mockId) {
        return UUID.nameUUIDFromBytes(mockId.getBytes(StandardCharsets.UTF_8));
    }

    private static AdminUserListItemDto toListItem(MockUser user) {
        return new AdminUserListItemDto(
                idForMockUser(user.id()),
                user.username(),
                user.fullName(),
                switch (user.role()) {
                    case ADMIN -> UserRole.ADMIN;
                    case USER -> UserRole.USER;
                },
                AccountStatus.valueOf(user.status().name()),
                user.createdAt()
        );
    }

    private static Comparator<AdminUserListItemDto> comparatorFor(AdminUserFilter filter) {
        Comparator<AdminUserListItemDto> primary = "createdAt".equalsIgnoreCase(filter.sortBy())
                ? Comparator.comparing(AdminUserListItemDto::createdAt)
                : Comparator.comparing(AdminUserListItemDto::fullName, Collator.getInstance(VI_LOCALE));
        if (filter.sortDir() == SortDirection.DESC) {
            primary = primary.reversed();
        }
        return primary.thenComparing(AdminUserListItemDto::id);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
    }
}
