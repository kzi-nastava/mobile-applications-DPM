package com.example.dpm.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Adapter.ChatListAdapter;
import com.example.dpm.Model.Chat;
import com.example.dpm.R;
import com.example.dpm.Repository.ChatRepository;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminChatListFragment extends Fragment {

    private RecyclerView recyclerChats;
    private ChatListAdapter adapter;
    private final List<Chat> chats = new ArrayList<>();
    private final ChatRepository chatRepository = new ChatRepository();
    private ListenerRegistration chatsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_chat_list, container, false);

        recyclerChats = view.findViewById(R.id.recyclerChats);
        recyclerChats.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ChatListAdapter(chats, chat -> {
            SupportChatFragment frag = new SupportChatFragment();
            Bundle b = new Bundle();
            b.putString(SupportChatFragment.ARG_CHAT_ID, chat.getUserId()); // chatId = userId
            frag.setArguments(b);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, frag)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerChats.setAdapter(adapter);

        startListening();

        return view;
    }

    private void startListening() {
        chatsListener = chatRepository.listenAllChats((QuerySnapshot snapshots, FirebaseFirestoreException e) -> {
            if (e != null || snapshots == null) return;
            chats.clear();
            chats.addAll(snapshots.toObjects(Chat.class));
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatsListener != null) {
            chatsListener.remove();
            chatsListener = null;
        }
    }
}