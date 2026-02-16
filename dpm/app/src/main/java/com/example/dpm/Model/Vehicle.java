package com.example.dpm.Model;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentId;

import org.osmdroid.util.GeoPoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @DocumentId
    private String id;
    private boolean babyFriendly;
    private boolean petFriendly;
    private boolean busy;
    private String driverId;
    private String model;
    private String plateNumber;
    private VehicleType type;
    private int seats;
    private double latitude;
    private double longitude;

    private VehicleType type;

    public GeoPoint getPosition() {
        return new GeoPoint(latitude, longitude);
    }

}
