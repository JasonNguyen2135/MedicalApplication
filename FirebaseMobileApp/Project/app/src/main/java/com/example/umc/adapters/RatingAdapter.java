package com.example.umc.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.models.Rating;

import java.util.ArrayList;
import java.util.List;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.ViewHolder> {

    private List<Rating> list = new ArrayList<>();

    public void setData(List<Rating> data) {
        list = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rating, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        Rating r = list.get(i);

        h.tvStars.setText("⭐".repeat(r.getRating()));
        h.tvComment.setText(r.getComment());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStars, tvComment;

        ViewHolder(View v) {
            super(v);
            tvStars = v.findViewById(R.id.tvStars);
            tvComment = v.findViewById(R.id.tvComment);
        }
    }
}
