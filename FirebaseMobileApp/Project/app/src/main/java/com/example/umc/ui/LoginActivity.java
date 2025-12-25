package com.example.umc.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.umc.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoRegister, tvForgot;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);
        tvForgot = findViewById(R.id.tvForgot);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Nếu đã login trước đó → vào Home ngay
        FirebaseUser user = auth.getCurrentUser();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        btnLogin.setOnClickListener(v -> doLogin());

        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        tvForgot.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );
    }

    private void doLogin() {
        String email = text(etEmail);
        String pass = text(etPassword);

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    btnLogin.setEnabled(true);

                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    Toast.makeText(this, "Sai email hoặc mật khẩu!", Toast.LENGTH_LONG).show();
                });
    }


    private String text(TextInputEditText t) {
        return t.getText() != null ? t.getText().toString().trim() : "";
    }
}
