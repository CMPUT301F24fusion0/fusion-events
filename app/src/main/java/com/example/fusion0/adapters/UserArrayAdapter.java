
package com.example.fusion0.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.fusion0.R;
import com.example.fusion0.helpers.ManageImageProfile;
import com.example.fusion0.models.UserInfo;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * Adapter to display users in an admin list view.
 * <p>
 * This adapter is responsible for rendering each user item in the admin's list of users.
 * It fetches profile images directly from Firebase Storage and handles cases where users
 * have not uploaded a profile picture by generating a deterministic image based on their name.
 * </p>
 *
 * @author Ali Abouei
 */
public class UserArrayAdapter extends ArrayAdapter<UserInfo> {
    private final Context context;
    private final List<UserInfo> users;
    private final UserActionListener userActionListener;

    /**
     * Interface for handling user actions such as deletion.
     */
    public interface UserActionListener {
        /**
         * Called when a user is to be deleted.
         *
         * @param user     The {@link UserInfo} object representing the user to delete.
         * @param position The position of the user in the list.
         */
        void onDeleteUser(UserInfo user, int position);

        /**
         * Called when a user's profile picture is to be deleted.
         *
         * @param user     The {@link UserInfo} object representing the user whose picture to delete.
         * @param position The position of the user in the list.
         */
        void onDeletePicture(UserInfo user, int position);
    }

    /**
     * Constructs a new {@code UserArrayAdapter}.
     *
     * @param context  The context of the current state of the application.
     * @param users    The list of {@link UserInfo} objects to display.
     * @param listener A listener for user actions such as deletion.
     */
    public UserArrayAdapter(@NonNull Context context, @NonNull List<UserInfo> users, @NonNull UserActionListener listener) {
        super(context, R.layout.user_item, users);
        this.context = context;
        this.users = users;
        this.userActionListener = listener;
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.).
     *
     * @param position    The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent ViewGroup that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Inflate the view if it doesn't exist
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        }

        // Get the user at the current position
        UserInfo user = users.get(position);

        // UI Elements
        TextView userName = convertView.findViewById(R.id.userName);
        TextView userEmail = convertView.findViewById(R.id.userEmail);
        TextView userPhone = convertView.findViewById(R.id.userPhone);
        ImageView userProfilePicture = convertView.findViewById(R.id.userProfilePicture);

        // Set Text Data
        userName.setText(user.getFirstName() + " " + user.getLastName());
        userEmail.setText(user.getEmail());
        userPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "No phone number");

        // Reset ImageView and Assign Unique Tag
        userProfilePicture.setImageDrawable(null);
        String deviceID = user.getDeviceID();
        userProfilePicture.setTag(deviceID);

        // Load Profile Picture Directly from Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference userImageRef = storageReference.child("profile_images/" + deviceID + ".jpg");

        userImageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // Ensure the ImageView has not been reused for another item
                    if (userProfilePicture.getTag().equals(deviceID)) {
                        // Load the image into the ImageView using Glide
                        Glide.with(context)
                                .load(uri)
                                .placeholder(R.drawable.ic_profile_filled)
                                .into(userProfilePicture);
                    }
                })
                .addOnFailureListener(e -> {
                    // If the image doesn't exist, load a deterministic image
                    if (userProfilePicture.getTag().equals(deviceID)) {
                        loadDeterministicImage(user, userProfilePicture);
                    }
                });

        // Find delete buttons
        Button deleteUserButton = convertView.findViewById(R.id.deleteUserButton);
        Button deletePictureButton = convertView.findViewById(R.id.deletePictureButton);

        // Set up click listeners
        deleteUserButton.setOnClickListener(v -> {
            if (userActionListener != null) {
                userActionListener.onDeleteUser(user, position);
            }
        });

        deletePictureButton.setOnClickListener(v -> {
            if (userActionListener != null) {
                userActionListener.onDeletePicture(user, position);
            }
        });

        return convertView;
    }

    /**
     * Loads a deterministic profile picture for users without an uploaded image.
     * <p>
     * Generates an artistic image based on the user's full name using {@link ManageImageProfile#generateArtFromName}.
     * This ensures that each user has a unique placeholder image.
     * </p>
     *
     * @param user      The {@link UserInfo} object representing the user.
     * @param imageView The {@link ImageView} where the image will be displayed.
     */
    private void loadDeterministicImage(UserInfo user, ImageView imageView) {
        String fullName = user.getFirstName() + " " + user.getLastName();
        Drawable deterministicImage = ManageImageProfile.generateArtFromName(context, fullName, 100, 100);
        imageView.setImageDrawable(deterministicImage);
    }
}
