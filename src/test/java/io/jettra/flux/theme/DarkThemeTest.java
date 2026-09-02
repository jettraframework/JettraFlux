package io.jettra.flux.theme;

import io.jettra.flux.widgets.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DarkThemeTest {

    @Test
    public void testDarkThemeData() {
        ThemeData theme = DarkTheme.create();
        assertNotNull(theme);
        assertEquals("#6366f1", theme.primaryColor);
        assertEquals("#090d16", theme.backgroundColor);
        assertEquals("#ffffff", theme.onPrimaryColor);
        assertEquals("#f8fafc", theme.onSurfaceColor);
        assertNotNull(theme.customCss);
        assertNotNull(theme.customJs);
        assertTrue(theme.generateGlobalCss().contains("--primary-color: #6366f1;"));
        assertTrue(theme.generateGlobalCss().contains("--dark-bg: #090d16;"));
    }

    @Test
    public void testThemesFactoryMethods() {
        ThemeData theme1 = Themes.DarkTheme();
        ThemeData theme2 = Themes.Dark();
        assertNotNull(theme1);
        assertNotNull(theme2);
        assertEquals("#6366f1", theme1.primaryColor);
        assertEquals("#6366f1", theme2.primaryColor);
    }

    @Test
    public void testThemeRegistryLookup() {
        ThemeData t1 = ThemeRegistry.getTheme("DarkTheme");
        ThemeData t2 = ThemeRegistry.getTheme("Dark");
        ThemeData t3 = ThemeRegistry.getTheme("dark");
        ThemeData t4 = ThemeRegistry.getTheme("darktheme");

        assertNotNull(t1, "DarkTheme should be resolved by ThemeRegistry");
        assertNotNull(t2, "Dark should be resolved by ThemeRegistry");
        assertNotNull(t3, "dark (case-insensitive) should be resolved by ThemeRegistry");
        assertNotNull(t4, "darktheme (case-insensitive) should be resolved by ThemeRegistry");
        assertEquals("#6366f1", t1.primaryColor);
    }

    @Test
    public void testThemeChangedRenderingWithDark() {
        ThemeChanged widget = ThemeChanged.of().current("Dark");
        String html = widget.render(Themes.DarkTheme());
        assertNotNull(html);
        assertTrue(html.contains("DarkTheme") || html.contains("Dark"));
        assertTrue(html.contains("changeJettraTheme"));
    }

    @Test
    public void testWidgetsRenderingWithDarkTheme() {
        ThemeData dark = Themes.DarkTheme();

        // Button
        Button btn = Button.of("Save");
        String btnHtml = btn.render(dark);
        assertTrue(btnHtml.contains("<button"));
        assertTrue(btnHtml.contains("espresso-button"));

        // Card
        Card card = Card.of(Text.of("Dark Card"));
        String cardHtml = card.render(dark);
        assertTrue(cardHtml.contains("espresso-card"));

        // TextField
        TextField tf = TextField.of("name", "Enter your name");
        String tfHtml = tf.render(dark);
        assertTrue(tfHtml.contains("espresso-textfield") || tfHtml.contains("<input"));

        // Datatable
        Datatable table = Datatable.of(java.util.List.of("Column"), java.util.List.of(java.util.List.of("Row1")));
        String tableHtml = table.render(dark);
        assertTrue(tableHtml.contains("espresso-datatable") || tableHtml.contains("<table"));

        // Scaffold
        Scaffold scaffold = Scaffold.of().body(card);
        String scaffoldHtml = scaffold.render(dark);
        assertTrue(scaffoldHtml.contains("jettra-scaffold-layout"));
        assertTrue(scaffoldHtml.contains("--primary-color: #6366f1;"));
    }
}
