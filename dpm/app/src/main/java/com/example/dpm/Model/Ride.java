package com.example.dpm.Model;

import com.google.firebase.firestore.DocumentId;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ride {

    @DocumentId
    private String id;

    private String driverId;
    private String passengerId;
    private String vehicleId;

    private Integer distance;
    private double price;

    private String cancelReason;
    private String cancelledBy;

    private String startTime;
    private String endTime;
    private String scheduledAt;
    private boolean panicTriggered;

    private List<String> linkedPassengerIds;
    private List<RideLocation> locations;

    private RideStatus status;
}
