package com.example.dpm.Repository;

import com.example.dpm.Model.DriveHistory;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class DriveHistoryRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addDriveHistory(DriveHistory driveHistory) {
        db.collection("driveHistory").document(driveHistory.id).set(driveHistory);
    }

    public void getAllDriveHistory(OnSuccessListener<List<DriveHistory>> listener) {
        db.collection("driveHistory").get().addOnSuccessListener(snapshot ->
                listener.onSuccess(snapshot.toObjects(DriveHistory.class))
        );
    }

    public void getDriveHistoryByDriver(String driverId, OnSuccessListener<List<DriveHistory>> listener) {
        db.collection("driveHistory").whereEqualTo("driverId", driverId)
                .get().addOnSuccessListener(snapshot ->
                        listener.onSuccess(snapshot.toObjects(DriveHistory.class))
                );
    }

}