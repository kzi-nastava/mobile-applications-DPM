package com.example.dpm.Model;

import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.util.GeoPoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    public String id;
    public String name;
    public double latitude;
    public double longitude;
    public boolean busy;
    public String plateNumber;

    public Vehicle(String name, double latitude, double longitude, boolean busy, String plateNumber) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.busy = busy;
        this.plateNumber = plateNumber;
    }

    public GeoPoint getPosition() {
        return new GeoPoint(latitude, longitude);
    }

}
