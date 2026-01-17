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


        public static void seedVehicles() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Vehicle v1 = new Vehicle(
                UUID.randomUUID().toString(),
                "VW Golf GTD",
                44.7890, 20.5500,
                false,
                "BG 453 ZA"
        );

        Vehicle v2 = new Vehicle(
                UUID.randomUUID().toString(),
                "Audi RS4 B5",
              44.7320, 20.5000,
                true,
                "BG 764 ZK"
        );

        db.collection("vehicles")
                .document(v1.getId())
                .set(v1);

        db.collection("vehicles")
                .document(v2.getId())
                .set(v2);
    }

    public void getAllVehicles(OnSuccessListener<List<Vehicle>> listener) {
        db.collection("vehicles").get().addOnSuccessListener(snapshot ->
                listener.onSuccess(snapshot.toObjects(Vehicle.class))
        );
    }
}
