package com.chatsystem.common.api;

import java.util.List;
import java.util.Objects;

/** Opaque cursor page for time-ordered streams such as conversations, search hits, and login logs. */
public record CursorPage<T>(List<T> items, String nextCursor, boolean hasMore) {
    public CursorPage {
        Objects.requireNonNull(items, "items");
        items = List.copyOf(items);
        if (hasMore && (nextCursor == null || nextCursor.isBlank())) {
            throw new IllegalArgumentException("nextCursor is required when hasMore is true");
        }
        if (!hasMore && nextCursor != null && nextCursor.isBlank()) {
            throw new IllegalArgumentException("nextCursor must be null or non-blank");
        }
    }

    public static <T> CursorPage<T> empty() {
        return new CursorPage<>(List.of(), null, false);
    }
}
