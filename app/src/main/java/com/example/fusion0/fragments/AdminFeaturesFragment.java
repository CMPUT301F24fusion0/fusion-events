package com.example.fusion0.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.fusion0.R;

public class AdminFeaturesFragment extends Fragment {

    private TextView browseEventsButton, browseProfilesButton, browseFacilitiesButton;
    private ImageButton goBackButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_admin_features, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        browseEventsButton = view.findViewById(R.id.browseEventsButton);
        browseProfilesButton = view.findViewById(R.id.browseProfilesButton);
        browseFacilitiesButton = view.findViewById(R.id.browseFacilitiesButton);
        goBackButton = view.findViewById(R.id.goBackButton);

        // Back button functionality
        goBackButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        // Set up navigation for buttons
        browseEventsButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_adminFeaturesFragment_to_browseEventsFragment);
        });

        browseProfilesButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_adminFeaturesFragment_to_browseProfilesFragment);
        });

        browseFacilitiesButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_adminFeaturesFragment_to_browseFacilitiesFragment);
        });
    }
}
