package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Native First-Class Tree Widget for JettraFlux.
 * Implements:
 * - Composite Pattern (arbitrary nesting of FluxTreeNode<T>)
 * - State and Observer Pattern (reactive expansion, collapse, and listeners)
 * - Visitor Pattern traversal (mass updates, metrics, search)
 * - Accessible WAI-ARIA Treeview semantics (role="tree", role="treeitem", role="group", aria-expanded)
 * - Global deterministic expandAll() and collapseAll() operations in memory and interface.
 *
 * @param <T> data payload type
 */
public class FluxTree<T> extends Widget {

    private final String treeId;
    private final List<FluxTreeNode<T>> rootNodes = new ArrayList<>();
    private final List<FluxTreeStateObserver<T>> observers = new ArrayList<>();
    private boolean defaultExpanded = false;
    private String ariaLabel = "Hierarchical Storage Tree";
    private boolean showLines = true;
    private String onNodeSelectJs;

    public FluxTree() {
        this("fluxtree_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
    }

    public FluxTree(String treeId) {
        this.treeId = treeId != null ? treeId : ("fluxtree_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        this.id(this.treeId);
    }

    public static <T> FluxTree<T> of() {
        return new FluxTree<>();
    }

    public static <T> FluxTree<T> of(String treeId) {
        return new FluxTree<>(treeId);
    }

    @SafeVarargs
    public static <T> FluxTree<T> of(FluxTreeNode<T>... roots) {
        FluxTree<T> tree = new FluxTree<>();
        if (roots != null) {
            for (FluxTreeNode<T> root : roots) {
                tree.root(root);
            }
        }
        return tree;
    }

    public String getTreeId() {
        return treeId;
    }

    public List<FluxTreeNode<T>> getRootNodes() {
        return Collections.unmodifiableList(rootNodes);
    }

    public FluxTree<T> root(FluxTreeNode<T> rootNode) {
        if (rootNode != null) {
            this.rootNodes.add(rootNode);
        }
        return this;
    }

    public FluxTree<T> roots(List<FluxTreeNode<T>> roots) {
        if (roots != null) {
            this.rootNodes.addAll(roots);
        }
        return this;
    }

    public boolean isDefaultExpanded() {
        return defaultExpanded;
    }

    public FluxTree<T> defaultExpanded(boolean defaultExpanded) {
        this.defaultExpanded = defaultExpanded;
        if (defaultExpanded) {
            expandAll();
        } else {
            collapseAll();
        }
        return this;
    }

    public String getAriaLabel() {
        return ariaLabel;
    }

    public FluxTree<T> ariaLabel(String ariaLabel) {
        this.ariaLabel = ariaLabel;
        return this;
    }

    public boolean isShowLines() {
        return showLines;
    }

    public FluxTree<T> showLines(boolean showLines) {
        this.showLines = showLines;
        return this;
    }

    public FluxTree<T> onNodeSelect(String jsHandler) {
        this.onNodeSelectJs = jsHandler;
        return this;
    }

    // --- State / Observer Pattern ---

    public FluxTree<T> addObserver(FluxTreeStateObserver<T> observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
        return this;
    }

    public FluxTree<T> removeObserver(FluxTreeStateObserver<T> observer) {
        if (observer != null) {
            observers.remove(observer);
        }
        return this;
    }

    /**
     * Recursively expands all nodes in the tree in memory and notifies observers.
     */
    public void expandAll() {
        for (FluxTreeNode<T> root : rootNodes) {
            root.expandAll();
        }
        for (FluxTreeStateObserver<T> observer : observers) {
            try {
                observer.onTreeExpandedAll();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Recursively collapses all nodes in the tree in memory and notifies observers.
     */
    public void collapseAll() {
        for (FluxTreeNode<T> root : rootNodes) {
            root.collapseAll();
        }
        for (FluxTreeStateObserver<T> observer : observers) {
            try {
                observer.onTreeCollapsedAll();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Traverses the entire tree hierarchy using the Visitor Pattern.
     *
     * @param visitor functional visitor to apply
     */
    public void accept(FluxTreeVisitor<T> visitor) {
        if (visitor != null) {
            for (FluxTreeNode<T> root : rootNodes) {
                root.accept(visitor);
            }
        }
    }

    /**
     * Finds a node by ID anywhere in the tree.
     *
     * @param targetId node ID
     * @return node if found, otherwise null
     */
    public FluxTreeNode<T> findNode(String targetId) {
        for (FluxTreeNode<T> root : rootNodes) {
            FluxTreeNode<T> found = root.findNode(targetId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Returns a flat list of all nodes in the tree in pre-order traversal.
     */
    public List<FluxTreeNode<T>> flatten() {
        List<FluxTreeNode<T>> flat = new ArrayList<>();
        accept(flat::add);
        return flat;
    }

    public int getTotalNodeCount() {
        return flatten().size();
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div id=\"").append(treeId).append("\" role=\"tree\" aria-label=\"")
          .append(escapeHtml(ariaLabel)).append("\" class=\"jettra-flux-tree\" style=\"display:flex; flex-direction:column; gap:4px; width:100%; user-select:none;\">\n");

        for (FluxTreeNode<T> root : rootNodes) {
            renderNode(sb, root, theme);
        }

        sb.append("</div>\n");

        // Embedded deterministic reactive tree controller
        sb.append("<script>\n")
          .append("  window.FluxTree = window.FluxTree || {};\n")
          .append("  window.FluxTree.toggle = function(nodeId, treeId) {\n")
          .append("    var group = document.getElementById('group_' + nodeId);\n")
          .append("    var node = document.getElementById('node_' + nodeId);\n")
          .append("    var btn = document.getElementById('btn_toggle_' + nodeId);\n")
          .append("    var icon = document.getElementById('icon_' + nodeId);\n")
          .append("    if (!group) return;\n")
          .append("    var isExpanded = group.style.display !== 'none';\n")
          .append("    if (isExpanded) {\n")
          .append("      group.style.display = 'none';\n")
          .append("      if (node) node.setAttribute('aria-expanded', 'false');\n")
          .append("      if (btn) btn.setAttribute('aria-expanded', 'false');\n")
          .append("      if (icon) { icon.className = 'fas fa-chevron-right flux-tree-toggle-icon'; }\n")
          .append("    } else {\n")
          .append("      group.style.display = 'block';\n")
          .append("      if (node) node.setAttribute('aria-expanded', 'true');\n")
          .append("      if (btn) btn.setAttribute('aria-expanded', 'true');\n")
          .append("      if (icon) { icon.className = 'fas fa-chevron-down flux-tree-toggle-icon'; }\n")
          .append("    }\n")
          .append("  };\n")
          .append("  window.FluxTree.expandAll = function(treeId) {\n")
          .append("    var root = treeId ? document.getElementById(treeId) : document;\n")
          .append("    if (!root) root = document;\n")
          .append("    var groups = root.querySelectorAll('.flux-tree-group');\n")
          .append("    for (var i = 0; i < groups.length; i++) { groups[i].style.display = 'block'; }\n")
          .append("    var nodes = root.querySelectorAll('.flux-tree-node');\n")
          .append("    for (var j = 0; j < nodes.length; j++) { nodes[j].setAttribute('aria-expanded', 'true'); }\n")
          .append("    var btns = root.querySelectorAll('.flux-tree-toggle-btn');\n")
          .append("    for (var k = 0; k < btns.length; k++) { btns[k].setAttribute('aria-expanded', 'true'); }\n")
          .append("    var icons = root.querySelectorAll('.flux-tree-toggle-icon');\n")
          .append("    for (var l = 0; l < icons.length; l++) { icons[l].className = 'fas fa-chevron-down flux-tree-toggle-icon'; }\n")
          .append("  };\n")
          .append("  window.FluxTree.collapseAll = function(treeId) {\n")
          .append("    var root = treeId ? document.getElementById(treeId) : document;\n")
          .append("    if (!root) root = document;\n")
          .append("    var groups = root.querySelectorAll('.flux-tree-group');\n")
          .append("    for (var i = 0; i < groups.length; i++) { groups[i].style.display = 'none'; }\n")
          .append("    var nodes = root.querySelectorAll('.flux-tree-node');\n")
          .append("    for (var j = 0; j < nodes.length; j++) { nodes[j].setAttribute('aria-expanded', 'false'); }\n")
          .append("    var btns = root.querySelectorAll('.flux-tree-toggle-btn');\n")
          .append("    for (var k = 0; k < btns.length; k++) { btns[k].setAttribute('aria-expanded', 'false'); }\n")
          .append("    var icons = root.querySelectorAll('.flux-tree-toggle-icon');\n")
          .append("    for (var l = 0; l < icons.length; l++) { icons[l].className = 'fas fa-chevron-right flux-tree-toggle-icon'; }\n")
          .append("  };\n")
          .append("</script>\n");

        return sb.toString();
    }

    private void renderNode(StringBuilder sb, FluxTreeNode<T> node, ThemeData theme) {
        String nodeId = node.getId();
        boolean hasChildren = node.hasChildren();
        boolean isExpanded = node.isExpanded();
        String groupDisplay = isExpanded ? "block" : "none";
        String chevronIcon = isExpanded ? "fas fa-chevron-down" : "fas fa-chevron-right";

        sb.append("<div id=\"node_").append(nodeId).append("\" role=\"treeitem\" aria-expanded=\"")
          .append(isExpanded).append("\" class=\"flux-tree-node\" style=\"display:flex; flex-direction:column;\">\n");

        // Node Row Header
        sb.append("  <div class=\"flux-tree-node-row\" style=\"display:flex; align-items:center; justify-content:space-between; padding:3px 6px; border-radius:5px; transition:background 0.15s ease; gap:6px;\">\n");

        // Left section: Toggle Chevron + Icon + Label
        sb.append("    <div style=\"display:inline-flex; align-items:center; gap:5px; min-width:0; flex:1; cursor:pointer;\" ");
        if (hasChildren) {
            sb.append("onclick=\"FluxTree.toggle('").append(nodeId).append("', '").append(treeId).append("')\" ");
        } else if (onNodeSelectJs != null) {
            sb.append("onclick=\"").append(onNodeSelectJs.replace("{id}", nodeId)).append("\" ");
        }
        sb.append(">\n");

        if (hasChildren) {
            sb.append("      <button id=\"btn_toggle_").append(nodeId).append("\" type=\"button\" class=\"flux-tree-toggle-btn\" ")
              .append("aria-expanded=\"").append(isExpanded).append("\" aria-controls=\"group_").append(nodeId).append("\" ")
              .append("onclick=\"event.stopPropagation(); FluxTree.toggle('").append(nodeId).append("', '").append(treeId).append("')\" ")
              .append("style=\"background:none; border:none; padding:2px 4px; cursor:pointer; display:inline-flex; align-items:center; justify-content:center; color:var(--j-primary,#38bdf8); font-size:10px;\">")
              .append("<i id=\"icon_").append(nodeId).append("\" class=\"").append(chevronIcon).append(" flux-tree-toggle-icon\"></i>")
              .append("</button>\n");
        } else {
            // Leaf indentation spacer
            sb.append("      <span style=\"display:inline-block; width:16px;\"></span>\n");
        }

        // Icon
        if (node.getIcon() != null && !node.getIcon().isBlank()) {
            sb.append("      <i class=\"").append(node.getIcon()).append("\" style=\"color:").append(node.getIconColor())
              .append("; font-size:11px; flex-shrink:0;\"></i>\n");
        }

        // Label
        sb.append("      <span class=\"flux-tree-label\" style=\"font-size:11.5px; font-weight:600; color:var(--j-text-primary,#f8fafc); overflow:hidden; text-overflow:ellipsis; white-space:nowrap;\">")
          .append(escapeHtml(node.getLabel()))
          .append("</span>\n");

        // Badge if present
        if (node.getBadge() != null && !node.getBadge().isBlank()) {
            sb.append("      <span class=\"").append(node.getBadgeClass())
              .append("\" style=\"font-size:8.5px; padding:1px 5px; border-radius:3px; font-weight:700; flex-shrink:0;\">")
              .append(escapeHtml(node.getBadge()))
              .append("</span>\n");
        }

        sb.append("    </div>\n");

        // Right section: Actions
        if (!node.getActions().isEmpty()) {
            sb.append("    <div class=\"flux-tree-actions\" style=\"display:inline-flex; align-items:center; gap:4px; flex-shrink:0;\" onclick=\"event.stopPropagation();\">\n");
            for (Widget action : node.getActions()) {
                sb.append(action.render(theme));
            }
            sb.append("    </div>\n");
        }

        sb.append("  </div>\n");

        // Subtree Child Group Container
        if (hasChildren) {
            String borderStyle = showLines ? "border-left: 2px dashed rgba(56,189,248,0.25);" : "";
            sb.append("  <div id=\"group_").append(nodeId).append("\" role=\"group\" class=\"flux-tree-group\" ")
              .append("style=\"display:").append(groupDisplay).append("; margin-left:12px; ").append(borderStyle).append(" padding-left:8px; margin-top:2px;\">\n");

            for (FluxTreeNode<T> child : node.getChildren()) {
                renderNode(sb, child, theme);
            }

            sb.append("  </div>\n");
        }

        sb.append("</div>\n");
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
