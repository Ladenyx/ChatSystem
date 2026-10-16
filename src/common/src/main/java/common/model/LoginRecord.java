package common.model;

import java.time.LocalDateTime;

/**
 * Represents a login history entry.
 */
public class LoginRecord {

    private int id;
    private int userId;
    private String username;
    private String fullName;
    private LocalDateTime loginAt;

    public LoginRecord() {}

    public LoginRecord(int id, int userId, String username, String fullName, LocalDateTime loginAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.loginAt = loginAt;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDateTime getLoginAt() { return loginAt; }
    public void setLoginAt(LocalDateTime loginAt) { this.loginAt = loginAt; }

    @Override
    public String toString() {
        return "LoginRecord{userId=" + userId + ", username='" + username + "', loginAt=" + loginAt + "}";
    }
}
