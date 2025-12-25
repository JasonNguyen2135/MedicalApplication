package com.example.umc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.DoctorAdapter;
import com.example.umc.models.Doctor;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DoctorListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DoctorAdapter adapter;
    private final List<Doctor> doctors = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // nếu != null → đang RESCHEDULE / ĐỔI BÁC SĨ
    private String rescheduleFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbarDoctorList);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerDoctors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        rescheduleFrom = getIntent().getStringExtra("rescheduleFrom");

        adapter = new DoctorAdapter(
                this,
                doctors,
                this::openDoctorDetail // 👈 callback chuẩn
        );
        recyclerView.setAdapter(adapter);

        loadDoctors();
    }

    // ================= CLICK DOCTOR =================
    private void openDoctorDetail(Doctor doctor) {
        Intent i = new Intent(this, DoctorDetailActivity.class);
        i.putExtra("doctorId", doctor.getId());

        // 🔥 nếu là reschedule → truyền appointmentId cũ
        if (rescheduleFrom != null) {
            i.putExtra("rescheduleFrom", rescheduleFrom);
        }

        startActivity(i);
    }

    // ================= LOAD DATA =================
    private void loadDoctors() {
        if (rescheduleFrom == null) {
            loadAllDoctors();
        } else {
            loadDoctorsSameSpeciality();
        }
    }

    // 🔁 chỉ cho đổi bác sĩ CÙNG KHOA
    private void loadDoctorsSameSpeciality() {
        db.collection("appointments")
                .document(rescheduleFrom)
                .get()
                .addOnSuccessListener(appDoc -> {
                    if (!appDoc.exists()) return;

                    String speciality = appDoc.getString("speciality");

                    // 🔁 FALLBACK CHO DATA CŨ
                    if (speciality == null) {
                        String oldDoctorId = appDoc.getString("doctorId");

                        if (oldDoctorId == null) {
                            showNoSpeciality();
                            return;
                        }

                        db.collection("doctors")
                                .document(oldDoctorId)
                                .get()
                                .addOnSuccessListener(docDoc -> {
                                    if (!docDoc.exists()) {
                                        showNoSpeciality();
                                        return;
                                    }

                                    String sp = docDoc.getString("speciality");
                                    if (sp == null) {
                                        showNoSpeciality();
                                        return;
                                    }

                                    loadDoctorsBySpeciality(sp);
                                });

                        return;
                    }

                    // ✅ CASE CHUẨN
                    loadDoctorsBySpeciality(speciality);
                });
    }

    private void loadDoctorsBySpeciality(String speciality) {
        db.collection("doctors")
                .whereEqualTo("speciality", speciality)
                .get()
                .addOnSuccessListener(snapshot -> {
                    doctors.clear();
                    for (DocumentSnapshot d : snapshot) {
                        Doctor doctor = d.toObject(Doctor.class);
                        if (doctor != null) {
                            doctor.setId(d.getId());
                            doctors.add(doctor);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showNoSpeciality() {
        Toast.makeText(
                this,
                "Không xác định được chuyên khoa. Vui lòng liên hệ admin.",
                Toast.LENGTH_LONG
        ).show();
    }


    private void loadAllDoctors() {
        db.collection("doctors")
                .get()
                .addOnSuccessListener(snapshot -> {
                    doctors.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        Doctor d = doc.toObject(Doctor.class);
                        if (d != null) {
                            d.setId(doc.getId());
                            doctors.add(d);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Lỗi tải danh sách bác sĩ",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}
