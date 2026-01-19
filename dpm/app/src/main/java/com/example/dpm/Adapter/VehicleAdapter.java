package com.example.dpm.Adapter;

import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Model.Vehicle;
import com.example.dpm.R;

import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private final List<Vehicle> vehicles;

    public VehicleAdapter(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle v = vehicles.get(position);
        holder.bind(v);
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        TextView Name, Status, PlateNum;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            Name = itemView.findViewById(R.id.Name);
            Status = itemView.findViewById(R.id.Status);
            PlateNum = itemView.findViewById(R.id.PlateNum);
        }

        public void bind(Vehicle v) {
            Name.setText(android.text.Html.fromHtml("<b>Name: </b> " + v.getModel()));

            String statusText = v.isBusy() ? "BUSY" : "FREE";
            int color = v.isBusy() ?
                    itemView.getResources().getColor(R.color.red) :
                    itemView.getResources().getColor(R.color.green);

            SpannableString spannable = new SpannableString("Status: " + statusText);
            spannable.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "Status: ".length(), 0);
            spannable.setSpan(new android.text.style.ForegroundColorSpan(color), "Status: ".length(), spannable.length(), 0);

            Status.setText(spannable);


            PlateNum.setText(android.text.Html.fromHtml("<b>Plate number: </b> " + v.getPlateNumber()));

        }
    }
}
