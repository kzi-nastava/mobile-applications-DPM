package com.example.dpm.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.dpm.Model.User;
import com.example.dpm.R;
import com.example.dpm.Repository.UserRepository;
import com.example.dpm.Session.UserSession;

public class ChangePasswordFragment extends Fragment {

    private User loggedInUser;

    private UserRepository userRepository;

    public ChangePasswordFragment() {
        loadPage();
        userRepository = new UserRepository();
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

        btnChangePassword.setOnClickListener(v -> {
            changePassword(view);
        });
    }

    public void loadPage() {
        loggedInUser = UserSession.getInstance().getUser();
    }

    public void changePassword(View view) {
        String email = loggedInUser.getEmail();
        String oldPassword = ((EditText) view.findViewById(R.id.etOldPassword))
                .getText().toString().trim();
        String newPassword = ((EditText) view.findViewById(R.id.etNewPassword))
                .getText().toString().trim();
        String repeatNewPassword = ((EditText) view.findViewById(R.id.etRepeatNewPassword))
                .getText().toString().trim();
        if(newPassword.equals(repeatNewPassword)) {
            userRepository.reauthenticateAndChangePassword(email,
                    oldPassword,
                    newPassword,
                    aVoid -> {
                        Toast.makeText(getContext(), "Password successfully changed", Toast.LENGTH_SHORT).show();
                        ((EditText) view.findViewById(R.id.etOldPassword)).setText("");
                        ((EditText) view.findViewById(R.id.etNewPassword)).setText("");
                        ((EditText) view.findViewById(R.id.etRepeatNewPassword)).setText("");
                    },
                    e -> Toast.makeText(getContext(), "Old password entered incorrectly", Toast.LENGTH_LONG).show()
            );
        }
        else {
            Toast.makeText(getContext(), "New passwords aren't same", Toast.LENGTH_SHORT).show();
        }
    }

}
