package com.chatsystem.user.mock;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockGroup;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.user.messaging.ConversationCatalog;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
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
    private final Map<String, LinkedHashMap<String, MockGroup.Role>> groupMemberRoles = new LinkedHashMap<>();
    private final Set<String> deletedForSelfIds = new LinkedHashSet<>();
    private final Set<String> recalledIds = new LinkedHashSet<>();
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();
    private boolean groupStateDirty;

    public MockConversationCatalog() {
        this(DEFAULT_REFERENCE_TIME);
    }

    public MockConversationCatalog(Instant referenceTime) {
        this.referenceTime = Objects.requireNonNull(referenceTime, "referenceTime");
        this.messagesById = data.messages().stream()
                .collect(Collectors.toUnmodifiableMap(MockMessage::id, Function.identity()));
        initGroupMemberRoles();
    }

    private void initGroupMemberRoles() {
        groupMemberRoles.clear();
        for (MockGroup group : data.groups()) {
            var roles = new LinkedHashMap<String, MockGroup.Role>();
            for (MockGroup.Member member : group.members()) {
                roles.put(member.userId(), member.role());
            }
            groupMemberRoles.put(group.id(), roles);
        }
        groupStateDirty = false;
    }

    @Override
    public List<MockConversation> conversations() {
        return data.conversations().stream()
                .map(chat -> {
                    var roles = groupMemberRoles.get(chat.id());
                    if (chat.kind() != MockConversation.Kind.GROUP || roles == null) {
                        return chat;
                    }
                    return new MockConversation(chat.id(), chat.title(), chat.kind(),
                            List.copyOf(roles.keySet()), chat.unreadCount());
                })
                .toList();
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
    public List<MockGroup.Member> groupMembers(String conversationId) {
        if (conversationId == null) {
            return List.of();
        }
        var roles = groupMemberRoles.get(conversationId);
        if (roles == null) {
            return List.of();
        }
        return roles.entrySet().stream()
                .map(entry -> new MockGroup.Member(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public void setGroupAdminRole(String conversationId, String targetUserId, boolean admin) {
        var roles = requireGroupRoles(conversationId);
        requireCurrentUserAdmin(roles, "Chỉ Admin nhóm mới có thể quản lý quyền quản trị.");
        if (targetUserId == null || currentUserId().equals(targetUserId)) {
            throw new IllegalArgumentException("Hãy dùng luồng thôi quyền Admin cho chính mình.");
        }
        if (!roles.containsKey(targetUserId)) {
            throw new IllegalArgumentException("Thành viên không còn thuộc nhóm.");
        }
        MockGroup.Role nextRole = admin ? MockGroup.Role.ADMIN : MockGroup.Role.MEMBER;
        if (roles.get(targetUserId) == nextRole) {
            return;
        }
        if (!admin && countAdmins(roles) <= 1) {
            throw new IllegalStateException("Nhóm phải còn ít nhất một Admin.");
        }
        roles.put(targetUserId, nextRole);
        groupStateDirty = true;
        notifyChanged();
    }

    @Override
    public void removeGroupMember(String conversationId, String targetUserId) {
        var roles = requireGroupRoles(conversationId);
        requireCurrentUserAdmin(roles, "Chỉ Admin nhóm mới có thể xóa thành viên.");
        if (targetUserId == null || currentUserId().equals(targetUserId)) {
            throw new IllegalArgumentException("Hãy dùng chức năng rời nhóm để tự rời khỏi nhóm.");
        }
        if (!roles.containsKey(targetUserId)) {
            return;
        }
        if (roles.get(targetUserId) == MockGroup.Role.ADMIN && countAdmins(roles) <= 1) {
            throw new IllegalStateException("Nhóm phải còn ít nhất một Admin.");
        }
        roles.remove(targetUserId);
        groupStateDirty = true;
        notifyChanged();
    }

    @Override
    public void transferGroupAdmin(String conversationId, String successorUserId) {
        var roles = requireGroupRoles(conversationId);
        requireCurrentUserAdmin(roles, "Chỉ Admin nhóm mới có thể bàn giao quyền quản trị.");
        if (successorUserId == null || currentUserId().equals(successorUserId)) {
            throw new IllegalArgumentException("Cần chọn một thành viên khác để bàn giao quyền Admin.");
        }
        if (!roles.containsKey(successorUserId)) {
            throw new IllegalArgumentException("Người kế nhiệm không còn thuộc nhóm.");
        }
        if (user(successorUserId).status() != MockUser.Status.ACTIVE) {
            throw new IllegalStateException("Tài khoản kế nhiệm không đủ điều kiện nhận quyền Admin.");
        }
        roles.put(successorUserId, MockGroup.Role.ADMIN);
        roles.put(currentUserId(), MockGroup.Role.MEMBER);
        groupStateDirty = true;
        notifyChanged();
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
        if (deletedForSelfIds.isEmpty() && recalledIds.isEmpty() && !groupStateDirty) {
            return;
        }
        deletedForSelfIds.clear();
        recalledIds.clear();
        if (groupStateDirty) {
            initGroupMemberRoles();
        }
        notifyChanged();
    }

    private LinkedHashMap<String, MockGroup.Role> requireGroupRoles(String conversationId) {
        var roles = conversationId == null ? null : groupMemberRoles.get(conversationId);
        if (roles == null) {
            throw new IllegalArgumentException("Không tìm thấy nhóm.");
        }
        return roles;
    }

    private void requireCurrentUserAdmin(LinkedHashMap<String, MockGroup.Role> roles, String message) {
        if (roles.get(currentUserId()) != MockGroup.Role.ADMIN) {
            throw new IllegalStateException(message);
        }
    }

    private static long countAdmins(Map<String, MockGroup.Role> roles) {
        return roles.values().stream().filter(role -> role == MockGroup.Role.ADMIN).count();
    }

    private void notifyChanged() {
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }
}
