package com.example.umc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.umc.ui.EditProfileActivity;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.umc.R;
import com.example.umc.models.User;
import com.example.umc.utils.ChatUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvName, tvEmail;
    private Button btnEdit, btnLogout;

    private View itemPhone, itemBirthday, itemGender;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 🔥 UID ADMIN (đúng với ChatActivity)
    private static final String ADMIN_ID = "SuHKStIIR1Xo3JJBMaDQ1DsWMAc2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_profile);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        imgAvatar = findViewById(R.id.imgAvatar);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        btnEdit = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        itemPhone = findViewById(R.id.itemPhone);
        itemBirthday = findViewById(R.id.itemBirthday);
        itemGender = findViewById(R.id.itemGender);

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        loadProfile();

        btnEdit.setOnClickListener(v -> {
            Log.d("DEBUG_EDIT", "Opening EditProfileActivity");
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        // 🔥 LOGOUT + RESET CHAT
        btnLogout.setOnClickListener(v -> logoutOnly());
    }

    // =====================================================
    // LOGOUT + RESET CHAT
    // =====================================================
    private void logoutOnly() {
        // 1️⃣ Logout Firebase
        FirebaseAuth.getInstance().signOut();

        // 2️⃣ Quay về Login
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    // =====================================================
    // LOAD PROFILE
    // =====================================================
    private void loadProfile() {
        String uid = auth.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    User u = doc.toObject(User.class);
                    if (u == null) return;

                    tvName.setText(u.getName());
                    tvEmail.setText(auth.getCurrentUser().getEmail());

                    setupItem(itemPhone, "☎", "Số điện thoại", safe(u.getPhone()));
                    setupItem(itemBirthday, "🎂", "Ngày sinh", safe(u.getBirthday()));
                    setupItem(itemGender, "⚧", "Giới tính", safe(u.getGender()));

                    if (u.getAvatarUrl() != null && !u.getAvatarUrl().isEmpty()) {
                        Glide.with(this)
                                .load(u.getAvatarUrl())
                                .placeholder(R.drawable.doctor_placeholder)
                                .into(imgAvatar);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không tải được hồ sơ", Toast.LENGTH_SHORT).show()
                );
    }

    private void setupItem(View item, String icon, String label, String value) {
        TextView tvIcon = item.findViewById(R.id.imgIcon);
        TextView tvLabel = item.findViewById(R.id.tvLabel);
        TextView tvValue = item.findViewById(R.id.tvValue);

        tvIcon.setText(icon);
        tvLabel.setText(label);
        tvValue.setText(value);
    }

    private String safe(String s) {
        return (s == null || s.isEmpty()) ? "—" : s;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }
}
