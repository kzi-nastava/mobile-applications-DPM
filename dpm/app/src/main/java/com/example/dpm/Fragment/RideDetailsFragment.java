package com.example.dpm.Fragment;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.example.dpm.Model.Driver;
import com.example.dpm.Model.Ride;
import com.example.dpm.Model.RideLocation;
import com.example.dpm.R;
import com.example.dpm.Repository.DriverRepository;
import com.example.dpm.Repository.RideRepository;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import com.example.dpm.Repository.RatingRepository;
import com.example.dpm.Model.Rating;

import java.util.ArrayList;
import java.util.List;

public class RideDetailsFragment extends Fragment {

    private static final String KEY="rideId";

    public static RideDetailsFragment newInstance(String id){
        RideDetailsFragment f=new RideDetailsFragment();
        Bundle b=new Bundle();
        b.putString(KEY,id);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup c,@Nullable Bundle s){
        View v=i.inflate(R.layout.fragment_ride_details,c,false);

        MapView map=v.findViewById(R.id.map);
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        String rideId=getArguments().getString(KEY);
        RideRepository rideRepository = new RideRepository();
        rideRepository.getRideById(rideId, ride -> {

            if(ride == null || ride.getLocations()==null || ride.getLocations().isEmpty())
                return;

            // NAĐI START I END po orderIndex
            RideLocation first = ride.getLocations().get(0);
            RideLocation last  = ride.getLocations().get(0);

            for(RideLocation loc : ride.getLocations()){
                if(loc.getOrderIndex() < first.getOrderIndex()) first = loc;
                if(loc.getOrderIndex() > last.getOrderIndex())  last  = loc;
            }

            GeoPoint start = new GeoPoint(first.getLatitude(), first.getLongitude());
            GeoPoint end   = new GeoPoint(last.getLatitude(), last.getLongitude());

            // OSRM ROUTING (PO PUTU)
            new Thread(() -> {

                try{

                    ArrayList<GeoPoint> waypoints = new ArrayList<>();
                    waypoints.add(start);
                    waypoints.add(end);

                    OSRMRoadManager rm =
                            new OSRMRoadManager(requireContext(), "UBERIO");

                    Road road = rm.getRoad(waypoints);

                    requireActivity().runOnUiThread(() -> {

                        Polyline line = new Polyline();
                        line.setPoints(road.mRouteHigh);
                        line.getOutlinePaint().setStrokeWidth(10f);

                        map.getOverlays().clear();

                        // START MARKER
                        org.osmdroid.views.overlay.Marker startMarker =
                                new org.osmdroid.views.overlay.Marker(map);
                        startMarker.setPosition(start);
                        startMarker.setAnchor(
                                org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                                org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM
                        );
                        startMarker.setTitle("Start");

                        // ako imaš ikonicu koristi je
                        // startMarker.setIcon(getResources().getDrawable(R.drawable.location_green, null));

                        // END MARKER
                        org.osmdroid.views.overlay.Marker endMarker =
                                new org.osmdroid.views.overlay.Marker(map);
                        endMarker.setPosition(end);
                        endMarker.setAnchor(
                                org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                                org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM
                        );
                        endMarker.setTitle("Destination");

                        // endMarker.setIcon(getResources().getDrawable(R.drawable.location_red, null));

                        map.getOverlays().add(line);
                        map.getOverlays().add(startMarker);
                        map.getOverlays().add(endMarker);

                        map.getController().setZoom(13.0);
                        map.getController().setCenter(start);
                        map.invalidate();
                    });


                }catch(Exception e){
                    e.printStackTrace();
                }

            }).start();

            // DRIVER INFO (ostaje isto)
            DriverRepository driverRepo = new DriverRepository();
            TextView tv = v.findViewById(R.id.tvDriver);

            driverRepo.getDriverById(ride.getDriverId(), driver -> {

                if(driver != null)
                    tv.setText("Driver: " + driver.getFirstName()+" "+driver.getLastName());
                else
                    tv.setText("Driver: unknown");

            });

            TextView tvRatings = v.findViewById(R.id.tvRatings);
            tvRatings.setText("Loading ratings...");

            RatingRepository ratingRepo = new RatingRepository();
            ratingRepo.getRatingsByRideId(ride.getId(), ratings -> {

                if (ratings == null || ratings.isEmpty()) {
                    tvRatings.setText("No ratings");
                    return;
                }

                double driverSum = 0;
                double vehicleSum = 0;

                int driverCount = 0;
                int vehicleCount = 0;

                for (Rating r : ratings) {
                    if (r.getDriverRating() > 0) {
                        driverSum += r.getDriverRating();
                        driverCount++;
                    }
                    if (r.getVehicleRating() > 0) {
                        vehicleSum += r.getVehicleRating();
                        vehicleCount++;
                    }

                }

                String driverAvg = (driverCount == 0) ? "-" : String.format("%.1f", (driverSum / driverCount));
                String vehicleAvg = (vehicleCount == 0) ? "-" : String.format("%.1f", (vehicleSum / vehicleCount));

                StringBuilder sb = new StringBuilder();
                sb.append("Driver rating: ").append(driverAvg).append("\n");
                sb.append("Vehicle rating: ").append(vehicleAvg).append("\n");

                tvRatings.setText(sb.toString());
            });

        });


        return v;
    }
}