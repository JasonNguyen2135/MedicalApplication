package com.example.umc.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.models.NotificationModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.VH> {

    List<NotificationModel> list;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    OnNotificationClick listener;
    ImageView ivIcon, ivDelete;


    // 🔥 CALLBACK
    public interface OnNotificationClick {
        void onClick(NotificationModel n);
    }

    public NotificationAdapter(
            List<NotificationModel> list,
            OnNotificationClick listener

    ) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        NotificationModel n = list.get(i);
        h.tvTitle.setText(n.getTitle());
        h.tvContent.setText(n.getContent());
        h.tvTime.setText(timeAgo(n.getCreatedAt()));



        // ===== CHƯA ĐỌC =====
        h.viewUnread.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);

        // ===== ICON THEO TYPE =====
        switch (n.getType()) {

            case "CHAT":
                h.ivIcon.setImageResource(R.drawable.ic_chat);
                h.iconBg.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#E0F2FE")));
                break;

            case "BOOKING":
            case "APPOINTMENT":
                h.ivIcon.setImageResource(R.drawable.ic_calendar);
                h.iconBg.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#ECFDF5")));
                break;

            case "PAY":
            case "PAYMENT":
                h.ivIcon.setImageResource(R.drawable.ic_payment);
                h.iconBg.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
                break;

            case "NEWS":
                h.ivIcon.setImageResource(R.drawable.ic_news);
                break;
            case "REFUND":
                h.ivIcon.setImageResource(R.drawable.ic_refund);
                h.iconBg.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
                break;


            default:
                h.ivIcon.setImageResource(R.drawable.ic_notification);
        }

        // ===== CLICK =====
        h.itemView.setOnClickListener(v -> {

            // mark read
            if (!n.isRead()) {
                db.collection("notifications")
                        .document(n.getId())
                        .update("read", true);
            }

            // callback cho Activity
            if (listener != null) {
                listener.onClick(n);
            }
        });
        h.ivDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Xóa thông báo?")
                    .setMessage("Bạn có chắc muốn xóa thông báo này?")
                    .setPositiveButton("Xóa", (d, w) -> {

                        db.collection("notifications")
                                .document(n.getId())
                                .delete()
                                .addOnSuccessListener(a -> {
                                    int pos = h.getAdapterPosition();
                                    if (pos != RecyclerView.NO_POSITION) {
                                        list.remove(pos);
                                        notifyItemRemoved(pos);
                                    }
                                });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= VIEW HOLDER =================
    static class VH extends RecyclerView.ViewHolder {

        TextView tvTitle, tvContent, tvTime;
        ImageView ivIcon, ivDelete;
        View viewUnread, iconBg;

        VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvContent = v.findViewById(R.id.tvContent);
            tvTime = v.findViewById(R.id.tvTime);
            ivIcon = v.findViewById(R.id.ivNotificationIcon);
            ivDelete = v.findViewById(R.id.ivDelete); // ✅ THÊM
            viewUnread = v.findViewById(R.id.viewUnread);
            iconBg = v.findViewById(R.id.viewStatusBg);
        }
    }


    // ================= TIME AGO =================
    private String timeAgo(Timestamp t) {
        if (t == null) return "";
        long diff = System.currentTimeMillis() - t.toDate().getTime();

        long min = diff / 60000;
        if (min < 60) return min + " phút trước";

        long hour = min / 60;
        if (hour < 24) return hour + " giờ trước";

        return (hour / 24) + " ngày trước";
    }
}
