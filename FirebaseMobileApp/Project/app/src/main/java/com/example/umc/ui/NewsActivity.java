package com.example.umc.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.NewsAdapter;
import com.example.umc.models.NewsModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity {

    RecyclerView rv;
    List<NewsModel> list = new ArrayList<>();
    NewsAdapter adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_news);

        rv = findViewById(R.id.rvNews);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NewsAdapter(list);
        rv.setAdapter(adapter);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        loadNews();
    }

    private void loadNews() {
        try {
            FirebaseFirestore.getInstance()
                    .collection("news")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            return;
                        }
                        if (snapshots == null) return;

                        list.clear();
                        for (QueryDocumentSnapshot d : snapshots) {
                            try {
                                NewsModel n = d.toObject(NewsModel.class);
                                if (n != null) {
                                    list.add(n);
                                }
                            } catch (Exception ex) {
                                // Skip if object conversion fails
                            }
                        }
                        adapter.notifyDataSetChanged();
                    });
        } catch (Exception e) {
            // Handle any Firestore exceptions
        }
    }
}
