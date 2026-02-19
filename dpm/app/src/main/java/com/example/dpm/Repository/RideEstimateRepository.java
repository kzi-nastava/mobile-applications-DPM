package com.example.dpm.Repository;
import android.content.Context;

import com.example.dpm.Model.RideEstimate;

import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RideEstimateRepository {

    public interface Callback {
        void onSuccess(RideEstimate estimate);
        void onError(Exception e);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void estimate(Context ctx,
                         String from,
                         String to,
                         List<String> stations,
                         Callback cb)
    {
        executor.execute(() -> {
            try {

                GeocoderNominatim geo =
                        new GeocoderNominatim(Locale.getDefault().getLanguage());

                List<android.location.Address> fromList =
                        geo.getFromLocationName(from,1);

                List<android.location.Address> toList =
                        geo.getFromLocationName(to,1);

                if(fromList.isEmpty() || toList.isEmpty())
                    throw new Exception("Adresa nije pronađena");

                GeoPoint start = new GeoPoint(
                        fromList.get(0).getLatitude(),
                        fromList.get(0).getLongitude());

                GeoPoint end = new GeoPoint(
                        toList.get(0).getLatitude(),
                        toList.get(0).getLongitude());

                ArrayList<GeoPoint> waypoints = new ArrayList<>();

                waypoints.add(start);

                for (String stationAddress : stations) {

                    List<android.location.Address> list =
                            geo.getFromLocationName(stationAddress, 1);

                    if (list == null || list.isEmpty())
                        throw new Exception("Stanica nije pronađena: " + stationAddress);

                    GeoPoint stationPoint = new GeoPoint(
                            list.get(0).getLatitude(),
                            list.get(0).getLongitude()
                    );

                    waypoints.add(stationPoint);
                }

                waypoints.add(end);


                OSRMRoadManager rm = new OSRMRoadManager(ctx, ctx.getPackageName());
                rm.setMean(OSRMRoadManager.MEAN_BY_CAR);

                Road road = rm.getRoad(waypoints);

                if (road == null || road.mStatus != Road.STATUS_OK) {
                    throw new Exception("Routing failed. Status = " +
                            (road != null ? road.mStatus : "null"));
                }

                double km = road.mLength;
                double min = road.mDuration / 60.0;


                RideEstimate result =
                        new RideEstimate(start, end, km, min, road.mRouteHigh, waypoints);

                new android.os.Handler(ctx.getMainLooper())
                        .post(() -> cb.onSuccess(result));

            }catch(Exception e){
                new android.os.Handler(ctx.getMainLooper())
                        .post(() -> cb.onError(e));
            }
        });
    }

    public void routeByPoints(Context ctx, GeoPoint from, GeoPoint to, Callback cb) {

        executor.execute(() -> {
            try {

                ArrayList<GeoPoint> waypoints = new ArrayList<>();
                waypoints.add(from);
                waypoints.add(to);

                OSRMRoadManager rm = new OSRMRoadManager(ctx, "UBERIO");
                Road road = rm.getRoad(waypoints);

                if (road == null || road.mRouteHigh == null || road.mRouteHigh.isEmpty()) {
                    throw new Exception("Ruta nije pronađena");
                }

                double km = road.mLength;
                double min = road.mDuration / 60.0;

                RideEstimate result =
                        new RideEstimate(from, to, km, min, road.mRouteHigh);

                new android.os.Handler(ctx.getMainLooper())
                        .post(() -> cb.onSuccess(result));

            } catch (Exception e) {
                new android.os.Handler(ctx.getMainLooper())
                        .post(() -> cb.onError(e));
            }
        });
    }

}