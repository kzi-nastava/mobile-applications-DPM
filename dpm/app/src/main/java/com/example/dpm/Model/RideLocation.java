package com.example.dpm.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideLocation {
    private String address;
    private double latitude;
    private double longitude;
    private int orderIndex;
}
