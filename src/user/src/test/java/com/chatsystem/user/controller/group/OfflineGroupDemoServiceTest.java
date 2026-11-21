package com.chatsystem.user.controller.group;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockGroup.Member;
import com.chatsystem.desktop.mock.model.MockGroup.Role;
import com.chatsystem.desktop.mock.model.MockMessage.State;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.user.controller.chat.OfflineChatDemoService;
import com.chatsystem.user.controller.chat.OfflineChatDemoService.Access;
import com.chatsystem.user.controller.group.OfflineGroupDemoService.ExitAction;
import java.util.List;
import org.junit.jupiter.api.Test;

class OfflineGroupDemoServiceTest {
    private final MockData data = MockData.create();
    private final OfflineChatDemoService chat = new OfflineChatDemoService(data);
    private final OfflineGroupDemoService groups = chat.groups();

    @Test
    void requiresNameAndEligibleFriendsAndCreatesAnEmptyEncryptedConversation() {
        var ids = groups.invitees(null).stream().map(MockUser::id).toList();
        assertTrue(ids.contains("usr-02")); // Offline friend.
        assertFalse(ids.contains("usr-05")); // Preview user blocked this friend.
        assertFalse(ids.contains("usr-06")); // This friend blocked the preview user.
        assertFalse(ids.contains("usr-13")); // Nonfriend.
        assertThrows(IllegalArgumentException.class, () -> chat.createGroup("  ", List.of("usr-02")));
        assertThrows(IllegalArgumentException.class, () -> chat.createGroup("Java", List.of()));
        assertThrows(IllegalArgumentException.class, () -> chat.createGroup("Java", List.of("usr-02", "usr-06")));
        chat.setBlocked("direct-01", true);
        assertThrows(IllegalArgumentException.class, () -> chat.createGroup("Java", List.of("usr-02")));
        chat.setBlocked("direct-01", false);
        assertEquals(data.groups().size(), groups.all().size());

        var result = chat.createGroup("  Ôn tập Java  ", List.of("usr-02", "usr-02"));
        assertTrue(result.encrypted());
        assertEquals("Ôn tập Java", result.title());
        assertEquals(List.of(MockData.PREVIEW_USER_ID, "usr-02"), result.participantIds());
        assertTrue(groups.isAdmin(result.id()));
        assertEquals(result.id(), chat.conversations().getFirst().id());
        assertTrue(chat.page(result.id(), 50).messages().isEmpty());
    }

    @Test
    void roleChangesRenameAndAddKeepExistingHistoryAndRespectMemberPermissions() {
        String id = chat.createGroup("Ban đầu", List.of("usr-02")).id();
        var message = chat.send(id, "Tin lịch sử");
        assertTrue(chat.acknowledge(id, message.id(), false));
        groups.rename(id, "Nhóm Java");
        assertEquals("Nhóm Java", chat.conversation(id).title());
        assertEquals(message.id(), chat.latest(id).id());
        groups.addMembers(id, List.of("usr-03"));
        assertThrows(IllegalArgumentException.class, () -> groups.addMembers(id, List.of("usr-03", "usr-04")));
        assertFalse(chat.conversation(id).participantIds().contains("usr-04"));
        groups.changeRole(id, "usr-02", Role.ADMIN);
        assertFalse(groups.needsSuccessor(id));
        groups.changeRole(id, "usr-02", Role.MEMBER);
        assertTrue(groups.needsSuccessor(id));
        groups.removeMember(id, "usr-03");
        assertFalse(chat.conversation(id).participantIds().contains("usr-03"));

        var before = groups.group(id);
        assertThrows(IllegalArgumentException.class, () -> chat.exitGroup(id, ExitAction.STEP_DOWN, "usr-03"));
        assertEquals(before, groups.group(id));
        chat.setBlocked("direct-01", true);
        assertTrue(groups.successors(id).stream().anyMatch(user -> user.id().equals("usr-02")));
        chat.exitGroup(id, ExitAction.STEP_DOWN, "usr-02");
        assertFalse(groups.isAdmin(id));
        assertEquals(Role.ADMIN, role(id, "usr-02"));
        assertEquals(Role.MEMBER, role(id, MockData.PREVIEW_USER_ID));
        assertThrows(IllegalStateException.class, () -> groups.rename(id, "Tên không hợp lệ theo quyền"));
        assertThrows(IllegalStateException.class, () -> groups.removeMember(id, "usr-02"));
        assertThrows(IllegalStateException.class, () -> groups.changeRole(id, "usr-02", Role.MEMBER));
        groups.addMembers(id, List.of("usr-04"));
        assertEquals(Role.MEMBER, role(id, "usr-04"));
    }

    @Test
    void leavingAsSoleAdminIsAtomicAndPreservesOnlyReadOnlyChatAccess() {
        String id = chat.createGroup("Bàn giao", List.of("usr-02")).id();
        var history = chat.send(id, "Tin còn trong lịch sử");
        assertTrue(chat.acknowledge(id, history.id(), false));
        var pending = chat.send(id, "Tin đang chờ");
        var original = groups.group(id);
        assertThrows(IllegalArgumentException.class, () -> chat.exitGroup(id, ExitAction.LEAVE, null));
        assertEquals(original, groups.group(id));
        assertEquals(Access.AVAILABLE, chat.access(id));

        chat.exitGroup(id, ExitAction.LEAVE, "usr-02");
        assertEquals(Access.GROUP_LEFT, chat.access(id));
        assertEquals(List.of(new Member("usr-02", Role.ADMIN)), groups.group(id).members());
        assertNull(chat.send(id, "Tin mới"));
        assertFalse(chat.retry(id, pending.id()));
        assertFalse(chat.acknowledge(id, pending.id(), false));
        assertFalse(chat.advanceReceipt(id, history.id(), State.DELIVERED));
        assertFalse(chat.recall(id, history.id()));
        assertEquals(history.id(), chat.search(id, "còn trong lịch sử").messages().getFirst().id());
    }

    @Test
    void finalMemberCanCloseGroupAndRemovedFixtureDoesNotExposeUnreadHistory() {
        String id = chat.createGroup("Nhóm sắp đóng", List.of("usr-02")).id();
        groups.removeMember(id, "usr-02");
        assertFalse(groups.needsSuccessor(id));
        assertThrows(IllegalStateException.class, () -> chat.exitGroup(id, ExitAction.STEP_DOWN, null));
        chat.exitGroup(id, ExitAction.LEAVE, null);
        assertTrue(groups.group(id).members().isEmpty());
        assertEquals(Access.GROUP_LEFT, chat.access(id));
        assertThrows(IllegalStateException.class, () -> groups.addMembers(id, List.of("usr-03")));

        assertEquals(Access.GROUP_REMOVED, chat.access("grp-04"));
        assertEquals(data.messagesFor("grp-04").subList(0, 2), chat.page("grp-04", 50).messages());
        assertEquals(0, chat.historyLimitFor("grp-04", data.messagesFor("grp-04").getLast().id()));
        assertNull(chat.send("grp-04", "Không có quyền gửi"));
    }

    @Test
    void leavingWithoutKeysDoesNotRevealHistoryAndSuccessionDoesNotRequireFriendship() {
        String id = "grp-03";
        assertEquals(Access.MISSING_KEY, chat.access(id));
        assertTrue(chat.page(id, 50).messages().stream().allMatch(message -> message.text().isEmpty()));
        groups.changeRole(id, "usr-04", Role.MEMBER);
        assertFalse(groups.invitees(null).stream().anyMatch(user -> user.id().equals("usr-13")));
        assertTrue(groups.successors(id).stream().anyMatch(user -> user.id().equals("usr-13")));
        chat.exitGroup(id, ExitAction.LEAVE, "usr-13");
        assertEquals(Role.ADMIN, role(id, "usr-13"));
        assertEquals(Access.GROUP_LEFT, chat.access(id));
        assertTrue(chat.page(id, 50).messages().isEmpty());
        assertTrue(chat.search(id, "tài liệu").messages().isEmpty());
    }

    private Role role(String id, String user) {
        return groups.group(id).members().stream().filter(member -> member.userId().equals(user))
                .findFirst().orElseThrow().role();
    }
}
