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
import com.example.dpm.Model.Vehicle;
import com.example.dpm.R;
import com.example.dpm.Repository.VehicleRepository;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

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