package com.example.umc.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.umc.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewsDetailActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_news_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvContent = findViewById(R.id.tvContent);
        TextView tvMeta = findViewById(R.id.tvMeta);

        String newsId = getIntent().getStringExtra("newsId");
        if (newsId == null) {
            finish();
            return;
        }

        db.collection("news")
                .document(newsId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    tvTitle.setText(doc.getString("title"));
                    tvContent.setText(doc.getString("content"));
                    tvMeta.setText("Đăng bởi Admin");
                });
    }
}
