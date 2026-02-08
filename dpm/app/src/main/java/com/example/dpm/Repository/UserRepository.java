package com.example.dpm.Repository;

import android.util.Log;

import com.example.dpm.Model.Admin;
import com.example.dpm.Model.Driver;
import com.example.dpm.Model.Passenger;
import com.example.dpm.Model.User;
import com.example.dpm.Model.UserRole;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void updateUserData(String userId,
                               String city,
                               String country,
                               String street,
                               String number,
                               String email,
                               String firstName,
                               String lastName,
                               String phoneNumber,
                               OnSuccessListener<Void> onSuccess,
                               OnFailureListener onFailure) {

        Map<String, Object> updates = new HashMap<>();

        updates.put("city", city);
        updates.put("country", country);
        updates.put("street", street);
        updates.put("number", number);
        updates.put("email", email);
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("phoneNumber", phoneNumber);

        db.collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void reauthenticateAndChangePassword(String email,
                                                String oldPassword,
                                                String newPassword,
                                                OnSuccessListener<Void> onSuccess,
                                                OnFailureListener onFailure) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            onFailure.onFailure(new Exception("User not logged in"));
            return;
        }

        AuthCredential credential =
                EmailAuthProvider.getCredential(email, oldPassword);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid ->
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(onSuccess)
                                .addOnFailureListener(onFailure)
                )
                .addOnFailureListener(onFailure);
    }


}

