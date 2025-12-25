package com.example.umc.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.umc.R;
import com.example.umc.models.Appointment;
import com.example.umc.utils.NotificationUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminAppointmentDetailActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String appointmentId;

    TextView tvPatient, tvDoctor, tvTime, tvStatus, tvPaymentContent;
    Button btnApprove, btnCancel, btnComplete, btnConfirmPayment;

    Appointment appointment;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin_appointment_detail);

        appointmentId = getIntent().getStringExtra("appointmentId");
        if (appointmentId == null) {
            finish();
            return;
        }

        tvPatient = findViewById(R.id.tvPatient);
        tvDoctor = findViewById(R.id.tvDoctor);
        tvTime = findViewById(R.id.tvTime);
        tvStatus = findViewById(R.id.tvStatus);
        tvPaymentContent = findViewById(R.id.tvPaymentContent);

        btnApprove = findViewById(R.id.btnApprove);
        btnCancel = findViewById(R.id.btnCancel);
        btnComplete = findViewById(R.id.btnComplete);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnApprove.setOnClickListener(v -> approve());
        btnCancel.setOnClickListener(v -> cancelByDoctor());
        btnComplete.setOnClickListener(v -> complete());
        btnConfirmPayment.setOnClickListener(v -> confirmPayment());

        loadDetail();
    }

    private void loadDetail() {
        db.collection("appointments")
                .document(appointmentId)
                .get()
                .addOnSuccessListener(doc -> {

                    appointment = doc.toObject(Appointment.class);
                    if (appointment == null) return;

                    tvPatient.setText(
                            "👤 Bệnh nhân: " +
                                    safe(appointment.getUserName())
                    );

                    tvDoctor.setText(
                            "🩺 Bác sĩ: " +
                                    safe(appointment.getDoctorName())
                    );

                    tvTime.setText("⏰ Thời gian: " + appointment.getAppointmentTime());
                    tvStatus.setText("📌 Trạng thái: " + appointment.getStatus());

                    if (appointment.getPaymentContent() != null) {
                        tvPaymentContent.setVisibility(View.VISIBLE);
                        tvPaymentContent.setText(
                                "💰 Nội dung chuyển khoản:\n" +
                                        appointment.getPaymentContent()
                        );
                    } else {
                        tvPaymentContent.setVisibility(View.GONE);
                    }

                    updateButtons();
                });
    }

    // ===============================
    // BUTTON STATE
    // ===============================
    private void updateButtons() {

        btnApprove.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnComplete.setVisibility(View.GONE);
        btnConfirmPayment.setVisibility(View.GONE);

        switch (appointment.getStatus()) {

            case "HOLDING":
                btnApprove.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;

            case "APPROVED":
                btnConfirmPayment.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;

            case "BOOKED":
                btnComplete.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;
        }
    }

    // ===============================
    // APPROVE
    // ===============================
    private void approve() {

        long approveUntil = System.currentTimeMillis() + 10 * 60 * 1000;

        String paymentCode = "UMC-" + appointmentId.substring(0, 6);
        String paymentContent =
                appointment.getUserEmail() + "_" + paymentCode;

        db.collection("appointments")
                .document(appointmentId)
                .update(
                        "status", "APPROVED",
                        "approveUntil", approveUntil,
                        "paymentCode", paymentCode,
                        "paymentContent", paymentContent
                )
                .addOnSuccessListener(a -> {

                    NotificationUtil.create(
                            db,
                            appointment.getUserId(),
                            "Lịch được duyệt",
                            "Vui lòng chuyển khoản với nội dung:\n" + paymentContent,
                            "APPROVED",
                            appointmentId
                    );

                    loadDetail();
                });
    }

    // ===============================
    // ADMIN CONFIRM PAYMENT
    // ===============================
    private void confirmPayment() {

        db.collection("appointments")
                .document(appointmentId)
                .update(
                        "status", "BOOKED",
                        "paid", true,
                        "paidAt", System.currentTimeMillis(),
                        "paymentMethod", "BANK_TRANSFER"
                )
                .addOnSuccessListener(a -> {

                    NotificationUtil.create(
                            db,
                            appointment.getUserId(),
                            "Thanh toán thành công",
                            "Admin đã xác nhận thanh toán lịch khám",
                            "PAYMENT",
                            appointmentId
                    );

                    loadDetail();
                });
    }

    // ===============================
    // CANCEL
    // ===============================
    private void cancelByDoctor() {
        db.collection("appointments")
                .document(appointmentId)
                .update("status", "CANCELLED_BY_DOCTOR")
                .addOnSuccessListener(a -> {

                    NotificationUtil.create(
                            db,
                            appointment.getUserId(),
                            "Lịch bị hủy",
                            "Lịch khám bị hủy bởi bác sĩ",
                            "CANCEL",
                            appointmentId
                    );

                    loadDetail();
                });
    }

    // ===============================
    // COMPLETE
    // ===============================
    private void complete() {
        db.collection("appointments")
                .document(appointmentId)
                .update("status", "COMPLETED")
                .addOnSuccessListener(a -> loadDetail());
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
