package com.example.dpm.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Adapter.MessageAdapter;
import com.example.dpm.Model.Chat;
import com.example.dpm.Model.Message;
import com.example.dpm.Model.UserRole;
import com.example.dpm.R;
import com.example.dpm.Repository.ChatRepository;
import com.example.dpm.Session.UserSession;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SupportChatFragment extends Fragment {

    public static final String ARG_CHAT_ID = "chatId";
    private TextView txtChatTitle;
    private RecyclerView recyclerMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private final List<Message> messages = new ArrayList<>();
    private MessageAdapter adapter;
    private final ChatRepository chatRepository = new ChatRepository();
    private ListenerRegistration messagesListener;
    private UserSession session = UserSession.getInstance();
    private String chatId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_support_chat, container, false);

        txtChatTitle = view.findViewById(R.id.txtChatTitle);
        recyclerMessages = view.findViewById(R.id.recyclerMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        adapter = new MessageAdapter(messages);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMessages.setAdapter(adapter);


        if (getArguments() != null) {
            chatId = getArguments().getString(ARG_CHAT_ID);
        }

        if (TextUtils.isEmpty(chatId)) {
            chatId = FirebaseAuth.getInstance().getUid();
        }

        setupTitleAndEnsureChat();

        btnSend.setOnClickListener(v -> send());

        startListening();

        return view;
    }

    private void setupTitleAndEnsureChat() {

        boolean isAdmin = session.getUser() != null && session.getUser().getRole() == UserRole.ADMIN;

        if (!isAdmin) {
            txtChatTitle.setText("Support");
            String role = session.getUser() != null ? session.getUser().getRole().name() : "PASSENGER";
            String name = session.getUser() != null ? (session.getUser().getFirstName() + " " + session.getUser().getLastName()) : "Unknown";
            chatRepository.ensureChatExists(chatId, role, name);
        } else {
            chatRepository.getChat(chatId).addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Chat c = doc.toObject(Chat.class);
                    if (c != null && c.getUserName() != null) {
                        txtChatTitle.setText("Chat: " + c.getUserName());
                    } else {
                        txtChatTitle.setText("Chat: " + chatId);
                    }
                } else {
                    txtChatTitle.setText("Chat: " + chatId);
                }
            });
        }
    }

    private void startListening() {
        messagesListener = chatRepository.listenMessages(chatId, (QuerySnapshot snapshots, FirebaseFirestoreException e) -> {
            if (e != null || snapshots == null) return;

            messages.clear();
            messages.addAll(snapshots.toObjects(Message.class));
            adapter.notifyDataSetChanged();

            if (!messages.isEmpty()) {
                recyclerMessages.scrollToPosition(messages.size() - 1);
            }
        });
    }

    private void send() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        boolean isAdmin = session.getUser() != null && session.getUser().getRole() == UserRole.ADMIN;

        String fromType = isAdmin ? "ADMIN" : "USER";
        String fromId = FirebaseAuth.getInstance().getUid();

        chatRepository.sendMessage(chatId, fromType, fromId, text)
                .addOnSuccessListener(aVoid -> etMessage.setText(""));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (messagesListener != null) {
            messagesListener.remove();
            messagesListener = null;
        }
    }
}