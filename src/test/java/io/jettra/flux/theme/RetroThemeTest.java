package io.jettra.flux.theme;

import io.jettra.flux.widgets.Button;
import io.jettra.flux.widgets.Card;
import io.jettra.flux.widgets.JettraButton;
import io.jettra.flux.widgets.Paragraph;
import io.jettra.flux.widgets.TextField;
import io.jettra.flux.widgets.ThemeChanged;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RetroThemeTest {

    @Test
    public void testRetroThemeRegistration() {
        ThemeData themeFromRegistry = ThemeRegistry.getTheme("Retro");
        assertNotNull(themeFromRegistry, "Retro theme should be registered in ThemeRegistry");

        ThemeData themeAlias = ThemeRegistry.getTheme("RetroTheme");
        assertNotNull(themeAlias, "RetroTheme alias should be registered in ThemeRegistry");

        ThemeData retroThemeDirect = Themes.RetroTheme();
        assertNotNull(retroThemeDirect, "Themes.RetroTheme() should return a valid ThemeData");

        assertEquals("#5c8e32", retroThemeDirect.primaryColor);
        assertEquals("#242220", retroThemeDirect.backgroundColor);
        assertEquals("#343236", retroThemeDirect.surfaceColor);
    }

    @Test
    public void testGlobalCssGeneration() {
        ThemeData retroTheme = Themes.RetroTheme();
        String css = retroTheme.generateGlobalCss();

        assertNotNull(css);
        assertTrue(css.contains("Pixelify Sans"), "Should contain pixelated font reference");
        assertTrue(css.contains("--primary-color: #5c8e32"), "Should define Minecraft green primary color");
        assertTrue(css.contains("espresso-btn"), "Should style espresso buttons");
        assertTrue(css.contains("espresso-card"), "Should style espresso cards");
    }

    @Test
    public void testThemeChangedRendering() {
        ThemeData retroTheme = Themes.RetroTheme();
        ThemeChanged widget = ThemeChanged.of().current("Retro");
        String html = widget.render(retroTheme);

        assertNotNull(html);
        assertTrue(html.contains("⛏️"), "ThemeChanged should contain the Minecraft pickaxe icon for Retro theme");
        assertTrue(html.contains("Retro"), "ThemeChanged menu should include Retro option");
        assertTrue(html.contains("changeJettraTheme('Retro')"), "ThemeChanged should invoke changeJettraTheme with Retro");
    }

    @Test
    public void testWidgetRenderingWithRetroTheme() {
        ThemeData retroTheme = Themes.RetroTheme();

        String btnHtml = Button.of("Craft Item").render(retroTheme);
        assertNotNull(btnHtml);
        assertTrue(btnHtml.contains("Craft Item"));
        assertTrue(btnHtml.contains("espresso-button"));

        String jettraBtnHtml = JettraButton.of("Mine Block").severity(JettraButton.Severity.PRIMARY).render(retroTheme);
        assertNotNull(jettraBtnHtml);
        assertTrue(jettraBtnHtml.contains("Mine Block"));
        assertTrue(jettraBtnHtml.contains("espresso-btn-primary"));

        String cardHtml = Card.of(Paragraph.of("Inventory Slot")).render(retroTheme);
        assertNotNull(cardHtml);
        assertTrue(cardHtml.contains("Inventory Slot"));
        assertTrue(cardHtml.contains("espresso-card"));

        String tfHtml = TextField.of("playerName", "Steve").render(retroTheme);
        assertNotNull(tfHtml);
        assertTrue(tfHtml.contains("espresso-textfield"));
        assertTrue(tfHtml.contains("Steve"));
    }
}
