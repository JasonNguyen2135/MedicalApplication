package com.example.umc.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.umc.R;
import com.example.umc.models.Doctor;
import com.example.umc.models.WorkSchedule;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDoctorActivity extends AppCompatActivity {

    EditText etDoctorName, etAvatarUrl;
    Spinner spSpeciality, spExperience;
    ImageView imgPreview;
    Button btnSave, btnPickImage;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Uri pickedImageUri = null;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_add_doctor);

        etDoctorName = findViewById(R.id.etDoctorName);
        etAvatarUrl = findViewById(R.id.etAvatarUrl);
        spSpeciality = findViewById(R.id.spSpeciality);
        spExperience = findViewById(R.id.spExperience);
        imgPreview = findViewById(R.id.imgPreview);
        btnSave = findViewById(R.id.btnSaveDoctor);
        btnPickImage = findViewById(R.id.btnPickImage);

        setupSpecialitySpinner();
        setupExperienceSpinner();

        btnPickImage.setOnClickListener(v -> pickImage());
        btnSave.setOnClickListener(v -> saveDoctor());

        MaterialToolbar toolbar = findViewById(R.id.toolbar_add_doctor);
        toolbar.setNavigationOnClickListener(v -> finish());
        findViewById(R.id.btnCancelAdd).setOnClickListener(v -> finish());
    }

    private void pickImage() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            pickedImageUri = data.getData();
            Glide.with(this).load(pickedImageUri).into(imgPreview);
        }
    }

    private void saveDoctor() {
        String name = formatName(etDoctorName.getText().toString());
        if (name.isEmpty()) {
            toast("Tên bác sĩ không được trống");
            return;
        }

        String imageUrl = etAvatarUrl.getText().toString().trim();
        if (pickedImageUri != null) {
            imageUrl = pickedImageUri.toString();
        }

        Doctor d = new Doctor();
        d.setName(name);
        d.setSpeciality(spSpeciality.getSelectedItem().toString());
        d.setExperience(Integer.parseInt(spExperience.getSelectedItem().toString()));
        d.setImageUrl(imageUrl);
        d.setRatingAvg(0.0);
        d.setRatingCount(0L);

        // ================== 🔥 WORK SCHEDULE 🔥 ==================
        WorkSchedule ws = new WorkSchedule();
        ws.startTime = "08:00";
        ws.endTime = "17:00";
        ws.workingDays = Arrays.asList(2, 3, 4, 5, 6); // T2–T6
        ws.dayOffs = null; // OK

        d.setWorkSchedule(ws);
        // =========================================================

        db.collection("doctors")
                .add(d)
                .addOnSuccessListener(doc -> {
                    toast("Đã thêm bác sĩ + lịch làm việc");
                    finish();
                })
                .addOnFailureListener(e -> toast("Lỗi Firebase"));
    }


    private void setupSpecialitySpinner() {
        String[] s = {"Nội", "Nhi", "Tim mạch", "Ngoại", "Da liễu", "Tai mũi họng"};
        spSpeciality.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, s));
    }

    private void setupExperienceSpinner() {
        Integer[] y = new Integer[40];
        for (int i = 0; i < 40; i++) y[i] = i + 1;
        spExperience.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, y));
    }

    private String formatName(String s) {
        if (s == null) return "";
        s = s.trim().toLowerCase();
        StringBuilder b = new StringBuilder();
        for (String w : s.split("\\s+")) {
            if (!w.isEmpty())
                b.append(w.substring(0,1).toUpperCase()).append(w.substring(1)).append(" ");
        }
        return b.toString().trim();
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
