package common.model;

import java.time.LocalDateTime;

/**
 * Represents a chat message between users or in a group.
 */
public class Message {

    public enum Type { DIRECT, GROUP }
    public enum Status { SENT, DELIVERED, READ, DELETED }

    private int id;
    private int senderId;
    private String senderName;   // denormalized for display
    private Integer receiverId;  // null if group message
    private Integer groupId;     // null if direct message
    private String content;
    private Type type;
    private Status status;
    private LocalDateTime sentAt;
    private boolean deletedBySender;

    public Message() {}

    public Message(int id, int senderId, String senderName, Integer receiverId,
                   Integer groupId, String content, Type type,
                   Status status, LocalDateTime sentAt) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.groupId = groupId;
        this.content = content;
        this.type = type;
        this.status = status;
        this.sentAt = sentAt;
        this.deletedBySender = false;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public boolean isDeletedBySender() { return deletedBySender; }
    public void setDeletedBySender(boolean deletedBySender) { this.deletedBySender = deletedBySender; }

    @Override
    public String toString() {
        return "Message{id=" + id + ", from=" + senderId + ", content='" + content + "'}";
    }
}
