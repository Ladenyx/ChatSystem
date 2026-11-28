package com.chatsystem.desktop.skeleton;

import static org.junit.jupiter.api.Assertions.*;

import com.chatsystem.common.api.ApiHeaders;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

class SkeletonLayoutsTest {
    @Test
    void enforcesNonFocusableAndMouseTransparentAcrossAllNineSkeletonLayouts() {
        AdminKpiStripSkeleton kpis = new AdminKpiStripSkeleton();
        TableSkeleton table = new TableSkeleton(4, 0.4, 0.3, 0.3);
        ChartSkeleton chart = new ChartSkeleton();
        DetailInspectorSkeleton detail = new DetailInspectorSkeleton();
        ConversationListSkeleton conversations = new ConversationListSkeleton(5);
        MessageHistorySkeleton history = new MessageHistorySkeleton(6);
        ContactListSkeleton contacts = new ContactListSkeleton(4);
        SearchResultsSkeleton search = new SearchResultsSkeleton(3);
        AiSuggestionsSkeleton ai = new AiSuggestionsSkeleton();

        assertEquals(4, kpis.getChildren().size());
        assertEquals(5, table.getChildren().size()); // 1 header + 4 rows
        assertEquals(5, conversations.getChildren().size());
        assertEquals(6, history.getChildren().size());
        assertEquals(4, contacts.getChildren().size());
        assertEquals(3, search.getChildren().size());
        assertEquals(ApiHeaders.AI_SUGGESTION_COUNT, ai.getChildren().size());

        List<Parent> allLayouts = List.of(kpis, table, chart, detail, conversations, history, contacts, search, ai);
        for (Parent layout : allLayouts) {
            assertNonInteractiveTree(layout);
        }

        assertThrows(IllegalArgumentException.class, () -> new TableSkeleton(0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new TableSkeleton(3));
        assertThrows(IllegalArgumentException.class, () -> new TableSkeleton(3, 0.5, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new ConversationListSkeleton(0));
        assertThrows(IllegalArgumentException.class, () -> new MessageHistorySkeleton(0));
        assertThrows(IllegalArgumentException.class, () -> new ContactListSkeleton(0));
        assertThrows(IllegalArgumentException.class, () -> new SearchResultsSkeleton(0));
    }

    @Test
    void skeletonPulseStaysStoppedWhenDetachedAndClosesCleanly() {
        VBox box = new VBox(Skeletons.bar(120, 14));
        SimpleBooleanProperty reducedMotion = new SimpleBooleanProperty(false);
        SkeletonPulse handle = SkeletonPulse.attach(box, reducedMotion);

        assertEquals(1.0, box.getOpacity());
        reducedMotion.set(true);
        assertEquals(1.0, box.getOpacity());
        assertDoesNotThrow(handle::close);
    }

    private static void assertNonInteractiveTree(Node node) {
        assertFalse(node.isFocusTraversable(), "Skeleton node must not receive keyboard focus: " + node);
        assertTrue(node.isMouseTransparent(), "Skeleton node must be mouse-transparent: " + node);
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                assertNonInteractiveTree(child);
            }
        }
    }
}
