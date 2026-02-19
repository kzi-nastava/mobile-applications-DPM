package com.example.dpm.Adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PassengerEmailAdapter
        extends RecyclerView.Adapter<PassengerEmailAdapter.ViewHolder>{

    private List<String> emails;

    public PassengerEmailAdapter(List<String> emails){
        this.emails = emails;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        TextView tv = new TextView(parent.getContext());
        tv.setPadding(16,16,16,16);
        return new ViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(emails.get(position));
    }

    @Override
    public int getItemCount() {
        return emails.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
