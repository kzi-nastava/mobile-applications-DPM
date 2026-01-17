package com.example.dpm.Model;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseProvider {

    private static FirebaseFirestore db;

    private FirebaseProvider() {}

    public static FirebaseFirestore getDb() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }
}