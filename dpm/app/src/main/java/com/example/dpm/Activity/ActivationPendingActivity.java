package com.example.dpm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ActivationPendingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation_pending);

        Button btn = findViewById(R.id.go_to_login);
        btn.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

    }
}
