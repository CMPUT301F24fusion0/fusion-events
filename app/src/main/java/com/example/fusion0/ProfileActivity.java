package com.example.fusion0;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {

    // Initialize variables
    private TextView fullName;
    private TextView emailAddress;
    private TextView phoneNumber;
    private ProfileManagement profileManager;
    private ImageButton backButton;
    private ImageView profileImage;
    private ManageImageProfile manageImage;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fullName = findViewById(R.id.fullName);
        emailAddress = findViewById(R.id.emailAddress);
        phoneNumber = findViewById(R.id.phoneNumber);
        profileImage = findViewById(R.id.profileImage);
        backButton = findViewById(R.id.backButton);

        profileManager = new ProfileManagement();
        manageImage = new ManageImageProfile();

        profileManager.getUserData(new ProfileManagement.UserDataCallback() {
            @Override
            public void onUserDataReceived(UserInfo user) {
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

        // Check if profile image exists in Firebase
        manageImage.checkImageExists(new ManageImageProfile.ImageCheckCallback() {
            @Override
            public void onImageExists() {
                // If the image exists, retrieve it and display it
                manageImage.getImage(new ManageImageProfile.ImageRetrievedCallback() {
                    @Override
                    public void onImageRetrieved(Uri uri) {
                        Glide.with(ProfileActivity.this)
                                .load(uri)
                                .into(profileImage);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ProfileActivity.this, "Error fetching image", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onImageDoesNotExist() {
                // Set a default image or do nothing
                profileImage.setImageResource(R.drawable.default_profile_image);
            }
        });

        // Set click listener for back button
        backButton.setOnClickListener(view -> finish());

        profileImage.setOnClickListener(view -> selectImage());
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);

            // Upload the selected image to Firebase Storage
            manageImage.uploadImage(imageUri, new ManageImageProfile.ImageUploadCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ProfileActivity.this, "Image uploaded successfully",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ProfileActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



}
