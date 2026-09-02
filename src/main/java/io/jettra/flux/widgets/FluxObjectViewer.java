package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.UUID;

/**
 * Native JettraFlux Widget for structured object and snapshot inspection.
 * Provides:
 * - Compact single-line preview with typography styling.
 * - Interactive expand/collapse toggle to view complete payload attributes.
 * - Hierarchical tree representation of nested fields using FluxJsonTree.
 * - Full clipboard copy helper.
 * Completely self-contained without external JSON library dependencies.
 */
public class FluxObjectViewer extends Widget {

    private final String rawPayload;
    private final String viewerId;
    private String title = "Snapshot Attributes";
    private boolean expandable = true;
    private boolean defaultExpanded = false;
    private int maxPreviewLength = 65;
    private boolean showCopyButton = true;
    private boolean showBadge = true;

    private FluxObjectViewer(String rawPayload) {
        this.rawPayload = (rawPayload != null && !rawPayload.isBlank()) ? rawPayload.trim() : "{}";
        this.viewerId = "fov_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public static FluxObjectViewer of(String payload) {
        return new FluxObjectViewer(payload);
    }

    public static FluxObjectViewer of(Object object) {
        if (object == null) return new FluxObjectViewer("{}");
        return new FluxObjectViewer(object.toString());
    }

    public FluxObjectViewer title(String title) {
        this.title = title;
        return this;
    }

    public FluxObjectViewer expandable(boolean expandable) {
        this.expandable = expandable;
        return this;
    }

    public FluxObjectViewer defaultExpanded(boolean expanded) {
        this.defaultExpanded = expanded;
        return this;
    }

    public FluxObjectViewer maxPreviewLength(int length) {
        this.maxPreviewLength = length;
        return this;
    }

    public FluxObjectViewer showCopyButton(boolean show) {
        this.showCopyButton = show;
        return this;
    }

    public FluxObjectViewer showBadge(boolean show) {
        this.showBadge = show;
        return this;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public String getCompactPreview() {
        String clean = rawPayload.replaceAll("[\\r\\n\\t]+", " ");
        if (clean.length() > maxPreviewLength) {
            return clean.substring(0, maxPreviewLength) + "...";
        }
        return clean;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        String compact = getCompactPreview();
        String detailId = viewerId + "_detail";
        String iconId = viewerId + "_icon";

        sb.append("<div id=\"").append(viewerId).append("\" class=\"jettra-flux-object-viewer\" style=\"display:flex; flex-direction:column; gap:4px; width:100%;\">\n");

        // Row preview
        sb.append("  <div style=\"display:flex; align-items:center; gap:6px; min-width:0;\">\n");

        if (expandable) {
            String initialIcon = defaultExpanded ? "fas fa-chevron-down" : "fas fa-chevron-right";
            sb.append("    <button type=\"button\" onclick=\"")
              .append("var d=document.getElementById('").append(detailId).append("');")
              .append("var ic=document.getElementById('").append(iconId).append("');")
              .append("if(d){if(d.style.display==='none'){d.style.display='block';if(ic)ic.className='fas fa-chevron-down';}")
              .append("else{d.style.display='none';if(ic)ic.className='fas fa-chevron-right';}}\" ")
              .append("title=\"Expand full snapshot details\" ")
              .append("style=\"background:none; border:none; color:var(--j-text-muted,#94a3b8); cursor:pointer; padding:2px 4px; font-size:10px; display:inline-flex; align-items:center; justify-content:center;\">")
              .append("<i id=\"").append(iconId).append("\" class=\"").append(initialIcon).append("\"></i>")
              .append("</button>\n");
        }

        // Preview snippet
        sb.append("    <span style=\"flex:1; font-family:ui-monospace,SFMono-Regular,Menlo,Monaco,Consolas,monospace; font-size:11px; color:var(--j-text-secondary,#cbd5e1); overflow:hidden; text-overflow:ellipsis; white-space:nowrap; cursor:pointer;\" ")
          .append(expandable ? "onclick=\"var d=document.getElementById('" + detailId + "');var ic=document.getElementById('" + iconId + "');if(d){if(d.style.display==='none'){d.style.display='block';if(ic)ic.className='fas fa-chevron-down';}else{d.style.display='none';if(ic)ic.className='fas fa-chevron-right';}}\"" : "")
          .append(">")
          .append(escapeHtml(compact))
          .append("</span>\n");

        if (showCopyButton) {
            String b64 = java.util.Base64.getEncoder().encodeToString(rawPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            sb.append("    <button type=\"button\" onclick=\"navigator.clipboard.writeText(atob('").append(b64).append("'));")
              .append("this.innerHTML='<i class=\\\'fas fa-check\\\' style=\\\'color:#10b981;\\\'></i>';")
              .append("var b=this;setTimeout(function(){b.innerHTML='<i class=\\\'fas fa-copy\\\'></i>';},1500);\" ")
              .append("title=\"Copy JSON Payload\" ")
              .append("style=\"background:none; border:none; color:var(--j-text-muted,#94a3b8); cursor:pointer; font-size:10px; padding:2px 4px;\">")
              .append("<i class=\"fas fa-copy\"></i>")
              .append("</button>\n");
        }

        sb.append("  </div>\n");

        // Expandable Detail Drawer / Tree view
        if (expandable) {
            String display = defaultExpanded ? "block" : "none";
            sb.append("  <div id=\"").append(detailId).append("\" style=\"display:").append(display).append("; margin-top:4px; padding:8px 10px; background:var(--j-bg-body,#0f172a); border:1px solid var(--j-border,rgba(255,255,255,0.08)); border-radius:6px; box-shadow:inset 0 1px 4px rgba(0,0,0,0.2);\">\n");

            // Header bar in detail view
            sb.append("    <div style=\"display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; padding-bottom:4px; border-bottom:1px solid var(--j-border,rgba(255,255,255,0.06));\">\n");
            sb.append("      <span style=\"font-size:10.5px; font-weight:700; color:var(--j-text-primary,#f8fafc); text-transform:uppercase; letter-spacing:0.5px;\">").append(escapeHtml(title)).append("</span>\n");
            sb.append("      <span style=\"font-size:10px; color:var(--j-text-muted,#64748b); font-family:monospace;\">").append(rawPayload.length()).append(" bytes</span>\n");
            sb.append("    </div>\n");

            // Render JSON Tree inside detail panel
            FluxJsonTree tree = FluxJsonTree.of(rawPayload, true);
            sb.append(tree.render(theme));

            sb.append("  </div>\n");
        }

        sb.append("</div>\n");
        return sb.toString();
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
