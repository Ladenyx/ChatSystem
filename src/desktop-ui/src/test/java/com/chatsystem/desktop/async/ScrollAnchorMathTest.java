package com.chatsystem.desktop.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ScrollAnchorMathTest {
    @Test
    void preservesVisibleTopMessageWhenOlderMessagesArePrepended() {
        // Old content 1000px, viewport 400px -> maxScroll 600px. Top message was at vvalue=0.0 (offset 0px).
        // Prepend 500px -> new content 1500px, maxScroll 1100px. Old top should sit at offset 500px -> 500/1100.
        double nextVvalue = ScrollAnchorMath.preservePrependAnchor(1000.0, 1500.0, 400.0, 0.0);
        assertEquals(500.0 / 1100.0, nextVvalue, 1e-9);

        // Mid-scroll at vvalue=0.5 (offset 300px) + 500px prepend -> offset 800px / 1100px.
        double midVvalue = ScrollAnchorMath.preservePrependAnchor(1000.0, 1500.0, 400.0, 0.5);
        assertEquals(800.0 / 1100.0, midVvalue, 1e-9);
    }

    @Test
    void returnsZeroWhenContentFitsInViewportAndValidatesInputs() {
        assertEquals(0.0, ScrollAnchorMath.preservePrependAnchor(200.0, 350.0, 400.0, 0.25));
        assertThrows(IllegalArgumentException.class,
                () -> ScrollAnchorMath.preservePrependAnchor(Double.NaN, 1000.0, 400.0, 0.0));
    }
}
