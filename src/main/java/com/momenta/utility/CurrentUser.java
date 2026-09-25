package com.momenta.utility;

import com.momenta.model.User;

public final class CurrentUser {
    private static User user;
    private CurrentUser() {}

    public static void login(User loggedInUser) { user = loggedInUser; }
    public static User get() { return user; }
    public static boolean isLoggedIn() { return user != null; }
    public static int getId() {
        if (user == null) throw new IllegalStateException("No user is currently logged in");
        return user.getId();
    }
    public static String getName() { return user == null ? "" : user.getName(); }
    public static String getPersona() { return user == null ? "" : user.getPersona(); }
    public static void logout() { user = null; }
}
