package com.example.dpm.Model;


import com.google.firebase.firestore.DocumentId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanicNotification {

    @DocumentId
    private String id;

    private String rideId;
    private String triggeredByUserId;
    private String createdAt;
    private boolean resolved;
}