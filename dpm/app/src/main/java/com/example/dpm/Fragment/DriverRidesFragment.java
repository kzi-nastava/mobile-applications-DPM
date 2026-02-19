package com.example.dpm.Fragment;

import android.os.Bundle;
import android.util.Log;
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
import com.example.dpm.Simulation.RideSimulator;
import com.example.dpm.Model.RideStatus;
import com.example.dpm.R;
import com.example.dpm.Repository.RideRepository;
import com.example.dpm.Repository.VehicleRepository;
import com.example.dpm.Session.UserSession;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class DriverRidesFragment extends Fragment implements DriverRidesAdapter.RideActionListener {

    private RecyclerView rv;
    private TextView tvEmpty;
    private DriverRidesAdapter adapter;
    private final List<Ride> items = new ArrayList<>();
    private RideRepository rideRepository = new RideRepository();
    private VehicleRepository vehicleRepository = new VehicleRepository();
    private final RideSimulator simulator = new RideSimulator();
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

            items.sort((r1, r2) -> {
                if (r1.getStatus() == RideStatus.STARTED && r2.getStatus() != RideStatus.STARTED)
                    return -1;
                if (r1.getStatus() != RideStatus.STARTED && r2.getStatus() == RideStatus.STARTED)
                    return 1;
                return 0;
            });

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

                    vehicleRepository.setVehicleBusy(ride.getVehicleId(), true,
                            u -> Log.d("RIDE", "Vehicle updated OK"),
                            err -> Log.e("RIDE", "Vehicle update ERROR", err)
                    );
                    rideRepository.notifyLinkedPassengersRideStarted(ride.getId());
                    simulator.startFullRideSimulation(requireContext(), ride, 35.0, 2,
                            new com.example.dpm.Simulation.RideSimulator.Callback() {
                                @Override
                                public void onRouteReady(List<org.osmdroid.util.GeoPoint> fullRoute) {
                                    Log.d("SIM", "Full route points: " + fullRoute.size());
                                }

                                @Override
                                public void onFinished() {
                                    Log.d("SIM", "Simulation finished");
                                    // (opciono) možeš ovde automatski finishRide ako želiš
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("SIM", "Simulation error", e);
                                }
                            }
                    );

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

                    vehicleRepository.setVehicleBusy(ride.getVehicleId(), false,
                            u -> Log.d("RIDE", "Vehicle updated OK"),
                            err -> Log.e("RIDE", "Vehicle update ERROR", err)
                    );
                    rideRepository.notifyLinkedPassengersRideFinished(ride.getId());
                },
                err -> Toast.makeText(requireContext(), err.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}