package com.example.umc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.umc.R;
import com.example.umc.models.Appointment;
import com.example.umc.models.NotificationModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookingActivity extends AppCompatActivity {

    TextInputEditText etSymptoms;
    Button btnConfirm;

    String date, time, doctorId;
    Double price;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    String doctorName;
    String doctorSpeciality;

    // ⏱️ GIỮ SLOT 5 PHÚT
    private static final long HOLD_DURATION_MS = 5 * 60 * 1000;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_booking);

        etSymptoms = findViewById(R.id.etSymptoms);
        btnConfirm = findViewById(R.id.btnConfirmBooking);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        doctorId = getIntent().getStringExtra("doctorId");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnConfirm.setOnClickListener(v -> createBooking());
    }

    // =====================================================
    // 1️⃣ LẤY THÔNG TIN BÁC SĨ
    // =====================================================
    private void createBooking() {

        db.collection("doctors")
                .document(doctorId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "Không tìm thấy bác sĩ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    doctorName = doc.getString("name");
                    doctorSpeciality = doc.getString("speciality");
                    price = getPriceBySpeciality(doctorSpeciality);

                    checkSlotAndSave(); // 🔥
                });
    }



    // =====================================================
    // 2️⃣ CHECK SLOT
    // =====================================================
    private void checkSlotAndSave() {

        String selectedDateTime = date + " " + time;
        long now = System.currentTimeMillis();

        db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("appointmentTime", selectedDateTime)
                .get()
                .addOnSuccessListener(qs -> {

                    for (var d : qs.getDocuments()) {
                        Appointment a = d.toObject(Appointment.class);
                        if (a == null) continue;

                        // ⛔ APPROVED nhưng quá hạn → hủy
                        if ("APPROVED".equals(a.getStatus())
                                && a.getHoldUntil() != null
                                && a.getHoldUntil() < now) {

                            d.getReference()
                                    .update("status", "CANCELLED_BY_SYSTEM");
                            continue;
                        }

                        // ❌ SLOT ĐÃ CÓ NGƯỜI
                        if (!a.getStatus().startsWith("CANCELLED")) {
                            Toast.makeText(this,
                                    "Khung giờ này đã có người đặt",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    saveAppointment();
                });
    }
    // =====================================================
    // 3️⃣ SAVE – HOLDING
    // =====================================================
    private void saveAppointment() {

        long now = System.currentTimeMillis();
        long holdUntil = now + HOLD_DURATION_MS;

        Appointment a = new Appointment();
        a.setDoctorId(doctorId);
        a.setDoctorName(doctorName);
        a.setDoctorSpeciality(doctorSpeciality);

        a.setUserId(auth.getUid());
        a.setAppointmentTime(date + " " + time);
        a.setNote(etSymptoms.getText() != null ? etSymptoms.getText().toString() : "");

        // 🔥 TRẠNG THÁI MỚI
        a.setStatus("APPROVED");
        a.setHoldUntil(holdUntil);   // ⏳ hạn thanh toán
        a.setPaid(false);
        a.setPrice(price);

        db.collection("appointments")
                .add(a)
                .addOnSuccessListener(doc -> {

                    String appointmentId = doc.getId();

                    sendNotification(appointmentId);

                    Toast.makeText(this,
                            "Đặt lịch thành công. Vui lòng thanh toán trong 5 phút",
                            Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(this, AppointmentDetailActivity.class);
                    i.putExtra("appointmentId", appointmentId);
                    startActivity(i);
                    finish();
                });
    }
    private void sendNotification(String appointmentId) {

        String uid = auth.getUid();
        if (uid == null) return;

        String adminId = "SuHKStIIR1Xo3JJBMaDQ1DsWMAc2";

        // USER
        db.collection("notifications").add(
                new NotificationModel(
                        uid,
                        "BOOKING",
                        "Đặt lịch thành công",
                        "Bạn đã giữ lịch khám vào " + date + " " + time,
                        appointmentId
                )
        );

        // ADMIN
        db.collection("notifications").add(
                new NotificationModel(
                        adminId,
                        "BOOKING",
                        "Có lịch khám mới",
                        "Một bệnh nhân vừa đặt lịch khám",
                        appointmentId
                )
        );
    }

    // =====================================================
    // PRICE
    // =====================================================
    private double getPriceBySpeciality(String speciality) {
        if (speciality == null) return 150000;

        switch (speciality) {
            case "Nội tổng hợp": return 150000;
            case "Nhi khoa": return 180000;
            case "Tim mạch": return 250000;
            case "Da liễu": return 200000;
            case "Tai - Mũi - Họng": return 180000;
            case "Mắt": return 170000;
            case "Phụ sản": return 220000;
            case "Ngoại khoa": return 300000;
            default: return 150000;
        }
    }
}
