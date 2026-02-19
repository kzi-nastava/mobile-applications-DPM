package com.example.dpm.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.dpm.Model.PriceConfig;
import com.example.dpm.R;
import com.example.dpm.Repository.PriceRepository;

import java.util.HashMap;
import java.util.Map;

public class PricingFragment extends Fragment {

    private EditText etPricePerKm, etStandard, etLuxury, etVan;
    private Button btnSave;
    private PriceRepository pricingRepository;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pricing, container, false);

        pricingRepository = new PriceRepository();

        etPricePerKm = view.findViewById(R.id.etPricePerKm);
        etStandard = view.findViewById(R.id.etBaseStandard);
        etLuxury = view.findViewById(R.id.etBaseLuxury);
        etVan = view.findViewById(R.id.etBaseVan);
        btnSave = view.findViewById(R.id.btnSavePricing);

        loadCurrentPricing();

        btnSave.setOnClickListener(v -> savePricing());

        return view;
    }

    private void loadCurrentPricing() {
        pricingRepository.getCurrentPricing(config -> {

            if (config == null) {
                Toast.makeText(getContext(), "No pricing found. Enter values and save.", Toast.LENGTH_SHORT).show();
                return;
            }
            etPricePerKm.setText(String.valueOf(config.getPricePerKm()));
            Map<String, Long> map = config.getPricePerVehicleType();

            if (map != null) {
                Long standard = map.get("STANDARD");
                Long luxury = map.get("LUXURY");
                Long van = map.get("VAN");

                if (standard != null)
                    etStandard.setText(String.valueOf(standard));
                if (luxury != null)
                    etLuxury.setText(String.valueOf(luxury));
                if (van != null)
                    etVan.setText(String.valueOf(van));
            }
        });
    }


    private void savePricing() {
        String sKm = etPricePerKm.getText().toString().trim();
        String sStandard = etStandard.getText().toString().trim();
        String sLuxury = etLuxury.getText().toString().trim();
        String sVan = etVan.getText().toString().trim();

        if (TextUtils.isEmpty(sKm) || TextUtils.isEmpty(sStandard) || TextUtils.isEmpty(sLuxury) || TextUtils.isEmpty(sVan)) {
            Toast.makeText(getContext(), "Fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        long pricePerKm;
        long baseStandard;
        long baseLuxury;
        long baseVan;

        try {
            pricePerKm = Long.parseLong(sKm);
            baseStandard = Long.parseLong(sStandard);
            baseLuxury = Long.parseLong(sLuxury);
            baseVan = Long.parseLong(sVan);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Numbers only.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Long> map = new HashMap<>();
        map.put("STANDARD", baseStandard);
        map.put("LUXURY", baseLuxury);
        map.put("VAN", baseVan);

        PriceConfig config = new PriceConfig(map, pricePerKm);
        pricingRepository.updatePricing(config);
        Toast.makeText(getContext(), "Pricing updated successfully.", Toast.LENGTH_SHORT).show();
    }


}
