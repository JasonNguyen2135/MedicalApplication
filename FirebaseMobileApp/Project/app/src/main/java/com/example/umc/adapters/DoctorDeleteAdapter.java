package com.example.umc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.umc.R;

import com.example.umc.models.Doctor;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorDeleteAdapter
        extends RecyclerView.Adapter<DoctorDeleteAdapter.ViewHolder> {

    public interface OnDeleteDoctor {
        void onDelete(Doctor doctor, int position);
    }

    Context context;
    List<Doctor> list;
    OnDeleteDoctor callback;

    public DoctorDeleteAdapter(
            Context context,
            List<Doctor> list,
            OnDeleteDoctor callback
    ) {
        this.context = context;
        this.list = list;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delete_doctor, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Doctor d = list.get(pos);

        h.name.setText(d.getName());

        Glide.with(h.itemView.getContext())
                .load(d.getImageUrl())
                .placeholder(R.drawable.doctor_placeholder)
                .error(R.drawable.doctor_placeholder)
                .into(h.imgDoctor);

        h.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Xóa bác sĩ")
                    .setMessage("Xóa bác sĩ sẽ hủy toàn bộ lịch liên quan. Tiếp tục?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        callback.onDelete(d, h.getBindingAdapterPosition());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDoctor;
        TextView name;
        Button btnDelete;

        ViewHolder(View v) {
            super(v);
            imgDoctor = v.findViewById(R.id.imgDoctor);
            name = v.findViewById(R.id.tvDoctorName);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
    public void removeItem(int position) {
        if (position >= 0 && position < list.size()) {
            list.remove(position);
            notifyItemRemoved(position);
        }
    }

}
