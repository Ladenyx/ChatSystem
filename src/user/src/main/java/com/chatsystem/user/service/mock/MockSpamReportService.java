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
import java.util.stream.Collectors;

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
    public Optional<String> resolveTargetUserId(String conversationId, List<String> selectedMessageIds) {
        if (selectedMessageIds == null || selectedMessageIds.isEmpty()) {
            return Optional.empty();
        }
        Optional<MockConversation> conversation = findConversation(conversationId);
        if (conversation.isPresent()) {
            return catalog.messagesFor(conversation.get().id()).stream()
                    .filter(MockSpamReportService::isEligibleEvidenceMessage)
                    .filter(message -> !catalog.currentUserId().equals(message.senderId()))
                    .filter(message -> selectedMessageIds.contains(message.id()))
                    .map(MockMessage::senderId)
                    .findFirst();
        }
        if (selectedMessageIds.contains("msg-01") || selectedMessageIds.contains("msg-02")) {
            return Optional.of(DEFAULT_TARGET_ID);
        }
        return Optional.empty();
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

        Optional<MockConversation> conversation = findConversation(conversationId);
        List<ReportMessageItem> selected;
        List<ReportEvidenceItem> evidenceList;
        int contextCount;
        int intersectionCount = 0;

        if (conversation.isPresent()) {
            List<MockMessage> valid = catalog.messagesFor(conversation.get().id()).stream()
                    .filter(MockSpamReportService::isEligibleEvidenceMessage)
                    .toList();
            int contextFrom = Math.max(0, valid.size() - MAX_CONTEXT_MESSAGES);
            List<MockMessage> contextMessages = valid.subList(contextFrom, valid.size());
            Set<String> contextIds = contextMessages.stream()
                    .map(MockMessage::id)
                    .collect(Collectors.toSet());
            Set<String> validSelectedIds = valid.stream()
                    .filter(message -> message.senderId().equals(targetUserId)
                            && selectedMessageIds.contains(message.id()))
                    .map(MockMessage::id)
                    .collect(Collectors.toSet());
            if (validSelectedIds.isEmpty()) {
                throw new IllegalArgumentException(
                        "Cần ít nhất một tin hợp lệ của tài khoản đích và lý do có nội dung");
            }
            selected = new ArrayList<>();
            evidenceList = new ArrayList<>();
            contextCount = contextMessages.size();
            for (MockMessage message : valid) {
                boolean isSelected = validSelectedIds.contains(message.id());
                boolean isContext = contextIds.contains(message.id());
                if (!isSelected && !isContext) {
                    continue;
                }
                ReportMessageItem item = toMessageItem(message, targetUserId);
                if (isSelected) {
                    selected.add(item);
                }
                if (isSelected && isContext) {
                    intersectionCount++;
                }
                String classification = (isSelected && isContext) ? "Báo cáo + Ngữ cảnh"
                        : isSelected ? "Tin được báo cáo"
                        : "Ngữ cảnh";
                evidenceList.add(new ReportEvidenceItem(
                        item.id(), item.senderName(), item.formattedTime(), classification, item.content()));
            }
        } else {
            List<ReportMessageItem> eligible = getEligibleMessages(conversationId, targetUserId);
            selected = eligible.stream()
                    .filter(message -> selectedMessageIds.contains(message.id()))
                    .toList();
            List<ReportMessageItem> context = List.of(
                    new ReportMessageItem("msg-03", "u-minh", "Minh Anh", "10:14",
                            "Bạn kiểm tra lại nội dung giúp mình nhé.", false),
                    new ReportMessageItem("msg-01", targetUserId, targetUserName, "10:15",
                            "Nhận ưu đãi hôm nay! Nhắn riêng cho mình để mua gói tài liệu.", true));
            contextCount = context.size();
            Map<String, ReportEvidenceItem> evidenceMap = new LinkedHashMap<>();
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
            evidenceList = new ArrayList<>(evidenceMap.values());
        }

        int totalUnique = evidenceList.size();
        String consentNotice = selected.size()
                + " tin nhắn đã chọn và lý do báo cáo sẽ được gửi tới quản trị viên hệ thống để xem xét.";

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
                contextCount,
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
