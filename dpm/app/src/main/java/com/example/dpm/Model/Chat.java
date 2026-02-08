package com.example.dpm.Model;


import com.google.firebase.firestore.DocumentId;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chat {

    @DocumentId
    private String id;

    private String userId;
    private List<Message> messages;
}