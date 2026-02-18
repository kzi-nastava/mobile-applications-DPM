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

import com.example.dpm.Adapter.NotificationAdapter;
import com.example.dpm.Model.Notification;
import com.example.dpm.R;
import com.example.dpm.Repository.NotificationRepository;
import com.example.dpm.Session.UserSession;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView recycler;
    private NotificationAdapter adapter;
    private final List<Notification> items = new ArrayList<>();
    private final NotificationRepository repo = new NotificationRepository();
    private ListenerRegistration listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        recycler = view.findViewById(R.id.recyclerNotifications);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(items, this::onNotificationClick);
        recycler.setAdapter(adapter);

        String userId = UserSession.getInstance().getUser().getId();

        listener = repo.listenUserNotifications(userId, (snapshots, e) -> {
            if (e != null || snapshots == null) return;
            items.clear();
            items.addAll(snapshots.toObjects(Notification.class));
            adapter.notifyDataSetChanged();
        });

        return view;
    }

    private void onNotificationClick(Notification n) {

        if (!n.isRead() && n.getId() != null) {
            repo.markAsRead(n.getId());
        }

        // Navigate only for "accepted"
        if ("RIDE_ACCEPTED".equals(n.getType()) && n.getRideId() != null) {
            RideTrackingFragment frag = RideTrackingFragment.newInstance(n.getRideId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, frag)
                    .addToBackStack(null)
                    .commit();
        }
        // "RIDE_FINISHED" = samo info, ne vodi nigde
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }
}