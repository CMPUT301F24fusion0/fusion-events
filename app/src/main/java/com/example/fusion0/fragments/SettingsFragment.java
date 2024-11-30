package com.example.fusion0.fragments;

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
import androidx.navigation.Navigation;

import com.example.fusion0.R;
import com.example.fusion0.helpers.AppNotifications;

/**
 * The Settings fragment allows users to manage their notification preferences
 * and navigate back to the ProfileFragment.
 *
 * @author Nimi Akinroye
 */
public class SettingsFragment extends Fragment {

    private SwitchCompat switchNotifications;
    private SharedPreferences sharedPreferences;
    private ImageButton backButton;

    /**
     * Inflates the layout for the Settings fragment and initializes components.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The root View of the fragment's layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize the switch and SharedPreferences
        switchNotifications = view.findViewById(R.id.switchNotifications);
        sharedPreferences = requireContext().getSharedPreferences("App Settings", 0);

        // Load the saved switch state
        boolean isNotificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(isNotificationsEnabled);

        // Set up the listener for the switch toggle
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the new state to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();

            // Display a toast message based on the switch state
            if (isChecked) {
                Toast.makeText(getContext(), "Notifications Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Notifications Disabled", Toast.LENGTH_SHORT).show();
            }

            // Update notification settings in the backend
            updateNotificationSettings(isChecked);
        });

        // Initialize the back button and set its click listener
        backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_profileFragment);
        });

        return view;
    }

    /**
     * Updates the notification settings in the backend or server.
     *
     * @param isEnabled Whether notifications are enabled or disabled.
     */
    private void updateNotificationSettings(boolean isEnabled) {
         if (isEnabled) {
             AppNotifications.setNotificationPermission(requireContext(), true);
         } else {
             AppNotifications.setNotificationPermission(requireContext(), false);
         }
    }
}