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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.fusion0.R;
import com.example.fusion0.helpers.ManageImageProfile;
import com.example.fusion0.models.UserInfo;

import java.util.List;

public class UserArrayAdapter extends ArrayAdapter<UserInfo> {
    private final Context context;
    private final List<UserInfo> users;
    private final UserActionListener userActionListener;

    public interface UserActionListener {
        void onDeleteUser(UserInfo user, int position);
    }

    public UserArrayAdapter(@NonNull Context context, @NonNull List<UserInfo> users, @NonNull UserActionListener listener) {
        super(context, R.layout.user_item, users);
        this.context = context;
        this.users = users;
        this.userActionListener = listener;
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
                        Glide.with(context).load(uri).into(userProfilePicture);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        loadDeterministicImage(user, userProfilePicture);
                    }
                });
            }

            @Override
            public void onImageDoesNotExist() {
                loadDeterministicImage(user, userProfilePicture);
            }
        });

        // Handle "Delete User" button
        deleteUserButton.setOnClickListener(v -> userActionListener.onDeleteUser(user, position));

        // Handle "Delete Picture" button
        deletePictureButton.setOnClickListener(v -> {
            manageImageProfile.deleteImage(new ManageImageProfile.ImageDeleteCallback() {
                @Override
                public void onSuccess() {
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

    private void loadDeterministicImage(UserInfo user, ImageView imageView) {
        String fullName = user.getFirstName() + " " + user.getLastName();
        Drawable deterministicImage = ManageImageProfile.generateArtFromName(context, fullName, 100, 100);
        imageView.setImageDrawable(deterministicImage);
    }
}
