package com.example.dpm.Repository;


import static android.provider.Settings.System.DATE_FORMAT;

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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class RideRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm";
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

    public void startRide(String rideId, String scheduledAt, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {

        db.collection("ride").document(rideId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        if (onFailure != null) onFailure.onFailure(new Exception("Ride not found."));
                        return;
                    }

                    Ride ride = doc.toObject(Ride.class);
                    if (ride == null || ride.getStatus() == null) {
                        if (onFailure != null) onFailure.onFailure(new Exception("Invalid ride data."));
                        return;
                    }

                    if (ride.getStatus() != RideStatus.ACCEPTED) {
                        if (onFailure != null) onFailure.onFailure(new Exception("Ride is not in a startable state."));
                        return;
                    }
                    Date sched = parseDateOrNull(scheduledAt);
                    if (sched != null && new Date().before(sched)) {
                        if (onFailure != null) onFailure.onFailure(new Exception("Ne može START pre zakazanog vremena."));
                        return;
                    }

                    db.collection("ride").document(rideId)
                            .update(
                                    "status", RideStatus.STARTED,
                                    "startTime", nowString()
                            )
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);

                })
                .addOnFailureListener(onFailure);
    }

    public void finishRide(String rideId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {

        db.collection("ride").document(rideId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        if (onFailure != null) onFailure.onFailure(new Exception("Ride not found."));
                        return;
                    }

                    Ride ride = doc.toObject(Ride.class);
                    if (ride == null || ride.getStatus() == null) {
                        if (onFailure != null) onFailure.onFailure(new Exception("Invalid ride data."));
                        return;
                    }

                    if (ride.getStatus() != RideStatus.STARTED) {
                        if (onFailure != null) onFailure.onFailure(new Exception("Ne može FINISH ako vožnja nije STARTED."));
                        return;
                    }

                    db.collection("ride").document(rideId)
                            .update(
                                    "status", RideStatus.FINISHED,
                                    "endTime", nowString()
                            )
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);

                })
                .addOnFailureListener(onFailure);
    }


    public void getActiveRidesByDriver(String driverId, OnSuccessListener<List<Ride>> listener) {

        var q1 = db.collection("ride")
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("status", RideStatus.ACCEPTED)
                .get();

        var q2 = db.collection("ride")
                .whereEqualTo("driverId", driverId)
                .whereEqualTo("status", RideStatus.STARTED)
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
    private Date parseDateOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Belgrade"));
            return sdf.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private String nowString() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Belgrade"));
        return sdf.format(new Date());
    }


}