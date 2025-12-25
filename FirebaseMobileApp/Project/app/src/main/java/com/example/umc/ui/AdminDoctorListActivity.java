package com.example.umc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.AdminDoctorAdapter;
import com.example.umc.models.Doctor;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminDoctorListActivity extends AppCompatActivity {

    RecyclerView rvDoctors;
    AdminDoctorAdapter adapter;
    List<Doctor> doctors = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_doctor_list);

        // ===== TOOLBAR =====
        MaterialToolbar toolbar = findViewById(R.id.toolbarAdminDoctorList);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());


        // ===== RECYCLER =====
        rvDoctors = findViewById(R.id.rvAdminDoctors);
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminDoctorAdapter(this, doctors, doctor -> {
            showEditDialog(doctor);   // 🔥 CLICK ITEM → SỬA
        });

        rvDoctors.setAdapter(adapter);

        loadDoctors();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_doctor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_manage) {
            showToolbarActionDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showToolbarActionDialog() {
        String[] actions = {
                "➕ Thêm bác sĩ",
                "🗑️ Xóa bác sĩ"
        };

        new AlertDialog.Builder(this)
                .setTitle("Quản lý bác sĩ")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        startActivity(new Intent(this, AddDoctorActivity.class));
                    } else {
                        startActivity(new Intent(this, DeleteDoctorActivity.class));
                    }
                })
                .show();
    }


    private void loadDoctors() {
        db.collection("doctors")
                .get()
                .addOnSuccessListener(qs -> {
                    doctors.clear();
                    for (DocumentSnapshot doc : qs) {
                        Doctor d = doc.toObject(Doctor.class);
                        if (d != null) {
                            d.setId(doc.getId());
                            doctors.add(d);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Lỗi tải danh sách bác sĩ",
                                Toast.LENGTH_SHORT).show());
    }

    // ===== DIALOG SỬA (CLICK BÁC SĨ) =====
    private void showEditDialog(Doctor doctor) {
        new AlertDialog.Builder(this)
                .setTitle("Sửa thông tin bác sĩ")
                .setMessage("Bạn muốn chỉnh sửa thông tin bác sĩ này?")
                .setPositiveButton("Sửa", (d, w) -> {
                    startActivity(
                            new Intent(this, AdminEditDoctorActivity.class)
                                    .putExtra("doctorId", doctor.getId())
                    );
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    // ===== DIALOG TOOLBAR (THÊM / XOÁ) =====
    private void showToolbarDialog() {
        String[] actions = {
                "➕ Thêm bác sĩ",
                "🗑️ Xóa bác sĩ"
        };

        new AlertDialog.Builder(this)
                .setTitle("Quản lý bác sĩ")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        startActivity(
                                new Intent(this, AddDoctorActivity.class)
                        );
                    } else {
                        startActivity(
                                new Intent(this, DeleteDoctorActivity.class)
                        );
                    }
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDoctors();
    }
}
