package com.example.dpm.Repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class ChatRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private DocumentReference chatRef(String chatId) {
        return db.collection("support_chats").document(chatId);
    }

    private CollectionReference messagesRef(String chatId) {
        return chatRef(chatId).collection("messages");
    }

    public Task<Void> ensureChatExists(String chatId, String userRole, String userName) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", chatId);
        data.put("userRole", userRole);
        data.put("userName", userName);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());
        return chatRef(chatId).set(data, SetOptions.merge());
    }

    public Task<Void> sendMessage(String chatId, String fromType, String fromId, String text) {

        DocumentReference msgDoc = messagesRef(chatId).document();

        Map<String, Object> msg = new HashMap<>();
        msg.put("fromType", fromType); // "USER" / "ADMIN"
        msg.put("fromId", fromId);
        msg.put("text", text);
        msg.put("sentAt", FieldValue.serverTimestamp());

        Map<String, Object> chatUpdate = new HashMap<>();
        chatUpdate.put("lastMessage", text);
        chatUpdate.put("updatedAt", FieldValue.serverTimestamp());

        WriteBatch batch = db.batch();
        batch.set(msgDoc, msg);
        batch.set(chatRef(chatId), chatUpdate, SetOptions.merge());

        return batch.commit();
    }

    public ListenerRegistration listenMessages(String chatId, EventListener<QuerySnapshot> listener) {
        return messagesRef(chatId)
                .orderBy("sentAt", Query.Direction.ASCENDING)
                .addSnapshotListener(listener);
    }

    public ListenerRegistration listenAllChats(EventListener<QuerySnapshot> listener) {
        return db.collection("support_chats")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    public Task<DocumentSnapshot> getChat(String chatId) {
        return chatRef(chatId).get();
    }

}
