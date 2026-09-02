package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

import java.util.UUID;

/**
 * Slide-over drawer and detailed inspector dialog component in JettraFlux.
 * Encapsulates full entity snapshot preview with expandable inspection and instant rollback controls.
 */
public class FluxSnapshotDrawer extends Widget {

    private final String drawerId;
    private final String title;
    private final Widget content;
    private Widget trigger;
    private String width = "540px";
    private String position = "right";

    private String version;
    private String timestamp;
    private String author;
    private String payload;

    private FluxSnapshotDrawer(String title, Widget content) {
        this.title = title != null ? title : "Historical Snapshot Inspector";
        this.content = content;
        this.drawerId = "snapshot_drawer_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private FluxSnapshotDrawer(String version, String timestamp, String author, String payload) {
        this.version = version;
        this.timestamp = timestamp;
        this.author = author;
        this.payload = (payload != null && !payload.isBlank()) ? payload.trim() : "{}";
        this.title = "Snapshot " + (version != null ? version : "");
        this.drawerId = "snapshot_drawer_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        this.content = null;
    }

    public static FluxSnapshotDrawer of(String title, Widget content) {
        return new FluxSnapshotDrawer(title, content);
    }

    public static FluxSnapshotDrawer of(Widget content) {
        return new FluxSnapshotDrawer("Snapshot Detail Inspector", content);
    }

    public static FluxSnapshotDrawer of(String version, String timestamp, String author, String payload) {
        return new FluxSnapshotDrawer(version, timestamp, author, payload);
    }

    public FluxSnapshotDrawer trigger(Widget trigger) {
        this.trigger = trigger;
        return this;
    }

    public FluxSnapshotDrawer width(String width) {
        this.width = width;
        return this;
    }

    public FluxSnapshotDrawer position(String position) {
        this.position = position;
        return this;
    }

    public String getDrawerId() {
        return drawerId;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        if (trigger != null) {
            sb.append("<div onclick=\"(function(){ var d=document.getElementById('").append(drawerId).append("'); if(d){ d.classList.add('open'); d.style.display='flex'; } })()\" style=\"display:inline-flex; align-items:center; cursor:pointer;\">");
            sb.append(trigger.render(theme));
            sb.append("</div>");
        }

        sb.append("<div id=\"").append(drawerId).append("\" class=\"jettra-flux-snapshot-drawer jettra-snapshot-drawer\" style=\"position:fixed; top:0; right:0; bottom:0; width:100%; max-width:").append(width).append("; background:var(--j-bg-surface, #0f172a); border-left:1px solid var(--j-border, #334155); box-shadow:-8px 0 24px rgba(0,0,0,0.5); z-index:999999; transform:translateX(100%); transition:transform 0.25s cubic-bezier(0.4, 0, 0.2, 1); display:flex; flex-direction:column;\">");

        // Header
        sb.append("<div style=\"display:flex; justify-content:space-between; align-items:center; padding:16px 20px; border-bottom:1px solid var(--j-border, #334155); background:var(--j-bg-subsurface, #1e293b);\">");
        sb.append("<div style=\"display:flex; align-items:center; gap:8px;\">");
        sb.append("<i class=\"fas fa-history\" style=\"color:#38bdf8; font-size:16px;\"></i>");
        sb.append("<h4 style=\"margin:0; font-size:14px; font-weight:700; color:var(--j-text-primary, #f8fafc);\">").append(title).append("</h4>");
        if (version != null && !version.isEmpty()) {
            sb.append("<span style=\"padding:2px 8px; border-radius:12px; font-size:11px; font-weight:700; background:rgba(56,189,248,0.15); color:#38bdf8; border:1px solid rgba(56,189,248,0.3);\">").append(version).append("</span>");
        }
        sb.append("</div>");
        sb.append("<button type=\"button\" onclick=\"(function(){ var d=document.getElementById('").append(drawerId).append("'); if(d){ d.classList.remove('open'); d.style.display='none'; } })()\" style=\"background:none; border:none; color:var(--j-text-muted, #94a3b8); font-size:18px; cursor:pointer; padding:4px;\">&times;</button>");
        sb.append("</div>");

        // Metadata Subheader (if metadata present)
        if (timestamp != null || author != null) {
            sb.append("<div style=\"display:flex; gap:16px; align-items:center; padding:10px 20px; background:rgba(15,23,42,0.6); border-bottom:1px solid var(--j-border, #334155); font-size:12px; color:var(--j-text-muted, #94a3b8);\">");
            if (timestamp != null && !timestamp.isEmpty()) {
                sb.append("<div><i class=\"far fa-clock\" style=\"margin-right:4px;\"></i><span>").append(timestamp).append("</span></div>");
            }
            if (author != null && !author.isEmpty()) {
                sb.append("<div><i class=\"far fa-user\" style=\"margin-right:4px;\"></i><span>").append(author).append("</span></div>");
            }
            sb.append("</div>");
        }

        // Content
        sb.append("<div style=\"padding:18px 20px; flex:1; overflow-y:auto;\">");
        if (content != null) {
            sb.append(content.render(theme));
        } else if (payload != null && !payload.isEmpty()) {
            sb.append(FluxJsonTree.of(payload, true).render(theme));
        }
        sb.append("</div>");

        sb.append("</div>");

        sb.append("<style>#").append(drawerId).append(".open { transform: translateX(0) !important; display: flex !important; }</style>");
        return sb.toString();
    }
}
