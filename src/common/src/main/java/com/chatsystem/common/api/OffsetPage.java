package com.chatsystem.common.api;

import java.util.List;
import java.util.Objects;

/** Zero-based offset page for filterable and sortable Admin App tables. */
public record OffsetPage<T>(List<T> items, int page, int size, long totalItems, int totalPages) {
    public OffsetPage {
        Objects.requireNonNull(items, "items");
        items = List.copyOf(items);
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size < 1 || size > ApiHeaders.MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + ApiHeaders.MAX_PAGE_SIZE);
        }
        if (totalItems < 0) {
            throw new IllegalArgumentException("totalItems must be >= 0");
        }
        int expectedPages = calculateTotalPages(totalItems, size);
        if (totalPages != expectedPages) {
            throw new IllegalArgumentException(
                    "totalPages (" + totalPages + ") does not match expected (" + expectedPages + ")");
        }
    }

    public static <T> OffsetPage<T> of(List<T> items, int page, int size, long totalItems) {
        return new OffsetPage<>(items, page, size, totalItems, calculateTotalPages(totalItems, size));
    }

    private static int calculateTotalPages(long totalItems, int size) {
        if (totalItems == 0) {
            return 0;
        }
        return Math.toIntExact((totalItems + size - 1) / size);
    }
}
