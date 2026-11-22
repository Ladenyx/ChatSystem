package com.chatsystem.admin.mock;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockSpamReport;
import com.chatsystem.desktop.mock.model.MockUser;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** In-memory changes for the offline spam preview, not server moderation or account state. */
public final class SpamModerationPreview {
    public record Review(Instant reviewedAt, String note) {
    }

    private final MockData data = MockData.create();
    private final Map<String, Review> reviews = new HashMap<>();
    private final Map<String, MockUser.Status> accountStatuses = new HashMap<>();

    public SpamModerationPreview() {
        for (MockSpamReport report : data.spamReports()) {
            if (report.status() == MockSpamReport.Status.REVIEWED) {
                reviews.put(report.id(), new Review(report.createdAt().plusSeconds(3600),
                        "Đã xác minh nội dung vi phạm và lưu hồ sơ xử lý"));
            }
        }
    }

    public List<MockSpamReport> reports() {
        return data.spamReports();
    }

    public MockUser user(String id) {
        return data.user(id);
    }

    public Review review(MockSpamReport report) {
        return reviews.get(report.id());
    }

    public MockSpamReport.Status status(MockSpamReport report) {
        return review(report) == null ? MockSpamReport.Status.PENDING : MockSpamReport.Status.REVIEWED;
    }

    public void markReviewed(MockSpamReport report, String note) {
        if (!note.isBlank()) {
            reviews.putIfAbsent(report.id(), new Review(Instant.now(), note));
        }
    }

    public MockUser.Status accountStatus(MockUser user) {
        return accountStatuses.getOrDefault(user.id(), user.status());
    }

    public boolean canLockReportedAccount(MockSpamReport report) {
        MockUser target = user(report.reportedUserId());
        return target.role() == MockUser.Role.USER && accountStatus(target) == MockUser.Status.ACTIVE;
    }

    public boolean lockReportedAccount(MockSpamReport report) {
        if (!canLockReportedAccount(report)) {
            return false;
        }
        accountStatuses.put(report.reportedUserId(), MockUser.Status.LOCKED);
        return true;
    }
}
