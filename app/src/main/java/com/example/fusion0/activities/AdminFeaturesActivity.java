package com.example.fusion0.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.fusion0.R;
import com.example.fusion0.fragments.ProfileFragment;
import com.example.fusion0.fragments.QRFragment;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author Malshaan Kodithuwakku
 * This activity allows admins to browse events, profiles, and facilities.
 */
public class AdminFeaturesActivity extends AppCompatActivity{
    // Declare TextView buttons
    private TextView browseEventsButton, browseProfilesButton, browseFacilitiesButton;
    private ImageButton profileButton, addButton, scannerButton, homeButton; ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_features);

        // Initialize UI components
        browseEventsButton = findViewById(R.id.browseEventsButton);
        browseProfilesButton = findViewById(R.id.browseProfilesButton);
        browseFacilitiesButton = findViewById(R.id.browseFacilitiesButton);

        // Set click listeners for each button
        browseEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Browse Events screen or handle action
                //Toast.makeText(AdminFeaturesActivity.this, "Browse Events clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AdminFeaturesActivity.this, BrowseEventsActivity.class);
                startActivity(intent); // Start the new activity
            }
        });

        browseProfilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Browse Profiles screen
                Intent intent = new Intent(AdminFeaturesActivity.this, BrowseProfilesActivity.class);
                startActivity(intent);
            }
        });

        browseFacilitiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Browse Images screen or handle action
                Toast.makeText(AdminFeaturesActivity.this, "Browse Facilities clicked", Toast.LENGTH_SHORT).show();
                // Example: Start a new activity
                Intent intent = new Intent(AdminFeaturesActivity.this, AdminFacilityActivity.class);
                startActivity(intent);
            }
        });

        scannerButton = findViewById(R.id.toolbar_camera);
        scannerButton.setOnClickListener(view -> {
            Intent intent = new Intent(AdminFeaturesActivity.this, QRFragment.class);
            startActivity(intent);
        });

        addButton = findViewById(R.id.toolbar_add);

        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(AdminFeaturesActivity.this, EventActivity.class);
            startActivity(intent);
        });

        // Initialize profile button to navigate to ProfileFragment
        profileButton = findViewById(R.id.toolbar_person);

        profileButton.setOnClickListener(view -> {
            ProfileFragment profileFragment = new ProfileFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_favourite, profileFragment)
                    .addToBackStack(null)
                    .commit();

            Intent intent = new Intent(AdminFeaturesActivity.this, ProfileFragment.class);
            startActivity(intent);
        });

        homeButton = findViewById(R.id.toolbar_home);

        homeButton.setOnClickListener(view -> {
            Intent intent = new Intent(AdminFeaturesActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

}
