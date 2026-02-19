package com.example.dpm.Model;
import org.osmdroid.util.GeoPoint;
import java.util.List;

public class RideEstimate {

    private GeoPoint fromPoint;
    private GeoPoint toPoint;
    private double distanceKm;
    private double durationMin;
    private List<GeoPoint> routePoints;

    private List<GeoPoint> waypoints;

    public RideEstimate(GeoPoint fromPoint, GeoPoint toPoint,
                        double distanceKm, double durationMin,
                        List<GeoPoint> routePoints, List<GeoPoint> waypoints) {
        this.fromPoint = fromPoint;
        this.toPoint = toPoint;
        this.distanceKm = distanceKm;
        this.durationMin = durationMin;
        this.routePoints = routePoints;
        this.waypoints = waypoints;
    }

    public GeoPoint getFromPoint(){ return fromPoint; }
    public GeoPoint getToPoint(){ return toPoint; }
    public double getDistanceKm(){ return distanceKm; }
    public double getDurationMin(){ return durationMin; }
    public List<GeoPoint> getRoutePoints(){ return routePoints; }

    public List<GeoPoint> getWaypoints(){
        return waypoints;
    }
}