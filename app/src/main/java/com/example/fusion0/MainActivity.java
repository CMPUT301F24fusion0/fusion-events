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

    private final int REQUEST_CODE = 100;

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

        // Get Device ID
        final String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize Firebase for the app
        FirebaseApp.initializeApp(this);

        // Initialize Notification Channel
        AppNotifications.createChannel(this);

        // Instantiate login manager and retrieve login state
        loginManagement = new LoginManagement(this);
        loginManagement.isUserLoggedIn(isLoggedIn -> {
            if (isLoggedIn) {
                // they are logged in
                AppNotifications.permission(this, deviceId);
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

    /**
     * @author Sehej Brar
     * Decides whether the permission is granted and then sends them the notification
     * @param requestCode The request code passed in
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        final String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AppNotifications.getNotification(deviceId, this);
            } else {
                // go to phone settings
            }
        } else {
            Log.d("Wrong", "Code");
        }
    }

    /**
     * Initializes the toolbar and sends them to the correct page if the button is clicked.
     */
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