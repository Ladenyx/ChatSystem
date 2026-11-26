package com.chatsystem.common.dto.messaging;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Idempotent message send payload shared between REST {@code POST /conversations/{id}/messages}
 * and WebSocket {@code message.send}. The client generates and reuses {@code messageId} on retries.
 */
public record SendMessageRequest(
        UUID messageId,
        UUID conversationId,
        String contentPlain,
        String contentCipher,
        Map<String, Object> cryptoMetadata
) {
    public SendMessageRequest {
        Objects.requireNonNull(messageId, "messageId");
        cryptoMetadata = cryptoMetadata == null ? null : Map.copyOf(cryptoMetadata);
        boolean hasPlain = contentPlain != null && !contentPlain.isEmpty()
                && contentCipher == null && cryptoMetadata == null;
        boolean hasCipher = contentPlain == null && contentCipher != null && !contentCipher.isEmpty()
                && cryptoMetadata != null;
        if (!hasPlain && !hasCipher) {
            throw new IllegalArgumentException(
                    "SendMessageRequest requires either contentPlain or (contentCipher + cryptoMetadata)");
        }
    }
}
