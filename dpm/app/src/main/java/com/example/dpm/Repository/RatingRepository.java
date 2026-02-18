package com.example.dpm.Repository;

import com.example.dpm.Model.Rating;
import com.example.dpm.Model.Ride;
import com.example.dpm.Model.RideStatus;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;

public class RatingRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<Void> addRating(Rating rating) {
        String docId = db.collection("ratings").document().getId();
        rating.setId(docId);
        return db.collection("ratings").document(docId).set(rating);
    }

    public void getRatingsByRideId(String rideId, OnSuccessListener<List<Rating>> listener) {
        db.collection("ratings").whereEqualTo("rideId", rideId).get()
                .addOnSuccessListener(snapshot -> {
                    List<Rating> result = snapshot.toObjects(Rating.class);
                    listener.onSuccess(result);
                });
    }
}