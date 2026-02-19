package com.example.dpm.Model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceConfig {
    private Map<String, Long> pricePerVehicleType;
    private long pricePerKm;
}