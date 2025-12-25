package com.example.umc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.example.umc.R;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_profile_fragment, container, false);

        v.findViewById(R.id.btnOpenProfile).setOnClickListener(view ->
                startActivity(new Intent(getActivity(), ProfileActivity.class))
        );

        return v;
    }
}
