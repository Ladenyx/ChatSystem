package com.chatsystem.common.domain;

/** Message delivery lifecycle per DEC-02 and UC-USR-MSG-01 (receipt progress is monotonic: STORED < DELIVERED < READ). */
public enum MessageDeliveryState {
    PENDING(false, 0),
    FAILED(false, 0),
    STORED(true, 1),
    DELIVERED(true, 2),
    READ(true, 3),
    RECALLED(true, 4);

    private final boolean acceptedByServer;
    private final int receiptRank;

    MessageDeliveryState(boolean acceptedByServer, int receiptRank) {
        this.acceptedByServer = acceptedByServer;
        this.receiptRank = receiptRank;
    }

    public boolean isAcceptedByServer() {
        return acceptedByServer;
    }

    public boolean canAdvanceReceiptTo(MessageDeliveryState target) {
        if (target == null || !this.acceptedByServer || !target.acceptedByServer) {
            return false;
        }
        return target != RECALLED && target.receiptRank > this.receiptRank;
    }
}
