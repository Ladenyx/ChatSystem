package com.chatsystem.common.dto.ai;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.domain.AiAssistMode;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Request payload for {@code POST /api/v1/ai/assist} supporting REFINE and RECOMMEND modes (U-27, DEC-08). */
public record AiAssistRequest(
        UUID conversationId,
        AiAssistMode mode,
        String draft,
        List<AiAssistContextMessageDto> contextMessages
) {
    public AiAssistRequest {
        Objects.requireNonNull(conversationId, "conversationId");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(contextMessages, "contextMessages");
        contextMessages = List.copyOf(contextMessages);
        if (contextMessages.size() > ApiHeaders.AI_MAX_CONTEXT_MESSAGES) {
            throw new IllegalArgumentException(
                    "contextMessages must not exceed " + ApiHeaders.AI_MAX_CONTEXT_MESSAGES);
        }
        if (mode == AiAssistMode.REFINE && (draft == null || draft.isBlank())) {
            throw new IllegalArgumentException("REFINE mode requires a non-blank draft");
        }
    }
}
