package com.example.dpm.Model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ride {

    private String id;
    private String driverId;
    private String passengerId;
    private String startTime;
    private String endTime;
    private List<String> linkedPassengerIds;
    private List<RideLocation> locations;
    private RideStatus status;
    private String cancelledBy;
    private double price;
    private double distance;
    private boolean panicTriggered;



}
