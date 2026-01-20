package com.example.dpm.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dpm.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ActivationPendingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation_pending);

        Button checkButton = findViewById(R.id.go_to_login);

        checkButton.setOnClickListener(v -> checkActivation());

    }

    private void checkActivation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        user.reload().addOnSuccessListener(v -> {
            if (user.isEmailVerified()) {
                FirebaseFirestore.getInstance()
                        .collection("passengers")
                        .document(user.getUid())
                        .update("active", true)
                        .addOnSuccessListener(x -> {
                            Toast.makeText(this, "Account activated", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        });
            } else {
                Toast.makeText(this, "Email not verified yet", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
