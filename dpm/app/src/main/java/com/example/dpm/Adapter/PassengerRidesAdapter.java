package com.example.dpm.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Model.Ride;
import com.example.dpm.Model.RideLocation;
import com.example.dpm.Model.RideStatus;
import com.example.dpm.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PassengerRidesAdapter extends RecyclerView.Adapter<PassengerRidesAdapter.VH> {

    public interface Listener {
        void onTrack(Ride ride);
        void onCancel(Ride ride);
    }

    private final List<Ride> rides;
    private final Listener listener;

    private final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public PassengerRidesAdapter(List<Ride> rides, Listener listener) {
        this.rides = rides;
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView route, when, status, cancelInfo;
        Button track, cancel;
        VH(View v) {
            super(v);
            route = v.findViewById(R.id.tvRoute);
            when = v.findViewById(R.id.tvWhen);
            status = v.findViewById(R.id.tvStatus);
            cancelInfo = v.findViewById(R.id.tvCancelInfo);
            track = v.findViewById(R.id.btnTrack);
            cancel = v.findViewById(R.id.btnCancel);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_passenger_ride, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        Ride r = rides.get(i);

        h.route.setText(buildRoute(r));
        h.when.setText("Scheduled: " + safe(r.getScheduledAt()));
        h.status.setText("Status: " + (r.getStatus() == null ? "-" : r.getStatus().name()));

        boolean started = r.getStatus() == RideStatus.STARTED;
        boolean accepted = r.getStatus() == RideStatus.ACCEPTED;

        // TRACK: samo kad je STARTED
        h.track.setEnabled(started);
        h.track.setAlpha(started ? 1f : 0.5f);

        // CANCEL: samo kad je ACCEPTED i >10 min pre scheduledAt
        boolean canCancel = accepted && isMoreThan10MinBefore(r.getScheduledAt());
        h.cancel.setEnabled(canCancel);
        h.cancel.setAlpha(canCancel ? 1f : 0.5f);

        if (accepted && !canCancel) {
            h.cancelInfo.setVisibility(View.VISIBLE);
            h.cancelInfo.setText("Cancellation disabled (less than 10 minutes to start).");
        } else {
            h.cancelInfo.setVisibility(View.GONE);
        }

        h.track.setOnClickListener(v -> {
            if (listener != null && started) listener.onTrack(r);
        });

        h.cancel.setOnClickListener(v -> {
            if (listener != null && canCancel) listener.onCancel(r);
        });
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    private String buildRoute(Ride r) {
        if (r.getLocations() == null || r.getLocations().isEmpty()) return "-";
        RideLocation first = r.getLocations().get(0);
        RideLocation last = r.getLocations().get(0);
        for (RideLocation loc : r.getLocations()) {
            if (loc.getOrderIndex() < first.getOrderIndex()) first = loc;
            if (loc.getOrderIndex() > last.getOrderIndex()) last = loc;
        }
        return safe(first.getAddress()) + " → " + safe(last.getAddress());
    }

    private boolean isMoreThan10MinBefore(String scheduledAt) {
        if (scheduledAt == null || scheduledAt.isEmpty()) return false;
        try {
            Date d = df.parse(scheduledAt);
            if (d == null) return false;
            long diff = d.getTime() - System.currentTimeMillis();
            return diff >= 10L * 60L * 1000L;
        } catch (ParseException e) {
            return false;
        }
    }

    private String safe(String s) {
        return (s == null) ? "-" : s;
    }
}