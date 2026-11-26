package com.chatsystem.common.dto.ai;

import com.chatsystem.common.api.ApiHeaders;
import com.chatsystem.common.domain.AiAssistMode;
import java.util.List;
import java.util.Objects;

/** Structured response returning exactly 3 suggestions for U-DLG-03 per DEC-08. */
public record AiAssistResponse(AiAssistMode mode, List<String> suggestions) {
    public AiAssistResponse {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(suggestions, "suggestions");
        suggestions = List.copyOf(suggestions);
        if (suggestions.size() != ApiHeaders.AI_SUGGESTION_COUNT) {
            throw new IllegalArgumentException(
                    "AI assist response must contain exactly " + ApiHeaders.AI_SUGGESTION_COUNT + " suggestions");
        }
        for (String suggestion : suggestions) {
            if (suggestion.isBlank()) {
                throw new IllegalArgumentException("AI suggestion must not be blank");
            }
        }
    }
}
