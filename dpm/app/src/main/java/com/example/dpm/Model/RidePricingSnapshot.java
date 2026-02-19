package com.example.dpm.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RidePricingSnapshot {

    private long pricePerKm;
    private long pricePerType;
    private VehicleType vehicleType;
}