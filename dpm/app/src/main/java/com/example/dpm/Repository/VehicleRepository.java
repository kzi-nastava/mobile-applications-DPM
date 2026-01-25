package com.example.dpm.Repository;

import com.example.dpm.Model.FirebaseProvider;
import com.example.dpm.Model.Vehicle;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.util.GeoPoint;

import java.util.List;
import java.util.UUID;

public class VehicleRepository {

    private final FirebaseFirestore db = FirebaseProvider.getDb();


    public void getAllVehicles(OnSuccessListener<List<Vehicle>> listener) {
        db.collection("vehicles").get().addOnSuccessListener(snapshot ->
                listener.onSuccess(snapshot.toObjects(Vehicle.class))
        );
    }

    public void getVehiclesByUserId(String userId, OnSuccessListener<List<Vehicle>> listener) {
        db.collection("vehicles")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot ->
                        listener.onSuccess(snapshot.toObjects(Vehicle.class))
                );
    }
}
