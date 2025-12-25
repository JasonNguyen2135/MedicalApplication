package com.example.umc.ui;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.umc.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class    EditProfileActivity extends AppCompatActivity {

    ImageView imgAvatar;
    Button  btnSave;
    EditText etName, etPhone, etBirthday, etGender;

    Uri selectedImageUri;
    EditText etAvatarUrl;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();

    ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Log.e("EDIT_LIFE", "onCreate");

        imgAvatar = findViewById(R.id.imgEditAvatar);
        etAvatarUrl = findViewById(R.id.etAvatarUrl);
        btnSave = findViewById(R.id.btnSaveProfile);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etBirthday = findViewById(R.id.etBirthday);
        etGender = findViewById(R.id.etGender);


        loadUserInfo();

        btnSave.setOnClickListener(v -> saveProfile());
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("EDIT_LIFE", "onDestroy");
    }

    private void loadUserInfo() {
        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    etName.setText(doc.getString("name"));
                    etPhone.setText(doc.getString("phone"));
                    etBirthday.setText(doc.getString("birthday"));
                    etGender.setText(doc.getString("gender"));

                    String avatarUrl = doc.getString("avatarUrl");
                    if (avatarUrl != null) {
                        Glide.with(this).load(avatarUrl).into(imgAvatar);
                    }
                });
    }

    private void saveProfile() {
        String uid = auth.getUid();
        if (uid == null) return;

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        String gender = etGender.getText().toString().trim();
        String avatarUrl = etAvatarUrl.getText().toString().trim(); // 👈 ô nhập URL ảnh

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
            return;
        }

        // 👉 Nếu user không nhập URL → gán avatar mặc định
        if (avatarUrl.isEmpty()) {
            avatarUrl = "https://i.pravatar.cc/150?u=" + uid;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phone);
        data.put("birthday", birthday);
        data.put("gender", gender);
        data.put("avatarUrl", avatarUrl);

        db.collection("users")
                .document(uid)
                .update(data)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Đã lưu hồ sơ!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi lưu hồ sơ", Toast.LENGTH_SHORT).show()
                );
    }



    private void uploadAvatar(String uid, Map<String, Object> data) {
        StorageReference ref = storage
                .getReference("avatars/" + uid + ".jpg");

        ref.putFile(selectedImageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    data.put("avatarUrl", uri.toString());

                    db.collection("users")
                            .document(uid)
                            .update(data)
                            .addOnSuccessListener(a -> {
                                Toast.makeText(this, "Đã lưu hồ sơ!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload avatar lỗi", Toast.LENGTH_SHORT).show()
                );
    }

}
