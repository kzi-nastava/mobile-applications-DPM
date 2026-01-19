package com.example.dpm.Model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanicNotification {

    private String id;
    private String rideId;
    private String triggeredByUserId;
    private long timestamp;
    private boolean resolved;
}