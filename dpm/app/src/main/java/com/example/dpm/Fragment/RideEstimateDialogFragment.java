package com.example.dpm.Fragment;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.dpm.Model.User;
import com.example.dpm.R;
import com.example.dpm.Session.UserSession;

import java.util.ArrayList;
import java.util.List;

public class RideEstimateDialogFragment extends DialogFragment {

    private User loggedInUser;

    public interface Listener {
        void onAddressesEntered(String from, String to, List<String> stations);

    }

    private Listener listener;

    public RideEstimateDialogFragment(Listener listener){

        this.listener = listener;
        this.loggedInUser = UserSession.getInstance().getUser();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_ride_estimate, null);

        EditText from = v.findViewById(R.id.etFrom);
        EditText to = v.findViewById(R.id.etTo);

        LinearLayout stationsSection = v.findViewById(R.id.layoutStationsSection);
        LinearLayout stationsContainer = v.findViewById(R.id.stationsContainer);
        Button btnAddStation = v.findViewById(R.id.btnAddStation);

        if(loggedInUser != null) {
            stationsSection.setVisibility(View.VISIBLE);
        }

        btnAddStation.setOnClickListener(btn -> {

            EditText station = new EditText(getContext());
            station.setHint("Enter station");
            station.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            );

            stationsContainer.addView(station);
        });


        return new AlertDialog.Builder(requireContext())
                .setTitle("Ride estimate")
                .setView(v)
                .setPositiveButton("Show", (d, w) -> {

                    String f = from.getText().toString().trim();
                    String t = to.getText().toString().trim();

                    List<String> stations = new ArrayList<>();

                    if (stationsSection.getVisibility() == View.VISIBLE) {
                        for (int i = 0; i < stationsContainer.getChildCount(); i++) {
                            EditText et = (EditText) stationsContainer.getChildAt(i);
                            String value = et.getText().toString().trim();
                            if (!value.isEmpty()) {
                                stations.add(value);
                            }
                        }
                    }

                    if(listener != null)
                        listener.onAddressesEntered(f, t, stations);
                })

                .setNegativeButton("Cancel", null)
                .create();
    }
}