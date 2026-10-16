package common.service;

import common.model.Conversation;
import common.model.Message;
import java.util.List;

/** Service interface for messaging operations. */
public interface ChatService {

    /** Returns all conversations of a user, ordered by last message time. */
    List<Conversation> getConversations(int userId);

    /** Returns the full message history of a direct conversation. */
    List<Message> getDirectMessages(int userId, int friendId);

    /** Sends a direct message. Returns the saved message. */
    Message sendDirectMessage(int senderId, int receiverId, String content);

    /** Deletes the entire chat history between two users (from the deleter's view). */
    void deleteConversation(int userId, int friendId);

    /** Deletes specific messages (from the deleter's view). */
    void deleteMessages(int userId, List<Integer> messageIds);

    /** Searches messages in a specific conversation. */
    List<Message> searchInConversation(int userId, int friendId, String query);

    /** Searches messages across all conversations of a user. */
    List<Message> searchAllConversations(int userId, String query);

    /** Reports a message or user for spam. */
    void reportSpam(int reporterId, int reportedUserId, String reason);
}
