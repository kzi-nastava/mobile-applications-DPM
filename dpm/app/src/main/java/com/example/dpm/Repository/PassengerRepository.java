package com.example.dpm.Repository;

import com.example.dpm.Model.Passenger;
import com.example.dpm.Model.UserRole;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PassengerRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addPassenger(Passenger passenger) {
        passenger.setRole(UserRole.PASSENGER); // OBAVEZNO
        db.collection("users")
                .document(passenger.getId())
                .set(passenger);
    }

    public void getPassengerById(String passengerId, OnSuccessListener<Passenger> listener) {
        db.collection("users")
                .document(passengerId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        listener.onSuccess(null);
                        return;
                    }

                    String role = doc.getString("role");
                    if (!UserRole.PASSENGER.name().equals(role)) {
                        listener.onSuccess(null); // nije passenger
                        return;
                    }

                    listener.onSuccess(doc.toObject(Passenger.class));
                });
    }

    public void getAllPassengers(OnSuccessListener<List<Passenger>> listener) {
        db.collection("users")
                .whereEqualTo("role", UserRole.PASSENGER.name())
                .get()
                .addOnSuccessListener(snapshot ->
                        listener.onSuccess(snapshot.toObjects(Passenger.class))
                );
    }

    public void activatePassenger(String userId,
                                  Runnable onSuccess,
                                  java.util.function.Consumer<Exception> onFailure) {

        db.collection("users")
                .document(userId)
                .update("active", true)
                .addOnSuccessListener(v -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                })
                .addOnFailureListener(e -> {
                    if (onFailure != null) {
                        onFailure.accept(e);
                    }
                });
    }

    public void getPassengerByEmail(String email,
                                    OnSuccessListener<Passenger> onSuccess,
                                    java.util.function.Consumer<Exception> onFailure) {

        db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("role", UserRole.PASSENGER.name())
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        onSuccess.onSuccess(null);
                        return;
                    }

                    Passenger passenger =
                            snapshot.getDocuments()
                                    .get(0)
                                    .toObject(Passenger.class);

                    onSuccess.onSuccess(passenger);
                })
                .addOnFailureListener(e -> {
                    if (onFailure != null) {
                        onFailure.accept(e);
                    }
                });
    }

}
