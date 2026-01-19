package com.example.dpm.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver extends User {

    private boolean available;
    private double workingHoursLast24h;
    private Vehicle vehicle;

}