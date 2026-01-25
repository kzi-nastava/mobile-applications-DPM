package com.example.dpm.Session;

import com.example.dpm.Model.User;
import com.google.firebase.auth.FirebaseAuth;

public class UserSession {

    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Poziva se NAKON uspešnog login-a i Firestore fetch-a
    public void setUser(User user) {
        this.currentUser = user;
    }

    public User getUser() {
        return currentUser;
    }

    // Login stanje uvek proverava FirebaseAuth
    public boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public void clear() {
        currentUser = null;
    }
}
