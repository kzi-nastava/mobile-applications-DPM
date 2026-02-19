package com.example.dpm.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Adapter.ChangeDataRequestAdapter;
import com.example.dpm.Model.ChangeDataRequest;
import com.example.dpm.R;
import com.example.dpm.Repository.ChangeDataRequestRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminChangeDataRequestsFragment extends Fragment {

    private RecyclerView rvRequests;
    private ChangeDataRequestAdapter adapter;
    private List<ChangeDataRequest> requests = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_admin_users,
                container,
                false
        );

        rvRequests = view.findViewById(R.id.rvUsers);
        rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ChangeDataRequestAdapter(getContext(), requests);
        rvRequests.setAdapter(adapter);

        loadRequests();

        return view;
    }

    public void loadRequests() {
        ChangeDataRequestRepository repository = new ChangeDataRequestRepository();

        repository.getPendingRequests(
                result -> {
                    requests.clear();
                    requests.addAll(result);
                    adapter.notifyDataSetChanged();
                },
                e -> Toast.makeText(
                        getContext(),
                        e.getMessage(),
                        Toast.LENGTH_LONG
                ).show()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRequests();
    }


}

