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
import java.util.TimeZone;

public class DriverRidesAdapter extends RecyclerView.Adapter<DriverRidesAdapter.VH> {

    public interface RideActionListener {
        void onStartRide(Ride ride);
        void onFinishRide(Ride ride);
        void onCancelRide(Ride ride);

    }
    private final List<Ride> items;
    private final RideActionListener listener;
    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm";

    public DriverRidesAdapter(List<Ride> items, RideActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver_ride, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Ride r = items.get(position);

        String from = "Unknown";
        List<RideLocation> locs = r.getLocations();
        if (locs != null && !locs.isEmpty() && locs.get(0).getAddress() != null) {
            from = locs.get(0).getAddress();
        }

        h.tvFrom.setText("From: " + from);
        h.tvWhen.setText("Scheduled: " + prettyWhen(r.getScheduledAt()));
        h.tvStatus.setText("Status: " + (r.getStatus() != null ? r.getStatus().name() : "UNKNOWN"));

        boolean canStart = (r.getStatus() == RideStatus.ACCEPTED)
                && !isBeforeScheduledTime(r.getScheduledAt());

        boolean canFinish = (r.getStatus() == RideStatus.STARTED);
        h.btnStart.setEnabled(canStart);
        h.btnFinish.setEnabled(canFinish);

        h.btnStart.setOnClickListener(v -> {
            if (canStart && listener != null) listener.onStartRide(r);
        });

        h.btnFinish.setOnClickListener(v -> {
            if (canFinish && listener != null) listener.onFinishRide(r);
        });

        if(r.getStatus() == RideStatus.STARTED){
            h.btnCancel.setEnabled(false);
            h.btnCancel.setAlpha(0.5f);
        }else{
            h.btnCancel.setEnabled(true);
            h.btnCancel.setAlpha(1f);
        }

        h.btnCancel.setOnClickListener(v -> {
            if(listener != null) listener.onCancelRide(r);
        });


    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvFrom, tvWhen, tvStatus;
        Button btnStart, btnFinish, btnCancel;

        VH(@NonNull View itemView) {
            super(itemView);
            tvFrom = itemView.findViewById(R.id.tvFrom);
            tvWhen = itemView.findViewById(R.id.tvWhen);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnStart = itemView.findViewById(R.id.btnStart);
            btnFinish = itemView.findViewById(R.id.btnFinish);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }

    private static String prettyWhen(String scheduledAt) {
        if (scheduledAt == null || scheduledAt.trim().isEmpty()) return "N/A";
        return scheduledAt;
    }

    private static boolean isBeforeScheduledTime(String scheduledAt) {
        if (scheduledAt == null || scheduledAt.trim().isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Belgrade"));
            Date sched = sdf.parse(scheduledAt);
            if (sched == null) return false;
            return new Date().before(sched);
        } catch (ParseException ex) {
            return false;
        }
    }

    public static String nowIso() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Belgrade"));
        return sdf.format(new Date());
    }
}