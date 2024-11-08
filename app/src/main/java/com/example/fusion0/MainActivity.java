package com.example.fusion0;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;

/**
 * The MainActivity serves as the main entry point for the app. It manages the login state
 * and directs users to the Profile page, initializing Firebase and LoginManagement to handle user sessions.
 */
public class MainActivity extends AppCompatActivity {

    private LoginManagement loginManagement;
    private Boolean loginState;
    private UserFirestore userFirestore;

    private ImageButton profileButton;
    private ImageButton addButton;
    private ImageButton cameraButton;
    private ImageButton favouriteButton;

    // Message related fields
    private TextView userName;
    private TextView welcomeMessage;
    private TextView emptyUserName;
    private TextView emptyWelcomeMessage;

    // Notifications related fields
    private RecyclerView notificationsListView;
    private NotificationAdapter notificationAdapter;
    private List<NotificationItem> notificationList;

    private final int REQUEST_CODE = 100;

    /**
     * Initializes the MainActivity and manages user session and state.
     * Sets up Firebase, handles login state, and initializes the profile button to access the ProfileActivity.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get Device ID
        final String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Log.d("DeviceID", "Device ID: " + deviceId);

        // Initialize Firebase for the app
        FirebaseApp.initializeApp(this);
        System.out.println(deviceId);

        // Initialize Notification Channel
        AppNotifications.createChannel(this);

        notificationsListView = findViewById(R.id.notificationsList);
        notificationsListView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        notificationsListView.addItemDecoration(dividerItemDecoration);

        notificationList = new ArrayList<>();


        // Instantiate login manager and retrieve login state
        loginManagement = new LoginManagement(this);
        loginManagement.isUserLoggedIn(isLoggedIn -> {
            if (isLoggedIn) {
                // they are logged in
                AppNotifications.permission(this, deviceId);
                UserFirestore userFirestore = new UserFirestore();

                userFirestore.findUser(deviceId, new UserFirestore.Callback() {
                    @Override
                    public void onSuccess(UserInfo user) {
                        userName = findViewById(R.id.userName);

                        String firstName = user.firstName;
                        String lastName = user.lastName;
                        String fullName = firstName + " " + lastName;

                        userName.setText(fullName);
                    }

                    @Override
                    public void onFailure(String error) {
                        // Get all fields
                        welcomeMessage = findViewById(R.id.welcomeMessage);
                        userName = findViewById(R.id.userName);
                        emptyUserName = findViewById(R.id.emptyUserName);
                        emptyWelcomeMessage = findViewById(R.id.emptyWelcomeMessage);

                        // Change visibilities
                        welcomeMessage.setVisibility(View.GONE);
                        userName.setVisibility(View.GONE);
                        emptyWelcomeMessage.setVisibility(View.VISIBLE);
                        emptyUserName.setVisibility(View.VISIBLE);

                        emptyWelcomeMessage.setText("Welcome");
                        emptyUserName.setText("New User");

                        Log.d("Firestore", "Did not find user with Id");
                    }
                });

                notificationAdapter = new NotificationAdapter(this, notificationList);
                notificationsListView.setAdapter(notificationAdapter);

                updateNotification(deviceId, notificationList, notificationAdapter);

                // Set up swipe-to-delete functionality
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    @Override
                    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                        // Enable swiping left and right
                        int swipeFlags = ItemTouchHelper.LEFT;
                        return makeMovementFlags(0, swipeFlags);
                    }

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        // Get the position of the swiped item
                        int position = viewHolder.getAdapterPosition();

                        // Remove the item from the list
                        NotificationItem removedItem = notificationList.get(position);
                        notificationList.remove(position);
                        notificationAdapter.notifyItemRemoved(position);
                        updateNotificationView();

                        deleteNotification(removedItem);

                        // Optionally, you can show a Snackbar to undo the delete action
                        Snackbar.make(notificationsListView, "Notification deleted", Snackbar.LENGTH_LONG)
                                .setAction("Undo", v -> {
                                    // Add the item back to the list
                                    notificationList.add(position, removedItem);
                                    notificationAdapter.notifyItemInserted(position);

                                    AppNotifications.sendNotification(deviceId, removedItem.getTitle(), removedItem.getBody(), removedItem.getFlag());
                                }).show();
                    }
                });

                // Attach the ItemTouchHelper to RecyclerView
                itemTouchHelper.attachToRecyclerView(notificationsListView);

            } else {
                // Do that
            }
        });

        initializeToolbarButtons();

    }

    /**
     * @author Sehej Brar
     * Decides whether the permission is granted and then sends them the notification
     * @param requestCode The request code passed in
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        final String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AppNotifications.getNotification(deviceId, this);
            } else {
                // go to phone settings
            }
        } else {
            Log.d("Wrong", "Code");
        }
    }

    /**
     * Initializes the toolbar and sends them to the correct page if the button is clicked.
     */
    private void initializeToolbarButtons() {
        cameraButton = findViewById(R.id.toolbar_camera);
        addButton = findViewById(R.id.toolbar_add);
        favouriteButton = findViewById(R.id.toolbar_favourite);
        profileButton = findViewById(R.id.toolbar_person);

        cameraButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, QRActivity.class);
            startActivity(intent);
        });

        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, EventActivity.class);
            startActivity(intent);
        });

        favouriteButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, FavouriteActivity.class);
            startActivity(intent);
        });

        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
        // Home button in the toolbar
        homeButton = findViewById(R.id.toolbar_home);
        homeButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });




    }

    /**
     * @author Nimi Akinroye
     * Displays a dialog with notification details.
     *
     * @param notificationItem The notification item to display in the dialog.
     */
    public void showNotificationDialog(NotificationItem notificationItem) {
        View dialogView = getLayoutInflater().inflate(R.layout.notifications_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogBody = dialogView.findViewById(R.id.dialogBody);
        Button buttonClose = dialogView.findViewById(R.id.buttonClose);

        dialogTitle.setText(notificationItem.getTitle());
        dialogBody.setText(notificationItem.getBody());

        buttonClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    /**
     * @author Nimi Akinroye
     * Updates the notifications by fetching from Firestore and updating the RecyclerView.
     * @param deviceId, notificationList, notificationAdapter
     */
    private void updateNotification(String deviceId, List<NotificationItem> notificationList, NotificationAdapter notificationAdapter) {
        UserFirestore userFirestore = new UserFirestore();
        userFirestore.findUser(deviceId, new UserFirestore.Callback() {
            @Override
            public void onSuccess(UserInfo user) {
                ArrayList<String> notifications = user.getNotifications();

                if (notifications != null && notifications.size() % 3 == 0) {
                    notificationList.clear();

                    for (int i = 0; i < notifications.size(); i += 3) {
                        String title = notifications.get(i);
                        String body = notifications.get(i + 1);
                        String flag = notifications.get(i + 2); // Assuming flag is stored as a string in Firebase

                        NotificationItem notificationItem = new NotificationItem(title, body, flag);
                        notificationList.add(notificationItem);
                    }

                    notificationAdapter.notifyDataSetChanged();
                    updateNotificationView();
                    Log.d("Firebase", "Notifications fetched successfully from Firebase.");
                } else {
                    Log.d("Firebase", "No notifications found.");
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("Firebase", "Failed to fetch notifications: " + error);
            }
        });
    }

    /**
     * @author Nimi Akinroye
     * Deletes a notification from Firestore.
     * @param notificationItem
     */
    private void deleteNotification(NotificationItem notificationItem) {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        UserFirestore userFirestore = new UserFirestore();

        userFirestore.findUser(deviceId, new UserFirestore.Callback() {
            @Override
            public void onSuccess(UserInfo user) {
                user.editMode(true);

                ArrayList<String> notifications = user.getNotifications();
                int indexToRemove = -1;

                for (int i = 0; i < notifications.size(); i += 3) {
                    String title = notifications.get(i);
                    String body = notifications.get(i + 1);
                    if (title.equals(notificationItem.getTitle()) && body.equals(notificationItem.getBody())) {
                        indexToRemove = i;
                        break;
                    }
                }

                if (indexToRemove != -1) {
                    notifications.remove(indexToRemove); // Remove title
                    notifications.remove(indexToRemove); // Remove body
                    notifications.remove(indexToRemove); // Remove flag
                    user.setNotifications(notifications);
                    updateNotificationView();
                    Log.d("Firebase", "Notification removed successfully from Firebase.");
                } else {
                    Log.d("Firebase", "Notification not found in user's list.");
                }

                user.editMode(false);
            }

            @Override
            public void onFailure(String error) {
                Log.e("Firebase", "Failed to delete notification: " + error);
            }
        });
    }

    /**
     * @author Nimi Akinroye
     * Handles the notifications ListView lifecycle
     */

    private void updateNotificationView() {
        TextView noNotifications = findViewById(R.id.noNotifications);

        if (notificationList.isEmpty()) {
            noNotifications.setVisibility(View.VISIBLE);
            notificationsListView.setVisibility(View.GONE);
        } else {
            noNotifications.setVisibility(View.GONE);
            notificationsListView.setVisibility(View.VISIBLE);
        }
    }

}