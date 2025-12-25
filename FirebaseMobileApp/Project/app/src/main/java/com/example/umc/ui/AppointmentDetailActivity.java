package com.example.umc.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.umc.R;
import com.example.umc.api.ApiService;
import com.example.umc.api.RetrofitClient;
import com.example.umc.dto.PaymentRequest;
import com.example.umc.dto.PaymentResponse;
import com.example.umc.models.Appointment;
import com.example.umc.models.NotificationModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentDetailActivity extends AppCompatActivity {

    TextView tvDoctor, tvDate, tvTime, tvStatus, tvPrice, tvIdappointment,tvRoom,tvFaculty;
    MaterialButton btnPay, btnCancel, btnRating;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ListenerRegistration appointmentListener;

    String appointmentId;
    Long currentPrice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);

        tvDoctor = findViewById(R.id.tvDetailDoctor);
        tvDate = findViewById(R.id.tvDetailDate);
        tvTime = findViewById(R.id.tvDetailTime);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvPrice = findViewById(R.id.tvPrice);
        tvIdappointment = findViewById(R.id.tvIdAppointment);
        tvFaculty=findViewById(R.id.tvDetailFaculty);

        btnPay = findViewById(R.id.btnPay);
        btnCancel = findViewById(R.id.btnCancelAppointment);
        btnRating = findViewById(R.id.btnRating);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        tvRoom=findViewById(R.id.tvRoom);

        appointmentId = getIntent().getStringExtra("appointmentId");
        if (appointmentId == null && getIntent().getData() != null) {
            appointmentId = getIntent().getData().getQueryParameter("appointmentId");
        }

        if (appointmentId == null) {
            finish();
            return;
        }

        btnPay.setOnClickListener(v -> payAppointment());
        btnCancel.setOnClickListener(v -> cancelAppointment());
        btnRating.setOnClickListener(v -> showRatingDialog());

        handlePaymentResult(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenAppointment();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (appointmentListener != null) appointmentListener.remove();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handlePaymentResult(intent);
    }

    // =====================================================
    // PAYMENT
    // =====================================================

    private void payAppointment() {
        Log.d("PAY_TEST", "Pay button clicked");

        if (currentPrice == null) {
            Toast.makeText(this, "Không xác định được giá khám", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("PAY_REQUEST", "appointmentId = " + appointmentId);
        Log.d("PAY_REQUEST", "amount = " + currentPrice);

        ApiService apiService = RetrofitClient.get().create(ApiService.class);

        PaymentRequest request = new PaymentRequest(appointmentId, currentPrice);

        apiService.createPayment(request)
                .enqueue(new Callback<PaymentResponse>() {
                    @Override
                    public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {

                        Log.d("PAY_RESPONSE", "HTTP code = " + response.code());

                        if (response.body() != null) {
                            Log.d("PAY_RESPONSE", "body = " + response.body().toString());
                        } else {
                            Log.d("PAY_RESPONSE", "body = null");
                        }

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getPaymentUrl() != null) {

                            Log.d("PAY_URL", response.body().getPaymentUrl());

                            startActivity(new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(response.body().getPaymentUrl())
                            ));
                        } else {
                            Toast.makeText(
                                    AppointmentDetailActivity.this,
                                    "Backend trả về lỗi",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PaymentResponse> call, Throwable t) {
                        Log.e("PAYMENT_API", "API error", t);
                        Toast.makeText(
                                AppointmentDetailActivity.this,
                                "Lỗi gọi backend",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void handlePaymentResult(Intent intent) {
        if (intent == null || intent.getData() == null) return;

        Uri uri = intent.getData();
        String status = uri.getQueryParameter("status");
        String apptId = uri.getQueryParameter("appointmentId");

        if (!appointmentId.equals(apptId)) return;

        if ("success".equals(status)) {

            // 1️⃣ Update appointment
            db.collection("appointments")
                    .document(appointmentId)
                    .update("status", "BOOKED", "paid", true);

            // 2️⃣ PUSH NOTIFICATION (Firestore)
            pushBookingNotification();

            Toast.makeText(this, "Thanh toán thành công", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, "Thanh toán thất bại", Toast.LENGTH_LONG).show();
        }
    }
    private void pushBookingNotification() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String date = tvDate.getText().toString().replace("Ngày: ", "");
        String time = tvTime.getText().toString().replace("Giờ: ", "");

        NotificationModel noti = new NotificationModel(
                uid,
                "PAYMENT",
                "Thanh toán thành công",
                "Bạn đã thanh toán lịch khám vào " + date + " " + time,
                appointmentId
        );

        db.collection("notifications")
                .add(noti)
                .addOnSuccessListener(doc ->
                        Log.d("NOTI_PUSH", "Notification saved"))
                .addOnFailureListener(e ->
                        Log.e("NOTI_PUSH", "Failed", e));
    }


    // =====================================================
    // FIRESTORE
    // =====================================================

    private void listenAppointment() {
        appointmentListener = db.collection("appointments")
                .document(appointmentId)
                .addSnapshotListener((doc, e) -> {
                    if (e != null || doc == null || !doc.exists()) return;

                    Appointment a = doc.toObject(Appointment.class);
                    if (a == null) return;

                    // ⏳ AUTO-CANCEL NẾU QUÁ HẠN THANH TOÁN
                    if ("APPROVED".equals(a.getStatus())
                            && !a.isPaid()
                            && a.getHoldUntil() != null
                            && a.getHoldUntil() < System.currentTimeMillis()) {

                        doc.getReference()
                                .update("status", "CANCELLED_BY_SYSTEM");
                        return;
                    }

                    // ===== UPDATE UI =====
                    tvDoctor.setText("Bác sĩ: " + safe(a.getDoctorName()));
                    tvIdappointment.setText("Mã lịch: " + appointmentId);
                    tvStatus.setText("Trạng thái: " + convertStatus(a.getStatus()));

                    if (a.getAppointmentTime() != null) {
                        String[] dt = a.getAppointmentTime().split(" ");
                        tvDate.setText("Ngày: " + dt[0]);
                        tvTime.setText("Giờ: " + dt[1]);
                    }

                    // ✅ CHUYÊN KHOA
                    if (a.getDoctorSpeciality() != null) {
                        tvFaculty.setText("Chuyên khoa: " + a.getDoctorSpeciality());

                        // ✅ PHÒNG KHÁM
                        String room = room(a.getDoctorSpeciality());
                        tvRoom.setText("Phòng khám: " + room);
                    }

                    if (a.getPrice() != null) {
                        currentPrice = a.getPrice().longValue();
                        tvPrice.setText("Giá: " + currentPrice + " VNĐ");
                    }

                    updateUI(a);
                });
    }


    public String room (String Speciality){

        String room="";
        switch (Speciality) {
            case "Nhi":
                room="Phòng 101";
                break;
            case "Nội":
                room="Phòng 102";
                break;
            case "Tim mạch":
                room="Phòng 103";
                break;
            case "Ngoại":
                room="Phòng 104";
                break;
            case "Da liễu":
                room="Phòng 105";
                break;
            case "Tai Mũi Họng":
                room="Phòng 106";
                break;
            case "Cơ xương khớp":
                room="Phòng 107";
                break;
            case "Tai mũi họng":
                room="Phòng 108";
                break;
            default:
                room="Phòng khám đa khoa";
                break;
        }
        return room;
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
            case "CANCELLED_BY_USER":
                return "Bạn đã hủy";
            case "CANCELLED_BY_DOCTOR":
                return "Bác sĩ đã hủy ";
            case "CANCELLED_BY_SYSTEM":
                return "Đã bị hủy";
            case "APPROVED":
                return "Đã duyệt";
            case "BOOKED":
                return "Đã đặt";
            case "HOLDING":
                return "Đang giữ chỗ";
            case "AFFECTED_BY_DOCTOR_LEAVE":
                return "Bị hủy do bác sĩ thay đổi lịch";
            default:
                return "Không xác định";
        }
    }

    private void updateUI(Appointment a) {
        btnPay.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnRating.setVisibility(View.GONE);

        if ("APPROVED".equals(a.getStatus()) && !a.isPaid()) {
            btnPay.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        }

        if ("BOOKED".equals(a.getStatus())) {
            btnCancel.setVisibility(View.VISIBLE);
        }

        if ("COMPLETED".equals(a.getStatus()) && a.getRating() == null) {
            btnRating.setVisibility(View.VISIBLE);
        }

        if ("AFFECTED_BY_DOCTOR".equals(a.getStatus())) {
            Toast.makeText(
                    this,
                    "Lịch khám bị hủy. Vui lòng ra quầy để được hoàn tiền.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }
    }

    // =====================================================
    // OTHERS
    // =====================================================

    private void cancelAppointment() {
        db.collection("appointments")
                .document(appointmentId)
                .update("status", "CANCELLED_BY_USER");
    }

    private void showRatingDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_rating, null);
        RatingBar ratingBar = v.findViewById(R.id.ratingBar);
        EditText etReview = v.findViewById(R.id.etReview);

        new AlertDialog.Builder(this)
                .setTitle("Đánh giá bác sĩ")
                .setView(v)
                .setPositiveButton("Gửi", (d, w) -> {
                    int rating = (int) ratingBar.getRating();
                    if (rating == 0) return;

                    db.collection("appointments")
                            .document(appointmentId)
                            .update("rating", rating,
                                    "review", etReview.getText().toString());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
