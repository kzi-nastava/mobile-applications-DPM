package com.example.dpm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private boolean imageSelected = false;
    private ImageView profileImage;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        profileImage = findViewById(R.id.profileImageView);
        TextView loginText = findViewById(R.id.register_login_text);
        Button nextButton = findViewById(R.id.register_next_button);

        // launcher za galeriju
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            profileImage.setImageURI(imageUri);
                            imageSelected = true;
                        }
                    }
                }
        );

        // klik na sliku → otvori galeriju
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // klik na "Login"
        loginText.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // klik na "Next"
        nextButton.setOnClickListener(v -> {

            if (!imageSelected) {
                Toast.makeText(
                        this,
                        "No image selected. Default profile image will be used.",
                        Toast.LENGTH_SHORT
                ).show();
            }

            // simulacija email aktivacije
            startActivity(new Intent(this, ActivationPendingActivity.class));
            finish();
        });
    }
}
