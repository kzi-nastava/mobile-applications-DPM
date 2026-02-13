package com.example.dpm.Repository;

import com.example.dpm.Model.PriceConfig;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class PriceRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getCurrentPricing(OnSuccessListener<PriceConfig> listener) {
        db.collection("pricing").document("current").get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        listener.onSuccess(snapshot.toObject(PriceConfig.class));
                    } else {
                        listener.onSuccess(null);
                    }
                });
    }

    public void updatePricing(PriceConfig config) {
        db.collection("pricing").document("current").set(config);
    }
}
