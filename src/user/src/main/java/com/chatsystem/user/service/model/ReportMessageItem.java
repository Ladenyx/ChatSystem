package com.chatsystem.user.service.model;

/**
 * Message item selectable for spam reporting.
 */
public record ReportMessageItem(
        String id,
        String senderId,
        String senderName,
        String formattedTime,
        String content,
        boolean targetUserMessage
) {
    public ReportMessageItem(String id, String senderId, String senderName, String formattedTime, String content) {
        this(id, senderId, senderName, formattedTime, content, true);
    }
}
