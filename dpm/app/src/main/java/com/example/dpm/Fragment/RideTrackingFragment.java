package com.example.dpm.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.dpm.Model.Ride;
import com.example.dpm.Model.RideLocation;
import com.example.dpm.R;
import com.example.dpm.Repository.RideEstimateRepository;
import com.example.dpm.Repository.RideRepository;
import com.example.dpm.Session.UserSession;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RideTrackingFragment extends Fragment {

    private static final String ARG_RIDE_ID = "rideId";

    public static RideTrackingFragment newInstance(String rideId) {
        RideTrackingFragment f = new RideTrackingFragment();
        Bundle b = new Bundle();
        b.putString(ARG_RIDE_ID, rideId);
        f.setArguments(b);
        return f;
    }

    private MapView map;
    private TextView tvEta, tvDistance;
    private EditText etReport;
    private Button btnSendReport;
    private final RideRepository rideRepo = new RideRepository();
    private RideEstimateRepository routeRepo = new RideEstimateRepository();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private UserSession session;
    private Ride currentRide;
    private Marker vehicleMarker;
    private Marker startMarker;
    private Marker endMarker;
    private Polyline routeLine;

    private ListenerRegistration vehicleReg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride_tracking, container, false);

        session = UserSession.getInstance();

        Configuration.getInstance().load(
                requireContext(),
                requireContext().getSharedPreferences("osmdroid", 0)
        );
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        map = view.findViewById(R.id.mapRideTracking);
        tvEta = view.findViewById(R.id.tvEta);
        tvDistance = view.findViewById(R.id.tvDistance);
        etReport = view.findViewById(R.id.etReport);
        btnSendReport = view.findViewById(R.id.btnSendReport);

        map.setMultiTouchControls(true);
        map.getController().setZoom(13.5);

        String rideId = getArguments() != null ? getArguments().getString(ARG_RIDE_ID) : null;
        if (rideId == null) {
            Toast.makeText(requireContext(), "Missing rideId", Toast.LENGTH_SHORT).show();
            return view;
        }

        loadRideAndSetup(rideId);

        btnSendReport.setOnClickListener(v -> sendInconsistencyReport());

        return view;
    }

    private void loadRideAndSetup(String rideId) {
        rideRepo.getRideById(rideId, ride -> {
            if (ride == null) {
                Toast.makeText(requireContext(), "Ride not found", Toast.LENGTH_SHORT).show();
                return;
            }
            currentRide = ride;

            drawRouteFromRideLocations(ride);
            listenVehiclePosition(ride.getVehicleId());
        });
    }

    private void drawRouteFromRideLocations(Ride ride) {
        List<RideLocation> locs = ride.getLocations();
        if (locs == null || locs.size() < 2) {
            Toast.makeText(requireContext(), "Ride locations missing", Toast.LENGTH_SHORT).show();
            return;
        }

        locs.sort(Comparator.comparingInt(RideLocation::getOrderIndex));

        List<GeoPoint> waypoints = new ArrayList<>();
        for (RideLocation rl : locs) {
            waypoints.add(new GeoPoint(rl.getLatitude(), rl.getLongitude()));
        }

        GeoPoint start = waypoints.get(0);
        GeoPoint end = waypoints.get(waypoints.size() - 1);

        map.getController().setCenter(start);

        // markers
        if (startMarker != null) map.getOverlays().remove(startMarker);
        if (endMarker != null) map.getOverlays().remove(endMarker);

        startMarker = new Marker(map);
        startMarker.setPosition(start);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Start");

        endMarker = new Marker(map);
        endMarker.setPosition(end);
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setTitle("Destination");

        map.getOverlays().add(startMarker);
        map.getOverlays().add(endMarker);


        fetchRouteSegments(waypoints, 0, new ArrayList<>(), start, end);
    }

    private void listenVehiclePosition(String vehicleId) {
        if (vehicleId == null || vehicleId.isEmpty()) return;


        if (vehicleReg != null) vehicleReg.remove();

        vehicleReg = db.collection("vehicles").document(vehicleId)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) return;
                    if (snap == null || !snap.exists()) return;

                    com.google.firebase.firestore.GeoPoint gp = snap.getGeoPoint("position");
                    if (gp == null) return;

                    GeoPoint cur = new GeoPoint(gp.getLatitude(), gp.getLongitude());
                    onVehiclePositionUpdate(cur);
                });
    }

    private void onVehiclePositionUpdate(GeoPoint curPos) {

        if (vehicleMarker == null) {
            vehicleMarker = new Marker(map);
            vehicleMarker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.car_marker));
            vehicleMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            vehicleMarker.setTitle("Vehicle");
            map.getOverlays().add(vehicleMarker);
        }

        vehicleMarker.setPosition(curPos);


        if (currentRide != null && currentRide.getLocations() != null && currentRide.getLocations().size() >= 2) {
            List<RideLocation> locs = new ArrayList<>(currentRide.getLocations());
            locs.sort(Comparator.comparingInt(RideLocation::getOrderIndex));
            RideLocation last = locs.get(locs.size() - 1);
            GeoPoint dest = new GeoPoint(last.getLatitude(), last.getLongitude());

            updateEtaAndDistance(curPos, dest);

        }

        map.invalidate();
    }

    private void updateEtaAndDistance(GeoPoint cur, GeoPoint dest) {
        double meters = cur.distanceToAsDouble(dest);
        double km = meters / 1000.0;


        double avgSpeedKmh = 35.0;
        double hours = (avgSpeedKmh > 0) ? (km / avgSpeedKmh) : 0;
        double minutes = hours * 60.0;

        tvDistance.setText("Remaining: " + String.format("%.2f km", km));

        if (meters < 30) {
            tvEta.setText("ETA: Arrived");
        } else {
            tvEta.setText("ETA: " + String.format("%.0f min", Math.max(1, minutes)));
        }
    }

    private void sendInconsistencyReport() {
        if (currentRide == null) return;

        String text = etReport.getText() != null ? etReport.getText().toString().trim() : "";
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(requireContext(), "Unesi tekst prijave.", Toast.LENGTH_SHORT).show();
            return;
        }

        String passengerId = session.getUser().getId();

        Map<String, Object> data = new HashMap<>();
        data.put("passengerId", passengerId);
        data.put("text", text);
        data.put("createdAt", new Date());

        db.collection("ride")
                .document(currentRide.getId())
                .collection("inconsistencies")
                .add(data)
                .addOnSuccessListener(doc -> {
                    etReport.setText("");
                    Toast.makeText(requireContext(), "Prijava poslata.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(err ->
                        Toast.makeText(requireContext(), err.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void fetchRouteSegments(List<GeoPoint> waypoints, int idx, List<GeoPoint> accumulated, GeoPoint start, GeoPoint end) {

        if (idx >= waypoints.size() - 1) {
            drawPolyline(accumulated);
            map.invalidate();
            updateEtaAndDistance(start, end);
            return;
        }

        GeoPoint a = waypoints.get(idx);
        GeoPoint b = waypoints.get(idx + 1);

        routeRepo.routeByPoints(requireContext(), a, b, new RideEstimateRepository.Callback() {
            @Override
            public void onSuccess(com.example.dpm.Model.RideEstimate e) {

                List<GeoPoint> seg = e.getRoutePoints();
                if (seg != null && !seg.isEmpty()) {
                    if (!accumulated.isEmpty() && accumulated.get(accumulated.size() - 1).equals(seg.get(0))) {
                        seg = seg.subList(1, seg.size());
                    }
                    accumulated.addAll(seg);
                }
                fetchRouteSegments(waypoints, idx + 1, accumulated, start, end);
            }

            @Override
            public void onError(Exception ex) {
                Toast.makeText(requireContext(), "Route error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                accumulated.add(a);
                accumulated.add(b);
                fetchRouteSegments(waypoints, idx + 1, accumulated, start, end);
            }
        });
    }

    private void drawPolyline(List<GeoPoint> pts) {
        if (pts == null || pts.size() < 2) return;
        if (routeLine != null) map.getOverlays().remove(routeLine);

        routeLine = new Polyline();
        routeLine.setPoints(pts);
        routeLine.getOutlinePaint().setStrokeWidth(10f);
        map.getOverlays().add(routeLine);
        try {
            org.osmdroid.util.BoundingBox bb = org.osmdroid.util.BoundingBox.fromGeoPoints(pts);
            map.zoomToBoundingBox(bb, true, 120);
        } catch (Exception ignored) {}
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (vehicleReg != null) vehicleReg.remove();
    }
}