package com.example.dpm.Repository;

import android.util.Log;
import static java.security.AccessController.getContext;


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

    public void setVehicleBusy(String vehicleIdRaw,
                               boolean busy,
                               OnSuccessListener<Void> onSuccess,
                               OnFailureListener onFailure) {

        if (vehicleIdRaw == null) {
            Log.e("VEHICLE", "vehicleId is NULL");
            if (onFailure != null) onFailure.onFailure(new Exception("vehicleId is null"));
            return;
        }

        String raw = vehicleIdRaw;
        String trimmed = vehicleIdRaw.trim();

        Log.d("VEHICLE", "setVehicleBusy raw:     len=" + raw.length() +
                " -> '" + raw.replace("\n", "\\n").replace("\r", "\\r") + "'");
        Log.d("VEHICLE", "setVehicleBusy trimmed: len=" + trimmed.length() +
                " -> '" + trimmed + "'");

        // Helper koji pokušava update za dati docId
        java.util.function.Consumer<String> tryUpdate = (docId) -> {
            Log.d("VEHICLE", "Trying docId = '" + docId.replace("\n","\\n").replace("\r","\\r") + "'");

            db.collection("vehicles").document(docId).get()
                    .addOnSuccessListener(doc -> {
                        Log.d("VEHICLE", "Exists(" + docId.replace("\n","\\n").replace("\r","\\r") + ") = " + doc.exists());

                        if (!doc.exists()) {
                            // samo signalizujemo da nije nađen, ne zovemo onFailure ovde
                            // jer možda treba probati drugi docId
                            return;
                        }

                        doc.getReference().update("busy", busy)
                                .addOnSuccessListener(unused -> {
                                    Log.d("VEHICLE", "busy updated OK to " + busy +
                                            " for docId='" + docId.replace("\n","\\n").replace("\r","\\r") + "'");
                                    if (onSuccess != null) onSuccess.onSuccess(null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("VEHICLE", "Update FAILED for docId='" +
                                            docId.replace("\n","\\n").replace("\r","\\r") + "'", e);
                                    if (onFailure != null) onFailure.onFailure(e);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("VEHICLE", "GET FAILED for docId='" +
                                docId.replace("\n","\\n").replace("\r","\\r") + "'", e);
                        if (onFailure != null) onFailure.onFailure(e);
                    });
        };

        // 1) probaj trimmed prvo (najčešći slučaj)
        db.collection("vehicles").document(trimmed).get()
                .addOnSuccessListener(docTrim -> {
                    if (docTrim.exists()) {
                        Log.d("VEHICLE", "Using TRIMMED docId");
                        docTrim.getReference().update("busy", busy)
                                .addOnSuccessListener(unused -> {
                                    Log.d("VEHICLE", "busy updated OK to " + busy + " (TRIMMED)");
                                    if (onSuccess != null) onSuccess.onSuccess(null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("VEHICLE", "Update FAILED (TRIMMED)", e);
                                    if (onFailure != null) onFailure.onFailure(e);
                                });
                        return;
                    }

                    // 2) ako trimmed nije nađen, probaj raw (ako je različit)
                    if (raw.equals(trimmed)) {
                        Exception e = new Exception("Vehicle document NOT FOUND for id='" + trimmed + "'");
                        Log.e("VEHICLE", e.getMessage());
                        if (onFailure != null) onFailure.onFailure(e);
                        return;
                    }

                    Log.d("VEHICLE", "TRIMMED not found, trying RAW docId");
                    db.collection("vehicles").document(raw).get()
                            .addOnSuccessListener(docRaw -> {
                                if (!docRaw.exists()) {
                                    Exception e = new Exception(
                                            "Vehicle document NOT FOUND for TRIMMED nor RAW. " +
                                                    "trimmed='" + trimmed + "', raw='" +
                                                    raw.replace("\n","\\n").replace("\r","\\r") + "'"
                                    );
                                    Log.e("VEHICLE", e.getMessage());
                                    if (onFailure != null) onFailure.onFailure(e);
                                    return;
                                }

                                docRaw.getReference().update("busy", busy)
                                        .addOnSuccessListener(unused -> {
                                            Log.d("VEHICLE", "busy updated OK to " + busy + " (RAW)");
                                            if (onSuccess != null) onSuccess.onSuccess(null);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("VEHICLE", "Update FAILED (RAW)", e);
                                            if (onFailure != null) onFailure.onFailure(e);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("VEHICLE", "GET FAILED (RAW)", e);
                                if (onFailure != null) onFailure.onFailure(e);
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e("VEHICLE", "GET FAILED (TRIMMED)", e);
                    if (onFailure != null) onFailure.onFailure(e);
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
