package com.example.fusion0;

import android.content.Intent;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

/**
 * The MainActivity serves as the main entry point for the app. It manages the login state
 * and directs users to the Profile page, initializing Firebase and LoginManagement to handle user sessions.
 */
public class MainActivity extends AppCompatActivity {

    private TextView textField;
    private LoginManagement loginManagement;
    private Boolean loginState;
    private ImageButton profileButton;
    private ImageButton addButton;

    /**
     * Initializes the MainActivity and manages user session and state.
     * Sets up Firebase, handles login state, and initializes the profile button to access the ProfileActivity.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Initialize Firebase for the app
        FirebaseApp.initializeApp(this);

        // Initialize Notification Channels
        // new Notifications.createChannel()

        // Instantiate login manager and retrieve login state
        loginManagement = new LoginManagement(this);
        loginManagement.isUserLoggedIn(isLoggedIn -> {
            if (isLoggedIn) {
                // Do this
            } else {
                // Do that
            }
        });

        addButton = findViewById(R.id.toolbar_add);

        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EventActivity.class);
            startActivity(intent);
        });
        // Initialize profile button to navigate to ProfileActivity
        profileButton = findViewById(R.id.toolbar_person);

        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}