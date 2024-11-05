package com.example.fusion0;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class HomeActivity extends AppCompatActivity {

    private ImageButton homeButton;
    private ImageButton profileButton;
    private ImageButton addButton;
    private ImageButton scannerButton;
    private ImageButton favouriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Use your home layout file

        // Initialize Firebase for the app
        FirebaseApp.initializeApp(this);

        // Initialize Notification Channel
        AppNotifications.createChannel(this);

        // Initialize toolbar buttons
        initializeToolbarButtons();

        // Set up the "Browse Events" button
        Button browseEventsButton = findViewById(R.id.browse_events_button);
        browseEventsButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, EventActivity.class);
            startActivity(intent);
        });

        // Set up the "Scan QR Code OR ANYTHING ELSE" button
        ImageButton scanQRButton = findViewById(R.id.toolbar_camera);
        scanQRButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, QRActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Update the intent
        recreate(); // Force the activity to reload
    }

    private void initializeToolbarButtons() {
        // Initialize the toolbar buttons and set up click listeners to navigate to other activities
        homeButton = findViewById(R.id.toolbar_home);
        profileButton = findViewById(R.id.toolbar_person);
        addButton = findViewById(R.id.toolbar_add);
        scannerButton = findViewById(R.id.toolbar_camera);
        favouriteButton = findViewById(R.id.toolbar_favourite);

        homeButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, EventActivity.class);
            startActivity(intent);
        });

        scannerButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, QRActivity.class);
            startActivity(intent);
        });

        favouriteButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, FavouriteActivity.class);
            startActivity(intent);
        });
    }
}
