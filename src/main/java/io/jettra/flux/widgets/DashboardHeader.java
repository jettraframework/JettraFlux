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
    private String currentTheme = "FlatTheme";
    private ColorMode colorMode;
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

    @Override
    public String render(ThemeData theme) {
        // Build the adjacent theme control bar
        DashboardThemeControlBar controlBar = DashboardThemeControlBar.of(currentTheme, colorMode);

        List<Widget> rightSectionChildren = new ArrayList<>(rightItems);
        rightSectionChildren.add(controlBar);

        Widget headerWidget = Top.of(
            Row.of(
                Header.of(4, title).modifier(new Modifier().style("margin: 0; font-weight: 700; color: var(--text-primary, var(--on-surface-color));"))
            ).modifier(new Modifier().style("align-items: center; gap: 12px;")),
            Row.of(
                rightSectionChildren.toArray(new Widget[0])
            ).modifier(new Modifier().style("align-items: center; gap: 14px;"))
        ).modifier(new Modifier().style("justify-content: space-between; width: 100%; border-bottom: 1px solid var(--border, rgba(128,128,128,0.2)); padding: 0 1.5rem;"));

        return headerWidget.render(theme);
    }
}
