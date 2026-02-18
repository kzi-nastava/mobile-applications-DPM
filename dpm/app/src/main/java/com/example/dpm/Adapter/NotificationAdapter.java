package com.example.dpm.Adapter;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Model.Notification;
import com.example.dpm.R;

import java.util.Date;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface OnClick {
        void onClick(Notification n);
    }

    private final List<Notification> items;
    private final OnClick onClick;

    public NotificationAdapter(List<Notification> items, OnClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Notification n = items.get(position);

        h.txtMessage.setText(n.getMessage());

        String time = DateFormat.format("dd.MM.yyyy HH:mm", new Date(n.getCreatedAt())).toString();
        h.txtTime.setText(time);

        // read/unread stil
        if (!n.isRead()) {
            h.txtDot.setVisibility(View.VISIBLE);
            h.txtMessage.setTypeface(null, android.graphics.Typeface.BOLD);
            h.itemView.setAlpha(1f);
        } else {
            h.txtDot.setVisibility(View.INVISIBLE);
            h.txtMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
            h.itemView.setAlpha(0.75f);
        }

        h.itemView.setOnClickListener(v -> onClick.onClick(n));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime, txtDot;

        VH(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtDot = itemView.findViewById(R.id.txtDot);
        }
    }
}