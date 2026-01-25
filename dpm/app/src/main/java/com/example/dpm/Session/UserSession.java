package com.example.dpm.Session;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.dpm.Model.User;
import com.example.dpm.Model.UserRole;

public class UserSession {

    private static UserSession instance;
    private User currentUser;

    private static final String PREFS_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // 👉 Pozvati nakon uspešnog logina
    public void login(Context context, User user) {
        this.currentUser = user;

        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        prefs.edit()
                .putString(KEY_USER_ID, user.getId())
                .putString(KEY_EMAIL, user.getEmail())
                .putString(KEY_ROLE, user.getRole().name())
                .apply();
    }

    // 👉 Učitavanje sesije pri startu aplikacije
    public void restoreSession(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String userId = prefs.getString(KEY_USER_ID, null);
        if (userId == null) return;

        User user = new User();
        user.setId(userId);
        user.setEmail(prefs.getString(KEY_EMAIL, ""));
        user.setRole(UserRole.valueOf(
                prefs.getString(KEY_ROLE, "USER")
        ));

        currentUser = user;
    }

    public User getUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // 👉 Logout
    public void logout(Context context) {
        currentUser = null;
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}
