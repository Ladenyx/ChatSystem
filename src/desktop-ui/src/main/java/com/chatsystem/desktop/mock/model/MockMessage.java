package com.chatsystem.desktop.mock.model;

import java.time.Instant;

/** Text is synthetic client-visible plaintext, including simulated decrypted group text. */
public record MockMessage(String id, String conversationId, String senderId, String text,
                          Instant sentAt, State state) {
    public boolean acceptedByServer() {
        return state != State.PENDING && state != State.FAILED;
    }

    public enum State { PENDING, FAILED, STORED, DELIVERED, READ, RECALLED }
}
