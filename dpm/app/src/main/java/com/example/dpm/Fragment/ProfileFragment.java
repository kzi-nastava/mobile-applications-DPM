package com.example.dpm.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.dpm.Model.ChangeDataRequest;
import com.example.dpm.Model.FirebaseProvider;
import com.example.dpm.Model.RequestStatus;
import com.example.dpm.Model.UserRole;
import com.example.dpm.Model.Vehicle;
import com.example.dpm.R;
import com.example.dpm.Repository.ChangeDataRequestRepository;
import com.example.dpm.Repository.VehicleRepository;
import com.example.dpm.Session.UserSession;
import com.example.dpm.Model.User;
import com.example.dpm.Model.Driver;
import com.example.dpm.Repository.UserRepository;

import com.example.dpm.Util.ImageUtil;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;

import java.io.IOException;


public class ProfileFragment extends Fragment {

    public UserRepository userRepository;

    public VehicleRepository vehicleRepository;
    
    public ChangeDataRequestRepository changeDataRequestRepository;

    private static User loggedInUser = UserSession.getInstance().getUser();

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView imgProfile;

    public ProfileFragment() {
        userRepository = new UserRepository();
        vehicleRepository = new VehicleRepository();
        changeDataRequestRepository = new ChangeDataRequestRepository();
        loadPage();
    }
    
    @RequiresApi(api = Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loadPage();

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgProfile = view.findViewById(R.id.imgProfile);

        Button btnAddImage = view.findViewById(R.id.btnAddImage);
        Button btnChangeImage = view.findViewById(R.id.btnChangeImage);

        btnAddImage.setOnClickListener(v -> openGallery());
        btnChangeImage.setOnClickListener(v -> uploadProfileImage());


        LinearLayout driverLayout = view.findViewById(R.id.layoutDriverInfo);
        Button btnSaveChanges = view.findViewById(R.id.btnSaveChanges);

        if(loggedInUser == null) {
            Log.d("Error", "User doesn't exist!");
        }

        fillInUserFields(view, loggedInUser);

        if(loggedInUser.getRole() == UserRole.DRIVER) {
            driverLayout.setVisibility(View.VISIBLE);
            btnSaveChanges.setVisibility(View.GONE);
            fillDriverFields(view, (Driver) loggedInUser);
            Button btnRequestChange = view.findViewById(R.id.btnRequestChange);
            btnRequestChange.setOnClickListener(v -> {
                onRequestChangeClicked(view);
            });
        }
        else {
            driverLayout.setVisibility(View.GONE);
            btnSaveChanges.setVisibility(View.VISIBLE);
            btnSaveChanges.setOnClickListener(v -> {
                onSaveChangesClicked(view);
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        loadPage();
        Button btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, new ChangePasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    //TODO: Odraditi slike
    public void fillInUserFields(View view, User user) {

        ((EditText) view.findViewById(R.id.etName)).setText(user.getFirstName());
        ((EditText) view.findViewById(R.id.etLastName)).setText(user.getLastName());
        ((EditText) view.findViewById(R.id.etEmail)).setText(user.getEmail());
        ((EditText) view.findViewById(R.id.etCountry)).setText(user.getCountry());
        ((EditText) view.findViewById(R.id.etCity)).setText(user.getCity());
        ((EditText) view.findViewById(R.id.etStreet)).setText(user.getStreet());
        ((EditText) view.findViewById(R.id.etNumber)).setText(user.getNumber());
        ((EditText) view.findViewById(R.id.etPhoneNumber)).setText(user.getPhoneNumber());

        imgProfile = view.findViewById(R.id.imgProfile);

        imgProfile.setImageResource(R.drawable.profile_icon);

        String url = user.getProfileImageUrl();

        if (url != null &&
                !url.trim().isEmpty() &&
                !url.trim().equalsIgnoreCase("DEFAULT")) {

            Bitmap bitmap = ImageUtil.base64ToBitmap(url);
            imgProfile.setImageBitmap(bitmap);
        }
    }
    public void fillDriverFields(View view, Driver driver) {

        vehicleRepository.getVehicleByDriverId(driver.getId(), vehicle -> {

            if (vehicle == null) {
                Log.d("Error", "Vehicle doesn't exist.");
                return;
            }

            ((TextView) view.findViewById(R.id.tvActiveHours)).setText("Active hours in last 24h: " +  driver.workingHoursLast24h());

            ((TextView) view.findViewById(R.id.tvModel)).setText("Model: " + vehicle.getModel());

            ((TextView) view.findViewById(R.id.tvRegistration)).setText("Registration: " + vehicle.getPlateNumber());
            String seats = "Seats: " + vehicle.getSeats();
            ((TextView) view.findViewById(R.id.tvSeats)).setText(seats);

            String petFriendly = "";
            if(vehicle.isPetFriendly()) petFriendly = "Pet friendly: YES";
            else petFriendly = "Pet friendly: NO";
            ((TextView) view.findViewById(R.id.tvPetFreindly)).setText(petFriendly);

            String babyFriendly = "";
            if(vehicle.isBabyFriendly()) babyFriendly = "Baby friendly: YES";
            else babyFriendly = "Baby friendly: NO";
            ((TextView) view.findViewById(R.id.tvBabyFriendly)).setText(babyFriendly);

        });
    }

    private void onRequestChangeClicked(View view) {

        ChangeDataRequest updateUser = new ChangeDataRequest("",
                loggedInUser.getId(),
                text(view, R.id.etCity),
                text(view, R.id.etCountry),
                text(view, R.id.etStreet),
                text(view, R.id.etNumber),
                text(view, R.id.etEmail),
                text(view, R.id.etName),
                text(view, R.id.etLastName),
                text(view, R.id.etPhoneNumber),
                RequestStatus.PENDING
        );

        fillInUserFields(view, loggedInUser);

        changeDataRequestRepository.create(
                updateUser,
                unused -> Toast.makeText(
                        getContext(),
                        "Change request sent successfully",
                        Toast.LENGTH_SHORT
                ).show(),
                e -> Toast.makeText(
                        getContext(),
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show()
        );
    }

    private String text(View v, int id) {
        return ((EditText) v.findViewById(id))
                .getText().toString().trim();
    }

    private void onSaveChangesClicked(View view) {

        userRepository.updateUserData(
                loggedInUser.getId(),

                text(view, R.id.etCity),
                text(view, R.id.etCountry),
                text(view, R.id.etStreet),
                text(view, R.id.etNumber),
                text(view, R.id.etEmail),
                text(view, R.id.etName),
                text(view, R.id.etLastName),
                text(view, R.id.etPhoneNumber),

                unused -> {
                    Toast.makeText(
                            getContext(),
                            "Data are succesfully changed.",
                            Toast.LENGTH_SHORT
                    ).show();
                    userRepository.getUserById(loggedInUser.getId(), user -> {
                        UserSession.getInstance().setUser(user);
                    });
                },

                e -> {
                    Toast.makeText(
                            getContext(),
                            "Errors: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
        );
    }

    private void loadPage() {
        loggedInUser = UserSession.getInstance().getUser();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                imgProfile.setImageURI(imageUri);
            }
        }
    }

    private void uploadProfileImage() {

        if (imageUri == null) {
            Toast.makeText(getContext(), "Select image first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().getContentResolver(),
                    imageUri
            );

            String imageBase64 = ImageUtil.bitmapToBase64(bitmap);

            FirebaseProvider.getDb()
                    .collection("users")
                    .document(loggedInUser.getId())
                    .update("profileImageUrl", imageBase64)
                    .addOnSuccessListener(unused -> {

                        Toast.makeText(getContext(),
                                "Profile image updated!",
                                Toast.LENGTH_SHORT).show();

                        imgProfile.setImageBitmap(bitmap);

                        // update session
                        loggedInUser.setProfileImageUrl(imageBase64);
                        UserSession.getInstance().setUser(loggedInUser);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(),
                                    e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(),
                    "Failed to process image",
                    Toast.LENGTH_LONG).show();
        }
    }



}
