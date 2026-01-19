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

    private String id;
    private String model;
    private double latitude;
    private double longitude;
    private boolean busy;
    private String plateNumber;
    private int seats;
    private boolean babyFriendly;
    private boolean petFriendly;

    public GeoPoint getPosition() {
        return new GeoPoint(latitude, longitude);
    }

}
