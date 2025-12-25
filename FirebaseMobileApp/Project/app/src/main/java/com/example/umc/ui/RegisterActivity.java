package com.example.umc.ui;

import android.content.Intent;
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
import java.util.HashMap;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;


public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvGoLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmailReg);
        etPassword = findViewById(R.id.etPasswordReg);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);



        // ⭐ MUST HAVE: Khởi tạo Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> register());

        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void register() {
        String name = text(etName);
        String email = text(etEmail);
        String pass = text(etPassword);
        String confirm = text(etConfirmPassword);

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        // ⭐ Chỉ gọi MỘT LẦN
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {

                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser == null) return;

                    String userId = firebaseUser.getUid();

                    HashMap<String, Object> user = new HashMap<>();
                    user.put("name", name);
                    user.put("email", email);
                    user.put("role", "patient");

                    db.collection("users")
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener(unused -> {

                                // 🔥 GỬI EMAIL VERIFY
                                firebaseUser.sendEmailVerification()
                                        .addOnSuccessListener(v -> {
                                            Toast.makeText(this,
                                                    "Đăng ký thành công! Vui lòng kiểm tra email để xác thực.",
                                                    Toast.LENGTH_LONG).show();

                                            auth.signOut(); // ⛔ chưa verify → không login

                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            btnRegister.setEnabled(true);
                                            Toast.makeText(this,
                                                    "Không gửi được email xác thực",
                                                    Toast.LENGTH_SHORT).show();
                                        });

                            })
                            .addOnFailureListener(e -> {
                                btnRegister.setEnabled(true);
                                Toast.makeText(this,
                                        "Lỗi lưu thông tin người dùng",
                                        Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);

                    String message = "Đăng ký thất bại";

                    if (e instanceof FirebaseAuthUserCollisionException) {
                        message = "Email đã được đăng ký. Vui lòng đăng nhập!";
                    }
                    else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        message = "Email không hợp lệ!";
                    }
                    else if (e instanceof FirebaseAuthWeakPasswordException) {
                        message = "Mật khẩu phải có ít nhất 6 ký tự!";
                    }
                    else {
                        message = e.getMessage();
                    }

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                });


    }

    private String text(TextInputEditText t) {
        return t.getText() != null ? t.getText().toString().trim() : "";
    }
}
