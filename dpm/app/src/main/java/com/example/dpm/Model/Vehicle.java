package com.example.dpm.Model;

import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.util.GeoPoint;

public class Vehicle {
    private String name;
    private GeoPoint position;
    private boolean busy;
    private String plateNumber;

    public Vehicle(String name, GeoPoint position, boolean busy, String plateNumber) {
        this.name = name;
        this.position = position;
        this.busy = busy;
        this.plateNumber = plateNumber;
    }

    public String getName() {
        return name;
    }
    public String getPlateNumber() {
        return plateNumber;
    }
    public GeoPoint getPosition() {
        return position;
    }
    public boolean isBusy() {
        return busy;
    }
}
