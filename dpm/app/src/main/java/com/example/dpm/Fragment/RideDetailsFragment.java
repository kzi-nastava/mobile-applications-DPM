package com.example.dpm.Fragment;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.example.dpm.Model.Driver;
import com.example.dpm.Model.Ride;
import com.example.dpm.Model.RideLocation;
import com.example.dpm.Model.UserRole;
import com.example.dpm.R;
import com.example.dpm.Repository.DriverRepository;
import com.example.dpm.Repository.PassengerRepository;
import com.example.dpm.Repository.RideRepository;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import com.example.dpm.Repository.RatingRepository;
import com.example.dpm.Model.Rating;
import com.example.dpm.Session.UserSession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RideDetailsFragment extends Fragment {

    Button btnRate;
    private static final String KEY="rideId";

    public static RideDetailsFragment newInstance(String id){
        RideDetailsFragment f=new RideDetailsFragment();
        Bundle b=new Bundle();
        b.putString(KEY,id);
        f.setArguments(b);
        return f;
    }

    private long parseEndTimeToMillis(String endTime) {
        if (endTime == null || endTime.trim().isEmpty()) return -1;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            sdf.setLenient(false);
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Belgrade"));
            Date d = sdf.parse(endTime.trim());
            return (d != null) ? d.getTime() : -1;
        } catch (ParseException e) {
            return -1;
        }
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


            btnRate = v.findViewById(R.id.btnRate);
            btnRate.setOnClickListener(x -> {
                long threeDaysInMillis = 3L * 24 * 60 * 60 * 1000;
                long now = System.currentTimeMillis();
                long rideEndTimeMillis = parseEndTimeToMillis(ride.getEndTime());

                if (rideEndTimeMillis <= 0) {
                    Toast.makeText(getContext(), "Invalid end time format.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (now - rideEndTimeMillis > threeDaysInMillis) {
                    Toast.makeText(getContext(), "Rating period expired.", Toast.LENGTH_SHORT).show();
                    return;
                }

                RatingDialogFragment dialog = RatingDialogFragment.newInstance(ride.getId());
                dialog.show(getParentFragmentManager(), "rating_dialog");
            });


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

            UserSession session = UserSession.getInstance();
            UserRole role = session.getUser().getRole();

            TextView tvPassengers = v.findViewById(R.id.tvPassengers);

            Button btnOrderAgain = v.findViewById(R.id.btnOrderAgain);
            Button btnRate = v.findViewById(R.id.btnRate);
            if(role == UserRole.ADMIN){
                btnOrderAgain.setVisibility(View.GONE);
                btnRate.setVisibility(View.GONE);
                PassengerRepository passengerRepo = new PassengerRepository();

                List<String> ids = new ArrayList<>();

                if(ride.getPassengerId()!=null)
                    ids.add(ride.getPassengerId());

                if(ride.getLinkedPassengerIds()!=null)
                    ids.addAll(ride.getLinkedPassengerIds());

                if(ids.isEmpty()){
                    tvPassengers.setText("Passengers: none");
                } else {

                    StringBuilder names = new StringBuilder("Passengers:\n");

                    for(String id : ids){
                        passengerRepo.getPassengerById(id, p -> {
                            if(p!=null){
                                names.append(p.getFirstName())
                                        .append(" ")
                                        .append(p.getLastName())
                                        .append("\n");

                                tvPassengers.setText(names.toString());
                            }
                        });
                    }
                }

            }else{
                tvPassengers.setVisibility(View.GONE);
            }

            TextView tvRatings = v.findViewById(R.id.tvRatings);
            tvRatings.setText("Loading ratings...");

            RatingRepository ratingRepo = new RatingRepository();
            ratingRepo.getRatingsByRideId(ride.getId(), ratings -> {

                boolean alreadyRated = ratings != null && !ratings.isEmpty();

                if (alreadyRated) {
                    btnRate.setEnabled(false);
                    btnRate.setText("Rated");
                } else {
                    btnRate.setEnabled(true);
                    btnRate.setText("Rate");
                }

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