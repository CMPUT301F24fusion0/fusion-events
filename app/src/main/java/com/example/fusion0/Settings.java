package com.example.fusion0;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;


public class Settings extends Fragment {

    private SwitchCompat switchNotfications;
    private SharedPreferences sharedPreferences;
    private ImageButton backButton;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize the switch and SharedPreferences
        switchNotfications = view.findViewById(R.id.switchNotifications);
        sharedPreferences = requireContext().getSharedPreferences("App Settings", 0);

        // Load the saved switch state
        boolean isNotificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        switchNotfications.setChecked(isNotificationsEnabled);

        // Set up the listener for the switch toggle
        switchNotfications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the new state to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();

            // Display a toast message
            if (isChecked) {
                Toast.makeText(getContext(), "Notifications Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Notifications Disabled", Toast.LENGTH_SHORT).show();
            }

            updateNotificationSettings(isChecked);
        });

        backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Start the ProfileActivity
            Intent intent = new Intent(requireContext(), ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }

    /**
     * Method to handle backend changes for notification settings.
     *
     * @param isEnabled Whether notifications are enabled or disabled.
     */
    private void updateNotificationSettings(boolean isEnabled) {
        // Here you can implement additional logic, such as updating the server.
        // For example:
        // UserFirestore userFirestore = new UserFirestore();
        // userFirestore.updateNotificationPreference(isEnabled);
//
//        if (isEnabled) {
//            AppNotifications.setPermission(context, true);
//        } else {
//            AppNotifications.setPermission(context, false);
//        }
    }

}