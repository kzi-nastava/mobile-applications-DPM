package com.example.dpm.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dpm.Model.Passenger;
import com.example.dpm.Model.Ride;
import com.example.dpm.Model.RideStatus;
import com.example.dpm.R;
import com.example.dpm.Repository.PassengerRepository;
import com.example.dpm.Repository.RideRepository;


import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.ViewHolder> {
    private List<Ride> rides;
    private RideRepository rideRepository;
    private PassengerRepository passengerRepository;
    public RideAdapter(List<Ride> rides) {
        this.rides = rides;
        this.passengerRepository = new PassengerRepository();
        this.rideRepository = new RideRepository();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTime, txtRoute, txtPrice, txtStatus, txtPanic, btnPassengers;
        LinearLayout passengersContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtRoute = itemView.findViewById(R.id.txtRoute);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtPanic = itemView.findViewById(R.id.txtPanic);
            btnPassengers = itemView.findViewById(R.id.btnPassengers);
            passengersContainer = itemView.findViewById(R.id.passengersContainer);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drive, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Ride r = rides.get(position);

        holder.txtTime.setText(r.getStartTime() + "  -  " + r.getEndTime());
        if (r.getLocations() != null && !r.getLocations().isEmpty()) {
            String from = r.getLocations().get(0).getAddress();
            String to = r.getLocations().get(r.getLocations().size() - 1).getAddress();
            holder.txtRoute.setText(from + " → " + to);
        } else {
            holder.txtRoute.setText("-");
        }
        holder.txtPrice.setText("Price: " + r.getPrice() + " RSD");
        holder.txtStatus.setText("Cancelled: " + (r.getStatus() == RideStatus.CANCELED ? "YES (" + r.getCancelledBy() + ")" : "NO"));
        holder.txtPanic.setText("PANIC: " + (r.isPanicTriggered() ? "YES" : "NO"));

        holder.passengersContainer.removeAllViews();

        List<String> allPassengerIds = new ArrayList<>();
        if (r.getPassengerId() != null) allPassengerIds.add(r.getPassengerId());
        if (r.getLinkedPassengerIds() != null) allPassengerIds.addAll(r.getLinkedPassengerIds());

        for (String passengerId: allPassengerIds) {
            passengerRepository.getPassengerById(passengerId, passenger -> {
                if (passenger != null) {
                    TextView tv = new TextView(holder.itemView.getContext());
                    tv.setText("• " + passenger.getFirstName() + " " + passenger.getLastName() + (passengerId.equals(r.getPassengerId()) ? " (Main)" : " (Linked)"));
                    holder.passengersContainer.addView(tv);
                }
            });
        }

        holder.btnPassengers.setOnClickListener(v -> {
            if (holder.passengersContainer.getVisibility() == View.GONE) {
                holder.passengersContainer.setVisibility(View.VISIBLE);
                holder.btnPassengers.setText("Hide passengers ▲");
            } else {
                holder.passengersContainer.setVisibility(View.GONE);
                holder.btnPassengers.setText("Show passengers ▼");
            }
        });
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }
}