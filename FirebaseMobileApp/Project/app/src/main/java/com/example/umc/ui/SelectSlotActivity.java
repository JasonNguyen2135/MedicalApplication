package com.example.umc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.SlotAdapter;
import com.example.umc.models.Appointment;
import com.example.umc.models.Doctor;
import com.example.umc.utils.DoctorScheduleUtil;
import com.example.umc.utils.SlotUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SelectSlotActivity extends AppCompatActivity {

    RecyclerView rvSlots;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String doctorId, date; // yyyy-MM-dd
    Doctor doctor;
    String appointmentId; // nếu reschedule

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_select_slot);

        rvSlots = findViewById(R.id.rvSlots);
        rvSlots.setLayoutManager(new GridLayoutManager(this, 3));
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        doctorId = getIntent().getStringExtra("doctorId");
        date = getIntent().getStringExtra("date");
        appointmentId = getIntent().getStringExtra("rescheduleFrom");

        // 🔥 FIX CHÍNH Ở ĐÂY
        if (appointmentId != null) {
            checkIfAllowedToReschedule();
        } else {
            loadDoctor(); // booking thường
        }
    }
    private void checkIfAllowedToReschedule() {
        db.collection("appointments")
                .document(appointmentId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        finish();
                        return;
                    }

                    String status = doc.getString("status");
                    String reason = doc.getString("affectedReason");

                    // ❌ Bác sĩ bị xóa → KHÔNG CHO ĐỔI LỊCH
                    if ("AFFECTED_BY_DOCTOR_LEAVE".equals(status)
                            && "DOCTOR_DELETED".equals(reason)) {

                        Toast.makeText(
                                this,
                                "Bác sĩ đã nghỉ. Vui lòng chọn đổi bác sĩ.",
                                Toast.LENGTH_LONG
                        ).show();
                        finish();
                        return;
                    }

                    // ✅ Các case khác → cho đổi lịch
                    loadDoctor();
                });
    }
    private boolean isSlotInFuture(String date, String slot) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date slotTime = sdf.parse(date + " " + slot);
            return slotTime != null && slotTime.getTime() > System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }


    private void openBooking(String slot) {
        if (appointmentId != null) {
            // RESCHEDULE
            db.collection("appointments")
                    .document(appointmentId)
                    .get()
                    .addOnSuccessListener(oldDoc -> {
                        Appointment old = oldDoc.toObject(Appointment.class);
                        if (old == null) return;

                        // 1️⃣ TẠO LỊCH MỚI
                        Appointment newApp = new Appointment();
                        newApp.setDoctorId(doctorId);
                        newApp.setUserId(old.getUserId());
                        newApp.setAppointmentTime(date + " " + slot);
                        newApp.setNote(old.getNote());
                        newApp.setPrice(old.getPrice());
                        newApp.setPaid(old.isPaid());

                        // giữ trạng thái logic
                        newApp.setStatus(old.getBaseStatus() != null
                                ? old.getBaseStatus()
                                : "HOLDING");

                        db.collection("appointments")
                                .add(newApp)
                                .addOnSuccessListener(newDoc -> {

                                    // 2️⃣ ĐÓNG LỊCH CŨ
                                    oldDoc.getReference().update(
                                            "status", "SUPERSEDED"
                                    );

                                    Toast.makeText(this,
                                            "Đã đổi lịch khám",
                                            Toast.LENGTH_SHORT).show();

                                    // 3️⃣ QUAY VỀ MÀN LỊCH
                                    Intent i = new Intent(this, AppointmentDetailActivity.class);
                                    i.putExtra("appointmentId", newDoc.getId()); // 🔥 QUAN TRỌNG
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                    finish();
                                });
                    });

        } else {
            // booking thường
            startActivity(
                    new Intent(this, BookingActivity.class)
                            .putExtra("doctorId", doctorId)
                            .putExtra("date", date)
                            .putExtra("time", slot)
            );
        }
    }



    private void loadDoctor() {
        db.collection("doctors")
                .document(doctorId)
                .get()
                .addOnSuccessListener(doc -> {
                    doctor = doc.toObject(Doctor.class);
                    if (doctor == null || doctor.getWorkSchedule() == null) {
                        Toast.makeText(this,
                                "Bác sĩ chưa có lịch làm việc",
                                Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    if (!DoctorScheduleUtil.isDoctorWorking(doctor, date)) {
                        Toast.makeText(this,
                                "Bác sĩ không làm việc ngày này",
                                Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    loadSlots();
                });
    }

    private void loadSlots() {
        List<String> rawSlots = SlotUtil.generateSlots(
                doctor.getWorkSchedule().startTime,
                doctor.getWorkSchedule().endTime,
                30
        );

// 🔥 LỌC SLOT THEO THỜI GIAN HIỆN TẠI
        List<String> allSlots = new ArrayList<>();

        for (String slot : rawSlots) {
            if (isSlotInFuture(date, slot)) {
                allSlots.add(slot);
            }
        }


        db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .get()
                .addOnSuccessListener(qs -> {

                    Set<String> bookedSlots = new HashSet<>();
                    long now = System.currentTimeMillis();

                    for (var d : qs.getDocuments()) {
                        Appointment a = d.toObject(Appointment.class);
                        if (a == null) continue;

                        if (a.getAppointmentTime() == null) continue;

                        if (!a.getAppointmentTime().startsWith(date)) continue;

                        // HOLD hết hạn → auto cancel
                        if ("HOLDING".equals(a.getStatus())
                                && a.getHoldUntil() != null
                                && a.getHoldUntil() < now) {

                            d.getReference().update("status", "CANCELLED_BY_SYSTEM");
                            continue;
                        }

                        // slot đã bị chiếm
                        if (!a.getStatus().startsWith("CANCELLED")) {
                            bookedSlots.add(
                                    a.getAppointmentTime().substring(11, 16)
                            );
                        }
                    }

                    SlotAdapter adapter = new SlotAdapter(
                            allSlots,
                            bookedSlots,
                            this::openBooking
                    );

                    rvSlots.setAdapter(adapter); // 🔥 BẮT BUỘC
                });
    }


}
