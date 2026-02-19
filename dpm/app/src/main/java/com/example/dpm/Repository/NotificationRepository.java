package com.example.dpm.Repository;

import com.example.dpm.Model.Notification;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class NotificationRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addNotification(String userId, String message) {

        Notification notification = new Notification();
        notification.setId(UUID.randomUUID().toString());
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setRead(false);

//        // FORMAT DATUMA
//        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
//        String formattedDate = sdf.format(new Date());

        notification.setCreatedAt(System.currentTimeMillis());
        notification.setRideId(null);
        db.collection("notifications")
                .document(notification.getId())
                .set(notification);
    }

    public Task<Void> createNotification(Notification n) {
        String id = db.collection("notifications").document().getId();
        n.setId(id);
        return db.collection("notifications").document(id).set(n);
    }
}

