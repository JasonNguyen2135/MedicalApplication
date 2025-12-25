package com.example.umc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.models.Doctor;

import java.util.List;

public class AdminDoctorAdapter
        extends RecyclerView.Adapter<AdminDoctorAdapter.VH> {

    public interface OnDoctorClick {
        void onDoctorClick(Doctor doctor);
    }

    Context context;
    List<Doctor> doctors;
    OnDoctorClick listener;

    public AdminDoctorAdapter(Context context,
                              List<Doctor> doctors,
                              OnDoctorClick listener) {
        this.context = context;
        this.doctors = doctors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new VH(LayoutInflater.from(context)
                .inflate(R.layout.item_admin_doctor, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        Doctor d = doctors.get(i);
        h.tvName.setText(d.getName());
        h.tvSpec.setText(d.getSpeciality());

        h.itemView.setOnClickListener(v ->
                listener.onDoctorClick(d)
        );
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvSpec;

        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvDoctorName);
            tvSpec = v.findViewById(R.id.tvDoctorSpec);
        }
    }
}
