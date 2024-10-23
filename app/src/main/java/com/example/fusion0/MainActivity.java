package com.example.fusion0;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private TextView textField;
    private LoginManagement loginManagement;
    private Boolean loginState;
    private Button profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize firebase in app
        FirebaseApp.initializeApp(this);

        loginManagement = new LoginManagement();
        loginState = loginManagement.loggedIn();

        // Managing State
        if (loginState) {
            // Grey out icons
            // Disallow user from using icons
            // Prompt user to upload QR code image
        } else {
            // Give icons colour
            // Allow user to visit other pages
        }

        // Profile Button
        profileButton = findViewById(R.id.profileButton);

        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

    }
}