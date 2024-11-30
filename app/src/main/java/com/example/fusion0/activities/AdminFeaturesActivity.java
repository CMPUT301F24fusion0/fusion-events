package com.example.fusion0.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

        // Initialize toolbar buttons
        toolbarHome = findViewById(R.id.toolbar_home_image);
        toolbarQRScanner = findViewById(R.id.toolbar_qrscanner_image);
        toolbarAdd = findViewById(R.id.toolbar_add_image);
        toolbarFavourite = findViewById(R.id.toolbar_favourite_image);
        toolbarPerson = findViewById(R.id.toolbar_person_image);

        // TextViews for toolbar buttons
        TextView homeTextView = findViewById(R.id.homeTextView);
        TextView qrTextView = findViewById(R.id.qrTextView);
        TextView addTextView = findViewById(R.id.addTextView);
        TextView favouriteTextView = findViewById(R.id.searchTextView);
        TextView profileTextView = findViewById(R.id.profileTextView);

        // Define colors
        int selectedColor = ContextCompat.getColor(this, R.color.selectedTabColor);
        int unselectedColor = ContextCompat.getColor(this, R.color.unselectedTabColor);

        // Highlight the Profile tab (set to selected color)
        toolbarPerson.setColorFilter(selectedColor);
        profileTextView.setTextColor(selectedColor);

        // Set other tabs to unselected color
        toolbarHome.setColorFilter(unselectedColor);
        homeTextView.setTextColor(unselectedColor);

        toolbarQRScanner.setColorFilter(unselectedColor);
        qrTextView.setTextColor(unselectedColor);

        toolbarAdd.setColorFilter(unselectedColor);
        addTextView.setTextColor(unselectedColor);

        toolbarFavourite.setColorFilter(unselectedColor);
        favouriteTextView.setTextColor(unselectedColor);

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
