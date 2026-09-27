package com.momenta.utility;

import java.util.prefs.Preferences;

/**
 * Section 30 (Settings) / login convenience: remembers which user checked
 * "Remember me" on the Login screen so MOMENTA can skip the Login/Register
 * screens on the next launch instead of asking the user to sign in every
 * single time the app is opened.
 *
 * This intentionally lives in its OWN global Preferences node rather than
 * the per-user node SettingsService uses (SettingsService.prefs() keys off
 * CurrentUser.getId(), but at the point this is read on startup no user is
 * logged in yet — that's the whole problem it solves).
 *
 * Only a user id is persisted, never a password. On launch, Main looks this
 * id up through UserService/UserDAO (a normal SELECT by primary key) and
 * logs that user in automatically; nothing is stored that could be replayed
 * to authenticate anywhere else.
 */
public final class SessionStore {

    private static final String NODE = "com.momenta.session";
    private static final String REMEMBERED_USER_ID = "rememberedUserId";
    private static final int NONE = -1;

    private SessionStore() {
    }

    private static Preferences prefs() {
        return Preferences.userRoot().node(NODE);
    }

    /** Called after a successful login/registration when "Remember me" is checked. */
    public static void remember(int userId) {
        prefs().putInt(REMEMBERED_USER_ID, userId);
    }

    /** Called on Logout, and whenever "Remember me" is left unchecked at login. */
    public static void forget() {
        prefs().remove(REMEMBERED_USER_ID);
    }

    /** @return the remembered user id, or -1 if no one asked to stay logged in. */
    public static int getRememberedUserId() {
        return prefs().getInt(REMEMBERED_USER_ID, NONE);
    }

    public static boolean hasRememberedUser() {
        return getRememberedUserId() != NONE;
    }
}
