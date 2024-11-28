package com.example.fusion0.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.fusion0.R;
import com.example.fusion0.fragments.EventFragment;

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

    }
}