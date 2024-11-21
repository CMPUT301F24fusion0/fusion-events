package com.example.fusion0;

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

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.List;

public class ProfileListAdapter extends ArrayAdapter<UserInfo> {
    private Context context;
    private List<UserInfo> listEntries;

    public ProfileListAdapter(@NonNull Context context, @NonNull List<UserInfo> objects) {
        super(context, 0, objects);
        this.context = context;
        this.listEntries = objects;
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
        }

        return convertView;
    }
}
