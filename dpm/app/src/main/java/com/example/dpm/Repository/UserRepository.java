package com.example.dpm.Repository;

import android.util.Log;

import com.example.dpm.Model.Admin;
import com.example.dpm.Model.Driver;
import com.example.dpm.Model.Passenger;
import com.example.dpm.Model.User;
import com.example.dpm.Model.UserRole;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 🔹 Dobavi sve korisnike (Admin, Driver, Passenger)
    public void getAllUsers(OnSuccessListener<List<User>> listener) {
        db.collection("users")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<User> users = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        User user = mapToConcreteUser(doc);
                        if (user != null) {
                            users.add(user);
                        }
                    }
                    listener.onSuccess(users);
                });
    }

    // 🔹 Dobavi korisnika po ID-u
    public void getUserById(String userId, OnSuccessListener<User> listener) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        listener.onSuccess(null);
                        return;
                    }
                    listener.onSuccess(mapToConcreteUser(doc));
                });
    }

    // 🔹 Dobavi korisnika po email-u
    public void getUserByEmail(String email, OnSuccessListener<User> listener) {
        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        listener.onSuccess(null);
                        return;
                    }
                    listener.onSuccess(mapToConcreteUser(snapshot.getDocuments().get(0)));
                });
    }

    // 🔧 Interna metoda – mapiranje po role
    private User mapToConcreteUser(DocumentSnapshot doc) {

        String roleStr = doc.getString("role");
        if (roleStr == null) return null;

        UserRole role = UserRole.valueOf(roleStr);

        User user;
        switch (role) {
            case ADMIN:
                user = doc.toObject(Admin.class);
                break;
            case DRIVER:
                user = doc.toObject(Driver.class);
                break;
            default:
                user = doc.toObject(Passenger.class);
        }

        if (user != null) {
            user.setId(doc.getId());
        }
        return user;
    }
}

