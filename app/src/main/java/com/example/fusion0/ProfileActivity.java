package com.example.fusion0;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProfileActivity extends AppCompatActivity {

    // Variables for UI components and other classes
    // Text fields and EditFields
    private TextView fullName;
    private TextView emailAddress;
    private TextView phoneNumber;

    private EditText editFullName;
    private EditText editEmailAddress;
    private EditText editPhoneNumber;

    // Image related fields
    private ImageView profileImage;
    private ImageButton backButton;
    private FloatingActionButton editButton;
    private Button saveButton;
    private Button cancelButton;

    // State managers
    private ProfileManagement profileManager;
    private ManageImageProfile manageImage;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // SECTION 1: Initialize Views and Managers
        fullName = findViewById(R.id.fullName);
        emailAddress = findViewById(R.id.emailAddress);
        phoneNumber = findViewById(R.id.phoneNumber);

        editFullName = findViewById(R.id.editFullName);
        editEmailAddress = findViewById(R.id.editEmailAddress);
        editPhoneNumber = findViewById(R.id.editPhoneNumber);

        profileImage = findViewById(R.id.profileImage);
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);


        profileManager = new ProfileManagement();  // Manages user data retrieval
        manageImage = new ManageImageProfile();  // Handles image upload and retrieval from Firebase

        // SECTION 2: Load User Data from Firebase
        profileManager.getUserData(new ProfileManagement.UserDataCallback() {
            @Override
            public void onUserDataReceived(UserInfo user) {
                // Set user details to the corresponding TextViews
                String fullPersonName = user.getFirstName() + " " + user.getLastName();
                fullName.setText(fullPersonName);
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

        // SECTION 3: Check and Load Profile Image
        manageImage.checkImageExists(new ManageImageProfile.ImageCheckCallback() {
            @Override
            public void onImageExists() {
                // If the image exists, retrieve and load it into the profileImage ImageView
                manageImage.getImage(new ManageImageProfile.ImageRetrievedCallback() {
                    @Override
                    public void onImageRetrieved(Uri uri) {
                        Glide.with(ProfileActivity.this)
                                .load(uri)
                                .into(profileImage);  // Glide is used to load the image
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ProfileActivity.this, "Error fetching image", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onImageDoesNotExist() {
                // If no image is found, display a default image
                profileImage.setImageResource(R.drawable.default_profile_image);
            }
        });

        // SECTION 4: Set Back Button Click Behavior
        backButton.setOnClickListener(view -> finish());

        // SECTION 5: Set Profile Image Click Behavior for Uploading New Image
        profileImage.setOnClickListener(view -> selectImage());


        // SECTION 6: Turn Profile Page to Edit Mode
        editButton.setOnClickListener(view -> {
            // Hide the TextViews and show EditText
            fullName.setVisibility(View.GONE);
            emailAddress.setVisibility(View.GONE);
            phoneNumber.setVisibility(View.GONE);

            editFullName.setVisibility(View.VISIBLE);
            editEmailAddress.setVisibility(View.VISIBLE);
            editPhoneNumber.setVisibility(View.VISIBLE);

            saveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);

            saveButton.setOnClickListener(saveView -> {
                // Save changes to profile data
                String newFullName = editFullName.getText().toString();
                String newEmailAddress = editEmailAddress.getText().toString();
                String newPhoneNumber = editPhoneNumber.getText().toString();

                // Update the TextViews with new data
                if (!newFullName.trim().isEmpty()) {
                    fullName.setText(newFullName);
                }

                if (!newEmailAddress.trim().isEmpty()) {
                    emailAddress.setText(newEmailAddress);
                }

                if (!newPhoneNumber.trim().isEmpty()) {
                    phoneNumber.setText(newPhoneNumber);
                }

                // Update in Firebase, will do later

                toggleViewMode();
            });

            cancelButton.setOnClickListener(cancelView -> {
                toggleViewMode();
            });
        });
    }

    private void toggleViewMode() {
        // Hide EditTexts and show TextViews
        fullName.setVisibility(View.VISIBLE);
        emailAddress.setVisibility(View.VISIBLE);
        phoneNumber.setVisibility(View.VISIBLE);

        editFullName.setVisibility(View.GONE);
        editEmailAddress.setVisibility(View.GONE);
        editPhoneNumber.setVisibility(View.GONE);

        saveButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        editButton.setVisibility(View.VISIBLE);

    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);  // Start activity to pick image with requestCode 100
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);  // Display the selected image in profileImage ImageView

            // Upload the selected image to Firebase Storage
            manageImage.uploadImage(imageUri, new ManageImageProfile.ImageUploadCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ProfileActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ProfileActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}