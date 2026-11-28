package com.chatsystem.desktop.async;

/** Pure scroll-math utility preserving visible message position when older history is prepended. */
public final class ScrollAnchorMath {
    private ScrollAnchorMath() {
    }

    public static double preservePrependAnchor(
            double oldContentHeight,
            double newContentHeight,
            double viewportHeight,
            double oldVvalue
    ) {
        requireFiniteNonNegative(oldContentHeight, "oldContentHeight");
        requireFiniteNonNegative(newContentHeight, "newContentHeight");
        requireFiniteNonNegative(viewportHeight, "viewportHeight");
        if (!Double.isFinite(oldVvalue)) {
            throw new IllegalArgumentException("oldVvalue must be finite");
        }
        double newMaxScroll = Math.max(0.0, newContentHeight - viewportHeight);
        if (newMaxScroll <= 0.0) {
            return 0.0;
        }
        double oldMaxScroll = Math.max(0.0, oldContentHeight - viewportHeight);
        double clampedOldVvalue = Math.max(0.0, Math.min(1.0, oldVvalue));
        double oldOffset = clampedOldVvalue * oldMaxScroll;
        double prependedDelta = Math.max(0.0, newContentHeight - oldContentHeight);
        double newOffset = oldOffset + prependedDelta;
        return Math.max(0.0, Math.min(1.0, newOffset / newMaxScroll));
    }

    private static void requireFiniteNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and >= 0: " + value);
        }
    }
}
