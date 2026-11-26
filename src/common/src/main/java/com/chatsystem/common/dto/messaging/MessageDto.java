package com.chatsystem.common.dto.messaging;

import com.chatsystem.common.domain.MessageDeliveryState;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Wire DTO representing a direct plaintext message, an E2EE group ciphertext message, or a recalled placeholder.
 * Enforces the same mutual-exclusion invariant as PostgreSQL {@code messages_payload_check}.
 */
public record MessageDto(
        UUID id,
        UUID conversationId,
        UUID senderUserId,
        Instant createdAt,
        String contentPlain,
        String contentCipher,
        Map<String, Object> cryptoMetadata,
        Instant recalledAt,
        MessageDeliveryState state
) {
    public MessageDto {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(conversationId, "conversationId");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(state, "state");
        cryptoMetadata = cryptoMetadata == null ? null : Map.copyOf(cryptoMetadata);

        boolean recalled = state == MessageDeliveryState.RECALLED || recalledAt != null;
        if (recalled) {
            if (state != MessageDeliveryState.RECALLED || recalledAt == null) {
                throw new IllegalArgumentException("Recalled messages require both state=RECALLED and recalledAt");
            }
            if (contentPlain != null || contentCipher != null || cryptoMetadata != null) {
                throw new IllegalArgumentException("Recalled messages must omit payload fields");
            }
        } else {
            boolean hasPlain = contentPlain != null && !contentPlain.isEmpty()
                    && contentCipher == null && cryptoMetadata == null;
            boolean hasCipher = contentPlain == null && contentCipher != null && !contentCipher.isEmpty()
                    && cryptoMetadata != null;
            if (!hasPlain && !hasCipher) {
                throw new IllegalArgumentException(
                        "Non-recalled message must have either contentPlain or (contentCipher + cryptoMetadata)");
            }
        }
    }
}
