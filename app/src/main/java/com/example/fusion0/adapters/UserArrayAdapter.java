package com.example.fusion0.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fusion0.R;
import com.example.fusion0.activities.BrowseProfilesActivity;
import com.example.fusion0.helpers.ManageImageProfile;
import com.example.fusion0.models.UserInfo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Adapter for displaying user profiles in the BrowseProfilesActivity.
 *
 * @author Ali Abouei
 */
public class UserArrayAdapter extends ArrayAdapter<UserInfo> {
    private final Context context;
    private final List<UserInfo> users;

    public UserArrayAdapter(@NonNull Context context, @NonNull List<UserInfo> users) {
        super(context, R.layout.user_item, users);
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        }

        UserInfo user = users.get(position);

        // UI Elements
        TextView userName = convertView.findViewById(R.id.userName);
        TextView userEmail = convertView.findViewById(R.id.userEmail);
        TextView userPhone = convertView.findViewById(R.id.userPhone);
        ImageView userProfilePicture = convertView.findViewById(R.id.userProfilePicture);
        Button deleteUserButton = convertView.findViewById(R.id.deleteUserButton);
        Button deletePictureButton = convertView.findViewById(R.id.deletePictureButton);

        // Set Text Data
        userName.setText(user.getFirstName() + " " + user.getLastName());
        userEmail.setText(user.getEmail());
        userPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "No phone number");

        // Load Profile Picture
        ManageImageProfile manageImageProfile = new ManageImageProfile(context);
        manageImageProfile.checkImageExists(new ManageImageProfile.ImageCheckCallback() {
            @Override
            public void onImageExists() {
                manageImageProfile.getImage(new ManageImageProfile.ImageRetrievedCallback() {
                    @Override
                    public void onImageRetrieved(Uri uri) {
                        // Load uploaded profile picture from Firebase Storage using Glide
                        Glide.with(context)
                                .load(uri)
                                .into(userProfilePicture);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Log the failure for debugging
                        Toast.makeText(context, "Error loading uploaded image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Fallback to deterministic image
                        loadDeterministicImage(user, userProfilePicture);
                    }
                });
            }

            @Override
            public void onImageDoesNotExist() {
                // Fallback to deterministic image when no uploaded profile exists
                loadDeterministicImage(user, userProfilePicture);
            }
        });

        // Handle "Delete User" button
        deleteUserButton.setOnClickListener(v -> {
            if (context instanceof BrowseProfilesActivity) {
                ((BrowseProfilesActivity) context).deleteUser(user, position);
            }
        });

        // Handle "Delete Picture" button
        deletePictureButton.setOnClickListener(v -> {
            manageImageProfile.deleteImage(new ManageImageProfile.ImageDeleteCallback() {
                @Override
                public void onSuccess() {
                    // Reset to deterministic image after successful deletion
                    loadDeterministicImage(user, userProfilePicture);
                    Toast.makeText(context, "Profile picture deleted successfully.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(context, "Failed to delete profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        return convertView;
    }

    /**
     * Loads a deterministic image based on the user's full name.
     *
     * @param user The user whose profile picture to generate.
     * @param imageView The ImageView to populate.
     */
    private void loadDeterministicImage(UserInfo user, ImageView imageView) {
        String fullName = user.getFirstName() + " " + user.getLastName();
        Drawable deterministicImage = ManageImageProfile.generateArtFromName(context, fullName, 100, 100);
        imageView.setImageDrawable(deterministicImage);
    }
}
