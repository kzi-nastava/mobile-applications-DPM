package com.example.dpm.Model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Passenger extends User {
    public Passenger() {
        super();
        this.role = UserRole.PASSENGER;
    }
}
