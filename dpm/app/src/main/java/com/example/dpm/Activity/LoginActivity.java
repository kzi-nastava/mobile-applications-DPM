package com.example.dpm.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dpm.Model.UserRole;
import com.example.dpm.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private EditText emailInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailInput = findViewById(R.id.login_email_input);
        passwordInput = findViewById(R.id.login_password_input);

        TextView forgotPassword = findViewById(R.id.login_forgot_password);
        TextView register = findViewById(R.id.login_register);
        Button loginButton = findViewById(R.id.login_button);

        forgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ResetPasswordActivity.class))
        );

        register.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        loginButton.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Unesi email i lozinku", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "Login error (user null)", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Uvek reload da bi isEmailVerified bio svež
                    user.reload().addOnSuccessListener(v -> {

                        // 1) Email verifikacija (Firebase)
                        if (!user.isEmailVerified()) {
                            Toast.makeText(this,
                                    "Nalog nije aktiviran. Proveri email (Spam).",
                                    Toast.LENGTH_LONG).show();

                            // (opciono) resend
                            user.sendEmailVerification();

                            // vodi na ekran koji kaže "Activation email sent"
                            startActivity(new Intent(this, ActivationPendingActivity.class));
                            auth.signOut();
                            return;
                        }

                        // 2) Provera u Firestore (active/blocked/role)
                        String uid = user.getUid();

                        // prvo passengers, pa drivers, pa admins
                        db.collection("passengers").document(uid).get()
                                .addOnSuccessListener(doc -> {
                                    if (doc.exists()) {
                                        handleUserDoc(doc, UserRole.PASSENGER, uid);
                                    } else {
                                        db.collection("drivers").document(uid).get()
                                                .addOnSuccessListener(doc2 -> {
                                                    if (doc2.exists()) {
                                                        handleUserDoc(doc2, UserRole.DRIVER, uid);
                                                    } else {
                                                        db.collection("admins").document(uid).get()
                                                                .addOnSuccessListener(doc3 -> {
                                                                    if (doc3.exists()) {
                                                                        handleUserDoc(doc3, UserRole.ADMIN, uid);
                                                                    } else {
                                                                        Toast.makeText(this,
                                                                                "Profil ne postoji u bazi (Firestore).",
                                                                                Toast.LENGTH_LONG).show();
                                                                        auth.signOut();
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });

                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void handleUserDoc(DocumentSnapshot doc, UserRole fallbackRole, String uid) {

        Boolean blocked = doc.getBoolean("blocked");
        Boolean active = doc.getBoolean("active");

        // blocked
        if (blocked != null && blocked) {
            Toast.makeText(this, "Nalog je blokiran.", Toast.LENGTH_LONG).show();
            auth.signOut();
            return;
        }

        // active (tvoja logika) – ako je false, a email je verified, možemo automatski podići active=true
        if (active == null || !active) {
            // email već verified -> upiši active=true da ti se uskladi stanje
            doc.getReference().update("active", true);
        }

        // role (iz baze ako postoji, inače fallback)
        UserRole role = fallbackRole;
        Object roleObj = doc.get("role");
        if (roleObj != null) {
            try {
                role = UserRole.valueOf(roleObj.toString());
            } catch (Exception ignored) {}
        }

        // spec deo: driver postaje dostupan na login
        if (role == UserRole.DRIVER) {
            doc.getReference().update("available", true);
        }

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("ROLE", role.name());
        startActivity(i);
        finish();
    }
}

