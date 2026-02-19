package com.example.dpm.Repository;

import static java.security.AccessController.getContext;

import android.util.Log;
import android.widget.Toast;

import com.example.dpm.Model.Driver;
import com.example.dpm.Model.FirebaseProvider;
import com.example.dpm.Model.UserRole;
import com.example.dpm.Model.Vehicle;
import com.example.dpm.Model.VehicleType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VehicleRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    public void getAllVehicles(OnSuccessListener<List<Vehicle>> listener) {
        db.collection("vehicles").get().addOnSuccessListener(snapshot ->
                listener.onSuccess(snapshot.toObjects(Vehicle.class))
        );
    }

    public void getVehicleById(String vehicleId, OnSuccessListener<Vehicle> listener) {
        db.collection("vehicles")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Vehicle> vehicles = snapshot.toObjects(Vehicle.class);

                    for (Vehicle v : vehicles) {
                        if (v.getId() != null && v.getId().trim().equals(vehicleId.trim())) {
                            listener.onSuccess(v);
                            return;
                        }
                    }

                    listener.onSuccess(null);
                });
    }

    public void getVehiclesByUserId(String userId, OnSuccessListener<List<Vehicle>> listener) {
        db.collection("vehicles")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot ->
                        listener.onSuccess(snapshot.toObjects(Vehicle.class))
                );
    }
    public void getVehicleByDriverId(String driverId, OnSuccessListener<Vehicle> listener) {
        db.collection("vehicles")
                .whereEqualTo("driverId", driverId)
                .limit(1)
                .get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) {
                        listener.onSuccess(null);
                    } else {
                        listener.onSuccess(qs.getDocuments().get(0).toObject(Vehicle.class));
                    }
                });
    }



    public void updateVehicle(Vehicle vehicle) {
        db.collection("vehicles")
                .document(vehicle.getId())
                .set(vehicle);
    }

    public void addVehicle(Vehicle vehicle) {
        db.collection("vehicles")
                .add(vehicle)
                .addOnSuccessListener(docRef -> {
                    vehicle.setId(docRef.getId());
                });
    }

    public void getEligibleVehicles(VehicleType vehicleType,
                                    boolean needsBaby,
                                    boolean needsPet,
                                    int numberOfPassengers,
                                    OnSuccessListener<List<Vehicle>> listener) {

        db.collection("vehicles")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Vehicle> filtered = new ArrayList<>();

                    for (Vehicle v : snapshot.toObjects(Vehicle.class)) {
                        if(v.getType() == vehicleType) {
                            if(v.isPetFriendly() == needsPet) {
                                if(v.isBabyFriendly() == needsBaby) {
                                    if(v.getSeats() >= numberOfPassengers + 1) {
                                        filtered.add(v);
                                    }
                                }
                            }
                        }
                    }

                    listener.onSuccess(filtered);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE_ERROR", e.getMessage(), e);
                });

    }

}
