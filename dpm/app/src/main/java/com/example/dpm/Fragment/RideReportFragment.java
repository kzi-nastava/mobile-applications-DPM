package com.example.dpm.Fragment;

import android.os.Bundle;
import androidx.core.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dpm.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class RideReportFragment extends Fragment {

    private Button btnDateRange;
    private LineChart lineChart;

    private long startDateMillis;
    private long endDateMillis;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ride_report, container, false);

        btnDateRange = view.findViewById(R.id.btnDateRange);
        lineChart = view.findViewById(R.id.lineChart);

        initDefaultRange();
        setupChart();
        setupDateRangePicker();

        loadDummyData();

        return view;
    }

    private void initDefaultRange() {
        Calendar end = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_MONTH, -30);

        startDateMillis = start.getTimeInMillis();
        endDateMillis = end.getTimeInMillis();

        btnDateRange.setText(
                dateFormat.format(start.getTime()) + " - " +
                        dateFormat.format(end.getTime())
        );
    }

    private void setupDateRangePicker() {
        btnDateRange.setOnClickListener(v -> {
            MaterialDatePicker<Pair<Long, Long>> picker =
                    MaterialDatePicker.Builder.dateRangePicker()
                            .setTitleText("Select date range")
                            .setSelection(
                                    new Pair<>(startDateMillis, endDateMillis)
                            )
                            .build();

            picker.addOnPositiveButtonClickListener(selection -> {
                startDateMillis = selection.first;
                endDateMillis = selection.second;

                btnDateRange.setText(
                        dateFormat.format(new Date(startDateMillis)) + " - " +
                                dateFormat.format(new Date(endDateMillis))
                );

                reloadChart();
            });

            picker.show(getParentFragmentManager(), "DATE_RANGE_PICKER");
        });
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
    }

    private void reloadChart() {
        loadDummyData();
    }

    private void loadDummyData() {
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            entries.add(new Entry(i, (float) (Math.random() * 10)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Number of rides");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);

        LineData data = new LineData(dataSet);
        lineChart.setData(data);
        lineChart.invalidate();
    }

}

