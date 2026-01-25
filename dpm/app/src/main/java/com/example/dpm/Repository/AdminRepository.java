package com.example.dpm.Repository;

import com.example.dpm.Model.Admin;
import com.example.dpm.Model.Passenger;
import com.example.dpm.Model.UserRole;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getAdminById(String adminId, OnSuccessListener<Admin> listener) {
        db.collection("users")
                .document(adminId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        listener.onSuccess(null);
                        return;
                    }

                    String role = doc.getString("role");
                    if (!UserRole.ADMIN.name().equals(role)) {
                        listener.onSuccess(null); // nije admin
                        return;
                    }

                    listener.onSuccess(doc.toObject(Admin.class));
                });
    }
}
