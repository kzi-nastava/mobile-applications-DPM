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

    // TODO:Napraviti funkciju u repositorijumu koja proverava da li je taj driver aktivan trenutno i pozvati ovde
    //public boolean active() {
        // return driverRepository.isActiveDriverById(...
    //}


    //TODO: Izracunati koliko je bio aktivan u poslednja 24h preko voznji
    public int workingHoursLast24h() {
        //Napravljeno zbog KT2 i pregleda profila
        return 1;
    }

    //TODO:Napraviti funkciju koja kroz driver repositori nalazi njegovo vozilo
    //public String getVehicleId() {}
}