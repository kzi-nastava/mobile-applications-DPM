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

public class PassengerRideAdapter extends RecyclerView.Adapter<PassengerRideAdapter.VH>{

    public interface Listener{
        void onDetails(Ride ride);
    }

    private final List<Ride> rides;
    private final Listener listener;

    public PassengerRideAdapter(List<Ride> rides, Listener listener){
        this.rides = rides;
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder{
        TextView route,date,endTime,price;
        Button details;
        VH(View v){
            super(v);
            route=v.findViewById(R.id.tvRoute);
            date=v.findViewById(R.id.tvDate);
            endTime = v.findViewById(R.id.tvEndTime);
            price=v.findViewById(R.id.tvPrice);
            details=v.findViewById(R.id.btnDetails);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p,int v){
        return new VH(LayoutInflater.from(p.getContext())
                .inflate(R.layout.ride_item_passenger,p,false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h,int i){
        Ride r=rides.get(i);

        String start = "";
        String end = "";

        if(r.getLocations()!=null && !r.getLocations().isEmpty()){

            RideLocation first = r.getLocations().get(0);
            RideLocation last  = r.getLocations().get(0);

            for(RideLocation loc : r.getLocations()){
                if(loc.getOrderIndex() < first.getOrderIndex()) first = loc;
                if(loc.getOrderIndex() > last.getOrderIndex())  last  = loc;
            }

            start = first.getAddress();
            end   = last.getAddress();
        }

        h.route.setText(start + " → " + end);

        h.date.setText("Start: " + r.getStartTime());

        if(r.getEndTime()!=null && !r.getEndTime().isEmpty())
            h.endTime.setText("End: " + r.getEndTime());
        else
            h.endTime.setText("End: -");


        h.price.setText("Price: "+r.getPrice()+" RSD");

        h.details.setOnClickListener(v->{
            if(listener!=null) listener.onDetails(r);
        });
    }

    @Override public int getItemCount(){ return rides.size(); }
}