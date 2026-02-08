package com.example.dpm.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class Admin extends User{
    public Admin() {
        super();
        this.role = UserRole.ADMIN;
    }
}
