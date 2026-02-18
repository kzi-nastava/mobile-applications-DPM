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
import com.example.dpm.R;

import java.util.List;

public class AdminRideAdapter extends RecyclerView.Adapter<AdminRideAdapter.VH> {

    public interface Listener {
        void onDetails(Ride ride);
    }

    private final List<Ride> rides;
    private final Listener listener;

    public AdminRideAdapter(List<Ride> rides, Listener listener) {
        this.rides = rides;
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView route, startEnd, price, cancel, panic;
        Button details;

        VH(View v) {
            super(v);
            route = v.findViewById(R.id.tvRoute);
            startEnd = v.findViewById(R.id.tvStartEnd);
            price = v.findViewById(R.id.tvPrice);
            cancel = v.findViewById(R.id.tvCancel);
            panic = v.findViewById(R.id.tvPanic);
            details = v.findViewById(R.id.btnDetails);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        return new VH(LayoutInflater.from(p.getContext())
                .inflate(R.layout.ride_item_admin, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        Ride r = rides.get(i);

        String startAddr = "";
        String endAddr = "";

        if (r.getLocations() != null && !r.getLocations().isEmpty()) {
            RideLocation first = r.getLocations().get(0);
            RideLocation last = r.getLocations().get(0);

            for (RideLocation loc : r.getLocations()) {
                if (loc.getOrderIndex() < first.getOrderIndex()) first = loc;
                if (loc.getOrderIndex() > last.getOrderIndex()) last = loc;
            }

            startAddr = first.getAddress();
            endAddr = last.getAddress();
        }

        h.route.setText(startAddr + " → " + endAddr);

        String st = (r.getStartTime() == null ? "-" : r.getStartTime());
        String et = (r.getEndTime() == null || r.getEndTime().isEmpty() ? "-" : r.getEndTime());
        h.startEnd.setText(st + " — " + et);

        h.price.setText("Price: " + r.getPrice() + " RSD");

        boolean cancelled = r.getCancelReason() != null && !r.getCancelReason().isEmpty();
        if (cancelled) {
            String by = (r.getCancelledBy() == null ? "-" : r.getCancelledBy());
            h.cancel.setText("Cancelled: YES (by: " + by + ")");
        } else {
            h.cancel.setText("Cancelled: NO");
        }

        h.panic.setText("PANIC: " + (r.isPanicTriggered() ? "YES" : "NO"));

        h.details.setOnClickListener(v -> {
            if (listener != null) listener.onDetails(r);
        });
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }
}