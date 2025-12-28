package com.example.dpm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView forgotPassword = findViewById(R.id.login_forgot_password);
        TextView register = findViewById(R.id.login_register);
//        TextView guest = findViewById(R.id.login_continue);
        Button loginButton = findViewById(R.id.login_button);

        forgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ResetPasswordActivity.class))
        );

        register.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

//        guest.setOnClickListener(v -> {
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        });

        loginButton.setOnClickListener(v -> {
            // TODO: validacija + auth
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}

