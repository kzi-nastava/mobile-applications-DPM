package com.example.dpm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        RadioGroup rgUserType = view.findViewById(R.id.rgUserType);
        LinearLayout driverLayout = view.findViewById(R.id.layoutDriverInfo);
        Button btnSaveChanges = view.findViewById(R.id.btnSaveChanges);

        // Po defaultu je Vozač čekiran → prikazano
        driverLayout.setVisibility(View.VISIBLE);

        rgUserType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbUser) {
                // Korisnik
                driverLayout.setVisibility(View.GONE);
                btnSaveChanges.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbDriver) {
                // Vozač
                driverLayout.setVisibility(View.VISIBLE);
                btnSaveChanges.setVisibility(View.GONE);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout2, new ChangePasswordFragment())
                    .addToBackStack(null) // da može nazad dugme
                    .commit();
        });
    }

}
