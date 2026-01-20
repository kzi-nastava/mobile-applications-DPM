package com.example.dpm.Model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class User {

    protected String id;
    protected String email;
    protected String password;
    protected String firstName;
    protected String lastName;
    protected String Country;
    protected String City;
    protected String Street;
    protected String Number;
    protected String phoneNumber;
    protected String profileImage;
    protected boolean active;
    protected boolean blocked;
    protected UserRole role;
}