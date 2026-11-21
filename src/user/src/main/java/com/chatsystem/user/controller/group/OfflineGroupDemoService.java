package com.chatsystem.user.controller.group;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockGroup;
import com.chatsystem.desktop.mock.model.MockGroup.Member;
import com.chatsystem.desktop.mock.model.MockGroup.Role;
import com.chatsystem.desktop.mock.model.MockUser;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/** Group policy and immutable snapshots for one offline workspace. */
public final class OfflineGroupDemoService {
    public enum Membership { ACTIVE, LEFT, REMOVED }
    public enum ExitAction { LEAVE, STEP_DOWN }

    private static final String SELF = MockData.PREVIEW_USER_ID;
    private static final Set<String> FRIEND_IDS = Set.of("usr-02", "usr-03", "usr-04", "usr-05", "usr-06",
            "usr-07", "usr-08", "usr-09", "usr-10", "usr-11", "usr-12");

    private final MockData data;
    private final Predicate<String> hasBlock;
    private final Map<String, MockGroup> groups = new LinkedHashMap<>();
    private final Map<String, Membership> formerMemberships = new LinkedHashMap<>();
    private int groupSequence;

    public OfflineGroupDemoService(MockData data, Predicate<String> hasBlock) {
        this.data = data;
        this.hasBlock = hasBlock;
        data.groups().forEach(group -> groups.put(group.id(), group));
        // Demo fixture: another group Admin has already removed the preview user.
        MockGroup removed = group("grp-04");
        replace(removed, removed.name(), removed.members().stream().filter(member -> !isSelf(member)).toList());
        formerMemberships.put(removed.id(), Membership.REMOVED);
    }

    public MockGroup group(String groupId) {
        return groups.get(groupId);
    }

    public List<MockGroup> all() {
        return List.copyOf(groups.values());
    }

    public Membership membership(String groupId) {
        return formerMemberships.getOrDefault(groupId, Membership.ACTIVE);
    }

    public boolean isAdmin(String groupId) {
        var group = group(groupId);
        return group != null && group.members().stream().anyMatch(member -> isSelf(member) && member.role() == Role.ADMIN);
    }

    /** Eligible invitees may be offline. Blocking is checked only against the inviting user. */
    public List<MockUser> invitees(String groupId) {
        Set<String> existing = groupId == null ? Set.of() : Set.copyOf(group(groupId).members().stream().map(Member::userId).toList());
        return data.users().stream().filter(user -> FRIEND_IDS.contains(user.id()) && isActive(user))
                .filter(user -> !hasBlock.test(user.id()) && !existing.contains(user.id())).toList();
    }

    public MockGroup create(String name, Collection<String> userIds, Instant createdAt) {
        String validName = requireName(name);
        var members = new ArrayList<>(inviteMembers(null, userIds));
        members.addFirst(new Member(SELF, Role.ADMIN));
        var group = new MockGroup("grp-created-" + ++groupSequence, validName, createdAt, members);
        groups.put(group.id(), group);
        return group;
    }

    public void rename(String groupId, String name) {
        MockGroup group = requireAdmin(groupId);
        replace(group, requireName(name), group.members());
    }

    public void addMembers(String groupId, Collection<String> userIds) {
        MockGroup group = requireMember(groupId);
        var members = new ArrayList<>(group.members());
        members.addAll(inviteMembers(groupId, userIds));
        replace(group, group.name(), members);
    }

    public void changeRole(String groupId, String userId, Role role) {
        MockGroup group = requireAdmin(groupId);
        requireOtherMember(group, userId);
        var changed = group.members().stream()
                .map(member -> member.userId().equals(userId) ? new Member(userId, role) : member).toList();
        replace(group, group.name(), changed);
    }

    public void removeMember(String groupId, String userId) {
        MockGroup group = requireAdmin(groupId);
        requireOtherMember(group, userId);
        replace(group, group.name(), group.members().stream().filter(member -> !member.userId().equals(userId)).toList());
    }

    public boolean needsSuccessor(String groupId) {
        MockGroup group = group(groupId);
        return isAdmin(groupId) && group.members().size() > 1
                && group.members().stream().filter(member -> member.role() == Role.ADMIN).count() == 1;
    }

    public List<MockUser> successors(String groupId) {
        if (!needsSuccessor(groupId)) {
            return List.of();
        }
        return group(groupId).members().stream().filter(member -> !isSelf(member))
                .map(member -> data.user(member.userId())).filter(OfflineGroupDemoService::isActive).toList();
    }

    public void exit(String groupId, ExitAction action, String successorId) {
        MockGroup group = action == ExitAction.STEP_DOWN ? requireAdmin(groupId) : requireMember(groupId);
        boolean handover = needsSuccessor(groupId);
        if (handover && successors(groupId).stream().noneMatch(user -> user.id().equals(successorId))) {
            throw new IllegalArgumentException("Hãy chọn một Admin kế nhiệm đủ điều kiện.");
        }
        if (!handover && successorId != null) {
            throw new IllegalStateException("Vai trò trong nhóm đã thay đổi. Hãy mở lại thao tác.");
        }
        if (action == ExitAction.STEP_DOWN && group.members().size() == 1) {
            throw new IllegalStateException("Bạn là thành viên duy nhất. Hãy rời nhóm nếu muốn đóng nhóm.");
        }

        var members = new ArrayList<Member>();
        for (Member member : group.members()) {
            if (isSelf(member)) {
                if (action == ExitAction.STEP_DOWN) {
                    members.add(new Member(SELF, Role.MEMBER));
                }
            } else if (handover && member.userId().equals(successorId)) {
                members.add(new Member(successorId, Role.ADMIN));
            } else {
                members.add(member);
            }
        }
        replace(group, group.name(), members);
        if (action == ExitAction.LEAVE) {
            formerMemberships.put(groupId, Membership.LEFT);
        }
    }

    private List<Member> inviteMembers(String groupId, Collection<String> userIds) {
        var selected = new LinkedHashSet<>(userIds);
        if (selected.isEmpty()) {
            throw new IllegalArgumentException("Chọn ít nhất một người bạn đủ điều kiện.");
        }
        var allowed = invitees(groupId).stream().map(MockUser::id).toList();
        if (!allowed.containsAll(selected)) {
            throw new IllegalArgumentException("Một số người không còn đủ điều kiện. Hãy chọn lại.");
        }
        return selected.stream().map(userId -> new Member(userId, Role.MEMBER)).toList();
    }

    private static String requireName(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Nhập tên nhóm.");
        }
        return name.strip();
    }

    private MockGroup requireMember(String groupId) {
        var group = group(groupId);
        if (group == null || group.members().stream().noneMatch(OfflineGroupDemoService::isSelf)) {
            throw new IllegalStateException("Bạn không còn là thành viên của nhóm.");
        }
        return group;
    }

    private MockGroup requireAdmin(String groupId) {
        var group = requireMember(groupId);
        if (!isAdmin(groupId)) {
            throw new IllegalStateException("Chỉ Admin nhóm được thực hiện thao tác này.");
        }
        return group;
    }

    private static void requireOtherMember(MockGroup group, String userId) {
        if (SELF.equals(userId) || group.members().stream().noneMatch(member -> member.userId().equals(userId))) {
            throw new IllegalArgumentException("Thành viên này không thể được quản lý bằng thao tác đã chọn.");
        }
    }

    private void replace(MockGroup group, String name, List<Member> members) {
        groups.put(group.id(), new MockGroup(group.id(), name, group.createdAt(), members));
    }

    private static boolean isSelf(Member member) {
        return SELF.equals(member.userId());
    }

    private static boolean isActive(MockUser user) {
        return user.status() == MockUser.Status.ACTIVE;
    }
}
