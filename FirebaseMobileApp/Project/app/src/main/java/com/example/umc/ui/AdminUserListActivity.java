package com.example.umc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.AdminUserAdapter;
import com.example.umc.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminUserListActivity extends AppCompatActivity {

    RecyclerView rv;
    AdminUserAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin_user_list);

        rv = findViewById(R.id.rvUsers);
        rv.setLayoutManager(new LinearLayoutManager(this));
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

// bật nút back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

// xử lý click
        toolbar.setNavigationOnClickListener(v -> finish());
        loadUsers();
    }

    private void loadUsers() {
        db.collection("users")
                .whereEqualTo("role", "patient")
                .get()
                .addOnSuccessListener(snap -> {
                    List<User> list = new ArrayList<>();
                    for (var doc : snap) {
                        User u = doc.toObject(User.class);
                        u.setId(doc.getId());
                        list.add(u);
                    }
                    adapter = new AdminUserAdapter(list, user -> {
                        Intent i = new Intent(this, AdminUserAppointmentListActivity.class);
                        i.putExtra("userId", user.getId());
                        i.putExtra("userName", user.getName());
                        startActivity(i);
                    });
                    rv.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải user", Toast.LENGTH_SHORT).show());
    }
}
