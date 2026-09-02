package io.jettra.flux.widgets;

import io.jettra.flux.theme.Themes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite for FluxTree, FluxTreeNode, FluxTreeVisitor, and FluxTreeStateObserver.
 */
public class FluxTreeTest {

    @Test
    @DisplayName("Test Composite Pattern: multi-level hierarchy construction, depth, and traversal")
    public void testTreeCompositeConstruction() {
        FluxTreeNode<String> rootDb = FluxTreeNode.of("db_ecommerce", "ecommerce_db", "DATABASE")
            .icon("fas fa-database")
            .badge("ACTIVE", "badge-active");

        FluxTreeNode<String> collOrders = FluxTreeNode.of("coll_orders", "orders (2 items)", "COLLECTION")
            .icon("fas fa-folder");

        FluxTreeNode<String> itemOrd1 = FluxTreeNode.of("item_ord_1", "ord_001", "ITEM")
            .icon("fas fa-file-code")
            .badge("v1");

        FluxTreeNode<String> itemOrd2 = FluxTreeNode.of("item_ord_2", "ord_002", "ITEM")
            .icon("fas fa-file-code")
            .badge("v2");

        collOrders.child(itemOrd1).child(itemOrd2);
        rootDb.child(collOrders);

        FluxTree<String> tree = FluxTree.of(rootDb);

        assertEquals(1, tree.getRootNodes().size());
        assertEquals(4, tree.getTotalNodeCount());

        assertEquals(0, rootDb.getDepth());
        assertEquals(1, collOrders.getDepth());
        assertEquals(2, itemOrd1.getDepth());
        assertEquals(2, itemOrd2.getDepth());

        assertFalse(rootDb.isLeaf());
        assertFalse(collOrders.isLeaf());
        assertTrue(itemOrd1.isLeaf());
        assertTrue(itemOrd2.isLeaf());

        assertNotNull(tree.findNode("item_ord_2"));
        assertEquals("ord_002", tree.findNode("item_ord_2").getLabel());
    }

    @Test
    @DisplayName("Test State Management: expandAll and collapseAll recursive propagation")
    public void testExpandAllAndCollapseAllOperations() {
        FluxTreeNode<String> root = FluxTreeNode.of("root", "Root");
        FluxTreeNode<String> branch1 = FluxTreeNode.of("b1", "Branch 1");
        FluxTreeNode<String> leaf1 = FluxTreeNode.of("l1", "Leaf 1");
        FluxTreeNode<String> leaf2 = FluxTreeNode.of("l2", "Leaf 2");

        branch1.child(leaf1).child(leaf2);
        root.child(branch1);

        FluxTree<String> tree = FluxTree.of(root);

        // Initially collapsed
        assertFalse(root.isExpanded());
        assertFalse(branch1.isExpanded());

        // Global expandAll
        tree.expandAll();
        assertTrue(root.isExpanded(), "Root must be expanded after expandAll");
        assertTrue(branch1.isExpanded(), "Branch must be expanded after expandAll");
        assertTrue(leaf1.isExpanded(), "Leaf must be expanded after expandAll");

        // Global collapseAll
        tree.collapseAll();
        assertFalse(root.isExpanded(), "Root must be collapsed after collapseAll");
        assertFalse(branch1.isExpanded(), "Branch must be collapsed after collapseAll");
        assertFalse(leaf1.isExpanded(), "Leaf must be collapsed after collapseAll");
    }

    @Test
    @DisplayName("Test Visitor Pattern: traversal and mass node transformations")
    public void testVisitorPattern() {
        FluxTreeNode<Integer> root = FluxTreeNode.of("r", "Root", 10);
        FluxTreeNode<Integer> c1 = FluxTreeNode.of("c1", "Child 1", 20);
        FluxTreeNode<Integer> c2 = FluxTreeNode.of("c2", "Child 2", 30);

        root.child(c1).child(c2);
        FluxTree<Integer> tree = FluxTree.of(root);

        AtomicInteger sum = new AtomicInteger(0);
        tree.accept(node -> {
            if (node.getData() != null) {
                sum.addAndGet(node.getData());
            }
        });

        assertEquals(60, sum.get(), "Visitor must visit all nodes and accumulate sum");

        // Use visitor to expand all nodes
        tree.accept(FluxTreeVisitor.expandAll());
        assertTrue(root.isExpanded());
        assertTrue(c1.isExpanded());
        assertTrue(c2.isExpanded());
    }

    @Test
    @DisplayName("Test Observer Pattern: reactive tree state listeners")
    public void testObserverPattern() {
        FluxTree<String> tree = FluxTree.of(FluxTreeNode.of("n1", "Node 1"));
        AtomicBoolean expandedNotified = new AtomicBoolean(false);
        AtomicBoolean collapsedNotified = new AtomicBoolean(false);

        tree.addObserver(new FluxTreeStateObserver<>() {
            @Override
            public void onTreeExpandedAll() {
                expandedNotified.set(true);
            }

            @Override
            public void onTreeCollapsedAll() {
                collapsedNotified.set(true);
            }
        });

        tree.expandAll();
        assertTrue(expandedNotified.get(), "Observer must be notified on expandAll");

        tree.collapseAll();
        assertTrue(collapsedNotified.get(), "Observer must be notified on collapseAll");
    }

    @Test
    @DisplayName("Test Accessible WAI-ARIA DOM rendering and client controller functions")
    public void testRenderWaiAriaAndAccessibleAttributes() {
        FluxTreeNode<String> root = FluxTreeNode.of("db_main", "Main DB", "db")
            .icon("fas fa-database")
            .badge("ACTIVE", "store-badge");

        FluxTreeNode<String> child = FluxTreeNode.of("coll_users", "users", "coll")
            .icon("fas fa-users");

        root.child(child);
        root.expand(); // expanded root

        FluxTree<String> tree = FluxTree.of("test_tree_01");
        tree.root(root);

        String html = tree.render(Themes.FlatTheme());

        assertNotNull(html);
        assertTrue(html.contains("role=\"tree\""), "Tree must declare role=tree");
        assertTrue(html.contains("role=\"treeitem\""), "Node must declare role=treeitem");
        assertTrue(html.contains("role=\"group\""), "Subtree group must declare role=group");
        assertTrue(html.contains("aria-expanded=\"true\""), "Expanded node must declare aria-expanded=true");
        assertTrue(html.contains("Main DB"), "Must render label Main DB");
        assertTrue(html.contains("fas fa-database"), "Must render database icon");
        assertTrue(html.contains("fas fa-chevron-down"), "Expanded node must display chevron-down");
        assertTrue(html.contains("window.FluxTree.toggle"), "Must include FluxTree.toggle client controller");
        assertTrue(html.contains("window.FluxTree.expandAll"), "Must include FluxTree.expandAll client controller");
        assertTrue(html.contains("window.FluxTree.collapseAll"), "Must include FluxTree.collapseAll client controller");
    }
}
