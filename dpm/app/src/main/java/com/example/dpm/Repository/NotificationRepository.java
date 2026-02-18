package com.example.dpm.Repository;

import com.example.dpm.Model.Notification;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

public class NotificationRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ListenerRegistration listenUserNotifications(String userId, EventListener<QuerySnapshot> listener) {
        return db.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    public ListenerRegistration listenUnreadCount(String userId, EventListener<QuerySnapshot> listener) {
        return db.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .addSnapshotListener(listener);
    }

    public Task<Void> markAsRead(String notificationId) {
        return db.collection("notifications")
                .document(notificationId)
                .update("read", true);
    }

    public Task<Void> createNotification(Notification n) {
        String id = db.collection("notifications").document().getId();
        n.setId(id);
        return db.collection("notifications").document(id).set(n);
    }
}