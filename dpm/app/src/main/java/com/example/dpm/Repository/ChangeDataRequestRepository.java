package com.example.dpm.Repository;

import com.example.dpm.Model.ChangeDataRequest;
import com.example.dpm.Model.RequestStatus;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class ChangeDataRequestRepository {

    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "change_data_requests";

    private UserRepository userRepository;

    public ChangeDataRequestRepository() {
        db = FirebaseFirestore.getInstance();
        userRepository = new UserRepository();
    }

    // CREATE
    public void create(ChangeDataRequest request,
                       OnSuccessListener<Void> onSuccess,
                       OnFailureListener onFailure) {

        db.collection(COLLECTION_NAME)
                .add(request)
                .addOnSuccessListener(docRef -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    // READ - get by id
    public void getById(String id,
                        OnSuccessListener<ChangeDataRequest> onSuccess,
                        OnFailureListener onFailure) {

        db.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        onSuccess.onSuccess(null);
                        return;
                    }

                    ChangeDataRequest request = doc.toObject(ChangeDataRequest.class);
                    request.setId(doc.getId());
                    onSuccess.onSuccess(request);
                })
                .addOnFailureListener(onFailure);
    }

    // READ - get all
    public void getAll(OnSuccessListener<List<ChangeDataRequest>> onSuccess,
                       OnFailureListener onFailure) {

        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(snapshot ->
                        onSuccess.onSuccess(snapshot.toObjects(ChangeDataRequest.class)))
                .addOnFailureListener(onFailure);
    }

    // READ - by status
    public void getByStatus(RequestStatus status,
                            OnSuccessListener<List<ChangeDataRequest>> onSuccess,
                            OnFailureListener onFailure) {

        db.collection(COLLECTION_NAME)
                .whereEqualTo("requestStatus", status)
                .get()
                .addOnSuccessListener(snapshot ->
                        onSuccess.onSuccess(snapshot.toObjects(ChangeDataRequest.class)))
                .addOnFailureListener(onFailure);
    }

    // UPDATE
    public void update(ChangeDataRequest request,
                       OnSuccessListener<Void> onSuccess,
                       OnFailureListener onFailure) {

        db.collection(COLLECTION_NAME)
                .document(request.getId())
                .set(request)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // UPDATE - only status
    public void updateStatus(String requestId,
                             RequestStatus status,
                             OnSuccessListener<Void> onSuccess,
                             OnFailureListener onFailure) {

        db.collection(COLLECTION_NAME)
                .document(requestId)
                .update("requestStatus", status)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void updateUser(ChangeDataRequest request,
                       OnSuccessListener<Void> onSuccess,
                       OnFailureListener onFailure) {
        userRepository.updateUserData(
                request.getUserId(),
                request.getCity(),
                request.getCountry(),
                request.getStreet(),
                request.getNumber(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                onSuccess,
                onFailure
        );
    }

    // DELETE
    public void delete(String id,
                       OnSuccessListener<Void> onSuccess,
                       OnFailureListener onFailure) {

        db.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getPendingRequests(
            OnSuccessListener<List<ChangeDataRequest>> onSuccess,
            OnFailureListener onFailure
    ) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("requestStatus", RequestStatus.PENDING.name())
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<ChangeDataRequest> list = snapshot.toObjects(ChangeDataRequest.class);

                    for (int i = 0; i < list.size(); i++) {
                        list.get(i).setId(snapshot.getDocuments().get(i).getId());
                    }

                    onSuccess.onSuccess(list);
                })
                .addOnFailureListener(onFailure);
    }
}
