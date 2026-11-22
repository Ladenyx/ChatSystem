package com.chatsystem.user.service.model;

/**
 * Message evidence item included in the prepared spam report.
 */
public record ReportEvidenceItem(
        String id,
        String senderName,
        String formattedTime,
        String classification,
        String content
) {
}
