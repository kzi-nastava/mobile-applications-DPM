package com.example.dpm.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dpm.Model.Passenger;
import com.example.dpm.Model.UserRole;
import com.example.dpm.R;
import com.example.dpm.Repository.PassengerRepository;
import com.example.dpm.Util.ImageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private PassengerRepository passengerRepository;
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

        // Gallery picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        if (imageUri != null) {
                            profileImage.setImageURI(imageUri);
                            imageSelected = true;
                        }
                    }
                }
        );

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        loginText.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        registerButton.setOnClickListener(v -> {

            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String repeatPassword = repeatPasswordInput.getText().toString().trim();

            // Minimal validation
            if (TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(password) ||
                    TextUtils.isEmpty(repeatPassword)) {

                Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(repeatPassword)) {
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser == null) {
                            Toast.makeText(this, "Auth error: user null", Toast.LENGTH_LONG).show();
                            return;
                        }

                        firebaseUser.sendEmailVerification()
                                .addOnSuccessListener(x -> {

                                    Passenger passenger = new Passenger();

                                    passenger.setId(firebaseUser.getUid());
                                    passenger.setEmail(email);

                                    passenger.setFirstName(firstNameInput.getText().toString().trim());
                                    passenger.setLastName(lastNameInput.getText().toString().trim());
                                    passenger.setPhoneNumber(phoneInput.getText().toString().trim());

                                    passenger.setCountry(countryInput.getText().toString().trim());
                                    passenger.setCity(cityInput.getText().toString().trim());
                                    passenger.setStreet(streetInput.getText().toString().trim());
                                    passenger.setNumber(numberInput.getText().toString().trim());

                                    String profileImageBase64 = "DEFAULT";
                                    if (imageSelected && imageUri != null) {
                                        try {
                                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                                    getContentResolver(),
                                                    imageUri
                                            );

                                            profileImageBase64 = ImageUtil.bitmapToBase64(bitmap);

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    passenger.setProfileImageUrl(profileImageBase64);


                                    passenger.setActive(false);   // čeka email verifikaciju
                                    passenger.setBlocked(false);
                                    passenger.setRole(UserRole.PASSENGER);

                                    passengerRepository = new PassengerRepository();
                                    passengerRepository.addPassenger(passenger);

                                    Toast.makeText(
                                            this,
                                            "Activation email sent. Check your inbox (or Spam).",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    startActivity(
                                            new Intent(this, ActivationPendingActivity.class)
                                    );
                                    finish();

                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(
                                                this,
                                                "Email verification failed: " + e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show()
                                );

                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    this,
                                    "Register failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show()
                    );
        });

    }
}
