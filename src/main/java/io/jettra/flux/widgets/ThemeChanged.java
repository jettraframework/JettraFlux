package io.jettra.flux.widgets;

import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ThemeData;

public class ThemeChanged extends Widget {
    
    private String currentTheme = "Ast"; // default

    private ThemeChanged() {}

    public static ThemeChanged of() {
        return new ThemeChanged();
    }
    
    public ThemeChanged current(String theme) {
        this.currentTheme = theme;
        return this;
    }

    @Override
    public String render(ThemeData theme) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<div class=\"espresso-theme-changed\" style=\"display: inline-block;\">\n");
        
        // Trigger ThemeRegistry loading by touching Themes class if not yet loaded
        try { Class.forName("io.jettra.flux.theme.Themes"); } catch (Exception ignored) {}
        
        String[] allThemes = io.jettra.flux.theme.ThemeRegistry.getAvailableThemeNames();
        java.util.List<String> uniqueThemes = new java.util.ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        
        for (String t : allThemes) {
            String norm = t.toLowerCase().replace("theme", "");
            if (!seen.contains(norm)) {
                seen.add(norm);
                uniqueThemes.add(t);
            }
        }
        
        String currentLogo = getThemeLogo(currentTheme);
        
        Widget trigger = Span.of(currentLogo)
                .modifier(new io.jettra.flux.core.Modifier().attribute("title", currentTheme).style("cursor:pointer; font-size:1.5rem;"));
        
        WidgetLet[] items = new WidgetLet[uniqueThemes.size()];
        for (int i = 0; i < uniqueThemes.size(); i++) {
            String themeName = uniqueThemes.get(i);
            String logo = getThemeLogo(themeName);
            items[i] = WidgetLet.of(themeName + " " + logo)
                    .url("javascript:changeJettraTheme('" + themeName + "')");
        }
        
        Widget menu = ((io.jettra.flux.widgets.OverlayMenu) OverlayMenu.of(items).trigger(trigger)).alignRight();
        
        sb.append(menu.render(theme));
        
        sb.append("  <script>\n");
        sb.append("    function changeJettraTheme(themeName) {\n");
        sb.append("      document.cookie = 'jettra_theme=' + themeName + '; path=/';\n");
        sb.append("      window.location.reload();\n");
        sb.append("    }\n");
        sb.append("  </script>\n");
        sb.append("</div>\n");
        
        return sb.toString();
    }

    private String getThemeLogo(String themeName) {
        if (themeName == null) return "🎨";
        String lower = themeName.toLowerCase();
        if (lower.contains("retro")) return "⛏️";
        if (lower.contains("ocean")) return "🌊";
        if (lower.contains("atlantis")) return "🔱";
        if (lower.contains("futuristic")) return "🚀";
        if (lower.contains("3d")) return "🧊";
        if (lower.contains("flat")) return "🟦";
        if (lower.contains("ast")) return "🪐";
        return "🎨";
    }
}
