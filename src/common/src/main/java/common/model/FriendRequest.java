package common.model;

import java.time.LocalDateTime;

/**
 * Represents a friend request between two users.
 */
public class FriendRequest {

    public enum Status { PENDING, ACCEPTED, DECLINED }

    private int id;
    private User sender;
    private User receiver;
    private Status status;
    private LocalDateTime sentAt;

    public FriendRequest() {}

    public FriendRequest(int id, User sender, User receiver, Status status, LocalDateTime sentAt) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.status = status;
        this.sentAt = sentAt;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    @Override
    public String toString() {
        return "FriendRequest{id=" + id + ", from=" + sender.getUsername() + ", to=" + receiver.getUsername() + ", status=" + status + "}";
    }
}
