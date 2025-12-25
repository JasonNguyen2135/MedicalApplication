package com.example.umc.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.models.ChatModel;
import com.example.umc.ui.AdminChatActivity;
import com.example.umc.utils.ChatUtil;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class AdminChatAdapter
        extends RecyclerView.Adapter<AdminChatAdapter.VH> {

    public interface OnDeleteClick {
        void onDelete(ChatModel chat);
    }

    List<ChatModel> list;
    OnDeleteClick onDelete;

    public AdminChatAdapter(List<ChatModel> list, OnDeleteClick onDelete) {
        this.list = list;
        this.onDelete = onDelete;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_list, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int i) {
        ChatModel c = list.get(i);

        h.tvName.setText(
                c.getUserName() != null ? c.getUserName() : "Người dùng"
        );

        h.tvLast.setText(
                c.getLastMessage() != null ? c.getLastMessage() : "Chưa có tin nhắn"
        );

        h.itemView.setOnClickListener(v -> {
            Intent it = new Intent(v.getContext(), AdminChatActivity.class);

            String adminId = c.getAdminId() != null
                    ? c.getAdminId()
                    : FirebaseAuth.getInstance().getUid();

            String chatId = ChatUtil.buildChatId(
                    c.getUserId(),
                    adminId
            );

            it.putExtra("chatId", chatId);
            it.putExtra("userName", c.getUserName());
            v.getContext().startActivity(it);
        });

        h.btnDelete.setOnClickListener(v -> {
            if (onDelete != null) {
                onDelete.onDelete(c);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvLast;
        ImageView btnDelete;

        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvUserName);
            tvLast = v.findViewById(R.id.tvLastMessage);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}

