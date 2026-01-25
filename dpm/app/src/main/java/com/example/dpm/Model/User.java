package com.example.dpm.Model;


import com.google.firebase.firestore.DocumentId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @DocumentId
    protected String id;

    protected boolean active;
    protected boolean blocked;
    protected String blockNote;

    protected String city;
    protected String country;
    protected String street;
    protected String number;

    protected String email;
    protected String password;
    protected String firstName;
    protected String lastName;
    protected String phoneNumber;
    protected String profileImageUrl;

    protected UserRole role;
}