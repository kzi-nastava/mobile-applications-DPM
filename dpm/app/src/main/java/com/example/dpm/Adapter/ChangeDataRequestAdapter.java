package com.example.dpm.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Activity.ChangeDataRequestDetailsActivity;
import com.example.dpm.Model.ChangeDataRequest;
import com.example.dpm.R;

import java.util.List;

public class ChangeDataRequestAdapter
        extends RecyclerView.Adapter<ChangeDataRequestAdapter.ViewHolder> {

    private List<ChangeDataRequest> requests;
    private Context context;

    public ChangeDataRequestAdapter(Context context, List<ChangeDataRequest> requests) {
        this.context = context;
        this.requests = requests;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_change_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChangeDataRequest req = requests.get(position);

        holder.tvName.setText(req.getFirstName() + " " + req.getLastName());
        holder.tvEmail.setText(req.getEmail());

        holder.btnView.setOnClickListener(v -> {
            Intent intent = new Intent(
                    v.getContext(),
                    ChangeDataRequestDetailsActivity.class
            );
            intent.putExtra("REQUEST_ID", req.getId());
            v.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        Button btnView;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}

