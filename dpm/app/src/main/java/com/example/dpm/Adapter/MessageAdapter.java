package com.example.dpm.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Model.Message;
import com.example.dpm.R;
import com.example.dpm.Session.UserSession;
import com.example.dpm.Model.UserRole;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {

    private static final int TYPE_LEFT = 0;
    private static final int TYPE_RIGHT = 1;

    private final List<Message> messages;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        Message m = messages.get(position);

        boolean isAdmin = UserSession.getInstance().getUser() != null
                && UserSession.getInstance().getUser().getRole() == UserRole.ADMIN;

        // Ako sam admin: moje poruke su ADMIN -> desno
        // Ako sam user: moje poruke su USER -> desno
        if (isAdmin) {
            return "ADMIN".equals(m.getFromType()) ? TYPE_RIGHT : TYPE_LEFT;
        } else {
            return "USER".equals(m.getFromType()) ? TYPE_RIGHT : TYPE_LEFT;
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == TYPE_RIGHT) ? R.layout.item_message_right : R.layout.item_message_left;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.txtMessage.setText(messages.get(position).getText());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtMessage;
        VH(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
        }
    }
}