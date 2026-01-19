package com.example.dpm.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Adapter.VehicleAdapter;

import com.example.dpm.Model.Driver;
import com.example.dpm.Model.Passenger;
import com.example.dpm.Model.Ride;
import com.example.dpm.Model.RideLocation;
import com.example.dpm.Model.RideStatus;
import com.example.dpm.Model.UserRole;
import com.example.dpm.Model.Vehicle;
import com.example.dpm.R;

import com.example.dpm.Repository.DriverRepository;
import com.example.dpm.Repository.PassengerRepository;
import com.example.dpm.Repository.RideRepository;
import com.example.dpm.Repository.VehicleRepository;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomePageFragment extends Fragment {

    private RecyclerView vehicleListRecycler;
    private VehicleAdapter vehicleAdapter;
    private VehicleRepository vehicleRepository;
    private MapView map;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        Configuration.getInstance().load(
                requireContext(),
                requireContext().getSharedPreferences("osmdroid", 0)
        );
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        //vehicleRepository.seedVehicles();

        DriverRepository driverRepo = new DriverRepository();
        RideRepository rideRepo = new RideRepository();

        PassengerRepository passengerRepo = new PassengerRepository();
//
//        Passenger p1 = new Passenger();
//        p1.setId(UUID.randomUUID().toString());
//        p1.setFirstName("Petar");
//        p1.setLastName("Petrović");
//        p1.setEmail("petar@gmail.com");
//        p1.setActive(true);
//        p1.setBlocked(false);
//        p1.setRole(UserRole.PASSENGER);
//        p1.setRideIds(new ArrayList<>());
//        p1.setFavoriteRouteIds(new ArrayList<>());
//
//        Passenger p2 = new Passenger();
//        p2.setId(UUID.randomUUID().toString());
//        p2.setFirstName("Ana");
//        p2.setLastName("Anić");
//        p2.setEmail("ana@gmail.com");
//        p2.setActive(true);
//        p2.setBlocked(false);
//        p2.setRole(UserRole.PASSENGER);
//        p2.setRideIds(new ArrayList<>());
//        p2.setFavoriteRouteIds(new ArrayList<>());
//
//        Passenger p3 = new Passenger();
//        p3.setId(UUID.randomUUID().toString());
//        p3.setFirstName("Marko");
//        p3.setLastName("Marković");
//        p3.setEmail("marko@gmail.com");
//        p3.setActive(true);
//        p3.setBlocked(false);
//        p3.setRole(UserRole.PASSENGER);
//        p3.setRideIds(new ArrayList<>());
//        p3.setFavoriteRouteIds(new ArrayList<>());
//
//        Passenger p4 = new Passenger();
//        p4.setId(UUID.randomUUID().toString());
//        p4.setFirstName("Jovana");
//        p4.setLastName("Jovanović");
//        p4.setEmail("jovana@gmail.com");
//        p4.setActive(true);
//        p4.setBlocked(false);
//        p4.setRole(UserRole.PASSENGER);
//        p4.setRideIds(new ArrayList<>());
//        p4.setFavoriteRouteIds(new ArrayList<>());
//
//        passengerRepo.addPassenger(p1);
//        passengerRepo.addPassenger(p2);
//        passengerRepo.addPassenger(p3);
//        passengerRepo.addPassenger(p4);
//
//
//
//        Driver d1 = new Driver();
//        d1.setId(UUID.randomUUID().toString());
//        d1.setFirstName("Milan");
//        d1.setLastName("Milanković");
//        d1.setEmail("milan@gmail.com");
//        d1.setActive(true);
//        d1.setBlocked(false);
//        d1.setRole(UserRole.DRIVER);
//        d1.setAvailable(true);
//        d1.setWorkingHoursLast24h(6.5);
//
//        Driver d2 = new Driver();
//        d2.setId(UUID.randomUUID().toString());
//        d2.setFirstName("Stefan");
//        d2.setLastName("Stefanović");
//        d2.setEmail("stefan@gmail.com");
//        d2.setActive(true);
//        d2.setBlocked(false);
//        d2.setRole(UserRole.DRIVER);
//        d2.setAvailable(false);
//        d2.setWorkingHoursLast24h(3.0);
//
//        driverRepo.addDriver(d1);
//        driverRepo.addDriver(d2);
//
//
//        List<RideLocation> route = List.of(
//                new RideLocation(
//                        "Bulevar kralja Aleksandra 73",
//                        44.8055,
//                        20.4761,
//                        0
//                ),
//                new RideLocation(
//                        "Trg republike",
//                        44.8167,
//                        20.4606,
//                        1
//                )
//        );
//
//        Ride r1 = new Ride(
//                UUID.randomUUID().toString(),
//                d1.getId(),
//                p1.getId(),
//                "15.01.2026 08:00",
//                "15.01.2026 08:30",
//                List.of(p2.getId()),
//                route,
//                RideStatus.FINISHED,
//                null,
//                1500,
//                12.5,
//                false
//        );
//
//// ✔ FINISHED – driver1
//        Ride r2 = new Ride(
//                UUID.randomUUID().toString(),
//                d1.getId(),
//                p2.getId(),
//                "16.01.2026 23:00",
//                "17.01.2026 01:20",
//                List.of(p3.getId()),
//                route,
//                RideStatus.FINISHED,
//                null,
//                900,
//                6.2,
//                false
//        );
//
//// ✔ FINISHED – driver1
//        Ride r3 = new Ride(
//                UUID.randomUUID().toString(),
//                d1.getId(),
//                p3.getId(),
//                "17.01.2026 12:00",
//                "17.01.2026 12:45",
//                List.of(p4.getId()),
//                route,
//                RideStatus.FINISHED,
//                null,
//                3200,
//                55.0,
//                true
//        );
//
//// ✖ CANCELED – driver1
//        Ride r4 = new Ride(
//                UUID.randomUUID().toString(),
//                d1.getId(),
//                p4.getId(),
//                "18.01.2026 14:00",
//                "18.01.2026 14:10",
//                List.of(),
//                route,
//                RideStatus.CANCELED,
//                "PASSENGER",
//                0,
//                0,
//                false
//        );
//
//// ✔ FINISHED – driver2
//        Ride r5 = new Ride(
//                UUID.randomUUID().toString(),
//                d2.getId(),
//                p1.getId(),
//                "17.01.2026 09:00",
//                "17.01.2026 09:25",
//                List.of(p3.getId(),p4.getId()),
//                route,
//                RideStatus.FINISHED,
//                null,
//                1100,
//                8.4,
//                false
//        );
//
//        rideRepo.addRide(r1);
//        rideRepo.addRide(r2);
//        rideRepo.addRide(r3);
//        rideRepo.addRide(r4);
//        rideRepo.addRide(r5);


        return view;



    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        map = view.findViewById(R.id.map);
        map.setMultiTouchControls(true);
        map.getController().setZoom(13.0);
        map.getController().setCenter(new GeoPoint(44.7866, 20.4489));


        vehicleListRecycler = view.findViewById(R.id.vehicle_list);
        vehicleListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        vehicleRepository = new VehicleRepository();

        loadVehicles();
    }

    private void loadVehicles() {
        try {
            vehicleRepository.getAllVehicles(vehicles -> {
                try {
                    for (Vehicle v : vehicles) {
                        Marker marker = new Marker(map);
                        marker.setPosition(v.getPosition());
                        marker.setTitle(v.getModel());

                        int statusColor = v.isBusy() ?
                                getResources().getColor(R.color.red) :
                                getResources().getColor(R.color.green);
                        String hexColor = String.format("#%06X", (0xFFFFFF & statusColor));
                        marker.setSubDescription(
                                "Plate: " + v.getPlateNumber() + "<br>" +
                                        "Status: <font color='" + hexColor + "'>" +
                                        (v.isBusy() ? "BUSY" : "FREE") + "</font>"
                        );

                        marker.setIcon(v.isBusy() ?
                                getResources().getDrawable(R.drawable.location_red, null) :
                                getResources().getDrawable(R.drawable.location_green, null));
                        map.getOverlays().add(marker);
                    }
                    map.invalidate();

                    vehicleAdapter = new VehicleAdapter(vehicles);
                    vehicleListRecycler.setAdapter(vehicleAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}