package com.example.dpm.Repository;
import com.example.dpm.Model.Rating;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;

public class RatingRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getRatingsByRideId(String rideId, OnSuccessListener<List<Rating>> onSuccess) {
        db.collection("ratings")
                .whereEqualTo("rideId", rideId)
                .get()
                .addOnSuccessListener(snapshot -> onSuccess.onSuccess(snapshot.toObjects(Rating.class)))
                .addOnFailureListener(e -> onSuccess.onSuccess(Collections.emptyList()));
    }
}