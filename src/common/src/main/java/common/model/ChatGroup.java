package common.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group chat.
 */
public class ChatGroup {

    private int id;
    private String name;
    private int creatorId;
    private LocalDateTime createdAt;
    private boolean encrypted;
    private List<User> members;
    private List<User> admins;
    private List<Message> messages;

    public ChatGroup() {
        this.members = new ArrayList<>();
        this.admins = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public ChatGroup(int id, String name, int creatorId, LocalDateTime createdAt, boolean encrypted) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.encrypted = encrypted;
        this.members = new ArrayList<>();
        this.admins = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    /** Returns two-letter initials from group name for avatar tile. */
    public String getInitials() {
        if (name == null || name.isBlank()) return "G";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return name.substring(0, Math.min(2, name.length())).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0)).toUpperCase();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCreatorId() { return creatorId; }
    public void setCreatorId(int creatorId) { this.creatorId = creatorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isEncrypted() { return encrypted; }
    public void setEncrypted(boolean encrypted) { this.encrypted = encrypted; }

    public List<User> getMembers() { return members; }
    public void setMembers(List<User> members) { this.members = members; }

    public List<User> getAdmins() { return admins; }
    public void setAdmins(List<User> admins) { this.admins = admins; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    @Override
    public String toString() {
        return "ChatGroup{id=" + id + ", name='" + name + "', members=" + members.size() + "}";
    }
}
