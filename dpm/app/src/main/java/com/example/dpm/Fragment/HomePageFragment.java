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

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class HomePageFragment extends Fragment {

    private RecyclerView vehicleListRecycler;
    private VehicleAdapter vehicleAdapter;
    private MapView map;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        Configuration.getInstance().setUserAgentValue(getContext().getPackageName());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        map = view.findViewById(R.id.map);
        map.setMultiTouchControls(true);
        map.getController().setZoom(13.0);
        map.getController().setCenter(new GeoPoint(44.7866, 20.4489));

        // RecyclerView
        vehicleListRecycler = view.findViewById(R.id.vehicle_list);
        vehicleListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        loadFakeVehicles();
    }

    private void loadFakeVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(new Vehicle("VW Passat ", new GeoPoint(44.7870, 20.4500), false, "BG 123 RA"));
        vehicles.add(new Vehicle("VW Sharan", new GeoPoint(44.7820, 20.4400), true, "BG 321 KL"));

        // Markeri na mapi
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

        // RecyclerView adapter
        vehicleAdapter = new VehicleAdapter(vehicles);
        vehicleListRecycler.setAdapter(vehicleAdapter);

    }
}