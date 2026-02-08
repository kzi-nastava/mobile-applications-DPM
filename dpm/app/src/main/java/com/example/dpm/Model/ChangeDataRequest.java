package com.example.dpm.Model;

import com.google.firebase.firestore.DocumentId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeDataRequest {

    @DocumentId
    protected String id;

    protected String userId;
    protected String city;
    protected String country;
    protected String street;
    protected String number;

    protected String email;
    protected String firstName;
    protected String lastName;
    protected String phoneNumber;

    protected RequestStatus requestStatus;
}