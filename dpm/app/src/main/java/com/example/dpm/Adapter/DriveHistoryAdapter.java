package com.example.dpm.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dpm.Model.Passenger;
import com.example.dpm.R;

import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Model.DriveHistory;

import java.util.List;

public class DriveHistoryAdapter  extends RecyclerView.Adapter<DriveHistoryAdapter.ViewHolder> {

    private List<DriveHistory> drives;

    public DriveHistoryAdapter(List<DriveHistory> drives) {
        this.drives = drives;
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
        DriveHistory r = drives.get(position);

        holder.txtTime.setText(r.startTime + "  -  " + r.endTime);
        holder.txtRoute.setText(r.startLocation + " → " + r.endLocation);
        holder.txtPrice.setText("Price: " + r.price + " RSD");
        holder.txtStatus.setText("Cancelled: " + (r.cancelled ? "YES (" + r.cancelledBy + ")" : "NO"));
        holder.txtPanic.setText("PANIC: " + (r.panicTriggered ? "YES" : "NO"));

        holder.passengersContainer.removeAllViews();

        for (Passenger p : r.passengers) {
            TextView tv = new TextView(holder.itemView.getContext());
            tv.setText("• " + p.firstName + " " + p.lastName);
            holder.passengersContainer.addView(tv);
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
        return drives.size();
    }
}