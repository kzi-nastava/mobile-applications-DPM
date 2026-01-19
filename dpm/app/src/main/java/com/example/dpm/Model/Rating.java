package com.example.dpm.Model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {

    private String id;
    private String rideId;

    private int driverRating;
    private int vehicleRating;
    private String comment;

    private long createdAt;
}