package com.example.dpm.Repository;


import com.example.dpm.Model.Driver;
import com.example.dpm.Model.UserRole;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DriverRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addDriver(Driver driver) {
        driver.setRole(UserRole.DRIVER); // obavezno
        db.collection("users")
                .document(driver.getId())
                .set(driver);
    }

    public void getAllDrivers(OnSuccessListener<List<Driver>> listener) {
        db.collection("users")
                .whereEqualTo("role", UserRole.DRIVER.name())
                .get()
                .addOnSuccessListener(snapshot ->
                        listener.onSuccess(snapshot.toObjects(Driver.class))
                );
    }

    public void getDriverById(String driverId, OnSuccessListener<Driver> listener) {
        db.collection("users")
                .document(driverId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        listener.onSuccess(null);
                        return;
                    }

                    String role = doc.getString("role");
                    if (!UserRole.DRIVER.name().equals(role)) {
                        listener.onSuccess(null); // nije driver
                        return;
                    }

                    listener.onSuccess(doc.toObject(Driver.class));
                });
    }
}
