package com.example.dpm.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dpm.Model.User;
import com.example.dpm.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserAdminAdapter extends RecyclerView.Adapter<UserAdminAdapter.ViewHolder> {

    private List<User> users;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public UserAdminAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);

        holder.tvName.setText(user.getFirstName() + " " + user.getLastName());
        holder.tvEmail.setText(user.getEmail());

        // stanje dugmadi
        holder.btnBlock.setEnabled(!user.isBlocked());
        holder.btnUnblock.setEnabled(user.isBlocked());

        holder.btnBlock.setOnClickListener(v ->
                showBlockDialog(v.getContext(), user)
        );

        holder.btnUnblock.setOnClickListener(v ->
                unblockUser(v.getContext(), user)
        );
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    // 🔴 DIALOG ZA NAPOMENU
    private void showBlockDialog(Context context, User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Block user");

        final EditText input = new EditText(context);
        input.setHint("Enter block note");
        builder.setView(input);

        builder.setPositiveButton("BLOCK", (dialog, which) -> {
            String note = input.getText().toString();

            db.collection("users")
                    .document(user.getId())
                    .update(
                            "blocked", true,
                            "blockNote", note
                    )
                    .addOnSuccessListener(aVoid -> {
                        user.setBlocked(true);
                        user.setBlockNote(note);
                        notifyDataSetChanged();
                        Toast.makeText(context,
                                "User blocked",
                                Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // 🟢 UNBLOCK
    private void unblockUser(Context context, User user) {
        db.collection("users")
                .document(user.getId())
                .update(
                        "blocked", false,
                        "blockNote", ""
                )
                .addOnSuccessListener(aVoid -> {
                    user.setBlocked(false);
                    user.setBlockNote(null);
                    notifyDataSetChanged();
                    Toast.makeText(context,
                            "User unblocked",
                            Toast.LENGTH_SHORT).show();
                });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        Button btnBlock, btnUnblock;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnBlock = itemView.findViewById(R.id.btnBlock);
            btnUnblock = itemView.findViewById(R.id.btnUnblock);
        }
    }
}
