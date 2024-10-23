package com.example.fusion0;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    // Initialize variables
    private TextView fullName;
    private TextView emailAddress;
    private TextView phoneNumber;
    private ProfileManagement profileManager;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fullName = findViewById(R.id.fullName);
        emailAddress = findViewById(R.id.emailAddress);
        phoneNumber = findViewById(R.id.phoneNumber);
        profileManager = new ProfileManagement();

        profileManager.getUserData(new ProfileManagement.UserDataCallback() {
            @Override
            public void onUserDataReceived(UserInfo user) {
                fullName.setText(user.getFirstName() + " " + user.getLastName());
                emailAddress.setText(user.getEmail());
                phoneNumber.setText(user.getPhoneNumber());
            }

            @Override
            public void onDataNotAvailable() {
                Toast.makeText(ProfileActivity.this, "Data not available", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ProfileActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton backButton = findViewById(R.id.backButton);

        // Set click listener for back button
        backButton.setOnClickListener(view -> {
            finish();
        });
    }
}
