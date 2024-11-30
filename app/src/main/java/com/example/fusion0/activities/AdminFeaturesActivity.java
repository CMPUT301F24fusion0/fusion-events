package com.example.fusion0.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fusion0.R;

/**
 * This activity allows admins to browse events, profiles, and facilities.
 */
public class AdminFeaturesActivity extends AppCompatActivity {
    // Declare UI components
    private TextView browseEventsButton, browseProfilesButton, browseFacilitiesButton;
    private ImageButton goBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_features);

        // Initialize TextView buttons
        browseEventsButton = findViewById(R.id.browseEventsButton);
        browseProfilesButton = findViewById(R.id.browseProfilesButton);
        browseFacilitiesButton = findViewById(R.id.browseFacilitiesButton);

        // Initialize back button
        goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(view -> {
            // Handle back navigation
            finish(); // Close the current activity and return to the previous one
        });

        // Set click listeners for TextView buttons
        browseEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminFeaturesActivity.this, BrowseEventsActivity.class);
            startActivity(intent);
        });

        browseProfilesButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminFeaturesActivity.this, BrowseProfilesActivity.class);
            startActivity(intent);
        });

        browseFacilitiesButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminFeaturesActivity.this, AdminFacilityActivity.class);
            startActivity(intent);
        });
    }
}
