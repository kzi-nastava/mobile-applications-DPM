package com.example.dpm.Fragment;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Adapter.RideAdapter;
import com.example.dpm.Model.Ride;
import com.example.dpm.Model.RideLocation;
import com.example.dpm.R;
import com.example.dpm.Repository.RideRepository;
import com.example.dpm.Session.UserSession;
import com.example.dpm.Adapter.PassengerRideAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PassengerHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private Button btnPickDate, btnReset, btnSort;

    private final List<Ride> allRides = new ArrayList<>();
    private final List<Ride> filteredRides = new ArrayList<>();

    private PassengerRideAdapter adapter;
    private final UserSession session = UserSession.getInstance();

    // shake sort
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener shakeListener;
    private long lastShakeMs = 0;
    private boolean sortNewestFirst = true;

    private final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerRides);
        btnPickDate = view.findViewById(R.id.btnPickDate);
        btnReset = view.findViewById(R.id.btnReset);
        btnSort = view.findViewById(R.id.btnSort);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        filteredRides.clear();
        allRides.clear();

        adapter = new PassengerRideAdapter(filteredRides, ride -> {

            RideDetailsFragment f = RideDetailsFragment.newInstance(ride.getId());

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, f)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setAdapter(adapter);

        recyclerView.setAdapter(adapter);

        RideRepository repo = new RideRepository();

        if (session.getUser() != null) {
            String passengerId = session.getUser().getId();

            repo.getPastRidesByPassenger(passengerId, rides -> {
                if (rides == null) return;

                allRides.clear();
                filteredRides.clear();

                allRides.addAll(rides);
                sortByDate(allRides, true); // start: newest->oldest
                filteredRides.addAll(allRides);

                adapter.notifyDataSetChanged();
            });
        }

        btnPickDate.setOnClickListener(v -> showDatePicker());

        btnReset.setOnClickListener(v -> {
            filteredRides.clear();
            filteredRides.addAll(allRides);
            adapter.notifyDataSetChanged();
        });
        btnSort.setOnClickListener(v -> {

            String[] options = {
                    "Start time",
                    "End time",
                    "Price",
                    "Route"
            };

            new AlertDialog.Builder(requireContext())
                    .setTitle("Sort by")
                    .setItems(options, (d, which) -> {

                        switch(which){

                            case 0: // START
                                Collections.sort(filteredRides,
                                        (a,b)->a.getStartTime().compareTo(b.getStartTime()));
                                break;

                            case 1: // END
                                Collections.sort(filteredRides,
                                        (a,b)->a.getEndTime().compareTo(b.getEndTime()));
                                break;

                            case 2: // PRICE
                                Collections.sort(filteredRides,
                                        (a,b)->Double.compare(b.getPrice(),a.getPrice()));
                                break;

                            case 3: // ROUTE
                                Collections.sort(filteredRides,
                                        (a,b)->getRoute(a).compareTo(getRoute(b)));
                                break;
                        }

                        adapter.notifyDataSetChanged();
                    })
                    .show();

        });

        setupShake();

        return view;
    }
    private String getRoute(Ride r){

        if(r.getLocations()==null || r.getLocations().isEmpty())
            return "";

        RideLocation first=r.getLocations().get(0);
        RideLocation last=r.getLocations().get(0);

        for(RideLocation l:r.getLocations()){
            if(l.getOrderIndex()<first.getOrderIndex()) first=l;
            if(l.getOrderIndex()>last.getOrderIndex()) last=l;
        }

        return first.getAddress()+"-"+last.getAddress();
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                R.style.MyDatePickerDialog,
                (view, year, month, day) -> {
                    String selectedDate = String.format(
                            Locale.getDefault(),
                            "%02d.%02d.%d",
                            day, month + 1, year
                    );
                    filterByDate(selectedDate);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void filterByDate(String selectedDate) {

        filteredRides.clear();

        for (Ride r : allRides) {
            String startDate = safeDatePart(r.getStartTime());
            String endDate = safeDatePart(r.getEndTime());
            if (selectedDate.equals(startDate) || selectedDate.equals(endDate)) {
                filteredRides.add(r);
            }
        }

        // zadrži trenutno sortiranje posle filtriranja
        sortByDate(filteredRides, sortNewestFirst);
        adapter.notifyDataSetChanged();
    }

    private String safeDatePart(String dt) {
        if (dt == null) return "";
        String[] p = dt.split(" ");
        return p.length > 0 ? p[0] : "";
    }

    private Date parseDateSafe(String dt) {
        if (dt == null) return new Date(0);
        try {
            return df.parse(dt);
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    private void sortByDate(List<Ride> list, boolean newestFirst) {
        Comparator<Ride> cmp = (a, b) -> {
            Date da = parseDateSafe(a.getStartTime());
            Date db = parseDateSafe(b.getStartTime());
            return newestFirst ? db.compareTo(da) : da.compareTo(db);
        };
        Collections.sort(list, cmp);
    }

    private void setupShake() {
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        shakeListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float g = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

                long now = System.currentTimeMillis();
                if (g > 12f && (now - lastShakeMs) > 700) {
                    lastShakeMs = now;

                    sortNewestFirst = !sortNewestFirst;
                    sortByDate(filteredRides, sortNewestFirst);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null && shakeListener != null) {
            sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null && shakeListener != null) {
            sensorManager.unregisterListener(shakeListener);
        }
    }
}