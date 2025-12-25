package com.example.umc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.models.MessageModel;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {

    List<MessageModel> list;
    String currentUserId;

    public ChatAdapter(List<MessageModel> list, String currentUserId) {
        this.list = list;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        MessageModel m = list.get(i);

        if (m.getSenderId().equals(currentUserId)) {
            h.left.setVisibility(View.GONE);
            h.right.setVisibility(View.VISIBLE);
            h.tvRight.setText(m.getContent());
        } else {
            h.right.setVisibility(View.GONE);
            h.left.setVisibility(View.VISIBLE);
            h.tvLeft.setText(m.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvLeft, tvRight;
        View left, right;

        VH(View v) {
            super(v);
            left = v.findViewById(R.id.layoutLeft);
            right = v.findViewById(R.id.layoutRight);
            tvLeft = v.findViewById(R.id.tvLeft);
            tvRight = v.findViewById(R.id.tvRight);
        }
    }
}
