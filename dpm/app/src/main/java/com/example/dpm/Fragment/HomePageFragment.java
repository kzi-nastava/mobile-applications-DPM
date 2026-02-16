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
import com.example.dpm.Repository.RideEstimateRepository;
import com.example.dpm.Repository.RideRepository;
import com.example.dpm.Repository.VehicleRepository;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;


import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import android.widget.ImageButton;

public class HomePageFragment extends Fragment {

    private RecyclerView vehicleListRecycler;
    private VehicleAdapter vehicleAdapter;
    private VehicleRepository vehicleRepository;
    private MapView map;
    private RideEstimateRepository rideEstimateRepository;

    private final Set<Overlay> routeOverlays = new HashSet<>();
    private final Set<Marker> vehicleMarkers = new HashSet<>();

    private com.google.android.material.card.MaterialCardView estimateCard;
    private android.widget.TextView tvEstimateBody;
    private boolean routeVisible = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        Configuration.getInstance().load(
                requireContext(),
                requireContext().getSharedPreferences("osmdroid", 0)
        );
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        DriverRepository driverRepo = new DriverRepository();
        RideRepository rideRepo = new RideRepository();

        PassengerRepository passengerRepo = new PassengerRepository();

        return view;



    }
    private void clearRouteOverlays() {
        for (Overlay o : routeOverlays) {
            map.getOverlays().remove(o);
        }
        routeOverlays.clear();
    }

    private void clearVehicleMarkers() {
        for (Marker m : vehicleMarkers) {
            map.getOverlays().remove(m);
        }
        vehicleMarkers.clear();
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
        estimateCard = view.findViewById(R.id.estimateCard);
        tvEstimateBody = view.findViewById(R.id.tvEstimateBody);

        rideEstimateRepository = new RideEstimateRepository();
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabEstimate);
        ImageButton close = view.findViewById(R.id.btnCloseEstimate);
        close.setOnClickListener(v -> {
            clearRouteOverlays();
            estimateCard.setVisibility(View.GONE);
            routeVisible = false;
            map.invalidate();
        });
        fab.setOnClickListener(v -> {

            // ako ruta već postoji → samo je skloni
            if(routeVisible){
                clearRouteOverlays();
                estimateCard.setVisibility(View.GONE);
                routeVisible = false;
                map.invalidate();
                return;
            }

            // inače otvori dialog za procenu
            new RideEstimateDialogFragment((from,to)->{

                rideEstimateRepository.estimate(requireContext(), from, to,
                        new RideEstimateRepository.Callback(){

                            @Override
                            public void onSuccess(com.example.dpm.Model.RideEstimate e){

                                clearRouteOverlays();

                                Marker start = new Marker(map);
                                start.setPosition(e.getFromPoint());
                                start.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                start.setTitle("Start");
                                start.setSnippet("Ride starting point");
                                start.showInfoWindow();

                                Marker end = new Marker(map);
                                end.setPosition(e.getToPoint());
                                end.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                end.setTitle("Destination");
                                end.setSnippet("Ride destination");
                                end.showInfoWindow();


                                Polyline line = new Polyline();
                                line.setPoints(e.getRoutePoints());
                                line.getOutlinePaint().setStrokeWidth(10f);

                                map.getOverlays().add(line);
                                map.getOverlays().add(start);
                                map.getOverlays().add(end);

                                routeOverlays.add(line);
                                routeOverlays.add(start);
                                routeOverlays.add(end);

                                estimateCard.setVisibility(View.VISIBLE);
                                tvEstimateBody.setText(
                                        "Distance: "+String.format("%.2f",e.getDistanceKm())+" km\n"+
                                                "Time: "+String.format("%.0f",e.getDurationMin())+" min"
                                );

                                routeVisible = true;
                                map.invalidate();
                            }

                            @Override
                            public void onError(Exception ex){
                                estimateCard.setVisibility(View.GONE);
                            }
                        });

            }).show(getChildFragmentManager(),"estimate");

        });

    }

    private void loadVehicles() {
        try {
            vehicleRepository.getAllVehicles(vehicles -> {
                try {
                    clearVehicleMarkers();

                    for (Vehicle v : vehicles) {
                        Marker marker = new Marker(map);
                        map.getOverlays().add(marker);
                        vehicleMarkers.add(marker);
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