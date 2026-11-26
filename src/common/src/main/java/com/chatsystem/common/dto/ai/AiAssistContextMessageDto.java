package com.chatsystem.common.dto.ai;

import java.time.Instant;
import java.util.Objects;

/** Plaintext local context message forwarded to the Server for LLM assistance (U-27, DEC-08). */
public record AiAssistContextMessageDto(String senderLabel, Instant sentAt, String text) {
    public AiAssistContextMessageDto {
        Objects.requireNonNull(senderLabel, "senderLabel");
        Objects.requireNonNull(sentAt, "sentAt");
        Objects.requireNonNull(text, "text");
        if (senderLabel.isBlank()) {
            throw new IllegalArgumentException("senderLabel must not be blank");
        }
    }
}
