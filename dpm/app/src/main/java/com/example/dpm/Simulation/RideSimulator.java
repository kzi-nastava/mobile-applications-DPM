package com.example.dpm.Simulation;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.dpm.Model.Ride;
import com.example.dpm.Model.RideLocation;
import com.example.dpm.Repository.RideEstimateRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RideSimulator {

    public interface Callback {
        void onRouteReady(List<org.osmdroid.util.GeoPoint> fullRoute);
        void onFinished();
        void onError(Exception e);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final RideEstimateRepository routeRepo = new RideEstimateRepository();

    private boolean running = false;
    private int index = 0;

    private String vehiclesCollectionName = "vehicles";

    public void setVehiclesCollectionName(String name) {
        this.vehiclesCollectionName = name;
    }

    public void stop() {
        running = false;
        handler.removeCallbacksAndMessages(null);
    }


    public void startFullRideSimulation(Context ctx,
                                        Ride ride,
                                        double speedKmh,
                                        int tickSeconds,
                                        Callback cb) {

        if (ride == null) {
            if (cb != null) cb.onError(new Exception("Ride is null"));
            return;
        }
        if (ride.getVehicleId() == null || ride.getVehicleId().isEmpty()) {
            if (cb != null) cb.onError(new Exception("Missing vehicleId"));
            return;
        }
        List<RideLocation> locs = ride.getLocations();
        if (locs == null || locs.size() < 2) {
            if (cb != null) cb.onError(new Exception("Ride locations missing"));
            return;
        }


        List<RideLocation> ordered = new ArrayList<>(locs);
        ordered.sort(Comparator.comparingInt(RideLocation::getOrderIndex));

        List<org.osmdroid.util.GeoPoint> waypoints = new ArrayList<>();
        for (RideLocation rl : ordered) {
            waypoints.add(new org.osmdroid.util.GeoPoint(rl.getLatitude(), rl.getLongitude()));
        }


        fetchSegments(ctx, waypoints, 0, new ArrayList<>(), new FetchCallback() {
            @Override
            public void onSuccess(List<org.osmdroid.util.GeoPoint> fullRoute) {
                if (cb != null) cb.onRouteReady(fullRoute);


                startVehicleUpdates(ride.getVehicleId(), fullRoute, speedKmh, tickSeconds, cb);
            }

            @Override
            public void onError(Exception e) {
                if (cb != null) cb.onError(e);
            }
        });
    }



    private interface FetchCallback {
        void onSuccess(List<org.osmdroid.util.GeoPoint> fullRoute);
        void onError(Exception e);
    }

    private void fetchSegments(Context ctx,
                               List<org.osmdroid.util.GeoPoint> waypoints,
                               int idx,
                               List<org.osmdroid.util.GeoPoint> acc,
                               FetchCallback cb) {

        if (idx >= waypoints.size() - 1) {
            if (acc.size() < 2) {
                cb.onError(new Exception("Route is empty"));
            } else {
                cb.onSuccess(acc);
            }
            return;
        }

        org.osmdroid.util.GeoPoint a = waypoints.get(idx);
        org.osmdroid.util.GeoPoint b = waypoints.get(idx + 1);

        routeRepo.routeByPoints(ctx, a, b, new RideEstimateRepository.Callback() {
            @Override
            public void onSuccess(com.example.dpm.Model.RideEstimate estimate) {

                List<org.osmdroid.util.GeoPoint> seg = estimate.getRoutePoints();

                if (seg != null && !seg.isEmpty()) {

                    if (!acc.isEmpty() && samePoint(acc.get(acc.size() - 1), seg.get(0))) {
                        for (int i = 1; i < seg.size(); i++) acc.add(seg.get(i));
                    } else {
                        acc.addAll(seg);
                    }
                } else {

                    acc.add(a);
                    acc.add(b);
                }

                fetchSegments(ctx, waypoints, idx + 1, acc, cb);
            }

            @Override
            public void onError(Exception e) {

                acc.add(a);
                acc.add(b);
                fetchSegments(ctx, waypoints, idx + 1, acc, cb);
            }
        });
    }

    private boolean samePoint(org.osmdroid.util.GeoPoint p1, org.osmdroid.util.GeoPoint p2) {
        if (p1 == null || p2 == null) return false;
        return Math.abs(p1.getLatitude() - p2.getLatitude()) < 1e-6
                && Math.abs(p1.getLongitude() - p2.getLongitude()) < 1e-6;
    }


    private void startVehicleUpdates(String vehicleId,
                                     List<org.osmdroid.util.GeoPoint> fullRoute,
                                     double speedKmh,
                                     int tickSeconds,
                                     Callback cb) {

        if (fullRoute == null || fullRoute.size() < 2) {
            if (cb != null) cb.onError(new Exception("Full route missing"));
            return;
        }

        stop();
        running = true;
        index = 0;

        long tickMs = Math.max(500, tickSeconds * 1000L);

        Runnable tick = new Runnable() {
            @Override
            public void run() {
                if (!running) return;

                if (index >= fullRoute.size()) {
                    running = false;
                    if (cb != null) cb.onFinished();
                    return;
                }

                org.osmdroid.util.GeoPoint p = fullRoute.get(index);

                db.collection(vehiclesCollectionName)
                        .document(vehicleId)
                        .update("position", new GeoPoint(p.getLatitude(), p.getLongitude()))
                        .addOnSuccessListener(unused -> {
                            index += computeStep(fullRoute, index, speedKmh, tickSeconds);
                            handler.postDelayed(this, tickMs);
                        })
                        .addOnFailureListener(e -> {
                            running = false;
                            if (cb != null) cb.onError(e);
                        });
            }
        };

        handler.post(tick);
    }


    private int computeStep(List<org.osmdroid.util.GeoPoint> route, int startIdx, double speedKmh, int tickSeconds) {
        double speedMs = (speedKmh * 1000.0) / 3600.0;
        double targetMeters = speedMs * tickSeconds;

        double acc = 0.0;
        int i = startIdx;

        while (i < route.size() - 1) {
            double seg = route.get(i).distanceToAsDouble(route.get(i + 1));
            acc += seg;
            i++;
            if (acc >= targetMeters) break;
        }

        int step = i - startIdx;
        return Math.max(1, step);
    }
}