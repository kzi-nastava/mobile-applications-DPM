package com.example.dpm.Fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Adapter.PassengerEmailAdapter;
import com.example.dpm.Adapter.VehicleAdapter;

import com.example.dpm.Model.Driver;
import com.example.dpm.Model.Notification;
import com.example.dpm.Model.Passenger;
import com.example.dpm.Model.PriceConfig;
import com.example.dpm.Model.Ride;
import com.example.dpm.Model.RideLocation;
import com.example.dpm.Model.RidePricingSnapshot;
import com.example.dpm.Model.RideStatus;
import com.example.dpm.Model.User;
import com.example.dpm.Model.UserRole;
import com.example.dpm.Model.Vehicle;
import com.example.dpm.Model.VehicleType;
import com.example.dpm.R;

import com.example.dpm.Repository.DriverRepository;
import com.example.dpm.Repository.NotificationRepository;
import com.example.dpm.Repository.PassengerRepository;
import com.example.dpm.Repository.PriceRepository;
import com.example.dpm.Repository.RideEstimateRepository;
import com.example.dpm.Repository.RideRepository;
import com.example.dpm.Repository.VehicleRepository;
import com.example.dpm.Session.UserSession;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.DocumentId;


import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class HomePageFragment extends Fragment {

    private User loggedInUser;

    private RecyclerView vehicleListRecycler;
    private VehicleAdapter vehicleAdapter;
    private VehicleRepository vehicleRepository;
    private MapView map;
    private RideEstimateRepository rideEstimateRepository;

    private final Set<Overlay> routeOverlays = new HashSet<>();
    private final Set<Marker> vehicleMarkers = new HashSet<>();

    private com.google.android.material.card.MaterialCardView estimateCard;
    private android.widget.TextView tvEstimateBody;
    private boolean routeVisible = false;

    private LinearLayout passengersSection;
    private EditText etPassengerEmail;
    private RecyclerView rvPassengers;
    private Button btnAddPassenger;

    private List<String> passengerEmails = new ArrayList<>();
    private PassengerEmailAdapter passengerAdapter;

    private Spinner spVehicleType;
    private CheckBox cbBaby;
    private CheckBox cbPet;
    private TextView tvRidePrice;

    private double currentDistanceKm = 0;

    private long price = 0;

    private RidePricingSnapshot ridePricingSnapshot = null;

    private Button btnRideNow;
    private Button btnRideLater;

    private List<GeoPoint> points;

    public String startAddress, endAddress;

    public List<String> stationsAddress;

    private PassengerRepository passengerRepository;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        Configuration.getInstance().load(
                requireContext(),
                requireContext().getSharedPreferences("osmdroid", 0)
        );
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        DriverRepository driverRepo = new DriverRepository();
        RideRepository rideRepo = new RideRepository();

//        PassengerRepository passengerRepo = new PassengerRepository();
        passengerRepository = new PassengerRepository();

        loggedInUser = UserSession.getInstance().getUser();

        points = new ArrayList<>();

        ridePricingSnapshot = new RidePricingSnapshot();

        startAddress = null;
        endAddress = null;
        stationsAddress = null;

        return view;



    }
    private void clearRouteOverlays() {
        for (Overlay o : routeOverlays) {
            map.getOverlays().remove(o);
        }
        routeOverlays.clear();
    }

    private void clearVehicleMarkers() {
        for (Marker m : vehicleMarkers) {
            map.getOverlays().remove(m);
        }
        vehicleMarkers.clear();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        map = view.findViewById(R.id.map);
        map.setMultiTouchControls(true);
        map.getController().setZoom(13.0);
        map.getController().setCenter(new GeoPoint(44.7866, 20.4489));


        vehicleListRecycler = view.findViewById(R.id.vehicle_list);
        vehicleListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        vehicleRepository = new VehicleRepository();

        loadVehicles();
        estimateCard = view.findViewById(R.id.estimateCard);
        tvEstimateBody = view.findViewById(R.id.tvEstimateBody);

        rideEstimateRepository = new RideEstimateRepository();
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        passengersSection = view.findViewById(R.id.layoutPassengersSection);
        etPassengerEmail = view.findViewById(R.id.etPassengerEmail);
        btnAddPassenger = view.findViewById(R.id.btnAddPassenger);
        rvPassengers = view.findViewById(R.id.rvPassengers);

        rvPassengers.setLayoutManager(new LinearLayoutManager(getContext()));
        passengerAdapter = new PassengerEmailAdapter(passengerEmails);
        rvPassengers.setAdapter(passengerAdapter);

        spVehicleType = view.findViewById(R.id.spVehicleType);
        cbBaby = view.findViewById(R.id.cbBaby);
        cbPet = view.findViewById(R.id.cbPet);
        tvRidePrice = view.findViewById(R.id.tvRidePrice);

        List<String> vehicleTypes = new ArrayList<>();
        vehicleTypes.add("STANDARD");
        vehicleTypes.add("LUXURY");
        vehicleTypes.add("VAN");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item,
                        vehicleTypes);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVehicleType.setAdapter(adapter);

        btnRideNow = view.findViewById(R.id.btnRideNow);
        btnRideLater = view.findViewById(R.id.btnRideLater);

        btnRideNow.setOnClickListener(v -> {
            assignDriverNow();
        });

        btnRideLater.setOnClickListener(v -> {
            showScheduleDialog();
        });

        btnAddPassenger.setOnClickListener(v -> {

            String email = etPassengerEmail.getText().toString().trim();

            if(email.isEmpty()){
                etPassengerEmail.setError("Enter email");
                return;
            }

            if(passengerEmails.contains(email)){
                etPassengerEmail.setError("Already added");
                return;
            }

            PassengerRepository passengerRepo = new PassengerRepository();

            passengerRepo.getPassengerByEmail(
                    email,
                    passenger -> {

                        if (passenger == null) {
                            etPassengerEmail.setError("Passenger not found");
                            return;
                        }

                        if (passenger.getId().equals(loggedInUser.getId())) {
                            etPassengerEmail.setError("You cannot add yourself");
                            return;
                        }

                        if (passengerEmails.contains(email)) {
                            etPassengerEmail.setError("Already added");
                            return;
                        }

                        passengerEmails.add(email);
                        passengerAdapter.notifyDataSetChanged();
                        etPassengerEmail.setText("");
                    },
                    e -> etPassengerEmail.setError("Error checking passenger")
            );

            etPassengerEmail.setText("");
        });

        spVehicleType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabEstimate);
        ImageButton close = view.findViewById(R.id.btnCloseEstimate);
        close.setOnClickListener(v -> {
            clearRouteOverlays();
            estimateCard.setVisibility(View.GONE);

            passengersSection.setVisibility(View.GONE);
            passengerEmails.clear();
            passengerAdapter.notifyDataSetChanged();

            vehicleListRecycler.setVisibility(View.VISIBLE);

            points = new ArrayList<>();
            startAddress = null;
            endAddress = null;
            stationsAddress = null;

            routeVisible = false;
            map.invalidate();
        });
        fab.setOnClickListener(v -> {

            // ako ruta već postoji → samo je skloni
            if(routeVisible){
                clearRouteOverlays();
                estimateCard.setVisibility(View.GONE);

                passengersSection.setVisibility(View.GONE);
                passengerEmails.clear();
                passengerAdapter.notifyDataSetChanged();

                vehicleListRecycler.setVisibility(View.VISIBLE);

                points = new ArrayList<>();
                startAddress = null;
                endAddress = null;
                stationsAddress = null;

                routeVisible = false;
                map.invalidate();
            }

            // inače otvori dialog za procenu
            new RideEstimateDialogFragment((from, to, stations) -> {

                startAddress = from;
                endAddress = to;
                stationsAddress = stations;

                rideEstimateRepository.estimate(
                        requireContext(),
                        from,
                        to,
                        stations,
                        new RideEstimateRepository.Callback(){

                            @Override
                            public void onSuccess(com.example.dpm.Model.RideEstimate e){

                                clearRouteOverlays();

                                Marker start = new Marker(map);
                                start.setPosition(e.getFromPoint());
                                start.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                start.setTitle("Start");
                                start.setSnippet("Ride starting point");
                                start.showInfoWindow();

                                // Stanice
                                int counter = 1;

                                for (GeoPoint p : e.getWaypoints()) {
                                    points.add(p);
                                    // preskoči prvi (start) i poslednji (dest)
                                    if (p.equals(e.getFromPoint()) || p.equals(e.getToPoint()))
                                        continue;

                                    Marker stationMarker = new Marker(map);
                                    stationMarker.setPosition(p);
                                    stationMarker.setTitle("Stop " + counter);
                                    stationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                                    map.getOverlays().add(stationMarker);
                                    routeOverlays.add(stationMarker);

                                    counter++;
                                }


                                Marker end = new Marker(map);
                                end.setPosition(e.getToPoint());
                                end.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                end.setTitle("Destination");
                                end.setSnippet("Ride destination");
                                end.showInfoWindow();


                                Polyline line = new Polyline();
                                line.setPoints(e.getRoutePoints());
                                line.getOutlinePaint().setStrokeWidth(10f);

                                map.getOverlays().add(line);
                                map.getOverlays().add(start);
                                map.getOverlays().add(end);

                                routeOverlays.add(line);
                                routeOverlays.add(start);
                                routeOverlays.add(end);

                                estimateCard.setVisibility(View.VISIBLE);
                                tvEstimateBody.setText(
                                        "Distance: "+String.format("%.2f",e.getDistanceKm())+" km\n"+
                                                "Time: "+String.format("%.0f",e.getDurationMin())+" min"
                                );

                                routeVisible = true;
                                map.invalidate();

                                if(loggedInUser != null) {
                                    passengersSection.setVisibility(View.VISIBLE);
                                    vehicleListRecycler.setVisibility(View.GONE);
                                }

                                currentDistanceKm = e.getDistanceKm();
                                updatePrice();

                            }

                            @Override
                            public void onError(Exception ex){
                                estimateCard.setVisibility(View.GONE);
                            }
                        });

            }).show(getChildFragmentManager(),"estimate");

        });

    }

    private void showScheduleDialog() {

        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {

                    TimePickerDialog timePicker = new TimePickerDialog(
                            requireContext(),
                            (timeView, hour, minute) -> {

                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, month, dayOfMonth, hour, minute);

                                long now = System.currentTimeMillis();
                                long maxAllowed = now + (5 * 60 * 60 * 1000); // 5 sati u ms

                                long selected = calendar.getTimeInMillis();

                                if (selected <= now) {
                                    Toast.makeText(getContext(),
                                            "Select future time",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (selected > maxAllowed) {
                                    Toast.makeText(getContext(),
                                            "Ride can be scheduled only within next 5 hours",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }


                                createScheduledRide(calendar);

                            },
                            Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                            Calendar.getInstance().get(Calendar.MINUTE),
                            true
                    );

                    timePicker.show();
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );

        datePicker.show();
    }

    private void createScheduledRide(Calendar calendar) {

        Ride ride = new Ride();

        ride.setPassengerId(loggedInUser.getId());
        ride.setDistance(currentDistanceKm);
        ride.setPrice(price);
        ride.setPricingSnapshot(ridePricingSnapshot);

        ride.setDriverId(null);
        ride.setVehicleId(null);

        ride.setStatus(RideStatus.SCHEDULED);
        ride.setPanicTriggered(false);

        SimpleDateFormat sdf =
                new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

        ride.setScheduledAt(sdf.format(calendar.getTime()));

        // Lokacije
        List<RideLocation> locations = new ArrayList<>();

        for (int i = 0; i < points.size(); i++) {

            if (i == 0) {
                locations.add(new RideLocation(
                        startAddress,
                        points.get(i).getLatitude(),
                        points.get(i).getLongitude(),
                        i
                ));
            }
            else if (i == points.size() - 1) {
                locations.add(new RideLocation(
                        endAddress,
                        points.get(i).getLatitude(),
                        points.get(i).getLongitude(),
                        i
                ));
            }
            else if (stationsAddress != null && stationsAddress.size() >= i) {
                locations.add(new RideLocation(
                        stationsAddress.get(i - 1),
                        points.get(i).getLatitude(),
                        points.get(i).getLongitude(),
                        i
                ));
            }
        }

        ride.setLocations(locations);
        ride.setLinkedPassengerIds(new ArrayList<>());

        saveRide(ride);
    }


    private void loadVehicles() {
        try {
            vehicleRepository.getAllVehicles(vehicles -> {
                try {
                    clearVehicleMarkers();

                    for (Vehicle v : vehicles) {
                        Marker marker = new Marker(map);
                        map.getOverlays().add(marker);
                        vehicleMarkers.add(marker);
                        marker.setPosition(v.getPosition());
                        marker.setTitle(v.getModel());

                        int statusColor = v.isBusy() ?
                                getResources().getColor(R.color.red) :
                                getResources().getColor(R.color.green);
                        String hexColor = String.format("#%06X", (0xFFFFFF & statusColor));
                        marker.setSubDescription(
                                "Plate: " + v.getPlateNumber() + "<br>" +
                                        "Status: <font color='" + hexColor + "'>" +
                                        (v.isBusy() ? "BUSY" : "FREE") + "</font>"
                        );

                        marker.setIcon(v.isBusy() ?
                                getResources().getDrawable(R.drawable.location_red, null) :
                                getResources().getDrawable(R.drawable.location_green, null));
                        map.getOverlays().add(marker);
                    }
                    map.invalidate();

                    vehicleAdapter = new VehicleAdapter(vehicles);
                    vehicleListRecycler.setAdapter(vehicleAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updatePrice() {

        if(currentDistanceKm == 0){
            tvRidePrice.setText("Price: 0 RSD");
            return;
        }

        PriceRepository priceRepository = new PriceRepository();

        priceRepository.getCurrentPricing(priceConfig -> {

            if(priceConfig == null){
                tvRidePrice.setText("Price: 0 RSD");
                return;
            }

            price = 0;

            String type = spVehicleType.getSelectedItem().toString();
            ridePricingSnapshot.setVehicleType(VehicleType.valueOf(VehicleType.class, type));


            Long base = priceConfig.getPricePerVehicleType().get(type);
            if(base != null){
                price += base;
                ridePricingSnapshot.setPricePerType(base);
            }

            price += Math.round(currentDistanceKm * priceConfig.getPricePerKm());
            ridePricingSnapshot.setPricePerKm(priceConfig.getPricePerKm());

            tvRidePrice.setText("Price: " + price + " RSD");
        });
    }

    private void proceedWithVehicleSelection(List<Vehicle> freeVehicles){

        if(freeVehicles.isEmpty()){
            showNoDriverNotification("There are currently no active drivers.");
            return;
        }

        List<Vehicle> nonBusyVehicles = new ArrayList<>();
        for(Vehicle v: freeVehicles){
            if(!v.isBusy()) nonBusyVehicles.add(v);
        }

        List<Vehicle> candidates =
                nonBusyVehicles.isEmpty() ? freeVehicles : nonBusyVehicles;

        if(points == null || points.isEmpty()){
            Toast.makeText(getContext(),"Problem with start point.",Toast.LENGTH_SHORT).show();
            return;
        }

        GeoPoint start = points.get(0);
        Vehicle selected = findNearestDriver(candidates, start);

        if(selected != null) createRide(selected);
    }

    private void checkFreeVehicles(List<Vehicle> activeDriverVehicles){

        RideRepository rideRepository = new RideRepository();

        List<Vehicle> freeVehicles = new ArrayList<>();
        AtomicInteger checked = new AtomicInteger(0);

        for (Vehicle v : activeDriverVehicles) {

            if (v.isBusy()) {

                rideRepository.hasScheduledRideInNext5Hours(v.getDriverId(), check -> {

                    if (!check) freeVehicles.add(v);

                    if (checked.incrementAndGet() == activeDriverVehicles.size()) {
                        proceedWithVehicleSelection(freeVehicles);
                    }
                });

            } else {

                freeVehicles.add(v);

                if (checked.incrementAndGet() == activeDriverVehicles.size()) {
                    proceedWithVehicleSelection(freeVehicles);
                }
            }
        }
    }

    private void assignDriverNow() {
        DriverRepository driverRepository = new DriverRepository();
        RideRepository rideRepository = new RideRepository();
        vehicleRepository = new VehicleRepository();

        int numberOfPassengers = passengerEmails.size();

        VehicleType vehicleTypeSelected;
        if(spVehicleType.getSelectedItemPosition() == 1) {
            vehicleTypeSelected = VehicleType.LUXURY;
        }
        else if(spVehicleType.getSelectedItemPosition() == 2) {
            vehicleTypeSelected = VehicleType.VAN;
        } else {
            vehicleTypeSelected = VehicleType.STANDARD;
        }
        boolean babyCheck = cbBaby.isChecked();
        boolean petCheck = cbPet.isChecked();

        vehicleRepository.getAllVehicles(vehicles -> {
            List<Vehicle> selectedVehicles = new ArrayList<>();

            for (Vehicle v : vehicles) {
                if(v.isPetFriendly() == petCheck) {
                    if(v.isBabyFriendly() == babyCheck) {
                        if(v.getSeats() >= numberOfPassengers + 1) {
                            if(v.getType().toString().equals(vehicleTypeSelected.toString())) {
                                selectedVehicles.add(v);
                            }
                        }
                    }
                }
            }

            if(selectedVehicles.isEmpty()) {
                Toast.makeText(getContext(), "There is no such vehicle.", Toast.LENGTH_SHORT);
            }
            else {

                List<Vehicle> activeDriverVehicles = new ArrayList<>();
                AtomicInteger checked = new AtomicInteger(0);

                for (Vehicle v : selectedVehicles) {

                    driverRepository.getDriverById(v.getDriverId(), driver -> {

                        if (driver != null && driver.isActive()) {
                            activeDriverVehicles.add(v);
                        }

                        if (checked.incrementAndGet() == selectedVehicles.size()) {

                            if (activeDriverVehicles.isEmpty()) {
                                showNoDriverNotification("There are currently no active drivers.");
                                return;
                            }

                            checkFreeVehicles(activeDriverVehicles);
                        }

                    });
                }

                if(activeDriverVehicles.isEmpty()) {
                    showNoDriverNotification("There are currently no active drivers.");
                    return;
                }

                List<Vehicle> freeVehicles = new ArrayList<>();
                for(Vehicle v: activeDriverVehicles) {
                    if(v.isBusy()) {
                        rideRepository.hasScheduledRideInNext5Hours(v.getDriverId(), check -> {
                            if(!check) {
                                freeVehicles.add(v);
                            }
                        });
                    }
                    else {
                        freeVehicles.add(v);
                    }
                }


                if(!freeVehicles.isEmpty()) {
                    List<Vehicle> nonBusyVehicles = new ArrayList<>();
                    for(Vehicle v: freeVehicles) {
                        if(!v.isBusy()) {
                            nonBusyVehicles.add(v);
                        }
                    }
                    if(nonBusyVehicles.isEmpty()) {
                        if (!points.isEmpty()) {
                            GeoPoint start = points.get(0);
                            Vehicle selected = findNearestDriver(freeVehicles, start);
                            createRide(selected);
                        }
                        else {
                            Toast.makeText(getContext(), "Problem with start point.", Toast.LENGTH_SHORT);
                        }
                    }
                    else {
                        if (!points.isEmpty()) {
                            GeoPoint start = points.get(0);
                            Vehicle selected = findNearestDriver(nonBusyVehicles, start);
                            createRide(selected);
                        }
                        else {
                            Toast.makeText(getContext(), "Problem with start point.", Toast.LENGTH_SHORT);
                        }
                    }
                }
                else {
                    showNoDriverNotification("There are currently no active drivers.");
                }

            }
        });

    }

    public void showNoDriverNotification(String message) {
        NotificationRepository notificationRepository = new NotificationRepository();

        Notification n = new Notification();
        n.setUserId(loggedInUser.getId());
        n.setMessage(message);
        n.setRead(false);
        n.setCreatedAt(System.currentTimeMillis());
        n.setType(null);
        n.setRideId(null);

        notificationRepository.createNotification(n);
    }

    private Vehicle findNearestDriver(List<Vehicle> vehicles, GeoPoint startPoint){

        if(vehicles.isEmpty()) return null;

        Vehicle nearest = null;

        double minDistance = Double.MAX_VALUE;

        for(Vehicle v: vehicles){

            double distance = startPoint.distanceToAsDouble(v.getPosition());

            if(distance < minDistance){
                minDistance = distance;
                nearest = v;
            }
        }

        return nearest;
    }
    private void saveRide(Ride ride){
        RideRepository repo = new RideRepository();
        repo.addRide(
                ride,
                u -> {
                    Toast.makeText(getContext(),"Ride created!",Toast.LENGTH_SHORT).show();
                    NotificationRepository notificationRepository = new NotificationRepository();
                    Notification n = new Notification();
                    n.setUserId(loggedInUser.getId());
                    n.setMessage("Ride has created.");
                    n.setRead(false);
                    n.setCreatedAt(System.currentTimeMillis());
                    n.setType("RIDE_CREATED");
                    n.setRideId(ride.getId());

                    notificationRepository.createNotification(n);
                },
                e -> Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show()
        );
    }


    public void createRide(Vehicle vehicle) {

        Ride ride = new Ride();

        ride.setDriverId(vehicle.getDriverId());
        ride.setVehicleId(vehicle.getId());
        ride.setDistance(currentDistanceKm);
        ride.setPrice(price);
        ride.setPricingSnapshot(ridePricingSnapshot);
        ride.setCancelReason(null);
        ride.setCancelledBy(null);
        ride.setStartTime(null);
        ride.setEndTime(null);

        // GLAVNI PASSENGER
        ride.setPassengerId(loggedInUser.getId());

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        ride.setScheduledAt(sdf.format(new Date()));

        ride.setPanicTriggered(false);
        ride.setStatus(RideStatus.ACCEPTED);

        // ---------- LOCATIONS ----------
        List<RideLocation> locations = new ArrayList<>();

        for (int i = 0; i < points.size(); i++) {

            if (i == 0) {
                locations.add(new RideLocation(
                        startAddress,
                        points.get(i).getLatitude(),
                        points.get(i).getLongitude(),
                        i
                ));
            }
            else if (i == points.size() - 1) {
                locations.add(new RideLocation(
                        endAddress,
                        points.get(i).getLatitude(),
                        points.get(i).getLongitude(),
                        i
                ));
            }
            else if (stationsAddress != null && stationsAddress.size() >= i) {
                locations.add(new RideLocation(
                        stationsAddress.get(i - 1),
                        points.get(i).getLatitude(),
                        points.get(i).getLongitude(),
                        i
                ));
            }
        }

        ride.setLocations(locations);

        // ---------- LINKED PASSENGERS (ASYNC) ----------
        if (passengerEmails.isEmpty()) {

            ride.setLinkedPassengerIds(new ArrayList<>());
            saveRide(ride);
            return;
        }

        List<String> ids = new ArrayList<>();
        AtomicInteger remaining = new AtomicInteger(passengerEmails.size());

        for (String email : passengerEmails) {

            passengerRepository.getPassengerByEmail(email, passenger -> {

                if (passenger != null) {
                    ids.add(passenger.getId());
                }

                if (remaining.decrementAndGet() == 0) {
                    ride.setLinkedPassengerIds(ids);
                    saveRide(ride);   // SNIMI TEK SADA
                }

            }, e -> {

                if (remaining.decrementAndGet() == 0) {
                    ride.setLinkedPassengerIds(ids);
                    saveRide(ride);
                }

            });
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}