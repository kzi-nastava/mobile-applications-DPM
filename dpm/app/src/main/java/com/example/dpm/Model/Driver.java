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

    // Napraviti funkciju u repositorijumu koja proverava da li je taj driver aktivan trenutno i pozvati ovde
    //public boolean active() {
        // return driverRepository.isActiveDriverById(...
    //}

    //Napraviti funkciju koja proverava koliko je radio u poslednjih 24 sata
    //public int workingHoursLast24h() {}

    //Napraviti funkciju koja kroz driver repositori nalazi njegovo vozilo
    //public String getVehicleId() {}
}