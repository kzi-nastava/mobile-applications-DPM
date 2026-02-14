package com.example.dpm.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.dpm.Model.GeoUtils;
import com.example.dpm.Model.Ride;
import com.example.dpm.Model.RideLocation;
import com.example.dpm.Model.User;
import com.example.dpm.Model.Vehicle;
import com.example.dpm.R;
import com.example.dpm.Repository.RideRepository;
import com.example.dpm.Repository.UserRepository;
import com.example.dpm.Repository.VehicleRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RideStateViewFragment extends Fragment {
    private MaterialAutoCompleteTextView actDriver;
    private MaterialButton btnApply;
    private View cardRideInfo;
    private TextView tvRideInfo;
    private TextView tvNoRide;
    private final List<User> drivers = new ArrayList<>();
    private final List<String> driverLabels = new ArrayList<>();
    private UserRepository userRepository;
    private RideRepository rideRepository;
    private VehicleRepository vehicleRepository;
    private User selectedDriver = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_ride_status, container, false);

        System.out.println("PROJECT: " +
                FirebaseFirestore.getInstance().getApp().getOptions().getProjectId());


        userRepository = new UserRepository();
        rideRepository = new RideRepository();
        vehicleRepository = new VehicleRepository();

        actDriver = view.findViewById(R.id.actDriver);
        btnApply = view.findViewById(R.id.btnApplyDriver);

        cardRideInfo = view.findViewById(R.id.cardRideInfo);
        tvRideInfo = view.findViewById(R.id.tvRideInfo);
        tvNoRide = view.findViewById(R.id.tvNoRide);

        setupDriverDropdown();

        btnApply.setOnClickListener(v -> {
            if (selectedDriver == null) {
                Toast.makeText(getContext(), "Select a driver first.", Toast.LENGTH_SHORT).show();
                return;
            }
            loadActiveRideForSelectedDriver();
        });

        return view;
    }

    private void setupDriverDropdown() {

        drivers.clear();
        driverLabels.clear();
        selectedDriver = null;

        userRepository.getAllDrivers(list -> {
            drivers.addAll(list);

            for (User u : drivers) {
                String label = (u.getFirstName()) + " " + (u.getLastName());
                driverLabels.add(label.trim());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    driverLabels
            );
            actDriver.setAdapter(adapter);

            actDriver.setOnItemClickListener((parent, view, position, id) -> {
                selectedDriver = drivers.get(position);
                cardRideInfo.setVisibility(View.GONE);
                tvNoRide.setVisibility(View.GONE);
            });

        });
    }

    private void loadActiveRideForSelectedDriver() {
        String driverId = selectedDriver.getId();
        if (driverId == null) {
            Toast.makeText(getContext(), "Driver id is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        rideRepository.getActiveRideForDriver(driverId, ride -> {
            if (ride == null) {
                cardRideInfo.setVisibility(View.GONE);
                tvNoRide.setVisibility(View.VISIBLE);
                return;
            }

            tvNoRide.setVisibility(View.GONE);
            cardRideInfo.setVisibility(View.VISIBLE);

            userRepository.getUserById(ride.getPassengerId(), passengerUser -> {
                System.out.println(ride.getVehicleId());

                if (ride.getVehicleId() == null || ride.getVehicleId().trim().isEmpty()) {
                    loadLinkedPassengersAndRender(ride, passengerUser, null);
                    return;
                }

                vehicleRepository.getVehicleById(ride.getVehicleId(), vehicle -> {
                    System.out.println(vehicle);
                    loadLinkedPassengersAndRender(ride, passengerUser, vehicle);

                });
            });
        });
    }

    private String buildRideInfoText(Ride ride, User passenger, Vehicle vehicle, List<User> linkedPassengers) {

        StringBuilder sb = new StringBuilder();

        sb.append("Driver: ").append(safe(selectedDriver.getFirstName())).append(" ").append(safe(selectedDriver.getLastName())).append("\n\n");
        sb.append("Status: ").append(ride.getStatus() != null ? ride.getStatus().name() : "—").append("\n");
        sb.append("Start time: ").append(formatDate(ride.getStartTime())).append("\n");
        sb.append("End time: ").append(formatDate(ride.getEndTime())).append("\n");
        sb.append("Passenger: ").append(formatUserName(passenger)).append("\n");
        sb.append("Linked passengers: ").append(formatUsersList(linkedPassengers)).append("\n");
        sb.append("Vehicle model: ").append(formatVehicleModel(vehicle)).append("\n");

        sb.append("\nDistance: ").append(ride.getDistance() != null ? ride.getDistance() : "—").append(" km\n");
        sb.append("Total price: ").append(ride.getPrice()).append("\n");

        if (ride.getPricingSnapshot() != null) {
            sb.append("Pricing: base=").append(ride.getPricingSnapshot().getPricePerType()).append(", km=").append(ride.getPricingSnapshot().getPricePerKm()).append("\n");
        }

        sb.append("Panic: ").append(ride.isPanicTriggered() ? "YES" : "NO").append("\n");

        if (ride.getCancelReason() != null && !ride.getCancelReason().trim().isEmpty()) {
            sb.append("\nCancelled by: ").append(safe(ride.getCancelledBy())).append("\nReason: ").append(safe(ride.getCancelReason())).append("\n");
        }

        return sb.toString().trim();
    }

    private String formatDate(String value) {
        if (value == null || value.trim().isEmpty()) return "—";
        return value;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String formatUserName(User u) {
        if (u == null) return "—";
        String name = (safe(u.getFirstName()) + " " + safe(u.getLastName())).trim();
        return name.isEmpty() ? "—" : name;
    }

    private String formatUsersList(List<User> users) {
        if (users == null || users.isEmpty()) return "—";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(formatUserName(users.get(i)));
        }
        return sb.toString();
    }

    private String formatVehicleModel(Vehicle v) {
        if (v == null) return "—";
        return safe(v.getModel());
    }

    private void loadLinkedPassengersAndRender(Ride ride, User passengerUser, Vehicle vehicle) {

        List<String> linkedIds = ride.getLinkedPassengerIds();
        List<User> linkedUsers = new ArrayList<>();

        if (linkedIds == null || linkedIds.isEmpty()) {

            if (vehicle != null) {
                GeoUtils.fetchAddressFromCoordinates(vehicle.getLatitude(), vehicle.getLongitude(),
                        address -> {
                            String text = buildRideInfoText(ride, passengerUser, vehicle, linkedUsers);
                            text += "\nAddress: " + address;
                            tvRideInfo.setText(text);
                        }
                );
            } else {
                tvRideInfo.setText(
                        buildRideInfoText(ride, passengerUser, vehicle, linkedUsers)
                );
            }

            return;
        }

        final int total = linkedIds.size();
        final int[] loaded = {0};

        for (String id : linkedIds) {
            userRepository.getUserById(id, user -> {
                if (user != null) {
                    linkedUsers.add(user);
                }
                loaded[0]++;
                if (loaded[0] == total) {
                    if (vehicle != null) {

                        GeoUtils.fetchAddressFromCoordinates(vehicle.getLatitude(), vehicle.getLongitude(),
                                address -> {
                                    String text = buildRideInfoText(ride, passengerUser, vehicle, linkedUsers);
                                    text += "\nAddress: " + address;
                                    tvRideInfo.setText(text);
                                }
                        );
                    } else {
                        tvRideInfo.setText(
                                buildRideInfoText(ride, passengerUser, vehicle, linkedUsers)
                        );
                    }
                }
            });
        }
    }

}