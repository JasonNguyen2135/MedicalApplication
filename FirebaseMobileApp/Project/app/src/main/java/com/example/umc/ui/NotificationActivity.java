package com.example.umc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.NotificationAdapter;
import com.example.umc.models.NotificationModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView rv;
    FirebaseFirestore db;
    FirebaseAuth auth;
    NotificationAdapter adapter;
    List<NotificationModel> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_notification);

        rv = findViewById(R.id.rvNotification);
        rv.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new NotificationAdapter(list, this::handleClick);
        rv.setAdapter(adapter);
        MaterialToolbar toolbar = findViewById(R.id.toolbarNotification);
        toolbar.setNavigationOnClickListener(v -> finish());

        loadNotifications();
    }
    private void handleClick(NotificationModel n) {

        if (n == null) return;

        String type = n.getType();
        String targetId = n.getTargetId();
        String notiId = n.getId();

        if (type == null || targetId == null || targetId.isEmpty()) {
            Toast.makeText(this, "Thông báo không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getUid();
        if (uid == null) return;

        // ✅ MARK AS READ
        if (notiId != null && !n.isRead()) {
            db.collection("notifications")
                    .document(notiId)
                    .update("read", true);
        }

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    String role = doc.getString("role");
                    boolean isAdmin = "admin".equals(role);

                    Intent i = null;

                    switch (type) {

                        // ===== APPOINTMENT FLOW =====
                        case "APPOINTMENT":
                        case "BOOKING":
                        case "APPROVED":
                        case "CANCEL":
                        case "COMPLETED":
                        case "PAYMENT":
                        case "AFFECTED":
                        case "AFFECTED_BY_DOCTOR":
                        case "AFFECTED_BY_DOCTOR_LEAVE":
                        case "REFUND_AT_COUNTER":

                            i = isAdmin
                                    ? new Intent(this, AdminAppointmentDetailActivity.class)
                                    : new Intent(this, AppointmentDetailActivity.class);

                            i.putExtra("appointmentId", targetId);
                            break;

                        // ===== REFUND =====
                        case "REFUND":
                            showRefundDialog();
                            return; // ❗ không mở activity

                        // ===== CHAT =====
                        case "CHAT":
                            i = isAdmin
                                    ? new Intent(this, AdminChatActivity.class)
                                    : new Intent(this, ChatActivity.class);

                            i.putExtra("chatId", targetId);
                            break;

                        // ===== NEWS =====
                        case "NEWS":
                            i = new Intent(this, NewsDetailActivity.class);
                            i.putExtra("newsId", targetId);
                            break;

                        default:
                            Toast.makeText(this,
                                    "Không hỗ trợ loại thông báo: " + type,
                                    Toast.LENGTH_SHORT).show();
                            return;
                    }

                    startActivity(i);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Không xác định được quyền người dùng",
                                Toast.LENGTH_SHORT).show()
                );
    }


    private void showRefundDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Thông báo hoàn tiền")
                .setMessage(
                        "Lịch khám của bạn đã bị hủy do bác sĩ ngừng hoạt động.\n\n" +
                                "Hệ thống sẽ hoàn tiền cho bạn trong thời gian sớm nhất."
                )
                .setPositiveButton("Đã hiểu", null)
                .show();
    }




    private void loadNotifications() {
        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("notifications")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {

                    if (e != null) {
                        Toast.makeText(this, "Lỗi tải thông báo", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots == null) return;

                    list.clear();
                    for (QueryDocumentSnapshot d : snapshots) {
                        NotificationModel n = d.toObject(NotificationModel.class);
                        n.setId(d.getId()); // ❗ bắt buộc
                        list.add(n);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
