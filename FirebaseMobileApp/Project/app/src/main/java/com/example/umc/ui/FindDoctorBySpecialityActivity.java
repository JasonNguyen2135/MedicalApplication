package com.example.umc.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.DoctorAdapter;

import com.example.umc.models.Doctor;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindDoctorBySpecialityActivity extends AppCompatActivity {

    Spinner sp;
    Button btnFind;
    RecyclerView rv;
    DoctorAdapter adapter;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    List<Doctor> doctors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_doctor_speciality);

        sp = findViewById(R.id.spSpeciality);
        btnFind = findViewById(R.id.btnFind);
        rv = findViewById(R.id.rvDoctorSpeciality);

        rv.setLayoutManager(new LinearLayoutManager(this));

        // 🔥 KHỞI TẠO ADAPTER TRƯỚC
        adapter = new DoctorAdapter(
                this,
                doctors,
                doctor -> {
                    // click vào bác sĩ → mở DoctorDetail
                    startActivity(
                            new android.content.Intent(
                                    FindDoctorBySpecialityActivity.this,
                                    com.example.umc.ui.DoctorDetailActivity.class
                            ).putExtra("doctorId", doctor.getId())
                    );
                }
        );
        rv.setAdapter(adapter);


        setupSpinner();

        btnFind.setOnClickListener(v -> searchBySpeciality());
    }

    private void setupSpinner() {
        String[] list = {
                "Nội tổng quát",
                "Da liễu",
                "Xương khớp",
                "Tim mạch",
                "Tai - Mũi - Họng",
                "Mắt",
                "Tiêu hóa",
                "Thần kinh"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        list);

        sp.setAdapter(adapter);
    }

    private void searchBySpeciality() {
        String selected = sp.getSelectedItem().toString(); // ✅ FIX LỖI 1

        db.collection("doctors")
                .whereEqualTo("speciality", selected)
                .get()
                .addOnSuccessListener(snapshot -> {
                    doctors.clear();

                    for (DocumentSnapshot doc : snapshot) {
                        Doctor d = doc.toObject(Doctor.class);
                        d.setId(doc.getId());          // 🔥 QUAN TRỌNG
                        doctors.add(d);
                    }

                    if (doctors.isEmpty()) {
                        Toast.makeText(this,
                                "Không tìm thấy bác sĩ",
                                Toast.LENGTH_SHORT).show();
                    }

                    adapter.notifyDataSetChanged();     // ✅ FIX LỖI 2
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Lỗi Firebase",
                                Toast.LENGTH_SHORT).show()
                );
    }
}
