package com.example.dpm.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dpm.R;

public class RideTrackingFragment extends Fragment {

    private static final String ARG_RIDE_ID = "rideId";

    public static RideTrackingFragment newInstance(String rideId) {
        RideTrackingFragment f = new RideTrackingFragment();
        Bundle b = new Bundle();
        b.putString(ARG_RIDE_ID, rideId);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride_tracking, container, false);

        TextView txt = view.findViewById(R.id.txtRideTracking);
        String rideId = getArguments() != null ? getArguments().getString(ARG_RIDE_ID) : "-";
        txt.setText("Ride tracking page (placeholder)\nRide ID: " + rideId);

        return view;
    }
}