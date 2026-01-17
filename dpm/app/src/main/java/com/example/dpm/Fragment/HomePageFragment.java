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
import com.example.dpm.Model.DriveHistory;
import com.example.dpm.Model.Driver;
import com.example.dpm.Model.Passenger;
import com.example.dpm.Model.Vehicle;
import com.example.dpm.R;
import com.example.dpm.Repository.DriveHistoryRepository;
import com.example.dpm.Repository.DriverRepository;
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
        DriveHistoryRepository driveRepo = new DriveHistoryRepository();


//        Driver driver1 = new Driver("driver1", "Marko", "Markovic");
//        Driver driver2 = new Driver("driver2", "Jovana", "Jovanovic");
//
//        driverRepo.addDriver(driver1);
//        driverRepo.addDriver(driver2);
//
//
//        List<Passenger> passengers1 = List.of(new Passenger(UUID.randomUUID().toString(), "Petar", "Petrovic"));
//        List<Passenger> passengers2 = List.of(new Passenger(UUID.randomUUID().toString(), "Milos", "Obilic"));
//
//        DriveHistory drive1 = new DriveHistory(UUID.randomUUID().toString(),"driver1", "17.01.2026 08:00", "17.01.2026 08:00", "Airport", "Arena", false, "", 500.0, false, passengers1);
//        DriveHistory drive2 = new DriveHistory(UUID.randomUUID().toString(),"driver1", "12.01.2026 09:00", "12.01.2026 09:00", "Airport", "Station", false, "", 450.0, false, passengers2);
//        DriveHistory drive3 = new DriveHistory(UUID.randomUUID().toString(),"driver1" ,"11.01.2026 23:00", "12.01.2026 01:00", "Arena", "Station", true, "Passenger", 0.0, false, passengers1);
//        DriveHistory drive4 = new DriveHistory(UUID.randomUUID().toString(), "driver2","18.01.2026 11:00", "18.01.2026 11:00", "Station", "Airport", false, "", 600.0, true, passengers2);
//
//        driveRepo.addDriveHistory(drive1);
//        driveRepo.addDriveHistory(drive2);
//        driveRepo.addDriveHistory(drive3);
//        driveRepo.addDriveHistory(drive4);

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
                        marker.setTitle(v.getName());

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