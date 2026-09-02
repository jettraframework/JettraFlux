package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.*;

/**
 * Native JettraFlux structured object & JSON inspector component.
 * Provides hierarchical tree inspection, key-value tabular views, data type badges,
 * and quick copy actions for complex entity state and historical snapshots.
 * Leverages Java 25+ pattern matching for switch expressions.
 */
public class FluxObjectViewer extends Widget {

    private final Object targetData;
    private String title = "Structured Record Inspector";
    private String maxHeight = "320px";
    private boolean collapsible = true;
    private boolean showTypeBadges = true;
    private boolean showCopyButton = true;
    private final String componentId;

    private FluxObjectViewer(Object targetData) {
        this.targetData = targetData;
        this.componentId = "flux_obj_viewer_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public static FluxObjectViewer of(Object data) {
        return new FluxObjectViewer(data);
    }

    public static FluxObjectViewer ofJson(String jsonString) {
        return new FluxObjectViewer(jsonString);
    }

    public static FluxObjectViewer ofMap(java.util.Map<String, Object> map) {
        return new FluxObjectViewer(map);
    }

    public FluxObjectViewer title(String title) {
        this.title = title;
        return this;
    }

    public FluxObjectViewer maxHeight(String maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public FluxObjectViewer collapsible(boolean collapsible) {
        this.collapsible = collapsible;
        return this;
    }

    public FluxObjectViewer showTypeBadges(boolean showTypeBadges) {
        this.showTypeBadges = showTypeBadges;
        return this;
    }

    public FluxObjectViewer showCopyButton(boolean showCopyButton) {
        this.showCopyButton = showCopyButton;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"").append(componentId).append("\" class=\"flux-object-viewer\" style=\"background:var(--j-bg-surface, #1e293b); border:1px solid var(--j-border, #334155); border-radius:8px; overflow:hidden; font-family:monospace; font-size:12px; margin:8px 0;\">");

        // Header toolbar
        sb.append("<div style=\"display:flex; justify-content:space-between; align-items:center; padding:8px 12px; background:var(--j-bg-subsurface, #0f172a); border-bottom:1px solid var(--j-border, #334155);\">");
        sb.append("<div style=\"display:flex; align-items:center; gap:6px;\">");
        sb.append("<i class=\"fas fa-cubes\" style=\"color:#38bdf8; font-size:13px;\"></i>");
        sb.append("<span style=\"font-weight:700; color:var(--j-text-primary, #f8fafc); font-size:11.5px;\">").append(escapeHtml(title)).append("</span>");
        sb.append("</div>");

        if (showCopyButton) {
            String rawJson = targetData != null ? targetData.toString() : "{}";
            String b64 = Base64.getEncoder().encodeToString(rawJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            sb.append("<button type=\"button\" onclick=\"(function(btn){ try { var text = atob('").append(b64).append("'); navigator.clipboard.writeText(text); var orig=btn.innerHTML; btn.innerHTML='<i class=\\'fas fa-check\\'></i> Copied!'; setTimeout(function(){ btn.innerHTML=orig; }, 1500); } catch(e){} })(this);\" style=\"background:rgba(56,189,248,0.15); border:1px solid rgba(56,189,248,0.3); color:#38bdf8; padding:3px 8px; border-radius:4px; font-size:10.5px; cursor:pointer; font-weight:600;\">");
            sb.append("<i class=\"fas fa-copy\"></i> Copy");
            sb.append("</button>");
        }
        sb.append("</div>");

        // Body with scrollable structured content
        sb.append("<div style=\"padding:12px; max-height:").append(maxHeight).append("; overflow-y:auto; line-height:1.5;\">");
        renderNode(sb, targetData, 0);
        sb.append("</div>");

        sb.append("</div>");
        return sb.toString();
    }

    private void renderNode(StringBuilder sb, Object data, int indentLevel) {
        int indentPx = indentLevel * 16;
        if (data == null) {
            sb.append("<span style=\"color:#94a3b8; font-style:italic;\">null</span>");
            return;
        }

        switch (data) {
            case String s -> {
                String str = s.trim();
                if ((str.startsWith("{") && str.endsWith("}")) || (str.startsWith("[") && str.endsWith("]"))) {
                    // Render structured formatted block
                    sb.append("<pre style=\"margin:0; font-family:monospace; color:#38bdf8; font-size:11px; white-space:pre-wrap; word-break:break-all;\">")
                      .append(escapeHtml(formatJsonIfPossible(str)))
                      .append("</pre>");
                } else {
                    sb.append("<span style=\"color:#4ade80;\">\"").append(escapeHtml(s)).append("\"</span>");
                    if (showTypeBadges) sb.append(" <span style=\"font-size:9px; background:rgba(74,222,128,0.15); color:#4ade80; padding:1px 4px; border-radius:3px;\">string</span>");
                }
            }
            case Number n -> {
                sb.append("<span style=\"color:#f59e0b; font-weight:bold;\">").append(n).append("</span>");
                if (showTypeBadges) sb.append(" <span style=\"font-size:9px; background:rgba(245,158,11,0.15); color:#f59e0b; padding:1px 4px; border-radius:3px;\">number</span>");
            }
            case Boolean b -> {
                sb.append("<span style=\"color:#a855f7; font-weight:bold;\">").append(b).append("</span>");
                if (showTypeBadges) sb.append(" <span style=\"font-size:9px; background:rgba(168,85,247,0.15); color:#a855f7; padding:1px 4px; border-radius:3px;\">boolean</span>");
            }
            case java.util.Map<?, ?> map -> {
                if (map.isEmpty()) {
                    sb.append("<span style=\"color:var(--j-text-muted, #94a3b8);\">{}</span>");
                    return;
                }
                sb.append("<div style=\"display:flex; flex-direction:column; gap:4px; margin-left:").append(indentPx).append("px;\">");
                for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
                    sb.append("<div style=\"display:flex; align-items:flex-start; gap:8px;\">");
                    sb.append("<span style=\"font-weight:700; color:var(--j-text-primary, #f8fafc); min-width:110px;\">").append(escapeHtml(String.valueOf(entry.getKey()))).append(":</span>");
                    sb.append("<div style=\"flex:1;\">");
                    renderNode(sb, entry.getValue(), indentLevel + 1);
                    sb.append("</div>");
                    sb.append("</div>");
                }
                sb.append("</div>");
            }
            case List<?> list -> {
                if (list.isEmpty()) {
                    sb.append("<span style=\"color:var(--j-text-muted, #94a3b8);\">[]</span>");
                    return;
                }
                sb.append("<div style=\"display:flex; flex-direction:column; gap:4px; margin-left:").append(indentPx).append("px;\">");
                for (int i = 0; i < list.size(); i++) {
                    sb.append("<div style=\"display:flex; align-items:flex-start; gap:6px;\">");
                    sb.append("<span style=\"color:var(--j-text-secondary, #94a3b8); font-size:10px; min-width:24px;\">[").append(i).append("]</span>");
                    sb.append("<div style=\"flex:1;\">");
                    renderNode(sb, list.get(i), indentLevel + 1);
                    sb.append("</div>");
                    sb.append("</div>");
                }
                sb.append("</div>");
            }
            case Record r -> {
                sb.append("<div style=\"background:rgba(255,255,255,0.03); padding:6px 8px; border-radius:4px;\">");
                sb.append("<span style=\"color:#38bdf8; font-weight:bold;\">").append(r.getClass().getSimpleName()).append("</span>: ");
                sb.append(escapeHtml(r.toString()));
                sb.append("</div>");
            }
            default -> {
                sb.append("<span style=\"color:var(--j-text-primary, #f8fafc);\">").append(escapeHtml(data.toString())).append("</span>");
            }
        }
    }

    private static String formatJsonIfPossible(String jsonStr) {
        if (jsonStr == null) return "{}";
        StringBuilder formatted = new StringBuilder();
        int indent = 0;
        boolean inQuotes = false;
        for (int i = 0; i < jsonStr.length(); i++) {
            char c = jsonStr.charAt(i);
            if (c == '\"' && (i == 0 || jsonStr.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
                formatted.append(c);
            } else if (!inQuotes) {
                switch (c) {
                    case '{', '[' -> {
                        formatted.append(c).append("\n");
                        indent++;
                        formatted.append("  ".repeat(Math.max(0, indent)));
                    }
                    case '}', ']' -> {
                        formatted.append("\n");
                        indent = Math.max(0, indent - 1);
                        formatted.append("  ".repeat(Math.max(0, indent)));
                        formatted.append(c);
                    }
                    case ',' -> {
                        formatted.append(c).append("\n");
                        formatted.append("  ".repeat(Math.max(0, indent)));
                    }
                    case ':' -> formatted.append(": ");
                    default -> {
                        if (!Character.isWhitespace(c) || formatted.length() == 0 || !Character.isWhitespace(formatted.charAt(formatted.length() - 1))) {
                            formatted.append(c);
                        }
                    }
                }
            } else {
                formatted.append(c);
            }
        }
        return formatted.toString();
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
