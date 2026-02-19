package com.example.dpm.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Driver extends User {
    public Driver() {
        super();
        this.role = UserRole.DRIVER;
    }
    public int workingHoursLast24h() {
        return 1;
    }

}