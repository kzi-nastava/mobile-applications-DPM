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

    public void estimate(Context ctx, String from, String to, Callback cb){

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
                waypoints.add(end);

                OSRMRoadManager rm = new OSRMRoadManager(ctx,"UBERIO");
                Road road = rm.getRoad(waypoints);

                double km = road.mLength;
                double min = road.mDuration/60.0;

                RideEstimate result =
                        new RideEstimate(start,end,km,min,road.mRouteHigh);

                new android.os.Handler(ctx.getMainLooper())
                        .post(() -> cb.onSuccess(result));

            }catch(Exception e){
                new android.os.Handler(ctx.getMainLooper())
                        .post(() -> cb.onError(e));
            }
        });
    }
}