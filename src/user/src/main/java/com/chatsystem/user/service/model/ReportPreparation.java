package com.chatsystem.user.service.model;

import java.util.List;

/**
 * Snapshot of the prepared evidence package ready for review and confirmation.
 */
public record ReportPreparation(
        String conversationId,
        String conversationTitle,
        String targetUserId,
        String targetUserName,
        String reason,
        List<ReportMessageItem> selectedMessages,
        List<ReportEvidenceItem> evidence,
        int selectedCount,
        int contextCount,
        int intersectionCount,
        int totalUniqueCount,
        String consentNotice
) {
}
