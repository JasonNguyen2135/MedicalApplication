package com.example.umc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.models.User;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.VH> {

    public interface OnUserClick {
        void click(User user);
    }

    List<User> list;
    OnUserClick listener;

    public AdminUserAdapter(List<User> list, OnUserClick l) {
        this.list = list;
        this.listener = l;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup p, int v) {
        return new VH(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_admin_user, p, false));
    }

    @Override
    public void onBindViewHolder(VH h, int i) {
        User u = list.get(i);
        h.name.setText(u.getName());
        h.email.setText(u.getEmail());
        h.itemView.setOnClickListener(v -> listener.click(u));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, email;
        VH(View v) {
            super(v);
            name = v.findViewById(R.id.tvName);
            email = v.findViewById(R.id.tvEmail);
        }
    }
}
