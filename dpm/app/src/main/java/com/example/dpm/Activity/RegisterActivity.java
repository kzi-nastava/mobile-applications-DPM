package com.example.dpm.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dpm.Model.UserRole;
import com.example.dpm.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ImageView profileImage;
    private Uri imageUri;
    private boolean imageSelected = false;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI
        profileImage = findViewById(R.id.profileImageView);
        Button registerButton = findViewById(R.id.register_next_button);
        TextView loginText = findViewById(R.id.register_login_text);

        EditText emailInput = findViewById(R.id.register_email_input);
        EditText passwordInput = findViewById(R.id.editTextTextPassword);
        EditText repeatPasswordInput = findViewById(R.id.register_repeatpassword_input);
        EditText firstNameInput = findViewById(R.id.register_firstname_input);
        EditText lastNameInput = findViewById(R.id.register_lastname_input);
        EditText phoneInput = findViewById(R.id.register_phone_input);

        EditText countryInput = findViewById(R.id.country_auto_complete);
        EditText cityInput = findViewById(R.id.register_city_input);
        EditText streetInput = findViewById(R.id.register_street_input);
        EditText numberInput = findViewById(R.id.register_number_input);

        // Image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        profileImage.setImageURI(imageUri);
                        imageSelected = true;
                    }
                }
        );

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Go to login
        loginText.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // REGISTER
        registerButton.setOnClickListener(v -> {

            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String repeatPassword = repeatPasswordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(repeatPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser == null) return;

                        // SEND ACTIVATION EMAIL (24h)
                        firebaseUser.sendEmailVerification()
                                .addOnSuccessListener(h -> {
                                    Log.d("EMAIL", "Verification email SENT");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("EMAIL", "Verification email FAILED", e);
                                });

                        // USER DATA (NO PASSWORD)
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", firebaseUser.getUid());
                        userMap.put("email", email);
                        userMap.put("firstName", firstNameInput.getText().toString());
                        userMap.put("lastName", lastNameInput.getText().toString());
                        userMap.put("phoneNumber", phoneInput.getText().toString());

                        userMap.put("country", countryInput.getText().toString());
                        userMap.put("city", cityInput.getText().toString());
                        userMap.put("street", streetInput.getText().toString());
                        userMap.put("number", numberInput.getText().toString());

                        userMap.put("profileImage",
                                imageSelected ? imageUri.toString() : "DEFAULT");

                        userMap.put("active", false); // WAITING FOR EMAIL VERIFICATION
                        userMap.put("blocked", false);
                        userMap.put("role", UserRole.PASSENGER.name());

                        db.collection("passengers")
                                .document(firebaseUser.getUid())
                                .set(userMap);

                        startActivity(new Intent(this, ActivationPendingActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }
}
