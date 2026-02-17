package com.example.dpm.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;

import com.example.dpm.Model.Rating;
import com.example.dpm.R;
import com.example.dpm.Repository.RatingRepository;

public class RatingDialogFragment extends DialogFragment {

    private static final String ARG_RIDE_ID = "rideId";

    //PORMENITI DINAMICKI ID VOZNJE KAD SE URADI ISTORIJA I ZAVRSETAK VOZNJE
    private static final String FALLBACK_RIDE_ID = "TEST_RIDE_ID_123";
    private RatingBar rbDriver, rbVehicle;
    private EditText etComment;
    private Button btnCancel, btnSubmit;
    private final RatingRepository ratingRepository = new RatingRepository();

    public static RatingDialogFragment newInstance( String rideId) {
        RatingDialogFragment f = new RatingDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_RIDE_ID, rideId);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_rating, container, false);

        rbDriver = view.findViewById(R.id.rbDriver);
        rbVehicle = view.findViewById(R.id.rbVehicle);
        etComment = view.findViewById(R.id.etComment);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        btnCancel.setOnClickListener(v -> dismiss());
        btnSubmit.setOnClickListener(v -> submit());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setLayout(width, height);
        }
    }

    private void submit() {
        int driverRating = (int) rbDriver.getRating();
        int vehicleRating = (int) rbVehicle.getRating();
        String comment = etComment.getText().toString().trim();

        boolean hasAtLeastOne = driverRating > 0 || vehicleRating > 0 || !TextUtils.isEmpty(comment);

        if (!hasAtLeastOne) {
            Toast.makeText(getContext(), "Please rate at least one field (driver, vehicle, or comment).", Toast.LENGTH_SHORT).show();
            return;
        }

        String rideId = FALLBACK_RIDE_ID;
        if (getArguments() != null) {
            String argRideId = getArguments().getString(ARG_RIDE_ID);
            if (!TextUtils.isEmpty(argRideId)) {
                rideId = argRideId;
            }
        }

        Rating rating = new Rating();
        rating.setRideId(rideId);
        rating.setDriverRating(driverRating);
        rating.setVehicleRating(vehicleRating);
        rating.setComment(comment);
        rating.setCreatedAt(System.currentTimeMillis());

        btnSubmit.setEnabled(false);

        ratingRepository.addRating(rating)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Successfully rated!", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}