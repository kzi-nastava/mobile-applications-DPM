package com.example.dpm.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Model.Chat;
import com.example.dpm.R;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.VH> {

    public interface OnChatClick {
        void onClick(Chat chat);
    }

    private final List<Chat> chats;
    private final OnChatClick onChatClick;

    public ChatListAdapter(List<Chat> chats, OnChatClick onChatClick) {
        this.chats = chats;
        this.onChatClick = onChatClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Chat c = chats.get(position);
        holder.txtName.setText(c.getUserName() != null ? c.getUserName() : c.getUserId());
        holder.txtLastMessage.setText(c.getLastMessage() != null ? c.getLastMessage() : "");
        holder.itemView.setOnClickListener(v -> onChatClick.onClick(c));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtName, txtLastMessage;

        VH(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
        }
    }
}