package com.example.umc.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umc.R;
import com.example.umc.adapters.SearchDoctorAdapter;
import com.example.umc.models.Doctor;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchDoctorActivity extends AppCompatActivity {

    EditText etSearch;
    Spinner spSpeciality;
    RecyclerView rv;
    Button btnSearch;

    SearchDoctorAdapter adapter;
    List<Doctor> allDoctors = new ArrayList<>();
    List<Doctor> filteredDoctors = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_doctor);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etSearch = findViewById(R.id.etSearch);
        spSpeciality = findViewById(R.id.spSpeciality);
        rv = findViewById(R.id.recyclerDoctors);
        btnSearch = findViewById(R.id.btnSearch);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchDoctorAdapter(this, filteredDoctors);
        rv.setAdapter(adapter);

        setupSpinner();
        loadDoctors();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s) {
                filterDoctors();
            }
        });

        btnSearch.setOnClickListener(v -> filterDoctors());
    }

    private void setupSpinner() {
        String[] specialities = {
                "Tất cả",
                "Nội",
                "Nhi",
                "Tim mạch",
                "Ngoại",
                "Da liễu",
                "Tai mũi họng"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        specialities
                );

        spSpeciality.setAdapter(adapter);
    }


    private void loadDoctors() {
        db.collection("doctors")
                .get()
                .addOnSuccessListener(snapshot -> {
                    allDoctors.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        Doctor d = doc.toObject(Doctor.class);
                        if (d != null) {
                            d.setId(doc.getId());
                            allDoctors.add(d);
                        }
                    }
                    filterDoctors();
                });
    }

    private void filterDoctors() {
        String keyword = etSearch.getText().toString().trim().toLowerCase();
        String speciality = spSpeciality.getSelectedItem().toString();

        filteredDoctors.clear();

        for (Doctor d : allDoctors) {

            String name = d.getName() != null
                    ? d.getName().toLowerCase()
                    : "";

            String spec = d.getSpeciality() != null
                    ? d.getSpeciality()
                    : "";

            boolean matchName =
                    keyword.isEmpty() || name.contains(keyword);

            boolean matchSpeciality =
                    speciality.equals("Tất cả") ||
                            spec.equalsIgnoreCase(speciality);

            if (matchName && matchSpeciality) {
                filteredDoctors.add(d);
            }
        }

        adapter.notifyDataSetChanged();
    }

}
