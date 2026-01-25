package com.example.dpm.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dpm.R;
import com.example.dpm.Repository.PassengerRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivationPendingActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private PassengerRepository passengerRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation_pending);

        auth = FirebaseAuth.getInstance();
        passengerRepository = new PassengerRepository();

        Button checkButton = findViewById(R.id.go_to_login);
        checkButton.setOnClickListener(v -> checkActivation());
    }

    private void checkActivation() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Nisi prijavljen.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        user.reload()
                .addOnSuccessListener(v -> {
                    if (user.isEmailVerified()) {

                        passengerRepository.activatePassenger(
                                user.getUid(),
                                () -> {
                                    Toast.makeText(this,
                                            "Account activated",
                                            Toast.LENGTH_SHORT).show();

                                    auth.signOut(); // posle aktivacije ide novi login
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                },
                                e -> Toast.makeText(this,
                                        "Activation failed: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show()
                        );

                    } else {
                        Toast.makeText(this,
                                "Email still not verified. Check Inbox/Spam.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Reload failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
}
