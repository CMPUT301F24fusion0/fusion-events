package com.example.fusion0.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.fusion0.R;
import com.example.fusion0.adapters.UserArrayAdapter;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.models.UserInfo;

import java.util.ArrayList;

public class BrowseProfilesFragment extends Fragment {

    private ListView userListView;
    private UserArrayAdapter userArrayAdapter;
    private final UserFirestore userFirestore = new UserFirestore();
    private final ArrayList<UserInfo> users = new ArrayList<>();
    private Button goBackButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_browse_profiles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userListView = view.findViewById(R.id.userListView);

        userArrayAdapter = new UserArrayAdapter(requireContext(), users, this::showDeleteConfirmationDialog);
        userListView.setAdapter(userArrayAdapter);
        goBackButton = view.findViewById(R.id.goBackButton);

        fetchUsers();

        goBackButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

    }

    private void fetchUsers() {
        userFirestore.getAllUsers(new UserFirestore.UserListCallback() {
            @Override
            public void onSuccess(ArrayList<UserInfo> fetchedUsers) {
                if (fetchedUsers == null || fetchedUsers.isEmpty()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "No users found.", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    users.clear();
                    users.addAll(fetchedUsers);
                    userArrayAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFailure(String error) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to load users: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showDeleteConfirmationDialog(UserInfo user, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", (dialog, which) -> deleteUser(user, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteUser(UserInfo user, int position) {
        userFirestore.deleteUserAndImage(user.getDeviceID(), new UserFirestore.DeleteCallback() {
            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() -> {
                    users.remove(position);
                    userArrayAdapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), "User deleted successfully.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to delete user: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });


    }
}
