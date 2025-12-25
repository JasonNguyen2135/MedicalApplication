package com.example.umc.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.umc.R;
import com.example.umc.utils.NotificationUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminPostNewsActivity extends AppCompatActivity {

    EditText etTitle, etContent;
    Button btnPost;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin_post_news);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnPost = findViewById(R.id.btnPost);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        btnPost.setOnClickListener(v -> postNews());
    }

    private void postNews() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("content", content);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("createdBy", auth.getUid());

        db.collection("news")
                .add(data)
                .addOnSuccessListener(d -> {

                    db.collection("users").get().addOnSuccessListener(snap -> {
                        for (var u : snap) {
                            NotificationUtil.create(
                                    db,
                                    u.getId(),
                                    "Tin tức mới",
                                    "Admin vừa đăng một bài viết mới",
                                    "NEWS",
                                    d.getId() // 🔥 newsId
                            );
                        }
                    });

                    Toast.makeText(this, "Đăng tin thành công", Toast.LENGTH_SHORT).show();
                });


    }
}
