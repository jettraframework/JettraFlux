package io.jettra.flux.theme;

import io.jettra.flux.widgets.ThemeToggle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

public class ThemeTokensAndToggleTest {

    @ParameterizedTest
    @EnumSource(JettraTheme.class)
    @DisplayName("Every JettraTheme enum constant provides non-null WCAG compliant ThemeTokens in WHITE and DARK")
    public void testThemeTokensCompleteness(JettraTheme theme) {
        for (ColorMode mode : ColorMode.values()) {
            ThemeTokens tokens = theme.tokens(mode);
            assertNotNull(tokens, "Tokens for " + theme + " in mode " + mode + " must not be null");
            assertNotNull(tokens.surfaceBackground(), "surfaceBackground must not be null");
            assertNotNull(tokens.cardBackground(), "cardBackground must not be null");
            assertNotNull(tokens.textPrimary(), "textPrimary must not be null");
            assertNotNull(tokens.textSecondary(), "textSecondary must not be null");
            assertNotNull(tokens.border(), "border must not be null");
            assertNotNull(tokens.accentPrimary(), "accentPrimary must not be null");
            assertNotNull(tokens.accentSecondary(), "accentSecondary must not be null");
            assertNotNull(tokens.focusRing(), "focusRing must not be null");
            assertNotNull(tokens.iconColor(), "iconColor must not be null");

            // Legibility sanity checks (text and surface should never be identical)
            assertNotEquals(tokens.textPrimary().toLowerCase(), tokens.surfaceBackground().toLowerCase(),
                "Text primary must not match surface background in " + theme + " (" + mode + ")");
            assertNotEquals(tokens.textPrimary().toLowerCase(), tokens.cardBackground().toLowerCase(),
                "Text primary must not match card background in " + theme + " (" + mode + ")");

            // CSS variable generation
            String cssVars = tokens.toCssVariables();
            assertTrue(cssVars.contains("--surface-background:"));
            assertTrue(cssVars.contains("--card-background:"));
            assertTrue(cssVars.contains("--text-primary:"));
            assertTrue(cssVars.contains("--text-secondary:"));
            assertTrue(cssVars.contains("--border:"));
            assertTrue(cssVars.contains("--accent-primary:"));
            assertTrue(cssVars.contains("--accent-secondary:"));
            assertTrue(cssVars.contains("--focus-ring:"));
            assertTrue(cssVars.contains("--icon-color:"));
        }
    }

    @ParameterizedTest
    @EnumSource(JettraTheme.class)
    @DisplayName("Every JettraTheme produces valid ThemeData injecting semantic tokens into global CSS")
    public void testThemeDataGeneration(JettraTheme theme) {
        for (ColorMode mode : ColorMode.values()) {
            ThemeData data = theme.create(mode);
            assertNotNull(data, "ThemeData for " + theme + " in " + mode + " must not be null");
            assertEquals(mode, data.getColorMode(), "ThemeData colorMode must match requested mode");
            assertNotNull(data.getTokens(), "ThemeData must contain ThemeTokens");

            String globalCss = data.generateGlobalCss();
            assertTrue(globalCss.contains("--surface-background:"), "Global CSS must contain --surface-background");
            assertTrue(globalCss.contains("--text-primary:"), "Global CSS must contain --text-primary");
            assertTrue(globalCss.contains("--accent-primary:"), "Global CSS must contain --accent-primary");
        }
    }

    @Test
    @DisplayName("Canonical refactored classes SL, Core, Heroes provide full functionality and backwards compatibility")
    public void testRefactoredCanonicalThemes() {
        // SL
        ThemeData slWhite = SL.create(ColorMode.WHITE);
        ThemeData slDark = SL.create(ColorMode.DARK);
        assertNotNull(slWhite);
        assertNotNull(slDark);
        assertEquals("#0f172a", slWhite.getTokens().textPrimary());
        assertEquals("#f1f5f9", slDark.getTokens().textPrimary());
        // SLTheme alias delegation
        assertEquals(slDark.primaryColor, SLTheme.create().primaryColor);
        assertEquals(slWhite.getTokens().accentPrimary(), SLTheme.create(ColorMode.WHITE).getTokens().accentPrimary());

        // Core
        ThemeData coreWhite = Core.create(ColorMode.WHITE);
        ThemeData coreDark = Core.create(ColorMode.DARK);
        assertNotNull(coreWhite);
        assertNotNull(coreDark);
        assertEquals("#0f172a", coreWhite.getTokens().textPrimary());
        assertEquals("#f1f5f9", coreDark.getTokens().textPrimary());
        // CoreTheme alias delegation
        assertEquals(coreDark.primaryColor, CoreTheme.create().primaryColor);
        assertEquals(coreWhite.getTokens().accentPrimary(), CoreTheme.create(ColorMode.WHITE).getTokens().accentPrimary());

        // Heroes
        ThemeData heroesWhite = Heroes.create(ColorMode.WHITE);
        ThemeData heroesDark = Heroes.create(ColorMode.DARK);
        assertNotNull(heroesWhite);
        assertNotNull(heroesDark);
        assertEquals("#111827", heroesWhite.getTokens().textPrimary());
        assertEquals("#f9fafb", heroesDark.getTokens().textPrimary());
        // HeroesTheme alias delegation
        assertEquals(heroesDark.primaryColor, HeroesTheme.create().primaryColor);
        assertEquals(heroesWhite.getTokens().accentPrimary(), HeroesTheme.create(ColorMode.WHITE).getTokens().accentPrimary());
    }

    @Test
    @DisplayName("ColorMode toggles and string resolution work accurately")
    public void testColorModeUtilities() {
        assertEquals(ColorMode.DARK, ColorMode.WHITE.toggle());
        assertEquals(ColorMode.WHITE, ColorMode.DARK.toggle());
        assertTrue(ColorMode.DARK.isDark());
        assertTrue(ColorMode.WHITE.isWhite());

        assertEquals(ColorMode.WHITE, ColorMode.fromString("white", ColorMode.DARK));
        assertEquals(ColorMode.WHITE, ColorMode.fromString("light", ColorMode.DARK));
        assertEquals(ColorMode.DARK, ColorMode.fromString("dark", ColorMode.WHITE));
        assertEquals(ColorMode.DARK, ColorMode.fromString("night", ColorMode.WHITE));
        assertEquals(ColorMode.DARK, ColorMode.fromString("invalid", ColorMode.DARK));
    }

    @Test
    @DisplayName("ThemeRegistry resolves canonical names and modes accurately")
    public void testThemeRegistry() {
        ThemeData sl = ThemeRegistry.getTheme("SL", ColorMode.WHITE);
        assertNotNull(sl);
        assertEquals(ColorMode.WHITE, sl.getColorMode());

        ThemeData matrix = ThemeRegistry.getTheme("Matrix", ColorMode.DARK);
        assertNotNull(matrix);
        assertEquals(ColorMode.DARK, matrix.getColorMode());

        ThemeData dark = ThemeRegistry.getTheme(JettraTheme.DARK_THEME, ColorMode.WHITE);
        assertNotNull(dark);
        assertEquals(ColorMode.WHITE, dark.getColorMode());
    }

    @Test
    @DisplayName("ThemeToggle renders Moon icon in WHITE mode and Sun icon in DARK mode with persistence script")
    public void testThemeToggleRendering() {
        ThemeData darkTheme = Themes.DarkTheme(ColorMode.DARK);
        ThemeData whiteTheme = Themes.DarkTheme(ColorMode.WHITE);

        // In Dark mode: must show Sun icon inviting user to switch to light mode
        ThemeToggle toggleDark = ThemeToggle.of().colorMode(ColorMode.DARK);
        String htmlDark = toggleDark.render(darkTheme);
        assertNotNull(htmlDark);
        assertTrue(htmlDark.contains("jettra-theme-icon-sun"), "Must render Sun icon in Dark mode");
        assertTrue(htmlDark.contains("Switch to Light Mode"));
        assertTrue(htmlDark.contains("data-next-mode=\"white\""));
        assertTrue(htmlDark.contains("toggleJettraColorMode"));
        assertTrue(htmlDark.contains("jettra_color_mode"));

        // In White mode: must show Moon icon inviting user to switch to dark mode
        ThemeToggle toggleWhite = ThemeToggle.of().colorMode(ColorMode.WHITE);
        String htmlWhite = toggleWhite.render(whiteTheme);
        assertNotNull(htmlWhite);
        assertTrue(htmlWhite.contains("jettra-theme-icon-moon"), "Must render Moon icon in White mode");
        assertTrue(htmlWhite.contains("Switch to Dark Mode"));
        assertTrue(htmlWhite.contains("data-next-mode=\"dark\""));
    }
}
