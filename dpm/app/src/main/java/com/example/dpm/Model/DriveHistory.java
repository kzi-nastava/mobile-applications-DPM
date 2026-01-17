package com.example.dpm.Model;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DriveHistory {

    public String id;
    public String startTime;
    public String endTime;
    public String startLocation;
    public String endLocation;
    public boolean cancelled;
    public String cancelledBy;
    public double price;
    public boolean panicTriggered;
    public List<Passenger> passengers;

    public DriveHistory(String startTime, String endTime, String startLocation, String endLocation, boolean cancelled, String cancelledBy, double price, boolean panicTriggered, List<Passenger> passengers) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.cancelled = cancelled;
        this.cancelledBy = cancelledBy;
        this.price = price;
        this.panicTriggered = panicTriggered;
        this.passengers = passengers;
    }
}
