package com.momenta.service;

import com.momenta.utility.CurrentUser;

import java.util.prefs.Preferences;

/**
 * Phase 23 - user-specific MOMENTA personalization.
 * Preferences are stored locally and keyed by the logged-in user id, so
 * changing one account's appearance does not change another account's settings.
 */
public final class SettingsService {
    private static final String NODE = "com.momenta.personalization";

    private static final String THEME = "theme";
    private static final String ACCENT = "accent";
    private static final String FONT_SCALE = "fontScale";
    private static final String ANIMATIONS = "animations";
    private static final String COMPACT = "compact";
    private static final String LANDING_VIEW = "landingView";

    private SettingsService() {}

    private static Preferences prefs() {
        String userKey = CurrentUser.isLoggedIn() ? String.valueOf(CurrentUser.getId()) : "guest";
        return Preferences.userRoot().node(NODE).node(userKey);
    }

    public static String getTheme() { return prefs().get(THEME, "Light"); }
    public static void setTheme(String value) { prefs().put(THEME, value == null ? "Light" : value); }

    public static String getAccent() { return prefs().get(ACCENT, "Sea Green"); }
    public static void setAccent(String value) { prefs().put(ACCENT, value == null ? "Sea Green" : value); }

    public static double getFontScale() { return prefs().getDouble(FONT_SCALE, 1.0); }
    public static void setFontScale(double value) { prefs().putDouble(FONT_SCALE, clamp(value, 0.85, 1.25)); }

    public static boolean isAnimationsEnabled() { return prefs().getBoolean(ANIMATIONS, true); }
    public static void setAnimationsEnabled(boolean value) { prefs().putBoolean(ANIMATIONS, value); }

    public static boolean isCompactMode() { return prefs().getBoolean(COMPACT, false); }
    public static void setCompactMode(boolean value) { prefs().putBoolean(COMPACT, value); }

    public static String getLandingView() { return prefs().get(LANDING_VIEW, "Dashboard"); }
    public static void setLandingView(String value) { prefs().put(LANDING_VIEW, value == null ? "Dashboard" : value); }

    public static void resetDefaults() {
        Preferences p = prefs();
        p.put(THEME, "Light");
        p.put(ACCENT, "Sea Green");
        p.putDouble(FONT_SCALE, 1.0);
        p.putBoolean(ANIMATIONS, true);
        p.putBoolean(COMPACT, false);
        p.put(LANDING_VIEW, "Dashboard");
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
