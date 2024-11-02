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

        // Instantiate login manager and retrieve login state
        loginManagement = new LoginManagement();
        loginState = loginManagement.loggedIn();

        // Managing user login state
        if (loginState) {
            // Manage UI for logged-in state (e.g., greyed-out icons, restricted features)
        } else {
            // Manage UI for logged-out state (e.g., enable icons, grant access to other views)
        }

        // Initialize profile button to navigate to ProfileActivity
        profileButton = findViewById(R.id.profile_button);

        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}