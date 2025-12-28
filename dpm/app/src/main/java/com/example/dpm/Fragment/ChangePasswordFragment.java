package com.example.dpm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class ChangePasswordFragment extends Fragment {

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnBack = view.findViewById(R.id.btnBack);
        Button btnChangePassword = view.findViewById(R.id.btnChangePassword);

        // Nazad dugme
        btnBack.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().popBackStack();
        });

        // Promeni lozinku dugme (samo GUI, vraća na profil)
        btnChangePassword.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().popBackStack();
        });
    }

}
