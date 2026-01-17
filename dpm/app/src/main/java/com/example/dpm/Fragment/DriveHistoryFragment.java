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
import com.example.dpm.Repository.DriveHistoryRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DriveHistoryFragment extends Fragment {
    RecyclerView recyclerView;
    Button btnPickDate, btnReset;
    List<DriveHistory> allDrives = new ArrayList<>();
    List<DriveHistory> filteredDrives = new ArrayList<>();
    DriveHistoryAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerRides);
        btnPickDate = view.findViewById(R.id.btnPickDate);
        btnReset = view.findViewById(R.id.btnReset);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        filteredDrives.clear();
        allDrives.clear();

        adapter = new DriveHistoryAdapter(filteredDrives);
        recyclerView.setAdapter(adapter);

        DriveHistoryRepository driveHistoryRepository = new DriveHistoryRepository();

        //promeniti sa driver1 na id ulogovanog korisnika za kt2
        driveHistoryRepository.getDriveHistoryByDriver("driver1", drives -> {
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
