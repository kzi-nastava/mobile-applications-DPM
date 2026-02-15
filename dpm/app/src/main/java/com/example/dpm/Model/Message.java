package com.example.dpm.Model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @DocumentId
    private String id;

    private String fromType;
    private String fromId;
    private String text;
    private Timestamp sentAt;
}