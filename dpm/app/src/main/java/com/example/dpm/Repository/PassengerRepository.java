package com.example.dpm.Repository;

import com.example.dpm.Model.Passenger;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PassengerRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    public void addPassenger(Passenger passenger) {
        db.collection("passengers").document(passenger.getId()).set(passenger);
    }


    public void getPassengerById(String passengerId, OnSuccessListener<Passenger> listener) {
        db.collection("passengers").document(passengerId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                listener.onSuccess(snapshot.toObject(Passenger.class));
            } else {
                listener.onSuccess(null);
            }
        });
    }

    public void getAllPassengers(OnSuccessListener<List<Passenger>> listener) {
        db.collection("passengers").get().addOnSuccessListener(snapshot ->
                listener.onSuccess(snapshot.toObjects(Passenger.class))
        );
    }

}