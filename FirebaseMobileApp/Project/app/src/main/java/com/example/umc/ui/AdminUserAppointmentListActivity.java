package com.example.umc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.AdminAppointmentAdapter;
import com.example.umc.models.Appointment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAppointmentListActivity extends AppCompatActivity {

    RecyclerView rv;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userId;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin_user_appointments);

        rv = findViewById(R.id.rvAppointments);
        rv.setLayoutManager(new LinearLayoutManager(this));
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

// bật nút back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

// xử lý click
        toolbar.setNavigationOnClickListener(v -> finish());

        userId = getIntent().getStringExtra("userId");
        loadAppointments();
    }

    private void loadAppointments() {
        db.collection("appointments")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snap, e) -> {

                    if (e != null || snap == null) {
                        Toast.makeText(this, "Lỗi tải lịch", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Appointment> list = new ArrayList<>();
                    for (var d : snap.getDocuments()) {
                        Appointment a = d.toObject(Appointment.class);
                        if (a == null) continue;
                        a.setId(d.getId());
                        list.add(a);
                    }

                    rv.setAdapter(new AdminAppointmentAdapter(list, a -> {
                        Intent i = new Intent(
                                AdminUserAppointmentListActivity.this,
                                AdminAppointmentDetailActivity.class
                        );
                        i.putExtra("appointmentId", a.getId());
                        startActivity(i);
                    }));
                });
    }

}
