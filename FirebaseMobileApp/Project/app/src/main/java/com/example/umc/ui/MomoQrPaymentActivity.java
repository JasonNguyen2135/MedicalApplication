package com.example.umc.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.umc.R;
import com.example.umc.utils.NotificationUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MomoQrPaymentActivity extends AppCompatActivity {

    TextView tvCountdown, tvAmount, tvOrder, tvPaymentContent;
    ImageView btnCopy;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    String appointmentId;
    CountDownTimer timer;

    private static final long TIMEOUT = 2 * 60 * 1000; // 2 phút

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_momo_qr_payment);

        tvCountdown = findViewById(R.id.tvCountdown);
        tvAmount = findViewById(R.id.tvAmount);
        tvOrder = findViewById(R.id.tvOrder);
        tvPaymentContent = findViewById(R.id.tvPaymentContent);
        btnCopy = findViewById(R.id.btnCopy);

        appointmentId = getIntent().getStringExtra("appointmentId");
        if (appointmentId == null) {
            finish();
            return;
        }

        loadPaymentInfo();
    }

    // ===============================
    // LOAD INFO + TẠO NỘI DUNG CK
    // ===============================
    private void loadPaymentInfo() {

        db.collection("appointments")
                .document(appointmentId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        finish();
                        return;
                    }

                    Double price = doc.getDouble("price");
                    String time = doc.getString("appointmentTime");

                    // 🔥 EMAIL USER (từ FirebaseAuth)
                    String email = auth.getCurrentUser() != null
                            ? auth.getCurrentUser().getEmail()
                            : "USER";

                    // 🔥 PAYMENT CODE
                    String paymentCode =
                            "APT_" + appointmentId.substring(0, 6).toUpperCase();

                    String paymentContent = email + "_" + paymentCode;

                    // (optional) lưu để admin đối soát
                    doc.getReference().update("paymentContent", paymentContent);

                    tvAmount.setText("Số tiền: " + price.intValue() + " VNĐ");
                    tvOrder.setText("Lịch khám: " + time);
                    tvPaymentContent.setText(paymentContent);

                    btnCopy.setOnClickListener(v ->
                            copyToClipboard(paymentContent)
                    );

                    startCountdown();
                });
    }

    // ===============================
    // COPY TO CLIPBOARD
    // ===============================
    private void copyToClipboard(String text) {

        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        clipboard.setPrimaryClip(
                ClipData.newPlainText("payment", text)
        );

        Toast.makeText(this,
                "Đã copy nội dung chuyển khoản",
                Toast.LENGTH_SHORT).show();
    }

    // ===============================
    // COUNTDOWN
    // ===============================
    private void startCountdown() {

        timer = new CountDownTimer(TIMEOUT, 1000) {
            @Override
            public void onTick(long millis) {
                long sec = millis / 1000;
                tvCountdown.setText(
                        String.format("⏳ %02d:%02d", sec / 60, sec % 60)
                );
            }

            @Override
            public void onFinish() {
                paymentTimeout();
            }
        };
        timer.start();
    }

    // ===============================
    // TIMEOUT → AUTO CANCEL
    // ===============================
    private void paymentTimeout() {

        db.collection("appointments")
                .document(appointmentId)
                .update(
                        "status", "CANCELLED_BY_SYSTEM",
                        "cancelType", "PAYMENT_TIMEOUT"
                )
                .addOnSuccessListener(a -> {

                    NotificationUtil.create(
                            db,
                            auth.getUid(),
                            "Hết thời gian thanh toán",
                            "Lịch khám đã bị hủy do quá hạn thanh toán",
                            "PAYMENT",
                            appointmentId
                    );

                    finish();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
