package com.example.umc.adapters;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.models.NewsModel;
import com.example.umc.ui.NewsDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.VH> {

    private final List<NewsModel> list;

    public NewsAdapter(List<NewsModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NewsModel n = list.get(position);

        h.tvTitle.setText(n.getTitle());
        h.tvDescription.setText(n.getContent());
        h.tvCategory.setText("SỨC KHỎE");

        if (n.getCreatedAt() != null) {
            h.tvDate.setText(formatDate(n.getCreatedAt()));
        }

        // 👉 MỞ CHI TIẾT TIN
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), NewsDetailActivity.class);
            i.putExtra("newsId", n.getId());
            i.putExtra("title", n.getTitle());
            i.putExtra("content", n.getContent());
            v.getContext().startActivity(i);
        });

        // 👉 CHỈ ADMIN MỚI ĐƯỢC XÓA
        if (isAdmin()) {
            h.btnDelete.setVisibility(View.VISIBLE);
            h.btnDelete.setOnClickListener(v -> {
                int pos = h.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    confirmDelete(v, n, pos);
                }
            });

        } else {
            h.btnDelete.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    // ================= VIEW HOLDER =================
    static class VH extends RecyclerView.ViewHolder {

        TextView tvCategory, tvTitle, tvDescription, tvDate;
        ImageButton btnDelete;

        VH(@NonNull View v) {
            super(v);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvTitle = v.findViewById(R.id.tvNewsTitle);
            tvDescription = v.findViewById(R.id.tvDescription);
            tvDate = v.findViewById(R.id.tvDate);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }

    // ================= ADMIN CHECK =================
    private boolean isAdmin() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
        // 👉 nếu có role admin trong Firestore, mình nâng cấp tiếp cho bạn
    }

    // ================= CONFIRM DELETE =================
    private void confirmDelete(View v, NewsModel n, int position) {
        new AlertDialog.Builder(v.getContext())
                .setTitle("Xóa tin tức")
                .setMessage("Bạn có chắc chắn muốn xóa tin này không?")
                .setPositiveButton("Xóa", (d, w) -> deleteNews(n, position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ================= DELETE FIRESTORE =================
    private void deleteNews(NewsModel n, int position) {
        FirebaseFirestore.getInstance()
                .collection("news")
                .document(n.getId())
                .delete()
                .addOnSuccessListener(a -> {

                    if (position >= 0 && position < list.size()) {
                        list.remove(position);
                        notifyItemRemoved(position);
                    } else {
                        notifyDataSetChanged(); // fallback an toàn
                    }
                });
    }


    // ================= FORMAT DATE =================
    private String formatDate(Date date) {
        return new SimpleDateFormat("dd 'thg' MM, yyyy", Locale.getDefault())
                .format(date);
    }
}
