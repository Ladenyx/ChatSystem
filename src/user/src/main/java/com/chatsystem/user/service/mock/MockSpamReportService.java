package com.chatsystem.user.service.mock;

import com.chatsystem.user.service.SpamReportService;
import com.chatsystem.user.service.model.ReportEvidenceItem;
import com.chatsystem.user.service.model.ReportMessageItem;
import com.chatsystem.user.service.model.ReportPreparation;
import com.chatsystem.user.service.model.ReportReceipt;
import com.chatsystem.user.service.model.TargetUserOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mock implementation of SpamReportService providing realistic sample data
 * according to Figma specifications (P1 / REPORT-GROUP).
 */
public final class MockSpamReportService implements SpamReportService {
    private static final String DEFAULT_TARGET_ID = "u-nam";
    private static final String DEFAULT_TARGET_NAME = "Hoàng Nam";

    @Override
    public List<TargetUserOption> getTargetUserOptions(String conversationId) {
        return List.of(
                new TargetUserOption(DEFAULT_TARGET_ID, DEFAULT_TARGET_NAME),
                new TargetUserOption("u-minh", "Minh Anh"),
                new TargetUserOption("u-duc", "Đức Huy")
        );
    }

    @Override
    public List<ReportMessageItem> getEligibleMessages(String conversationId, String targetUserId) {
        String name = DEFAULT_TARGET_ID.equals(targetUserId) ? DEFAULT_TARGET_NAME : "Thành viên";
        return List.of(
                new ReportMessageItem("msg-01", targetUserId, name, "10:15",
                        "Nhận ưu đãi hôm nay! Nhắn riêng cho mình để mua gói tài liệu.", true),
                new ReportMessageItem("msg-02", targetUserId, name, "10:18",
                        "Bên mình còn nhiều tài liệu ôn tập khác nữa nhé mọi người.", true)
        );
    }

    @Override
    public ReportPreparation prepareReport(String conversationId, String conversationTitle,
                                           String targetUserId, String targetUserName,
                                           String reason, Set<String> selectedMessageIds) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Cần ít nhất một tin hợp lệ của tài khoản đích và lý do có nội dung.");
        }
        if (selectedMessageIds == null || selectedMessageIds.isEmpty()) {
            throw new IllegalArgumentException("Cần ít nhất một tin hợp lệ của tài khoản đích và lý do có nội dung.");
        }
        if (selectedMessageIds.size() > 20) {
            throw new IllegalArgumentException("Bạn đã chọn hơn 20 tin. Giảm số tin được báo cáo xuống tối đa 20 để tiếp tục.");
        }

        List<ReportMessageItem> eligible = getEligibleMessages(conversationId, targetUserId);
        List<ReportMessageItem> selected = eligible.stream()
                .filter(m -> selectedMessageIds.contains(m.id()))
                .toList();

        // Sample context messages (up to 30 latest valid messages in conversation)
        List<ReportMessageItem> context = List.of(
                new ReportMessageItem("msg-03", "u-minh", "Minh Anh", "10:14",
                        "Bạn kiểm tra lại nội dung giúp mình nhé.", false),
                new ReportMessageItem("msg-01", targetUserId, targetUserName, "10:15",
                        "Nhận ưu đãi hôm nay! Nhắn riêng cho mình để mua gói tài liệu.", true)
        );

        // Evidence = Selected ∪ Context deduplicated by ID
        Map<String, ReportEvidenceItem> evidenceMap = new LinkedHashMap<>();
        int intersectionCount = 0;

        // Context messages in chronological order
        for (ReportMessageItem ctx : context) {
            boolean isSelected = selectedMessageIds.contains(ctx.id());
            if (isSelected) {
                intersectionCount++;
                evidenceMap.put(ctx.id(), new ReportEvidenceItem(
                        ctx.id(), ctx.senderName(), ctx.formattedTime(), "Báo cáo + Ngữ cảnh", ctx.content()));
            } else {
                evidenceMap.put(ctx.id(), new ReportEvidenceItem(
                        ctx.id(), ctx.senderName(), ctx.formattedTime(), "Ngữ cảnh", ctx.content()));
            }
        }

        // Add any selected messages outside the context
        for (ReportMessageItem sel : selected) {
            if (!evidenceMap.containsKey(sel.id())) {
                evidenceMap.put(sel.id(), new ReportEvidenceItem(
                        sel.id(), sel.senderName(), sel.formattedTime(), "Tin được báo cáo", sel.content()));
            }
        }

        List<ReportEvidenceItem> evidenceList = new ArrayList<>(evidenceMap.values());
        int totalUnique = evidenceList.size();
        int selectedCount = selected.size();
        int contextCount = context.size();

        String consentNotice = "Toàn bộ " + totalUnique + " tin và lý do sẽ được gửi dạng bản rõ tới server để Admin hệ thống xem xét, kể cả trong nhóm E2EE.";

        return new ReportPreparation(
                conversationId,
                conversationTitle != null && !conversationTitle.isBlank() ? conversationTitle : "Nhóm Java",
                targetUserId,
                targetUserName != null && !targetUserName.isBlank() ? targetUserName : DEFAULT_TARGET_NAME,
                reason.strip(),
                selected,
                evidenceList,
                selectedCount,
                contextCount,
                intersectionCount,
                totalUnique,
                consentNotice
        );
    }

    @Override
    public ReportReceipt submitReport(ReportPreparation preparation) {
        return new ReportReceipt("RPT-20261118-001", "18/11/2026, 14:40", "PENDING");
    }
}
