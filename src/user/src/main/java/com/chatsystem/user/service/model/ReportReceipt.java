package com.chatsystem.user.service.model;

/**
 * Receipt returned after report submission.
 */
public record ReportReceipt(
        String reportId,
        String timestamp,
        String status
) {
}
