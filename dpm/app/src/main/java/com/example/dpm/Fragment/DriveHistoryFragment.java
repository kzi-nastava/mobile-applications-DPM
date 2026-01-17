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

import com.example.dpm.Adapter.DriveHistoryAdapter;
import com.example.dpm.Model.DriveHistory;
import com.example.dpm.Model.Passenger;
import com.example.dpm.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DriveHistoryFragment extends Fragment {
    RecyclerView recyclerView;
    Button btnPickDate;
    List<DriveHistory> allDrives = new ArrayList<>();
    List<DriveHistory> filteredDrives = new ArrayList<>();
    DriveHistoryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerRides);
        btnPickDate = view.findViewById(R.id.btnPickDate);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // HARD-CODED DATA (kasnije menjaš iz baze)
        allDrives.add(new DriveHistory("20.12.2025 10:00", "20.12.2025 10:30", "Airport", "Center", false, "-", 1500, false, Arrays.asList(
                new Passenger("Petar", "Petrović"),
                new Passenger("Marko", "Marković")
        )));
        allDrives.add(new DriveHistory("25.12.2025 12:00", "25.12.2025 12:15", "Mall", "Station", true, "PASSENGER", 0, false,  Arrays.asList(
                new Passenger("Jovan", "Jovanović")
        )));
        allDrives.add(new DriveHistory("29.12.2025 23:30", "30.12.2025 01:45", "Novi Sad", "Belgrade", false, "-", 3200, true,  Arrays.asList(
                new Passenger("Nikola", "Jokić")
        )));

        filteredDrives.addAll(allDrives);

        adapter = new DriveHistoryAdapter(filteredDrives);
        recyclerView.setAdapter(adapter);

        btnPickDate.setOnClickListener(v -> showDatePicker());

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

        for (DriveHistory r : allDrives) {
            String startDate = r.startTime.split(" ")[0];
            String endDate = r.endTime.split(" ")[0];

            if (selectedDate.equals(startDate) || selectedDate.equals(endDate)) {
                filteredDrives.add(r);
            }
        }

        adapter.notifyDataSetChanged();
    }


}
