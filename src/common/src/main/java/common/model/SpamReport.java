package common.model;

import java.time.LocalDateTime;

/**
 * Represents a spam report submitted by a user.
 */
public class SpamReport {

    public enum Status { PENDING, RESOLVED, DISMISSED }

    private int id;
    private User reporter;
    private User reported;
    private String reason;
    private Status status;
    private LocalDateTime reportedAt;

    public SpamReport() {}

    public SpamReport(int id, User reporter, User reported,
                      String reason, Status status, LocalDateTime reportedAt) {
        this.id = id;
        this.reporter = reporter;
        this.reported = reported;
        this.reason = reason;
        this.status = status;
        this.reportedAt = reportedAt;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public User getReported() { return reported; }
    public void setReported(User reported) { this.reported = reported; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }

    @Override
    public String toString() {
        return "SpamReport{id=" + id + ", reporter=" + reporter.getUsername() + ", reported=" + reported.getUsername() + "}";
    }
}
