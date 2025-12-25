package com.example.umc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.HomeAdapter;
import com.example.umc.models.HomeItem;
import com.example.umc.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    RecyclerView rv;
    HomeAdapter adapter;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextView tvTitle, tvSeeMore;
    TextInputEditText etSearch;

    MaterialCardView cardUpcoming;
    TextView tvUpcomingDoctor, tvUpcomingTime, tvUpcomingStatus;
    MaterialButton btnManageAppointment;
    String upcomingAppointmentId;

    boolean isExpanded = false;
    List<HomeItem> fullMenu = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ===== BIND VIEW =====
        rv = findViewById(R.id.rvHomeGrid);
        rv.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new HomeAdapter(this);
        rv.setAdapter(adapter);

        tvTitle = findViewById(R.id.tvTitle);
        tvSeeMore = findViewById(R.id.tvSeeMore);
        etSearch = findViewById(R.id.etSearch);

        cardUpcoming = findViewById(R.id.cardUpcomingAppointment);
        tvUpcomingDoctor = findViewById(R.id.tvUpcomingDoctor);
        tvUpcomingTime = findViewById(R.id.tvUpcomingTime);
        tvUpcomingStatus = findViewById(R.id.tvUpcomingStatus);
        btnManageAppointment = findViewById(R.id.btnManageAppointment);



        // ===== SEARCH =====
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                adapter.filter(s.toString());
            }
        });

        tvSeeMore.setOnClickListener(v -> toggleMenu());

        loadUserName();
        checkRoleAndLoad();
    }

    // ================= USER NAME =================
    private void loadUserName() {
        if (auth.getCurrentUser() == null) return;

        db.collection("users")
                .document(auth.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    if (name == null || name.isEmpty()) name = "bạn";
                    tvTitle.setText("Chào bạn, " + name);
                });
    }

    // ================= ROLE =================
    private void checkRoleAndLoad() {
        if (auth.getCurrentUser() == null) return;

        db.collection("users")
                .document(auth.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    User u = doc.toObject(User.class);
                    if (u == null) return;

                    if ("admin".equalsIgnoreCase(u.getRole())) {
                        loadAdminMenu();
                        cardUpcoming.setVisibility(View.GONE);
                    } else {
                        loadUserMenu();
                        loadUpcomingAppointment();
                    }
                });
    }

    // ================= UPCOMING APPOINTMENT =================
    private void loadUpcomingAppointment() {
        if (auth.getCurrentUser() == null) return;

        db.collection("appointments")
                .whereEqualTo("userId", auth.getUid())
                .whereEqualTo("status", "BOOKED") // 🔥 CHỈ LẤY ĐÃ BOOKED
                .get()
                .addOnSuccessListener(q -> {

                    if (q.isEmpty()) {
                        cardUpcoming.setVisibility(View.GONE);
                        return;
                    }

                    DocumentSnapshot nearest = null;
                    long now = System.currentTimeMillis();
                    long minDiff = Long.MAX_VALUE;

                    for (DocumentSnapshot doc : q.getDocuments()) {
                        Log.d("UPCOMING_DEBUG", "id=" + doc.getId()
                                + " status=" + doc.getString("status")
                                + " time=" + doc.getString("appointmentTime"));

                        String timeStr = doc.getString("appointmentTime");
                        if (timeStr == null) continue;

                        // yyyy-MM-dd HH:mm
                        long appointmentMillis = parseTimeToMillis(timeStr);
                        if (appointmentMillis < now) continue; // bỏ lịch cũ

                        long diff = appointmentMillis - now;
                        if (diff < minDiff) {
                            minDiff = diff;
                            nearest = doc;
                        }
                    }

                    if (nearest == null) {
                        cardUpcoming.setVisibility(View.GONE);
                        return;
                    }

                    // ===== HIỂN THỊ =====
                    upcomingAppointmentId = nearest.getId();
                    cardUpcoming.setVisibility(View.VISIBLE);

                    tvUpcomingTime.setText("⏰ " + nearest.getString("appointmentTime"));
                    tvUpcomingStatus.setText("📌 Đã đặt lịch");

                    String doctorId = nearest.getString("doctorId");
                    if (doctorId != null) {
                        db.collection("doctors")
                                .document(doctorId)
                                .get()
                                .addOnSuccessListener(d ->
                                        tvUpcomingDoctor.setText("Bác sĩ: " + d.getString("name"))
                                );
                    }

                    btnManageAppointment.setOnClickListener(v -> {
                        Intent i = new Intent(this, AppointmentDetailActivity.class);
                        i.putExtra("appointmentId", upcomingAppointmentId);
                        startActivity(i);
                    });
                })
                .addOnFailureListener(e ->
                        cardUpcoming.setVisibility(View.GONE)
                );
    }
    private long parseTimeToMillis(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date d = sdf.parse(time);
            return d != null ? d.getTime() : 0;
        } catch (Exception e) {
            return 0;
        }
    }




    private String mapStatus(String s) {
        if (s == null) return "";
        switch (s) {
            case "PENDING": return "Đang chờ xác nhận";
            case "APPROVED": return "Đã duyệt";
            case "BOOKED": return "Đã đặt";
            case "PAID": return "Đã thanh toán";
            default: return s;
        }
    }

    // ================= MENU =================
    private void loadUserMenu() {
        fullMenu.clear();
        fullMenu.add(new HomeItem("🤖", "Chat AI", ChatAIActivity.class));
        fullMenu.add(new HomeItem("🗓️🩺", "Đặt khám", DoctorListActivity.class));
        fullMenu.add(new HomeItem("🔍", "Tìm bác sĩ", SearchDoctorActivity.class));
        fullMenu.add(new HomeItem("📅", "Lịch khám", AppointmentHistoryActivity.class));
        fullMenu.add(new HomeItem("💬", "Chat hỗ trợ", ChatActivity.class));
        fullMenu.add(new HomeItem("🔔", "Thông báo", NotificationActivity.class));
        fullMenu.add(new HomeItem("📰", "Tin tức", NewsActivity.class));
        fullMenu.add(new HomeItem("👤", "Hồ sơ", ProfileActivity.class));

        showCollapsed();
    }

    private void loadAdminMenu() {
        fullMenu.clear();
        fullMenu.add(new HomeItem("🤖", "Chat AI", ChatAIActivity.class));
        fullMenu.add(new HomeItem("🔔", "Thông báo", NotificationActivity.class));
        fullMenu.add(new HomeItem("📰", "Tin tức", NewsActivity.class));
        fullMenu.add(new HomeItem("✍️", "Đăng tin", AdminPostNewsActivity.class));
        fullMenu.add(new HomeItem("👤", "Hồ sơ", ProfileActivity.class));
        fullMenu.add(new HomeItem("👥", "Người dùng", AdminUserListActivity.class));
        fullMenu.add(new HomeItem("💬", "Chat User", AdminChatListActivity.class));
        fullMenu.add(new HomeItem("🩺", "Bác sĩ", AdminDoctorListActivity.class));

        showCollapsed();
    }

    private void showCollapsed() {
        adapter.setData(fullMenu.subList(0, Math.min(9, fullMenu.size())));
        tvSeeMore.setText("Xem thêm");
        isExpanded = false;
    }

    private void toggleMenu() {
        if (isExpanded) {
            showCollapsed();
        } else {
            adapter.setData(fullMenu);
            tvSeeMore.setText("Thu gọn");
            isExpanded = true;
        }
    }
}
