package common.service;

import common.model.ChatGroup;
import common.model.Message;
import java.util.List;

/** Service interface for group chat operations. */
public interface GroupService {

    /** Returns groups the user belongs to. */
    List<ChatGroup> getUserGroups(int userId);

    /** Returns all groups (admin use). */
    List<ChatGroup> getAllGroups();

    /** Creates a new group. Returns the created group. */
    ChatGroup createGroup(String name, int creatorId, List<Integer> memberIds);

    /** Renames a group (admin of group only). */
    void renameGroup(int groupId, int requesterId, String newName);

    /** Adds a member to the group. */
    void addMember(int groupId, int requesterId, int newMemberId);

    /** Removes a member from the group (admin of group only). */
    void removeMember(int groupId, int requesterId, int memberId);

    /** Grants admin rights in the group. */
    void grantAdmin(int groupId, int requesterId, int targetId);

    /** Returns the message history of a group. */
    List<Message> getGroupMessages(int groupId);

    /** Sends a message to the group. */
    Message sendGroupMessage(int groupId, int senderId, String content);

    /** Toggles end-to-end encryption on the group (admin of group only). */
    void toggleEncryption(int groupId, int requesterId);
}
