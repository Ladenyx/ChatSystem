package common.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a user account in the chat system.
 */
public class User {

    public enum Gender { MALE, FEMALE, OTHER }
    public enum Status { ACTIVE, LOCKED }

    private int id;
    private String username;
    private String fullName;
    private String email;
    private String address;
    private LocalDate dateOfBirth;
    private Gender gender;
    private Status status;
    private LocalDateTime createdAt;
    private boolean online;

    public User() {}

    public User(int id, String username, String fullName, String email,
                String address, LocalDate dateOfBirth, Gender gender,
                Status status, LocalDateTime createdAt, boolean online) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.status = status;
        this.createdAt = createdAt;
        this.online = online;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    /** Returns initials for avatar display (e.g. "Nguyen The Quan" -> "NQ") */
    public String getInitials() {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', fullName='" + fullName + "'}";
    }
}
