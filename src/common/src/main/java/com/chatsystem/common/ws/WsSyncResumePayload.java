package com.chatsystem.common.ws;

import com.chatsystem.common.api.ApiHeaders;

/** Payload for the {@code sync.resume} client command after reconnecting. */
public record WsSyncResumePayload(long lastAckedSeq, int limit) {
    public WsSyncResumePayload {
        if (lastAckedSeq < 0) {
            throw new IllegalArgumentException("lastAckedSeq must be >= 0");
        }
        if (limit < 1 || limit > ApiHeaders.MAX_SYNC_BATCH_SIZE) {
            throw new IllegalArgumentException("limit must be between 1 and " + ApiHeaders.MAX_SYNC_BATCH_SIZE);
        }
    }
}
