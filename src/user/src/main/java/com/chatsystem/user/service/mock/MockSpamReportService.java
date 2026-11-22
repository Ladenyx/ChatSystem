package com.chatsystem.user.service.mock;

import com.chatsystem.desktop.mock.MockData;
import com.chatsystem.desktop.mock.model.MockConversation;
import com.chatsystem.desktop.mock.model.MockMessage;
import com.chatsystem.desktop.mock.model.MockUser;
import com.chatsystem.user.UserViews;
import com.chatsystem.user.messaging.ConversationCatalog;
import com.chatsystem.user.service.SpamReportService;
import com.chatsystem.user.service.model.ReportEvidenceItem;
import com.chatsystem.user.service.model.ReportMessageItem;
import com.chatsystem.user.service.model.ReportPreparation;
import com.chatsystem.user.service.model.ReportReceipt;
import com.chatsystem.user.service.model.TargetUserOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Mock implementation of SpamReportService backed by ConversationCatalog when available,
 * with fallback fixtures for standalone service unit tests.
 */
public final class MockSpamReportService implements SpamReportService {
    private static final String DEFAULT_TARGET_ID = "u-nam";
    private static final String DEFAULT_TARGET_NAME = "Hoàng Nam";
    private static final int MAX_CONTEXT_MESSAGES = 30;
    private static final int MAX_SELECTED_MESSAGES = 20;
    private static final DateTimeFormatter MESSAGE_TIME = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(MockData.DISPLAY_ZONE);

    private final ConversationCatalog catalog;

    public MockSpamReportService() {
        this(UserViews.CONVERSATIONS_DATA);
    }

    public MockSpamReportService(ConversationCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<TargetUserOption> getTargetUserOptions(String conversationId) {
        Optional<MockConversation> conversation = findConversation(conversationId);
        if (conversation.isPresent()) {
            return conversation.get().participantIds().stream()
                    .filter(id -> !catalog.currentUserId().equals(id))
                    .map(catalog::user)
                    .map(user -> new TargetUserOption(user.id(), user.fullName()))
                    .toList();
        }
        return List.of(
                new TargetUserOption(DEFAULT_TARGET_ID, DEFAULT_TARGET_NAME),
                new TargetUserOption("u-minh", "Minh Anh"),
                new TargetUserOption("u-duc", "Đức Huy"));
    }

    @Override
    public List<ReportMessageItem> getEligibleMessages(String conversationId, String targetUserId) {
        Optional<MockConversation> conversation = findConversation(conversationId);
        if (conversation.isPresent()) {
            return catalog.messagesFor(conversation.get().id()).stream()
                    .filter(MockSpamReportService::isEligibleEvidenceMessage)
                    .filter(message -> message.senderId().equals(targetUserId))
                    .map(message -> toMessageItem(message, targetUserId))
                    .toList();
        }
        String name = DEFAULT_TARGET_ID.equals(targetUserId) ? DEFAULT_TARGET_NAME : "Thành viên";
        return List.of(
                new ReportMessageItem("msg-01", targetUserId, name, "10:15",
                        "Nhận ưu đãi hôm nay! Nhắn riêng cho mình để mua gói tài liệu.", true),
                new ReportMessageItem("msg-02", targetUserId, name, "10:18",
                        "Bên mình còn nhiều tài liệu ôn tập khác nữa nhé mọi người.", true));
    }

    @Override
    public ReportPreparation prepareReport(String conversationId, String conversationTitle,
                                           String targetUserId, String targetUserName,
                                           String reason, Set<String> selectedMessageIds) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Cần ít nhất một tin hợp lệ của tài khoản đích và lý do có nội dung");
        }
        if (selectedMessageIds == null || selectedMessageIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cần ít nhất một tin hợp lệ của tài khoản đích và lý do có nội dung");
        }
        if (selectedMessageIds.size() > MAX_SELECTED_MESSAGES) {
            throw new IllegalArgumentException(
                    "Bạn đã chọn hơn 20 tin — hãy giảm xuống tối đa 20 để tiếp tục");
        }

        List<ReportMessageItem> eligible = getEligibleMessages(conversationId, targetUserId);
        List<ReportMessageItem> selected = eligible.stream()
                .filter(message -> selectedMessageIds.contains(message.id()))
                .toList();

        Optional<MockConversation> conversation = findConversation(conversationId);
        List<ReportMessageItem> context = conversation.isPresent()
                ? latestContextMessages(conversation.get().id(), targetUserId)
                : List.of(
                        new ReportMessageItem("msg-03", "u-minh", "Minh Anh", "10:14",
                                "Bạn kiểm tra lại nội dung giúp mình nhé.", false),
                        new ReportMessageItem("msg-01", targetUserId, targetUserName, "10:15",
                                "Nhận ưu đãi hôm nay! Nhắn riêng cho mình để mua gói tài liệu.", true));

        Map<String, ReportEvidenceItem> evidenceMap = new LinkedHashMap<>();
        int intersectionCount = 0;

        for (ReportMessageItem sel : selected) {
            boolean inContext = context.stream().anyMatch(ctx -> ctx.id().equals(sel.id()));
            if (!inContext) {
                evidenceMap.put(sel.id(), new ReportEvidenceItem(
                        sel.id(), sel.senderName(), sel.formattedTime(), "Tin được báo cáo", sel.content()));
            }
        }

        for (ReportMessageItem ctx : context) {
            if (selectedMessageIds.contains(ctx.id())) {
                intersectionCount++;
                evidenceMap.put(ctx.id(), new ReportEvidenceItem(
                        ctx.id(), ctx.senderName(), ctx.formattedTime(), "Báo cáo + Ngữ cảnh", ctx.content()));
            } else {
                evidenceMap.put(ctx.id(), new ReportEvidenceItem(
                        ctx.id(), ctx.senderName(), ctx.formattedTime(), "Ngữ cảnh", ctx.content()));
            }
        }

        List<ReportEvidenceItem> evidenceList = new ArrayList<>(evidenceMap.values());
        int totalUnique = evidenceList.size();
        String consentNotice = "Toàn bộ " + totalUnique
                + " tin và lý do sẽ được gửi dạng bản rõ tới server để Admin hệ thống xem xét, kể cả trong nhóm E2EE.";

        String resolvedTitle = conversationTitle != null && !conversationTitle.isBlank()
                ? conversationTitle
                : conversation.map(MockConversation::title).orElse("Nhóm Java");

        return new ReportPreparation(
                conversationId,
                resolvedTitle,
                targetUserId,
                targetUserName != null && !targetUserName.isBlank() ? targetUserName : DEFAULT_TARGET_NAME,
                reason.strip(),
                selected,
                evidenceList,
                selected.size(),
                context.size(),
                intersectionCount,
                totalUnique,
                consentNotice);
    }

    @Override
    public ReportReceipt submitReport(ReportPreparation preparation) {
        return new ReportReceipt("RPT-20261118-001", "18/11/2026, 14:40", "PENDING");
    }

    private Optional<MockConversation> findConversation(String conversationId) {
        if (catalog == null || conversationId == null || conversationId.isBlank()) {
            return Optional.empty();
        }
        return catalog.conversations().stream()
                .filter(chat -> chat.id().equals(conversationId))
                .findFirst();
    }

    private List<ReportMessageItem> latestContextMessages(String conversationId, String targetUserId) {
        List<MockMessage> valid = catalog.messagesFor(conversationId).stream()
                .filter(MockSpamReportService::isEligibleEvidenceMessage)
                .toList();
        int fromIndex = Math.max(0, valid.size() - MAX_CONTEXT_MESSAGES);
        return valid.subList(fromIndex, valid.size()).stream()
                .map(message -> toMessageItem(message, targetUserId))
                .toList();
    }

    private ReportMessageItem toMessageItem(MockMessage message, String targetUserId) {
        MockUser sender = catalog.user(message.senderId());
        String senderName = catalog.currentUserId().equals(sender.id())
                ? sender.fullName() + " (Bạn)"
                : sender.fullName();
        return new ReportMessageItem(
                message.id(),
                message.senderId(),
                senderName,
                MESSAGE_TIME.format(message.sentAt()),
                message.text(),
                message.senderId().equals(targetUserId));
    }

    private static boolean isEligibleEvidenceMessage(MockMessage message) {
        return message.acceptedByServer() && message.state() != MockMessage.State.RECALLED;
    }
}
