package com.example.fusion0.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fusion0.R;
import com.example.fusion0.fragments.ProfileFragment;
import com.example.fusion0.fragments.QRFragment;

/**
 * @author
 * This activity allows admins to browse events, profiles, and facilities.
 */
public class AdminFeaturesActivity extends AppCompatActivity {
    // Declare UI components
    private TextView browseEventsButton, browseProfilesButton, browseFacilitiesButton;
    private ImageButton toolbarHome, toolbarAdd, toolbarQRScanner, toolbarFavourite, toolbarPerson, goBackButton;

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

        browseFacilitiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Browse Images screen or handle action
                //Toast.makeText(AdminFeaturesActivity.this, "Browse Facilities clicked", Toast.LENGTH_SHORT).show();
                // Example: Start a new activity
                Intent intent = new Intent(AdminFeaturesActivity.this, AdminFacilityActivity.class);
                startActivity(intent);
            }});

        browseProfilesButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminFeaturesActivity.this, BrowseProfilesActivity.class);
            startActivity(intent);
        });

        browseFacilitiesButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminFeaturesActivity.this, AdminFacilityActivity.class);
            startActivity(intent);
        });

        // Initialize toolbar buttons
        toolbarHome = findViewById(R.id.toolbar_home_image);
        toolbarQRScanner = findViewById(R.id.toolbar_qrscanner_image);
        toolbarAdd = findViewById(R.id.toolbar_add_image);
        toolbarFavourite = findViewById(R.id.toolbar_favourite_image);
        toolbarPerson = findViewById(R.id.toolbar_person_image);

        // Set click listeners for toolbar buttons
        toolbarHome.setOnClickListener(view -> {
            Intent intent = new Intent(AdminFeaturesActivity.this, MainActivity.class);
            startActivity(intent);
        });

        toolbarQRScanner.setOnClickListener(view -> {
            // Navigate to QR scanner screen
            Intent intent = new Intent(AdminFeaturesActivity.this, QRFragment.class);
            startActivity(intent);
        });

        toolbarAdd.setOnClickListener(view -> {
            // Navigate to Add Event screen
            Intent intent = new Intent(AdminFeaturesActivity.this, EventActivity.class);
            startActivity(intent);
        });

        toolbarFavourite.setOnClickListener(view -> {
            Toast.makeText(this, "Activities feature not yet implemented", Toast.LENGTH_SHORT).show();
        });

        toolbarPerson.setOnClickListener(view -> {
            // Replace with ProfileFragment
            ProfileFragment profileFragment = new ProfileFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.adminFeaturesContainer, profileFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

}
