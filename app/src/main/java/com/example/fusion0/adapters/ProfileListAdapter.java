package com.example.fusion0.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.fusion0.helpers.ManageImageProfile;
import com.example.fusion0.models.UserInfo;
import com.example.fusion0.R;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.List;
public class ProfileListAdapter extends ArrayAdapter<UserInfo> {
    private Context context;
    private List<UserInfo> listUsers;
    private ManageImageProfile manageImage;
    private SparseBooleanArray selectedItems = new SparseBooleanArray(); // Track selected items
    private boolean isSelectionMode = false; // Selection mode state

    static class ViewHolder {
        CircleImageView profilePic;
        TextView userName;
        TextView userEmail;
    }

    public ProfileListAdapter(@NonNull Context context, @NonNull List<UserInfo> objects) {
        super(context, 0, objects);
        this.context = context;
        this.listUsers = objects;
        this.manageImage = new ManageImageProfile(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_view_user, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.profilePic = convertView.findViewById(R.id.userProfilePic);
            viewHolder.userName = convertView.findViewById(R.id.userName);
            viewHolder.userEmail = convertView.findViewById(R.id.userEmail);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        UserInfo user = getItem(position);
        if (user != null) {
            String userNameString = user.getFirstName() + " " + user.getLastName();
            viewHolder.userName.setText(userNameString);
            viewHolder.userEmail.setText(user.getEmail());

            // Handle item background and click based on selection mode
            if (isSelectionMode) {
                if (selectedItems.get(position, false)) {
                    convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.light_red));
                } else {
                    convertView.setBackgroundColor(Color.TRANSPARENT);
                }

                // Toggle selection on item click
                convertView.setOnClickListener(v -> toggleSelection(position));
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
                convertView.setOnClickListener(null); // Disable item clicks when not in selection mode
            }
        }

        return convertView;
    }

    // Toggle selection state for an item at the given position
    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);  // Deselect if already selected
        } else {
            selectedItems.put(position, true);  // Select the item
        }
        notifyDataSetChanged();  // Notify the adapter to refresh the view
    }

    // Clear all selections
    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    // Set the selection mode (true to enable selection, false to disable)
    public void setSelectionMode(boolean isSelectionMode) {
        this.isSelectionMode = isSelectionMode;
        notifyDataSetChanged();  // Refresh the view
    }

    // Get the list of selected items (useful for removing items, etc.)
    public SparseBooleanArray getSelectedItems() {
        return selectedItems;
    }
}


 /*
            manageImage.checkImageExists(new ManageImageProfile.ImageCheckCallback() {
                @Override
                public void onImageExists() {
                    manageImage.getImage(new ManageImageProfile.ImageRetrievedCallback() {
                        @Override
                        public void onImageRetrieved(Uri uri) {
                            Log.d("ProfileImage", "Image URI: " + uri.toString());

                            Glide.with(ProfileListAdapter.this.getContext())
                                    .load(uri)
                                    .into(viewHolder.profilePic);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(ProfileListAdapter.this.getContext(), "Error fetching image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onImageDoesNotExist() {
                    String fullName = user.getFirstName() + " " + user.getLastName();
                    Drawable image = ManageImageProfile.generateArtFromName(ProfileListAdapter.this.getContext(), fullName, 100, 100);
                    viewHolder.profilePic.setImageDrawable(image);
                }
            });
             */