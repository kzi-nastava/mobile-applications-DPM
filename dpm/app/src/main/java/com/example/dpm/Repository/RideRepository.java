package com.example.dpm.Repository;


import com.example.dpm.Model.Ride;
import com.example.dpm.Model.RideStatus;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RideRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addRide(Ride ride) {
        db.collection("ride").document(ride.getId()).set(ride);
    }

    public void getAllRides(OnSuccessListener<List<Ride>> listener) {
        db.collection("ride").get().addOnSuccessListener(snapshot ->
                listener.onSuccess(snapshot.toObjects(Ride.class))
        );
    }
    public void getPastRidesByDriver(String driverId, OnSuccessListener<List<Ride>> listener) {
            db.collection("ride").whereEqualTo("driverId", driverId).whereEqualTo("status", RideStatus.FINISHED)
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        List<Ride> result = snapshot.toObjects(Ride.class);
                        listener.onSuccess(result);
                    });
    }

}