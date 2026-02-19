package com.example.dpm.Activity;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dpm.Adapter.VehicleAdapter;
import com.example.dpm.Model.Driver;
import com.example.dpm.Model.User;
import com.example.dpm.Model.UserRole;
import com.example.dpm.Model.Vehicle;
import com.example.dpm.Model.VehicleType;
import com.example.dpm.R;
import com.example.dpm.Repository.DriverRepository;
import com.example.dpm.Repository.VehicleRepository;
import com.example.dpm.Session.UserSession;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

public class RegisterDriverActivity extends AppCompatActivity {

    private static User loggedInUser = UserSession.getInstance().getUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // isti layout

        findViewById(R.id.register_password).setVisibility(View.GONE);
        findViewById(R.id.editTextTextPassword).setVisibility(View.GONE);
        findViewById(R.id.register_repeatpassword).setVisibility(View.GONE);
        findViewById(R.id.register_repeatpassword_input).setVisibility(View.GONE);

        // DRIVER vehicle section
        LinearLayout vehicleSection = findViewById(R.id.driver_vehicle_section);
        vehicleSection.setVisibility(View.VISIBLE);

        // "Already have an account?" section
        LinearLayout passengerLogin = findViewById(R.id.passenger_login);
        passengerLogin.setVisibility(View.GONE);

        Spinner spinner = findViewById(R.id.vehicle_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.vehicle_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        Button registerBtn = findViewById(R.id.register_next_button);
        registerBtn.setOnClickListener(v -> registerDriver());

    }

    private void registerDriver() {

        // USER FIELDS
        EditText email = findViewById(R.id.register_email_input);
        EditText firstName = findViewById(R.id.register_firstname_input);
        EditText lastName = findViewById(R.id.register_lastname_input);
        EditText phone = findViewById(R.id.register_phone_input);
        EditText country = findViewById(R.id.country_auto_complete);
        EditText city = findViewById(R.id.register_city_input);
        EditText street = findViewById(R.id.register_street_input);
        EditText number = findViewById(R.id.register_number_input);

        // VEHICLE FIELDS
        EditText vehicleModel = findViewById(R.id.vehicle_model_input);
        EditText vehiclePlate = findViewById(R.id.vehicle_plate_input);
        EditText vehicleSeats = findViewById(R.id.vehicle_seats_input);
        Spinner vehicleType = findViewById(R.id.vehicle_type_spinner);
        CheckBox baby = findViewById(R.id.vehicle_baby_checkbox);
        CheckBox pet = findViewById(R.id.vehicle_pet_checkbox);

        // EMAIL
        if (isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            showError("Invalid email");
            return;
        }

        // BASIC INFO
        if (isEmpty(firstName) || isEmpty(lastName)) {
            showError("First and last name are required");
            return;
        }

        if (isEmpty(phone)) {
            showError("Phone number is required");
            return;
        }

        if (isEmpty(country) || isEmpty(city) || isEmpty(street) || isEmpty(number)) {
            showError("Address fields are required");
            return;
        }

        // VEHICLE
        if (isEmpty(vehicleModel)) {
            showError("Vehicle model is required");
            return;
        }

        if (vehicleType.getSelectedItemPosition() == 0) {
            Toast.makeText(this,
                    "Please select vehicle type",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEmpty(vehiclePlate)) {
            showError("License plate is required");
            return;
        }

        if (isEmpty(vehicleSeats) || Integer.parseInt(vehicleSeats.getText().toString()) <= 0) {
            showError("Invalid number of seats");
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email.getText().toString().trim(), UUID.randomUUID().toString())
                .addOnSuccessListener(authResult -> {

                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        Toast.makeText(this, "Auth error: user null", Toast.LENGTH_LONG).show();
                        return;
                    }

                    firebaseUser.updatePassword("TEMP_DISABLED_PASSWORD");

                    Driver driver = new Driver();
                    driver.setId(firebaseUser.getUid());
                    driver.setEmail(email.getText().toString().trim());

                    driver.setFirstName(firstName.getText().toString().trim());
                    driver.setLastName(lastName.getText().toString().trim());
                    driver.setPhoneNumber(phone.getText().toString().trim());

                    driver.setCountry(country.getText().toString().trim());
                    driver.setCity(city.getText().toString().trim());
                    driver.setStreet(street.getText().toString().trim());
                    driver.setNumber(number.getText().toString().trim());

                    driver.setActive(false);
                    driver.setBlocked(false);
                    driver.setRole(UserRole.DRIVER);

                    DriverRepository driverRepository = new DriverRepository();
                    driverRepository.addDriver(driver);

                    addVehicle(driver.getId(), vehicleModel, vehiclePlate, vehicleSeats, vehicleType, baby, pet);

                    sendDriverActivationEmail(driver);

                    Toast.makeText(this, "Email is sent to set a password", Toast.LENGTH_LONG).show();

                    startActivity(
                            new Intent(this, MainActivity.class)
                    );
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Driver creation failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private boolean isEmpty(EditText et) {
        return et.getText().toString().trim().isEmpty();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void sendDriverActivationEmail(Driver driver) {

        FirebaseAuth.getInstance()
                .sendPasswordResetEmail(driver.getEmail())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(
                            this,
                            "Activation email sent to driver",
                            Toast.LENGTH_LONG
                    ).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Failed to send activation email: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    private void addVehicle(String uId, EditText vehicleModel, EditText vehiclePlate, EditText vehicleSeats, Spinner vehicleType, CheckBox baby, CheckBox pet) {
        Vehicle vehicle = new Vehicle();

        vehicle.setDriverId(uId);
        vehicle.setModel(vehicleModel.getText().toString().trim());
        vehicle.setPlateNumber(vehiclePlate.getText().toString().trim());
        vehicle.setSeats(Integer.parseInt(vehicleSeats.getText().toString().trim()));

        vehicle.setType(getVehicleType(vehicleType));
        vehicle.setBabyFriendly(baby.isChecked());
        vehicle.setPetFriendly(pet.isChecked());

        vehicle.setBusy(false);

        vehicle.setLatitude(40);
        vehicle.setLongitude(22);

        VehicleRepository vehicleRepository = new VehicleRepository();
        vehicleRepository.addVehicle(vehicle);
    }

    private VehicleType getVehicleType(Spinner spinner) {
        if(spinner.getSelectedItemPosition() == 1) {
            return VehicleType.STANDARD;
        }
        else if(spinner.getSelectedItemPosition() == 2) {
            return VehicleType.LUXURY;
        }
        return VehicleType.VAN;
    }

}
