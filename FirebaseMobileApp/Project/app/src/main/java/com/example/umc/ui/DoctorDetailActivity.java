package com.example.umc.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.umc.R;
import com.example.umc.models.Doctor;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class DoctorDetailActivity extends AppCompatActivity {

    ImageView imgDoctor;
    TextView tvName, tvDept, tvExp, tvRatingSummary;
    Button btnPickDate, btnPickSlot;

    String doctorId;
    String selectedDate = "";

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_detail);

        // ===== TOOLBAR =====
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // ===== VIEW =====
        imgDoctor = findViewById(R.id.imgDoctorDetail);
        tvName = findViewById(R.id.tvDoctorNameDetail);
        tvDept = findViewById(R.id.tvDoctorDeptDetail);
        tvExp = findViewById(R.id.tvDoctorExpDetail);
        tvRatingSummary = findViewById(R.id.tvRatingSummary);

        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickSlot = findViewById(R.id.btnPickSlot);

        doctorId = getIntent().getStringExtra("doctorId");
        if (doctorId == null || doctorId.isEmpty()) {
            Toast.makeText(this, "Thiếu doctorId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDoctorInfo();

        btnPickDate.setOnClickListener(v -> pickDate());

        btnPickSlot.setOnClickListener(v -> {
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày khám", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(
                    new Intent(this, SelectSlotActivity.class)
                            .putExtra("doctorId", doctorId)
                            .putExtra("date", selectedDate)
                            .putExtra("rescheduleFrom", getIntent().getStringExtra("rescheduleFrom"))
            );

        });
    }

    private void loadDoctorInfo() {
        db.collection("doctors")
                .document(doctorId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Doctor d = doc.toObject(Doctor.class);
                    if (d == null) return;

                    tvName.setText(d.getName());
                    tvDept.setText(d.getSpeciality());
                    tvExp.setText(d.getExperience() + " năm kinh nghiệm");

                    Glide.with(this)
                            .load(d.getImageUrl())
                            .placeholder(R.drawable.doctor_placeholder)
                            .error(R.drawable.doctor_placeholder)
                            .into(imgDoctor);

                    if (d.getRatingCount() == null || d.getRatingCount() == 0) {
                        tvRatingSummary.setText("⭐ Chưa có đánh giá");
                    } else {
                        tvRatingSummary.setText(
                                "⭐ " + String.format("%.1f", d.getRatingAvg())
                                        + " (" + d.getRatingCount() + " đánh giá)"
                        );
                    }
                });
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    selectedDate = year + "-" + (month + 1) + "-" + day;
                    btnPickDate.setText("📅 Ngày khám: " + selectedDate);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        // 🔥 CHỈ CHO CHỌN TỪ HÔM NAY
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());

        dialog.show();
    }
}
