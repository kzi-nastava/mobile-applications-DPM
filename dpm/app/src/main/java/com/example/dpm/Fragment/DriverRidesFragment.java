package com.example.dpm.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Adapter.DriverRidesAdapter;
import com.example.dpm.Model.Ride;
import com.example.dpm.Model.RideStatus;
import com.example.dpm.R;
import com.example.dpm.Repository.RideRepository;
import com.example.dpm.Session.UserSession;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class DriverRidesFragment extends Fragment implements DriverRidesAdapter.RideActionListener {

    private RecyclerView rv;
    private TextView tvEmpty;
    private DriverRidesAdapter adapter;
    private final List<Ride> items = new ArrayList<>();
    private RideRepository rideRepository = new RideRepository();
    private FirebaseFirestore db;
    private ListenerRegistration reg;
    private UserSession session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_driver_ride, container, false);

        rv = v.findViewById(R.id.rvDriverRides);
        tvEmpty = v.findViewById(R.id.tvEmpty);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DriverRidesAdapter(items, this);
        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        session = UserSession.getInstance();

        listenMyRides();

        return v;
    }
    private void listenMyRides() {
        String driverId = session.getUser().getId();

        rideRepository.getActiveRidesByDriver(driverId, rides -> {
            items.clear();
            items.addAll(rides);

            adapter.notifyDataSetChanged();
            tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (reg != null) reg.remove();
    }

    @Override
    public void onStartRide(Ride ride) {
        rideRepository.startRide(
                ride.getId(),
                ride.getScheduledAt(),
                unused -> {
                    Toast.makeText(requireContext(), "Ride started", Toast.LENGTH_SHORT).show();
                    rideRepository.notifyLinkedPassengersRideStarted(ride.getId());
                },
                err -> Toast.makeText(requireContext(), err.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onFinishRide(Ride ride) {
        rideRepository.finishRide(
                ride.getId(),
                unused ->{
                    Toast.makeText(requireContext(), "Ride finished", Toast.LENGTH_SHORT).show();
                    rideRepository.notifyLinkedPassengersRideFinished(ride.getId());
                },
                err -> Toast.makeText(requireContext(), err.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}