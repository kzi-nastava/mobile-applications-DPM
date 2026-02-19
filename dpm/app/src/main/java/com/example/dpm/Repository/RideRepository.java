package com.example.dpm.Repository;


import com.example.dpm.Model.Ride;
import com.example.dpm.Model.RideStatus;
import com.google.android.gms.tasks.OnFailureListener;
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

    public void addRide(Ride ride,
                        OnSuccessListener<Void> onSuccess,
                        OnFailureListener onFailure) {

        db.collection("ride")
                .document() // generise ID
                .set(ride)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getAllRides(OnSuccessListener<List<Ride>> listener) {
        db.collection("ride").get().addOnSuccessListener(snapshot ->
                listener.onSuccess(snapshot.toObjects(Ride.class))
        );
    }
    public void getPastRidesByDriver(String driverId, OnSuccessListener<List<Ride>> listener) {
            db.collection("ride").whereEqualTo("driverId", driverId).whereEqualTo("status", RideStatus.FINISHED).get()
                    .addOnSuccessListener(snapshot -> {

                        List<Ride> result = snapshot.toObjects(Ride.class);
                        listener.onSuccess(result);
                    });
    }

    public void getActiveRideForDriver(String driverId, OnSuccessListener<Ride> listener) {
        db.collection("ride").whereEqualTo("driverId", driverId).whereEqualTo("status", "STARTED").limit(1).get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) listener.onSuccess(null);
                    else listener.onSuccess(qs.getDocuments().get(0).toObject(Ride.class));
                });
    }

    public void hasScheduledRideInNext5Hours(String driverId,
                                             OnSuccessListener<Boolean> listener) {

        long now = System.currentTimeMillis();
        long fiveHoursLater = now + (5 * 60 * 60 * 1000);

        db.collection("ride")
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("status", RideStatus.ACCEPTED.name())
                .whereGreaterThanOrEqualTo("startTime", now)
                .whereLessThanOrEqualTo("startTime", fiveHoursLater)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        listener.onSuccess(false);
                    } else {
                        listener.onSuccess(true);
                    }
                });
    }


}