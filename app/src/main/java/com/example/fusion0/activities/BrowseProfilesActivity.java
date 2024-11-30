package com.example.fusion0.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fusion0.R;
import com.example.fusion0.adapters.UserArrayAdapter;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.models.UserInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for browsing and managing user profiles.
 * Fetches and displays profiles using UserFirestore and UserArrayAdapter.
 * Includes functionality to delete user profiles.
 *
 * @author Ali Abouei
 */
public class BrowseProfilesActivity extends AppCompatActivity {
    private ListView userListView;
    private UserArrayAdapter userArrayAdapter;
    private Button goBackButton;
    private final UserFirestore userFirestore = new UserFirestore();
    private final ArrayList<UserInfo> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_profiles);

        userListView = findViewById(R.id.userListView);
        goBackButton = findViewById(R.id.goBackButton);

        userArrayAdapter = new UserArrayAdapter(this, users);
        userListView.setAdapter(userArrayAdapter);

        fetchUsers();

        goBackButton.setOnClickListener(v -> finish()); // Return to the previous screen

        // Set up long-click listener for deleting users
        userListView.setOnItemLongClickListener((parent, view, position, id) -> {
            UserInfo selectedUser = users.get(position);
            showDeleteConfirmationDialog(selectedUser, position);
            return true;
        });
    }

    private void fetchUsers() {
        userFirestore.getAllUsers(new UserFirestore.UserListCallback() {
            @Override
            public void onSuccess(ArrayList<UserInfo> fetchedUsers) {
                if (fetchedUsers == null || fetchedUsers.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(BrowseProfilesActivity.this, "No users found.", Toast.LENGTH_SHORT).show());
                    return;
                }

                runOnUiThread(() -> {
                    users.clear(); // Clear the current list
                    users.addAll(fetchedUsers); // Add new data
                    userArrayAdapter.notifyDataSetChanged(); // Update the adapter
                });

                // Log for debugging
                for (UserInfo user : fetchedUsers) {
                    System.out.println("Fetched User: " + user.getFirstName() + " " + user.getLastName());
                }
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(BrowseProfilesActivity.this, "Failed to load users: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }


    private void showDeleteConfirmationDialog(UserInfo user, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", (dialog, which) -> deleteUser(user, position))
                .setNegativeButton("No", null)
                .show();
    }

    public void deleteUser(UserInfo user, int position) {
        userFirestore.deleteUserAndImage(user.getDeviceID(), new UserFirestore.DeleteCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    users.remove(position); // Remove the user from the list
                    userArrayAdapter.notifyDataSetChanged(); // Refresh the ListView
                    Toast.makeText(BrowseProfilesActivity.this, "User and profile picture deleted successfully.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(BrowseProfilesActivity.this, "Failed to delete user or profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

}
