package com.example.fusion0.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fusion0.R;

/**
 * The MainActivity serves as the main entry point for the app. It manages the login state
 * and directs users to the Profile page, initializing Firebase and LoginManagement to handle user sessions.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Initializes the MainActivity and manages user session and state.
     * Sets up Firebase, handles login state, and initializes the profile button to access the ProfileFragment.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EdgeToEdge.enable(this);

        View rootLayout = findViewById(R.id.rootLayout);

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (view, windowInsetsCompat) -> {
            // Get system bar insets using WindowInsetsCompat
            androidx.core.graphics.Insets systemBarInsets =
                    windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars());

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.graphics.Insets platformInsets = systemBarInsets.toPlatformInsets();

                view.setPadding(
                        platformInsets.left,
                        platformInsets.top,
                        platformInsets.right,
                        0
                );
            } else {
                view.setPadding(
                        systemBarInsets.left,
                        systemBarInsets.top,
                        systemBarInsets.right,
                        0
                );
            }

            return WindowInsetsCompat.CONSUMED;
        });

    }
}