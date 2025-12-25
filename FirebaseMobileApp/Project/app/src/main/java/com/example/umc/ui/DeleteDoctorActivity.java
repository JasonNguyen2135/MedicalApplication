package com.example.umc.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.DoctorDeleteAdapter;

import com.example.umc.models.Doctor;
import com.example.umc.utils.NotificationUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class DeleteDoctorActivity extends AppCompatActivity {

    RecyclerView rv;
    DoctorDeleteAdapter adapter;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_delete_doctor);

        rv = findViewById(R.id.recyclerDeleteDoctor);
        rv.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        loadDoctors();
        MaterialToolbar toolbar = findViewById(R.id.toolbar_delete_doctor);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    private void deleteDoctorAndHandleAppointments(
            Doctor doctor,
            int position
    ) {
        String doctorId = doctor.getId();

        db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (DocumentSnapshot d : snapshot) {

                        Boolean paid = d.getBoolean("paid");
                        String userId = d.getString("userId");
                        Double price = d.getDouble("price");


                        // ===== ĐÁNH DẤU LỊCH BỊ ẢNH HƯỞNG =====
                        d.getReference().update(
                                "status", "AFFECTED_BY_DOCTOR_LEAVE",
                                "affectedReason", "DOCTOR_DELETED",
                                "refundStatus", Boolean.TRUE.equals(paid)
                                        ? "WAITING_AT_COUNTER"
                                        : null
                        );
                        NotificationUtil.create(
                                db,
                                userId,
                                "Lịch khám bị hủy",
                                "Bác sĩ đã nghỉ làm. "
                                        + "Vui lòng mang mã lịch khám đến quầy để được hoàn tiền.",
                                "REFUND_AT_COUNTER",
                                d.getId()
                        );

                    }

                    // ===== XÓA BÁC SĨ =====
                    db.collection("doctors")
                            .document(doctorId)
                            .delete()
                            .addOnSuccessListener(a -> {
                                Toast.makeText(
                                        this,
                                        "Đã xóa bác sĩ và hoàn tiền các lịch liên quan",
                                        Toast.LENGTH_SHORT
                                ).show();
                                adapter.removeItem(position);
                            });
                });
    }


    private void loadDoctors() {
        db.collection("doctors")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Doctor> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot) {
                        Doctor d = doc.toObject(Doctor.class);
                        d.setId(doc.getId()); // 🔥 QUAN TRỌNG
                        list.add(d);
                    }

                    adapter = new DoctorDeleteAdapter(
                            this,
                            list,
                            (doctor, position) -> {
                                deleteDoctorAndHandleAppointments(doctor, position);
                            }
                    );


                    rv.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải danh sách bác sĩ", Toast.LENGTH_SHORT).show()
                );
    }
}
