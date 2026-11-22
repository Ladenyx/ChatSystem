package com.chatsystem.user.service;

import com.chatsystem.user.service.model.ReportMessageItem;
import com.chatsystem.user.service.model.ReportPreparation;
import com.chatsystem.user.service.model.ReportReceipt;
import com.chatsystem.user.service.model.TargetUserOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Business service interface for preparing and submitting user spam reports.
 */
public interface SpamReportService {
    /**
     * Retrieves participants in the conversation who can be reported.
     */
    List<TargetUserOption> getTargetUserOptions(String conversationId);

    /**
     * Resolves the sender user ID for a set of preselected message IDs, when known.
     */
    default Optional<String> resolveTargetUserId(String conversationId, List<String> selectedMessageIds) {
        return Optional.empty();
    }

    /**
     * Retrieves eligible messages sent by the target user in the conversation.
     */
    List<ReportMessageItem> getEligibleMessages(String conversationId, String targetUserId);

    /**
     * Prepares the evidence package (Selected ∪ Context up to 30) with deduplication and metadata.
     */
    ReportPreparation prepareReport(String conversationId, String conversationTitle,
                                   String targetUserId, String targetUserName,
                                   String reason, Set<String> selectedMessageIds);

    /**
     * Submits the confirmed report and returns the receipt.
     */
    ReportReceipt submitReport(ReportPreparation preparation);
}
