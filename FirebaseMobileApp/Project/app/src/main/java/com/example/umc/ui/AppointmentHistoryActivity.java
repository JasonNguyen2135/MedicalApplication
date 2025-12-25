package com.example.umc.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.AppointmentHistoryAdapter;
import com.example.umc.models.Appointment;
import com.example.umc.utils.NotificationUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AppointmentHistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AppointmentHistoryAdapter adapter;
    List<Appointment> list = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_appointment_history);

        recyclerView = findViewById(R.id.recyclerAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AppointmentHistoryAdapter(this, list);
        recyclerView.setAdapter(adapter);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        loadHistory();
    }

    private void loadHistory() {
        if (auth.getCurrentUser() == null) return;

        db.collection("appointments")
                .whereEqualTo("userId", auth.getUid())
                .addSnapshotListener((snapshot, e) -> {

                    if (e != null || snapshot == null) {
                        Toast.makeText(this,
                                "Lỗi tải lịch",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    list.clear();

                    for (DocumentSnapshot doc : snapshot) {
                        Appointment a = doc.toObject(Appointment.class);
                        if (a == null) continue;
                        a.setId(doc.getId());
                        list.add(a);
                    }

                    // mới nhất lên đầu
                    list.sort((a, b) ->
                            b.getAppointmentTime().compareTo(a.getAppointmentTime())
                    );

                    adapter.notifyDataSetChanged();
                });

    }
    @Override
    protected void onStart() {
        super.onStart();
        autoCancelExpiredAppointments();
        loadHistory();
    }
    public String convertStatus(String status) {
        switch (status) {
            case "PENDING":
                return "Đang chờ";
            case "CONFIRMED":
                return "Đã xác nhận";
            case "COMPLETED":
                return "Đã hoàn thành";
            case "CANCELLED":
                return "Đã hủy";
            case "CANCELLED_BY_DOCTOR":
                return "Đã hủy bởi ";
            case "CANCELLED_BY_SYSTEM":
                return "Đã bị hủy";
            case "APPROVED":
                return "Đã duyệt";
            case "BOOKED":
                return "Đã đặt";
            case "HOLDING":
                return "Đang giữ chỗ";
            default:
                return "Không xác định";
        }
    }
    private void autoCancelExpiredAppointments() {
        long now = System.currentTimeMillis();

        // ❌ APPROVED quá hạn thanh toán
        db.collection("appointments")
                .whereEqualTo("status", "HOLDING")
                .whereLessThan("holdUntil", now)
                .get()
                .addOnSuccessListener(qs -> {
                    for (var d : qs) {
                        String userId = d.getString("userId");

                        d.getReference().update(
                                "status", "CANCELLED_BY_SYSTEM",
                                "cancelType", "HOLD_TIMEOUT"
                        );

                        if (userId != null) {
                            NotificationUtil.create(
                                    db,
                                    userId,
                                    "Lịch khám bị huỷ",
                                    "Lịch khám của bạn bị huỷ do quá thời gian giữ chỗ.",
                                    "APPOINTMENT",
                                    d.getId()
                            );
                        }
                    }
                });

        // ❌ HOLDING quá hạn
        // APPROVED quá hạn thanh toán
        db.collection("appointments")
                .whereEqualTo("status", "APPROVED")
                .whereLessThan("approveUntil", now)
                .get()
                .addOnSuccessListener(qs -> {
                    for (var d : qs) {
                        String userId = d.getString("userId");

                        d.getReference().update(
                                "status", "CANCELLED_BY_SYSTEM",
                                "cancelType", "PAYMENT_TIMEOUT"
                        );

                        if (userId != null) {
                            NotificationUtil.create(
                                    db,
                                    userId,
                                    "Lịch khám bị huỷ",
                                    "Lịch khám đã bị huỷ do bạn chưa thanh toán sau khi được duyệt.",
                                    "APPOINTMENT",
                                    d.getId()
                            );
                        }
                    }
                });
    }

}
