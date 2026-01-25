package com.example.dpm.Fragment;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.dpm.Model.UserRole;
import com.example.dpm.Model.Vehicle;
import com.example.dpm.R;
import com.example.dpm.Repository.VehicleRepository;
import com.example.dpm.Session.UserSession;
import com.example.dpm.Model.User;
import com.example.dpm.Model.Driver;
import com.example.dpm.Repository.UserRepository;


public class ProfileFragment extends Fragment {

    public UserRepository userRepository;

    public VehicleRepository vehicleRepository;

    public User loggedUInUser;

    public ProfileFragment() {
        // Required empty public constructor
        userRepository = new UserRepository();
        vehicleRepository = new VehicleRepository();
        loggedUInUser = new User(); // UserSession.getInstance().getUser();
    }

    @RequiresApi(api = Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        LinearLayout driverLayout = view.findViewById(R.id.layoutDriverInfo);
        Button btnSaveChanges = view.findViewById(R.id.btnSaveChanges);

        userRepository.getUserById("lhjHXrZvqSz6QBG8Kyf2", user -> {

            if (user == null) {
                Log.d("Errors", "User doesnt exist.");;
                return;
            }

            loggedUInUser = user;

            Log.d("SESSION", user.getEmail());
        });

        fillInUserFields(view, loggedUInUser);

        if(loggedUInUser.getRole() == UserRole.DRIVER) {
            driverLayout.setVisibility(View.VISIBLE);
            btnSaveChanges.setVisibility(View.GONE);
        }
        else {
            driverLayout.setVisibility(View.GONE);
            btnSaveChanges.setVisibility(View.VISIBLE);
        }

//        if (user.getRole() == UserRole.DRIVER) {
//
//
//            VehicleRepository vehicleRepository = new VehicleRepository();
//
//        }
//        else {
//
//        }


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, new ChangePasswordFragment())
                    .addToBackStack(null) // da može nazad dugme
                    .commit();
        });
    }

    public void fillInUserFields(View view, User user) {

        ((EditText) view.findViewById(R.id.etName)).setText(user.getFirstName());
        ((EditText) view.findViewById(R.id.etLastName)).setText(user.getLastName());
        ((EditText) view.findViewById(R.id.etEmail)).setText(user.getEmail());
        ((EditText) view.findViewById(R.id.etAddress)).setText(user.getStreet() + user.getNumber().toString());
        ((EditText) view.findViewById(R.id.etPhoneNumber)).setText(user.getPhoneNumber());

        ImageView imgProfile = view.findViewById(R.id.imgProfile);

        if (user.getProfileImageUrl() == null || user.getProfileImageUrl().isEmpty()) {
            imgProfile.setImageResource(R.drawable.profile_icon);
        } else {
//            Glide.with(view.getContext())
//                    .load(user.getProfileImageUrl())
//                    .placeholder(R.drawable.profile_icon)
//                    .error(R.drawable.profile_icon)
//                    .into(imgProfile);
        }

    }

    public void fillDriverFields(User user) {

    }

}
