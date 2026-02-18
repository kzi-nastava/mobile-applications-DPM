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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
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
    public void getPastRidesByPassenger(String passengerId, OnSuccessListener<List<Ride>> listener) {

        var q1 = db.collection("ride")
                .whereEqualTo("passengerId", passengerId)
                .whereEqualTo("status", RideStatus.FINISHED)
                .get();

        var q2 = db.collection("ride")
                .whereArrayContains("linkedPassengerIds", passengerId)
                .whereEqualTo("status", RideStatus.FINISHED)
                .get();

        Tasks.whenAllSuccess(q1, q2).addOnSuccessListener(results -> {

            Map<String, Ride> unique = new HashMap<>();

            QuerySnapshot s1 = (QuerySnapshot) results.get(0);
            QuerySnapshot s2 = (QuerySnapshot) results.get(1);

            for (Ride r : s1.toObjects(Ride.class)) unique.put(r.getId(), r);
            for (Ride r : s2.toObjects(Ride.class)) unique.put(r.getId(), r);

            listener.onSuccess(new ArrayList<>(unique.values()));
        }).addOnFailureListener(e -> listener.onSuccess(new ArrayList<>()));
    }
    public void getRideById(String id, OnSuccessListener<Ride> listener){
        db.collection("ride").document(id).get()
                .addOnSuccessListener(doc -> {
                    if(doc.exists()){
                        Ride r = doc.toObject(Ride.class);
                        listener.onSuccess(r);
                    } else {
                        listener.onSuccess(null);
                    }
                });
    }

    public void getPastRidesForAdmin(OnSuccessListener<List<Ride>> listener) {

        var qFinished = db.collection("ride")
                .whereEqualTo("status", "FINISHED")
                .get();

        var qCancelled = db.collection("ride")
                .whereEqualTo("status", "CANCELLED")
                .get();

        Tasks.whenAllSuccess(qFinished, qCancelled)
                .addOnSuccessListener(results -> {

                    Map<String, Ride> unique = new HashMap<>();

                    QuerySnapshot s1 = (QuerySnapshot) results.get(0);
                    QuerySnapshot s2 = (QuerySnapshot) results.get(1);

                    for (Ride r : s1.toObjects(Ride.class)) unique.put(r.getId(), r);
                    for (Ride r : s2.toObjects(Ride.class)) unique.put(r.getId(), r);

                    listener.onSuccess(new ArrayList<>(unique.values()));
                })
                .addOnFailureListener(e -> listener.onSuccess(new ArrayList<>()));
    }

}