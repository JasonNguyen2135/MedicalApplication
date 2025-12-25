package com.example.umc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.umc.R;
import com.example.umc.models.Doctor;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    private final Context context;
    private final List<Doctor> list;
    private final OnDoctorClick listener;

    // ===== CALLBACK =====
    public interface OnDoctorClick {
        void onClick(Doctor doctor);
    }

    // ===== CONSTRUCTOR =====
    public DoctorAdapter(Context context,
                         List<Doctor> list,
                         OnDoctorClick listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater
                .from(context)
                .inflate(R.layout.item_doctor, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder h,
            int position
    ) {
        Doctor d = list.get(position);

        // ===== DATA =====
        h.tvName.setText(d.getName());
        h.tvDept.setText(d.getSpeciality());
        h.tvExp.setText(d.getExperience() + " năm kinh nghiệm");

        Glide.with(context)
                .load(d.getImageUrl())
                .placeholder(R.drawable.doctor_placeholder)
                .error(R.drawable.doctor_placeholder)
                .into(h.imgPhoto);

        // ===== CLICK (CẢ CARD + NÚT) =====
        View.OnClickListener click = v -> {
            if (listener != null) {
                listener.onClick(d);
            }
        };

        h.itemView.setOnClickListener(click);
        h.btnBookDoctor.setOnClickListener(click);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    // ===== VIEW HOLDER =====
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPhoto;
        TextView tvName, tvDept, tvExp, btnBookDoctor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgDoctor);
            tvName = itemView.findViewById(R.id.tvDoctorName);
            tvDept = itemView.findViewById(R.id.tvDoctorDept);
            tvExp = itemView.findViewById(R.id.tvDoctorExp);
            btnBookDoctor = itemView.findViewById(R.id.btnBookDoctor);
        }
    }
}
