package com.example.dpm.Fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Adapter.RideAdapter;

import com.example.dpm.Model.Ride;
import com.example.dpm.R;
import com.example.dpm.Repository.RideRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DriveHistoryFragment extends Fragment {
    RecyclerView recyclerView;
    Button btnPickDate, btnReset;
    List<Ride> allDrives = new ArrayList<>();
    List<Ride> filteredDrives = new ArrayList<>();
    RideAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerRides);
        btnPickDate = view.findViewById(R.id.btnPickDate);
        btnReset = view.findViewById(R.id.btnReset);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        filteredDrives.clear();
        allDrives.clear();

        adapter = new RideAdapter(filteredDrives);
        recyclerView.setAdapter(adapter);

        RideRepository rideRepository = new RideRepository();

        //promeniti sa driver1 na id ulogovanog korisnika za kt2
        rideRepository.getPastRidesByDriver("30fda86c-f909-4f3d-88a6-97b77c6d2612", drives -> {
            if (drives != null) {
                allDrives.addAll(drives);
                filteredDrives.addAll(drives);
                adapter.notifyDataSetChanged();
            }
        });

        btnPickDate.setOnClickListener(v -> showDatePicker());

        btnReset.setOnClickListener(v -> {
            if (filteredDrives.size() != allDrives.size()) {
                filteredDrives.clear();
                filteredDrives.addAll(allDrives);
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
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

        filteredDrives.clear();

        for (Ride r : allDrives) {
            String startDate = r.getStartTime().split(" ")[0];
            String endDate = r.getEndTime().split(" ")[0];

            if (selectedDate.equals(startDate) || selectedDate.equals(endDate)) {
                filteredDrives.add(r);
            }
        }

        adapter.notifyDataSetChanged();
    }


}
