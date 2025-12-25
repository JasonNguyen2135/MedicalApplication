package com.example.umc.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.umc.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText etEmail;
    Button btnSend;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnSend = findViewById(R.id.btnSendOtp);

        btnSend.setOnClickListener(v -> sendResetEmail());
        MaterialToolbar toolbar = findViewById(R.id.toolbar_forgot);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void sendResetEmail() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(a ->
                        Toast.makeText(this,
                                "Email đặt lại mật khẩu đã gửi!",
                                Toast.LENGTH_LONG).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Email không tồn tại!",
                                Toast.LENGTH_SHORT).show()
                );
    }
}
