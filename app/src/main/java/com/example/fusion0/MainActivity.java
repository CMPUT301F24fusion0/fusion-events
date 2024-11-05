package com.example.fusion0;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

/**
 * The MainActivity serves as the main entry point for the app. It manages the login state
 * and directs users to the Profile page, initializing Firebase and LoginManagement to handle user sessions.
 */
public class MainActivity extends AppCompatActivity {

    private LoginManagement loginManagement;
    private Boolean loginState;

    private ImageButton profileButton;
    private ImageButton addButton;
    private ImageButton cameraButton;
    private ImageButton favouriteButton;
    private ImageButton homeButton;

    private Button browseEventsButton;
    private Button scanQRButton;


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

        final String deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize Firebase for the app
        FirebaseApp.initializeApp(this);

        // Initialize Notification Channel
        AppNotifications.createChannel(this);

        // Instantiate login manager and retrieve login state
        loginManagement = new LoginManagement(this);
        loginManagement.isUserLoggedIn(isLoggedIn -> {
            if (isLoggedIn) {
                AppNotifications.getNotification(deviceId, this);
            } else {
                // Do that
            }
        });

        initializeToolbarButtons();

        browseEventsButton = findViewById(R.id.browse_events_button);
        browseEventsButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, EventActivity.class);
            startActivity(intent);
        });

        scanQRButton = findViewById(R.id.scan_qr_button);
        scanQRButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, QRActivity.class);
            startActivity(intent);
        });

    }

    private void initializeToolbarButtons() {
        homeButton = findViewById(R.id.toolbar_home);
        cameraButton = findViewById(R.id.toolbar_camera);
        addButton = findViewById(R.id.toolbar_add);
        favouriteButton = findViewById(R.id.toolbar_favourite);
        profileButton = findViewById(R.id.toolbar_person);

        cameraButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, QRActivity.class);
            startActivity(intent);
        });

        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, EventActivity.class);
            startActivity(intent);
        });

        favouriteButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, FavouriteActivity.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
    }

}