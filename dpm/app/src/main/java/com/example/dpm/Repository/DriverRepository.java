package com.example.dpm.Repository;


import com.example.dpm.Model.Driver;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DriverRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addDriver(Driver driver) {
        db.collection("drivers").document(driver.id).set(driver);
    }

    public void getAllDrivers(OnSuccessListener<List<Driver>> listener) {
        db.collection("drivers").get().addOnSuccessListener(snapshot ->
                listener.onSuccess(snapshot.toObjects(Driver.class))
        );
    }

    public void getDriverById(String driverId, OnSuccessListener<Driver> listener) {
        db.collection("drivers").document(driverId).get().addOnSuccessListener(snapshot ->
                listener.onSuccess(snapshot.toObject(Driver.class))
        );
    }

}