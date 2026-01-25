package com.example.dpm.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dpm.Model.Admin;
import com.example.dpm.Model.Driver;
import com.example.dpm.Model.Passenger;
import com.example.dpm.Model.User;
import com.example.dpm.R;
import com.example.dpm.Repository.AdminRepository;
import com.example.dpm.Repository.DriverRepository;
import com.example.dpm.Repository.PassengerRepository;
import com.example.dpm.Session.UserSession;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private PassengerRepository passengerRepository;
    private DriverRepository driverRepository;
    private AdminRepository adminRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        passengerRepository = new PassengerRepository();
        driverRepository = new DriverRepository();
        adminRepository = new AdminRepository();

        EditText emailInput = findViewById(R.id.login_email_input);
        EditText passwordInput = findViewById(R.id.login_password_input);
        Button loginButton = findViewById(R.id.login_button);
        TextView registerText = findViewById(R.id.login_register);

        loginButton.setOnClickListener(v -> {

            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser == null) return;

                        if (!firebaseUser.isEmailVerified()) {
                            Toast.makeText(this,
                                    "Please verify your email before login!",
                                    Toast.LENGTH_LONG).show();
                            auth.signOut();
                            return;
                        }

                        String uid = firebaseUser.getUid();

                        passengerRepository.getPassengerById(uid, passenger -> {
                            if (passenger != null) {
                                handleLoginSuccess(passenger);
                            } else {
                                tryDriver(uid);
                            }
                        });
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Login failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
        });

        registerText.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void tryDriver(String uid) {
        driverRepository.getDriverById(uid, driver -> {
            if (driver != null) {
                handleLoginSuccess(driver);
            } else {
                tryAdmin(uid);
            }
        });
    }

    private void tryAdmin(String uid) {
        adminRepository.getAdminById(uid, admin -> {
            if (admin != null) {
                handleLoginSuccess(admin);
            } else {
                Toast.makeText(this,
                        "User not found",
                        Toast.LENGTH_LONG).show();
                auth.signOut();
            }
        });
    }

    // ZAJEDNIČKA LOGIKA ZA SVE ROLE
    private void handleLoginSuccess(User user) {

        if (!user.isActive()) {
            Toast.makeText(this,
                    "Account not activated",
                    Toast.LENGTH_LONG).show();
            auth.signOut();
            return;
        }

        if (user.isBlocked()) {
            Toast.makeText(this,
                    "Account is blocked",
                    Toast.LENGTH_LONG).show();
            auth.signOut();
            return;
        }

        // SET SESSION
        UserSession.getInstance().setUser(user);

        // IDE SE U ISTI MAIN ACTIVITY
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
