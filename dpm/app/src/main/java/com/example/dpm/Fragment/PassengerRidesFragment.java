package com.example.dpm.Fragment;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Adapter.PassengerRidesAdapter;
import com.example.dpm.Model.Ride;
import com.example.dpm.R;
import com.example.dpm.Repository.RideRepository;
import com.example.dpm.Session.UserSession;

import java.util.ArrayList;
import java.util.List;

public class PassengerRidesFragment extends Fragment implements PassengerRidesAdapter.Listener {

    private RecyclerView rv;
    private TextView tvEmpty;
    private PassengerRidesAdapter adapter;

    private final List<Ride> items = new ArrayList<>();
    private final RideRepository rideRepository = new RideRepository();
    private final UserSession session = UserSession.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_passenger_rides, container, false);

        rv = v.findViewById(R.id.rvPassengerRides);
        tvEmpty = v.findViewById(R.id.tvEmpty);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PassengerRidesAdapter(items, this);
        rv.setAdapter(adapter);

        loadMyActiveRides();

        return v;
    }

    private void loadMyActiveRides() {
        if (session.getUser() == null) return;

        String passengerId = session.getUser().getId();

        rideRepository.getActiveRidesByPassenger(passengerId, rides -> {
            items.clear();
            if (rides != null) items.addAll(rides);

            adapter.notifyDataSetChanged();
            tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onTrack(Ride ride) {
        RideTrackingFragment frag = RideTrackingFragment.newInstance(ride.getId());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, frag)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onCancel(Ride ride) {

        final EditText input = new EditText(requireContext());
        input.setHint("Enter cancellation reason");

        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel ride")
                .setView(input)
                .setPositiveButton("Cancel ride", (d, w) -> {

                    String reason = input.getText().toString().trim();

                    if(reason.isEmpty()){
                        Toast.makeText(requireContext(),"Reason required",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    rideRepository.cancelRideByPassenger(
                            ride.getId(),
                            ride.getScheduledAt(),
                            reason,
                            unused -> {
                                Toast.makeText(requireContext(),"Ride canceled",Toast.LENGTH_SHORT).show();
                                loadMyActiveRides();
                            },
                            err -> Toast.makeText(requireContext(),err.getMessage(),Toast.LENGTH_SHORT).show()
                    );

                })
                .setNegativeButton("Back", null)
                .show();
    }

}