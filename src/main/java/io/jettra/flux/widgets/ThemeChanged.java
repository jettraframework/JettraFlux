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
        
        String[] themes = io.jettra.flux.theme.ThemeRegistry.getAvailableThemeNames();
        String[] logos = {"🪐", "🟦", "🧊", "🚀", "🔱", "🌊"};
        
        String currentLogo = "🎨";
        for (int i = 0; i < themes.length; i++) {
            String themeName = themes[i];
            String logo = i < logos.length ? logos[i] : "🎨";
            if (themeName.equalsIgnoreCase(currentTheme)) {
                currentLogo = logo;
            }
        }
        
        Widget trigger = Span.of(currentLogo)
                .modifier(new io.jettra.flux.core.Modifier().attribute("title", currentTheme).style("cursor:pointer; font-size:1.5rem;"));
        
        WidgetLet[] items = new WidgetLet[themes.length];
        for (int i = 0; i < themes.length; i++) {
            String logo = i < logos.length ? logos[i] : "🎨";
            items[i] = WidgetLet.of(themes[i] + " " + logo)
                    .url("javascript:changeJettraTheme('" + themes[i] + "')");
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
}
