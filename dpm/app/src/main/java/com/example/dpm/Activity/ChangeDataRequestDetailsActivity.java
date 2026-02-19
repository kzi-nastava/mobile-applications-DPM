package com.example.dpm.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dpm.Fragment.AdminChangeDataRequestsFragment;
import com.example.dpm.Model.ChangeDataRequest;
import com.example.dpm.Model.RequestStatus;
import com.example.dpm.R;
import com.example.dpm.Repository.ChangeDataRequestRepository;

public class ChangeDataRequestDetailsActivity extends AppCompatActivity {

    private ChangeDataRequestRepository repository;
    private ChangeDataRequest request;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_data_request_details);

        String requestId = getIntent().getStringExtra("REQUEST_ID");
        repository = new ChangeDataRequestRepository();

        loadRequest(requestId);
    }

    private void loadRequest(String requestId) {
        repository.getById(
                requestId,
                req -> {
                    if (req == null) {
                        finish();
                        return;
                    }
                    request = req;
                    bindData();
                },
                e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }

    private void bindData() {
        ((TextView) findViewById(R.id.tvName))
                .setText(request.getFirstName() + " " + request.getLastName());

        ((TextView) findViewById(R.id.tvEmail))
                .setText(request.getEmail());

        ((TextView) findViewById(R.id.tvPhone))
                .setText(request.getPhoneNumber());

        ((TextView) findViewById(R.id.tvAddress))
                .setText(
                        request.getStreet() + ", " +
                                request.getCity() + ", " +
                                request.getCountry()
                );

        Button btnAccept = findViewById(R.id.btnAccept);
        Button btnReject = findViewById(R.id.btnReject);

        btnAccept.setOnClickListener(v -> updateStatus(RequestStatus.APPROVED));
        btnReject.setOnClickListener(v -> updateStatus(RequestStatus.REJECTED));
    }

    private void updateStatus(RequestStatus status) {
        repository.updateStatus(
                request.getId(),
                status,
                unused -> {
                    Toast.makeText(this,
                            "Request " + status.name(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                },
                e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
        );
        if(status == RequestStatus.APPROVED) {
            repository.updateUser(
                    request,
                    unused -> {
                        Toast.makeText(
                                this,
                                "Request approved",
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    },
                    e -> Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show()
            );
        }

        finish();

    }
}

