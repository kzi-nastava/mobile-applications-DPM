package com.example.dpm.Fragment;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.dpm.R;

public class RideEstimateDialogFragment extends DialogFragment {

    public interface Listener {
        void onAddressesEntered(String from, String to);
    }

    private Listener listener;

    public RideEstimateDialogFragment(Listener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_ride_estimate, null);

        EditText from = v.findViewById(R.id.etFrom);
        EditText to = v.findViewById(R.id.etTo);

        return new AlertDialog.Builder(requireContext())
                .setTitle("Ride estimate")
                .setView(v)
                .setPositiveButton("Show", (d, w) -> {
                    String f = from.getText().toString().trim();
                    String t = to.getText().toString().trim();
                    if(listener!=null) listener.onAddressesEntered(f,t);
                })
                .setNegativeButton("Cancel", null)
                .create();
    }
}