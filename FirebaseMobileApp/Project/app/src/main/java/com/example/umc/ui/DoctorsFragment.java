package com.example.umc.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import android.content.Intent;

import com.example.umc.R;

public class DoctorsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_doctors_fragment, container, false);

        Button btnOpenDoctors = v.findViewById(R.id.btnOpenDoctors);
        Button btnAddDoctor = v.findViewById(R.id.btnAddDoctor);
        Button btnDeleteDoctor = v.findViewById(R.id.btnDeleteDoctor);

        SharedPreferences prefs = getActivity().getSharedPreferences("auth", MODE_PRIVATE);
        String role = prefs.getString("role", "NONE");

        // Show admin tools
        if ("ADMIN".equals(role)) {
            btnAddDoctor.setVisibility(View.VISIBLE);
            btnDeleteDoctor.setVisibility(View.VISIBLE);
        }


        btnOpenDoctors.setOnClickListener(view ->
                startActivity(new Intent(getActivity(), DoctorListActivity.class))
        );
        v.findViewById(R.id.btnFindBySpeciality).setOnClickListener(view ->
                startActivity(new Intent(getActivity(), FindDoctorBySpecialityActivity.class))
        );

        btnAddDoctor.setOnClickListener(view ->
                startActivity(new Intent(getActivity(), AddDoctorActivity.class))
        );

        btnDeleteDoctor.setOnClickListener(view ->
                startActivity(new Intent(getActivity(), DeleteDoctorActivity.class))
        );

        return v;
    }
}
