package io.jettra.flux.widgets;

import io.jettra.flux.core.Modifier;
import io.jettra.flux.core.Widget;
import io.jettra.flux.theme.ColorMode;
import io.jettra.flux.theme.ThemeData;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulated DashboardHeader widget demonstrating and implementing the adjacent
 * composition of ThemeSelectDropdown and ThemeModeToggle in JettraFlux.
 */
public class DashboardHeader extends Widget {

    private String title = "Dashboard";
    private String currentTheme = "Matrix";
    private ColorMode colorMode = ColorMode.DARK;
    private final List<Widget> rightItems = new ArrayList<>();

    public DashboardHeader() {
        super();
    }

    public static DashboardHeader of() {
        return new DashboardHeader();
    }

    public static DashboardHeader of(String title) {
        return new DashboardHeader().title(title);
    }

    public DashboardHeader title(String title) {
        this.title = title;
        return this;
    }

    public DashboardHeader currentTheme(String currentTheme) {
        this.currentTheme = currentTheme;
        return this;
    }

    public DashboardHeader colorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
        return this;
    }

    public DashboardHeader addRight(Widget widget) {
        if (widget != null) {
            this.rightItems.add(widget);
        }
        return this;
    }

    private boolean sticky = true;

    public DashboardHeader sticky(boolean sticky) {
        this.sticky = sticky;
        return this;
    }

    public boolean isSticky() {
        return sticky;
    }

    @Override
    public String render(ThemeData theme) {
        // Build the adjacent theme control bar
        DashboardThemeControlBar controlBar = DashboardThemeControlBar.of(currentTheme, colorMode);

        List<Widget> rightSectionChildren = new ArrayList<>(rightItems);
        rightSectionChildren.add(controlBar);

        String stickyStyle = sticky ? "position: sticky; top: 0; z-index: 50; flex-shrink: 0; " : "flex-shrink: 0; ";

        Widget headerWidget = Top.of(
            Row.of(
                Header.of(4, title).modifier(new Modifier().style("margin: 0; font-weight: 700; color: var(--jf-text-primary, var(--text-primary, var(--on-surface-color)));"))
            ).modifier(new Modifier().style("align-items: center; gap: 12px;")),
            Row.of(
                rightSectionChildren.toArray(new Widget[0])
            ).modifier(new Modifier().style("align-items: center; gap: 14px;"))
        ).modifier(new Modifier().style(stickyStyle + "justify-content: space-between; width: 100%; background: var(--jf-surface, var(--surface, #111827)); border-bottom: 1px solid var(--jf-border, var(--border, rgba(128,128,128,0.2))); padding: 0.75rem 1.5rem; box-sizing: border-box; " + (modifier != null ? modifier.getStyles() : "")));

        return headerWidget.render(theme);
    }
}
