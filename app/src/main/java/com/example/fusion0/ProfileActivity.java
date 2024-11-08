package com.example.fusion0;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Manages the user profile page, including displaying and editing user information
 * such as name, email, phone number, and profile image.
 * <p>
 * This activity interacts with Firebase Firestore for user data retrieval and Firebase Storage
 * for managing the profile image. It also provides edit functionality with two view states:
 * view mode (default) and edit mode, where users can update their profile information.
 * </p>
 */
public class ProfileActivity extends AppCompatActivity {

    // Variables for UI components and other classes
    private TextView fullName;
    private TextView emailAddress;
    private TextView phoneNumber;
  
    private EditText editFullName;
    private EditText editEmailAddress;
    private EditText editPhoneNumber;

    // Image related fields
    private CircleImageView profileImage;
    private ImageView editImage;
    private FloatingActionButton editButton;
    private Button saveButton;
    private Button cancelButton;

    // State managers
    private ProfileManagement profileManager;
    private ManageImageProfile manageImage;
    private Uri imageUri;

    // Toolbar buttons
    private ImageButton homeButton;
    private ImageButton cameraButton;
    private ImageButton addButton;
    private ImageButton favouriteButton;
    private ImageButton profileButton;

    /**
     * Initializes the ProfileActivity, setting up view components, loading user data,
     * and managing the profile image. Provides functionality for editing user information.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down.
     */
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
        editImage = findViewById(R.id.editImage);
        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        homeButton = findViewById(R.id.toolbar_home);
        cameraButton = findViewById(R.id.toolbar_camera);
        addButton = findViewById(R.id.toolbar_add);
        favouriteButton = findViewById(R.id.toolbar_favourite);
        profileButton = findViewById(R.id.toolbar_person);

        profileManager = new ProfileManagement();  // Manages user data retrieval
        manageImage = new ManageImageProfile(this);  // Handles image upload and retrieval from Firebase
        final String deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        System.out.println(deviceId);

        // SECTION 2: Load User Data from Firebase
        profileManager.getUserData(deviceId, new ProfileManagement.UserDataCallback() {
            @Override
            public void onUserDataReceived(UserInfo user) {
                String fullPersonName = user.getFirstName() + " " + user.getLastName();
                fullName.setText(fullPersonName);
                emailAddress.setText(user.getEmail());

                if (user.getPhoneNumber() == null) {
                    phoneNumber.setText("No phone number");
                } else {
                    phoneNumber.setText(user.getPhoneNumber());
                }
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
                String firstLetter = fullName.getText().toString().substring(0, 1).toUpperCase();

                Drawable deterministicImage = ManageImageProfile.createTextDrawable(ProfileActivity.this, firstLetter, getResources().getColor(R.color.textColor), Color.WHITE, 100, 100);

                profileImage.setImageDrawable(deterministicImage);

            }
        });

        // SECTION 4: Set Profile Image Click Behavior for Uploading New Image

        editImage.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Profile Image Options");

            String[] options = {"Change Image", "Delete Image"};
            builder.setItems(options, (dialog, which) -> {
                if (which == 0){
                    selectImage();
                } else if (which == 1) {
                    manageImage.deleteImage(new ManageImageProfile.ImageDeleteCallback() {
                        @Override
                        public void onSuccess() {
                            String firstLetter = fullName.getText().toString().substring(0, 1).toUpperCase();
                            Drawable deterministicImage = ManageImageProfile.createTextDrawable(ProfileActivity.this, firstLetter, getResources().getColor(R.color.textColor), Color.WHITE, 100, 100);

                            profileImage.setImageDrawable(deterministicImage);
                            Toast.makeText(ProfileActivity.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.d("FirebaseStorage", e.toString());
                            Toast.makeText(ProfileActivity.this, "Error deleting image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        // SECTION 5: Turn Profile Page to Edit Mode
        editButton.setOnClickListener(view -> {
            // Hide TextViews, show EditTexts for editing, and enable save/cancel buttons
            fullName.setVisibility(View.GONE);
            emailAddress.setVisibility(View.GONE);
            phoneNumber.setVisibility(View.GONE);

            editFullName.setVisibility(View.VISIBLE);
            editEmailAddress.setVisibility(View.VISIBLE);
            editPhoneNumber.setVisibility(View.VISIBLE);

            saveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);

            editImage.setVisibility(View.VISIBLE);

            // Set save button click behavior to update the profile
            saveButton.setOnClickListener(saveView -> {
                String newFullName = editFullName.getText().toString();
                String newEmailAddress = editEmailAddress.getText().toString();
                String newPhoneNumber = editPhoneNumber.getText().toString();

                boolean isUpdated = false;
                UserFirestore userFirestore = new UserFirestore();

                // Assuming we have a UserInfo object to work with
                UserInfo currentUser = new UserInfo();
                currentUser.setDeviceID(deviceId);

                if (!newFullName.trim().isEmpty()) {
                    String[] nameParts = newFullName.split(" ", 2);
                    if (nameParts.length == 2) {
                        userFirestore.editUser(currentUser, "first name", new ArrayList<String>(Collections.singletonList(nameParts[0])));
                        userFirestore.editUser(currentUser, "last name", new ArrayList<String>(Collections.singletonList(nameParts[1])));
                        fullName.setText(newFullName);
                        isUpdated = true;
                    }
                }
                if (!newEmailAddress.trim().isEmpty()) {
                    userFirestore.editUser(currentUser, "email", new ArrayList<String>(Collections.singletonList(newEmailAddress)));
                    emailAddress.setText(newEmailAddress);
                    isUpdated = true;
                }
                if (!newPhoneNumber.trim().isEmpty()) {
                    userFirestore.editUser(currentUser, "phone number", new ArrayList<String>(Collections.singletonList(newPhoneNumber)));
                    phoneNumber.setText(newPhoneNumber);
                    isUpdated = true;
                }

                if (isUpdated) {
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "No changes made to the profile", Toast.LENGTH_SHORT).show();
                }

                toggleViewMode();
            });

            // Set cancel button click behavior to revert to view mode without saving
            cancelButton.setOnClickListener(cancelView -> toggleViewMode());
        });

        homeButton.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
        });

        cameraButton.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, QRActivity.class);
            startActivity(intent);
        });

        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, EventActivity.class);
            startActivity(intent);
        });

        favouriteButton.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, FavouriteActivity.class);
            startActivity(intent);
        });

    }

    /**
     * Toggles between view mode and edit mode by managing the visibility of
     * TextViews and EditTexts, as well as edit controls (save/cancel buttons).
     */

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

        editImage.setVisibility(View.GONE);

    }

    /**
     * Starts an intent to select an image from the device gallery.
     */
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, 100);
    }

    /**
     * Handles the result of the image selection activity. Displays the selected image
     * and uploads it to Firebase Storage.
     *
     * @param requestCode The request code for the activity result.
     * @param resultCode The result code indicating success or failure.
     * @param data The data returned from the activity, including the image URI.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);

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