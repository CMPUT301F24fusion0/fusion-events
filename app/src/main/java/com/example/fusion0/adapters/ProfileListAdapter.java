package com.example.fusion0.adapters;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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


    public ProfileListAdapter(@NonNull Context context, @NonNull List<UserInfo> objects) {
        super(context, 0, objects);
        this.context = context;
        this.listUsers = objects;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_view_user, parent, false);
        }

        UserInfo user = getItem(position);

        if (user != null) {
            CircleImageView profilePic = convertView.findViewById(R.id.userProfilePic);
            TextView userName = convertView.findViewById(R.id.userName);
            TextView userEmail = convertView.findViewById(R.id.userEmail);


            String userNameString = user.getFirstName() + " " + user.getLastName();
            userName.setText(userNameString);
            userEmail.setText(user.getEmail());

            manageImage = new ManageImageProfile(ProfileListAdapter.this.getContext());
            manageImage.checkImageExists(new ManageImageProfile.ImageCheckCallback() {
                @Override
                public void onImageExists() {
                    // If the image exists, retrieve and load it into the profileImage ImageView
                    manageImage.getImage(new ManageImageProfile.ImageRetrievedCallback() {
                        @Override
                        public void onImageRetrieved(Uri uri) {
                            Glide.with(ProfileListAdapter.this.getContext())
                                    .load(uri)
                                    .into(profilePic);  // Glide is used to load the image
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(ProfileListAdapter.this.getContext(), "Error fetching image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onImageDoesNotExist() {
                    String firstLetter = user.getFirstName().substring(0, 1).toUpperCase();

                    Drawable deterministicImage = ManageImageProfile.createTextDrawable(ProfileListAdapter.this.getContext(), firstLetter, getContext().getResources().getColor(R.color.textColor), Color.WHITE, 100, 100);

                    profilePic.setImageDrawable(deterministicImage);

                }
            });

        }

        return convertView;
    }
}
