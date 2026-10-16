package common.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a direct conversation between two users (for UI display).
 */
public class Conversation {

    private int id;
    private User otherUser;         // the other participant
    private List<Message> messages;
    private Message lastMessage;    // for conversation list preview
    private int unreadCount;
    private LocalDateTime updatedAt;

    public Conversation() {
        this.messages = new ArrayList<>();
    }

    public Conversation(int id, User otherUser, LocalDateTime updatedAt) {
        this.id = id;
        this.otherUser = otherUser;
        this.updatedAt = updatedAt;
        this.messages = new ArrayList<>();
        this.unreadCount = 0;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getOtherUser() { return otherUser; }
    public void setOtherUser(User otherUser) { this.otherUser = otherUser; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public Message getLastMessage() { return lastMessage; }
    public void setLastMessage(Message lastMessage) { this.lastMessage = lastMessage; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Conversation{id=" + id + ", with=" + otherUser.getUsername() + "}";
    }
}
