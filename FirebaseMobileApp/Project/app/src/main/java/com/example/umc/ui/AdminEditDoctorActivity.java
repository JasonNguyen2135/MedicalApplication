package com.example.umc.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.umc.R;
import com.example.umc.models.WorkSchedule;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.example.umc.models.Appointment;
import com.example.umc.models.Doctor;
import com.example.umc.utils.DoctorScheduleUtil;
import com.example.umc.utils.NotificationUtil;

public class AdminEditDoctorActivity extends AppCompatActivity {

    EditText etName, etSpeciality, etExperience, etImage;
    EditText etStart, etEnd, etDayOffs;
    Button btnSave;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String doctorId;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin_edit_doctor);

        // ===== GET ID =====
        doctorId = getIntent().getStringExtra("doctorId");
        if (doctorId == null || doctorId.isEmpty()) {
            Toast.makeText(this, "Thiếu doctorId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ===== TOOLBAR =====
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // ===== VIEW =====
        etName = findViewById(R.id.etDoctorName);
        etSpeciality = findViewById(R.id.etSpeciality);
        etExperience = findViewById(R.id.etExperience);
        etImage = findViewById(R.id.etImageUrl);
        etStart = findViewById(R.id.etStartTime);
        etEnd = findViewById(R.id.etEndTime);
        etDayOffs = findViewById(R.id.etDayOffs);
        btnSave = findViewById(R.id.btnSaveDoctor);

        loadDoctor();
        btnSave.setOnClickListener(v -> saveDoctor());
    }

    // ===== LOAD DATA =====
    private void loadDoctor() {
        db.collection("doctors")
                .document(doctorId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    etName.setText(doc.getString("name"));
                    etSpeciality.setText(doc.getString("speciality"));

                    Long exp = doc.getLong("experience");
                    if (exp != null) {
                        etExperience.setText(String.valueOf(exp));
                    }

                    etImage.setText(doc.getString("imageUrl"));

                    if (doc.contains("workSchedule")) {
                        WorkSchedule s = doc.get("workSchedule", WorkSchedule.class);
                        if (s != null) {
                            etStart.setText(s.startTime);
                            etEnd.setText(s.endTime);

                            if (s.dayOffs != null && !s.dayOffs.isEmpty()) {
                                etDayOffs.setText(
                                        TextUtils.join(",", s.dayOffs)
                                );
                            }
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải thông tin bác sĩ", Toast.LENGTH_SHORT).show()
                );
    }

    // ===== SAVE =====
    private void saveDoctor() {

        String name = etName.getText().toString().trim();
        String speciality = etSpeciality.getText().toString().trim();
        String expStr = etExperience.getText().toString().trim();
        String imageUrl = etImage.getText().toString().trim();
        String start = etStart.getText().toString().trim();
        String end = etEnd.getText().toString().trim();
        String dayOffText = etDayOffs.getText().toString().trim();

        // ===== VALIDATE =====
        if (name.isEmpty() || speciality.isEmpty()
                || expStr.isEmpty() || start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidTime(start) || !isValidTime(end)) {
            Toast.makeText(this, "Giờ phải đúng định dạng HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isStartBeforeEnd(start, end)) {
            Toast.makeText(this, "Giờ kết thúc phải sau giờ bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }

        int experience;
        try {
            experience = Integer.parseInt(expStr);
        } catch (Exception e) {
            Toast.makeText(this, "Số năm kinh nghiệm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // ===== DAY OFFS =====
        List<String> dayOffs = new ArrayList<>();
        if (!dayOffText.isEmpty()) {
            String[] arr = dayOffText.split("\\s*,\\s*");
            for (String d : arr) {
                if (!d.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    Toast.makeText(this,
                            "Ngày nghỉ phải đúng định dạng yyyy-MM-dd",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                dayOffs.add(d);
            }
        }

        // ===== WORK SCHEDULE =====
        WorkSchedule s = new WorkSchedule();
        s.startTime = start;
        s.endTime = end;
        s.workingDays = Arrays.asList(2, 3, 4, 5, 6); // T2–T6
        s.dayOffs = dayOffs;

        // ===== UPDATE FIREBASE =====
        db.collection("doctors")
                .document(doctorId)
                .update(
                        "name", name,
                        "speciality", speciality,
                        "experience", experience,
                        "imageUrl", imageUrl,
                        "workSchedule", s
                )
                .addOnSuccessListener(a -> {

                    Doctor doctor = new Doctor();
                    doctor.setId(doctorId);
                    doctor.setName(name);
                    doctor.setSpeciality(speciality);
                    doctor.setExperience(experience);
                    doctor.setImageUrl(imageUrl);
                    doctor.setWorkSchedule(s);

                    handleAffectedAppointments(doctor);

                    Toast.makeText(this,
                            "Đã cập nhật bác sĩ & xử lý lịch bị ảnh hưởng",
                            Toast.LENGTH_SHORT).show();

                    finish();
                })

                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi cập nhật bác sĩ", Toast.LENGTH_SHORT).show()
                );
    }
    private void handleAffectedAppointments(Doctor doctor) {

        db.collection("appointments")
                .whereEqualTo("doctorId", doctor.getId())
                .get()
                .addOnSuccessListener(snapshot -> {

                    for (var doc : snapshot) {
                        Appointment a = doc.toObject(Appointment.class);
                        if (a == null) continue;

                        String status = a.getStatus();

                        // bỏ qua lịch đã kết thúc
                        if ("COMPLETED".equals(status)
                                || "CANCELLED_BY_USER".equals(status)
                                || "CANCELLED_BY_DOCTOR".equals(status)
                                || "CANCELLED_BY_SYSTEM".equals(status)) {
                            continue;
                        }

                        boolean valid =
                                DoctorScheduleUtil.isDoctorWorking(doctor,
                                        a.getAppointmentTime().split(" ")[0])
                                        && DoctorScheduleUtil.isTimeValid(
                                        doctor,
                                        a.getAppointmentTime()
                                );

                        if (!valid) {

                            boolean paid = a.isPaid();

                            doc.getReference().update(
                                    "status", "AFFECTED_BY_DOCTOR_LEAVE",
                                    "affectedReason", "DOCTOR_SCHEDULE_CHANGED",
                                    "refundStatus", paid ? "WAITING_AT_COUNTER" : null
                            );

                            if (paid) {
                                // 🔔 ĐÃ THANH TOÁN → RA QUẦY HOÀN TIỀN
                                NotificationUtil.create(
                                        db,
                                        a.getUserId(),
                                        "Lịch khám bị hủy",
                                        "Bác sĩ thay đổi lịch làm việc. "
                                                + "Vui lòng mang mã cuộc hẹn đến quầy để được hoàn tiền.",
                                        "REFUND_AT_COUNTER",
                                        doc.getId()
                                );
                            } else {
                                // 🔔 CHƯA THANH TOÁN → CHỈ THÔNG BÁO HỦY
                                NotificationUtil.create(
                                        db,
                                        a.getUserId(),
                                        "Lịch khám bị hủy",
                                        "Lịch khám của bạn đã bị hủy do bác sĩ thay đổi lịch làm việc.",
                                        "CANCELLED",
                                        doc.getId()
                                );
                            }
                        }
                    }
                });
    }


    // ===== UTIL =====
    private boolean isValidTime(String time) {
        return time.matches("^([01]\\d|2[0-3]):[0-5]\\d$");
    }

    private boolean isStartBeforeEnd(String start, String end) {
        return start.compareTo(end) < 0;
    }
}
