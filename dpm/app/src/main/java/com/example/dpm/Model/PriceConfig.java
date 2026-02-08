package com.example.dpm.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceConfig {

    private String id;
    private VehicleType vehicleType;
    private double basePrice;
}