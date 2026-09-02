package io.jettra.flux.theme;

public class Themes {

    static {
        ThemeRegistry.registerTheme("FlatTheme", FlatTheme());
        ThemeRegistry.registerTheme("Theme3D", Theme3D());
        ThemeRegistry.registerTheme("FuturisticTheme", FuturisticTheme());
        ThemeRegistry.registerTheme("AstTheme", AstTheme());
        ThemeRegistry.registerTheme("AtlantisTheme", AtlantisTheme());
        ThemeRegistry.registerTheme("OceanTheme", OceanTheme());
        ThemeRegistry.registerTheme("Matrix", Matrix());
        ThemeRegistry.registerTheme("Retro", Retro());
        ThemeRegistry.registerTheme("Dark", Dark());
        ThemeRegistry.registerTheme("DarkTheme", DarkTheme());
    }

    public static ThemeData FlatTheme() {
        return new ThemeData(
            "#2196F3", // primary
            "#FFC107", // secondary
            "#FAFAFA", // background
            "#FFFFFF", // surface
            "#FFFFFF", // onPrimary
            "#212121", // onSurface
            "border: none; border-radius: 4px; padding: 10px 20px; font-weight: 500; cursor: pointer; transition: background 0.3s;", // button
            "border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); padding: 16px; background-color: #FFFFFF;", // card
            "padding: 16px; border-radius: 4px;", // container
            "font-size: 16px; color: #212121;" // text
        );
    }

    public static ThemeData Theme3D() {
        return new ThemeData(
            "#E0E5EC", 
            "#FF9800",
            "#E0E5EC",
            "#E0E5EC",
            "#4D4D4D",
            "#4D4D4D",
            "border: none; border-radius: 12px; padding: 12px 24px; font-weight: bold; color: #4D4D4D; background-color: #E0E5EC; box-shadow: 9px 9px 16px rgb(163,177,198,0.6), -9px -9px 16px rgba(255,255,255, 0.5); cursor: pointer; transition: all 0.2s ease;",
            "border-radius: 20px; padding: 24px; background-color: #E0E5EC; box-shadow: 9px 9px 16px rgb(163,177,198,0.6), -9px -9px 16px rgba(255,255,255, 0.5);",
            "padding: 16px; border-radius: 12px;",
            "font-size: 16px; color: #4D4D4D; text-shadow: 1px 1px 0px #FFF;"
        );
    }

    public static ThemeData FuturisticTheme() {
        return new ThemeData(
            "#00F3FF", // neon blue
            "#FF00E4", // neon pink
            "#090A0F", // deep dark background
            "#12141D", // dark surface
            "#090A0F", // onPrimary (dark text on neon)
            "#00F3FF", // onSurface (neon text on dark)
            "border: 1px solid #00F3FF; border-radius: 0px; padding: 12px 24px; font-weight: bold; font-family: monospace; color: #00F3FF; background: transparent; text-transform: uppercase; box-shadow: 0 0 10px rgba(0,243,255,0.5); cursor: pointer; transition: all 0.2s;",
            "border: 1px solid rgba(255,0,228,0.5); border-radius: 4px; padding: 20px; background: rgba(18,20,29,0.8); box-shadow: inset 0 0 20px rgba(255,0,228,0.1);",
            "padding: 16px;",
            "font-size: 16px; color: #00F3FF; font-family: 'Courier New', Courier, monospace;"
        );
    }

    public static ThemeData AstTheme() {
        return new ThemeData(
            "#8A2BE2", // primary (blue violet)
            "#00CED1", // secondary (dark turquoise)
            "#0B0C10", // background (space dark)
            "#1F2833", // surface
            "#FFFFFF", // onPrimary
            "#C5C6C7", // onSurface
            "border: none; border-radius: 8px; padding: 12px 24px; font-weight: 600; color: #FFFFFF; background: linear-gradient(135deg, #8A2BE2 0%, #4B0082 100%); box-shadow: 0 4px 15px rgba(138,43,226,0.4); cursor: pointer; transition: all 0.3s ease;",
            "border: 1px solid rgba(138,43,226,0.3); border-radius: 12px; padding: 20px; background: #1F2833; box-shadow: 0 8px 32px rgba(0,0,0,0.5); backdrop-filter: blur(10px);",
            "padding: 20px; border-radius: 12px;",
            "font-size: 16px; color: #C5C6C7; font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;"
        );
    }

    public static ThemeData AtlantisTheme() {
        return new ThemeData(
            "#3B82F6", // primary (Atlantis Blue)
            "#6366F1", // secondary (indigo)
            "#F8FAFC", // background (slate 50)
            "#FFFFFF", // surface (white)
            "#FFFFFF", // onPrimary (white text on primary)
            "#334155", // onSurface (slate 700 text on surface)
            "border: none; border-radius: 8px; padding: 12px 24px; font-weight: 600; color: #FFFFFF; background-color: #3B82F6; box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.2), 0 2px 4px -1px rgba(59, 130, 246, 0.1); cursor: pointer; transition: all 0.2s ease-in-out;",
            "border: 1px solid #E2E8F0; border-radius: 16px; padding: 24px; background-color: #FFFFFF; box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -2px rgba(0, 0, 0, 0.025);",
            "padding: 24px; border-radius: 16px;",
            "font-size: 15px; color: #334155; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;"
        );
    }

    public static ThemeData OceanTheme() {
        return new ThemeData(
            "#20d077", // primary (Mint Green / Teal)
            "#06b6d4", // secondary (Cyan)
            "#121212", // background (very dark grey)
            "#1E1E1E", // surface (dark grey for cards/sidebar/topbar)
            "#121212", // onPrimary (dark text on bright green buttons)
            "#E2E8F0", // onSurface (light text)
            "border: none; border-radius: 8px; padding: 12px 24px; font-weight: 600; color: #121212; background-color: #20d077; box-shadow: 0 4px 6px -1px rgba(32, 208, 119, 0.3); cursor: pointer; transition: all 0.2s ease-in-out;",
            "border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 16px; padding: 24px; background-color: #1E1E1E; box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.5);",
            "padding: 24px; border-radius: 16px;",
            "font-size: 15px; color: #E2E8F0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;"
        );
    }

    public static ThemeData MatrixTheme() {
        return MatrixTheme.create();
    }

    public static ThemeData Matrix() {
        return MatrixTheme();
    }

    public static ThemeData RetroTheme() {
        return RetroTheme.create();
    }

    public static ThemeData Retro() {
        return RetroTheme();
    }

    public static ThemeData DarkTheme() {
        return DarkTheme.create();
    }

    public static ThemeData Dark() {
        return DarkTheme();
    }
}
