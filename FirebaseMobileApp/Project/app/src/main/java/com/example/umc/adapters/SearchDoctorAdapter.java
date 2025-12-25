package com.example.umc.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.umc.R;
import com.example.umc.models.Doctor;
import com.example.umc.ui.DoctorDetailActivity;

import java.util.List; // 🔥 QUAN TRỌNG

public class SearchDoctorAdapter
        extends RecyclerView.Adapter<SearchDoctorAdapter.ViewHolder> {

    private Context mContext;
    private List<Doctor> doctorList;   // 🔥 List, không phải ArrayList

    public SearchDoctorAdapter(Context context, List<Doctor> list) {
        this.mContext = context;
        this.doctorList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        Doctor d = doctorList.get(pos);

        holder.tvName.setText(d.getName());
        holder.tvSpeciality.setText(d.getSpeciality());

        Glide.with(mContext)
                .load(d.getImageUrl())
                .placeholder(R.drawable.doctor_placeholder)
                .into(holder.imgDoctor);

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(mContext, DoctorDetailActivity.class);
            i.putExtra("doctorId", d.getId()); // String ID (Firebase)
            mContext.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDoctor;
        TextView tvName, tvSpeciality;

        ViewHolder(View itemView) {
            super(itemView);
            imgDoctor = itemView.findViewById(R.id.imgDoctor);
            tvName = itemView.findViewById(R.id.tvDoctorName);
            tvSpeciality = itemView.findViewById(R.id.tvDoctorDept);
        }
    }
}
