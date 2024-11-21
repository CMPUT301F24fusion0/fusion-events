package com.example.fusion0.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fusion0.R;
import com.example.fusion0.adapters.NotificationAdapter;
import com.example.fusion0.helpers.AppNotifications;
import com.example.fusion0.helpers.LoginManagement;
import com.example.fusion0.helpers.NotificationHelper;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.models.NotificationItem;
import com.example.fusion0.models.UserInfo;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private LoginManagement loginManagement;
    private Boolean loginState;
    private UserFirestore userFirestore;
    private String deviceId;


    private ImageButton profileButton;
    private ImageButton addButton;
    private ImageButton homeButton;
    private ImageButton scannerButton;
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

    public MainFragment() {
        // Required empty public constructor
    }

    @SuppressLint("HardwareIds")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        loginManagement = new LoginManagement(requireContext());
        userFirestore = new UserFirestore();
        notificationList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();

        FirebaseApp.initializeApp(context);
        AppNotifications.createChannel(context);

        notificationsListView = view.findViewById(R.id.notificationsList);
        notificationsListView.setLayoutManager(new LinearLayoutManager(context));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, LinearLayoutManager.VERTICAL);
        notificationsListView.addItemDecoration(dividerItemDecoration);

        // Retrieve login state
        loginManagement.isUserLoggedIn(isLoggedIn -> {
            if (isLoggedIn) {
                AppNotifications.permission(requireActivity(), deviceId);
                UserFirestore.findUser(deviceId, new UserFirestore.Callback() {
                    @Override
                    public void onSuccess(UserInfo user) {
                        userName = view.findViewById(R.id.userName);

                        String firstName = user.getFirstName();
                        String lastName = user.getLastName();
                        String fullName = firstName + " " + lastName;

                        userName.setText(fullName);
                    }

                    @Override
                    public void onFailure(String error) {
                        // Get all fields
                        welcomeMessage = view.findViewById(R.id.welcomeMessage);
                        userName = view.findViewById(R.id.userName);
                        emptyUserName = view.findViewById(R.id.emptyUserName);
                        emptyWelcomeMessage = view.findViewById(R.id.emptyWelcomeMessage);

                        // Change visibilities
                        welcomeMessage.setVisibility(View.GONE);
                        userName.setVisibility(View.GONE);
                        emptyWelcomeMessage.setVisibility(View.VISIBLE);
                        emptyUserName.setVisibility(View.VISIBLE);

                        String fakeWelcomeMessage = "Welcome";
                        String newUserMessage = "New User";

                        emptyWelcomeMessage.setText(fakeWelcomeMessage);
                        emptyUserName.setText(newUserMessage);
                    }
                });

                notificationAdapter = new NotificationAdapter(context, notificationList);
                notificationsListView.setAdapter(notificationAdapter);

                NotificationHelper.updateNotifications(deviceId, new NotificationHelper.Callback() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onNotificationsUpdated(List<NotificationItem> updatedNotificationList) {
                        notificationList.clear();
                        notificationList.addAll(updatedNotificationList);
                        notificationAdapter.notifyDataSetChanged();
                        updateNotificationView(view);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("NotificationHelper", "Error: " + error);
                    }
                });

                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    @Override
                    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                        int swipeFlags = ItemTouchHelper.LEFT;
                        return makeMovementFlags(0, swipeFlags);
                    }

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        // Get position of the swiped item
                        int position = viewHolder.getAdapterPosition();

                        // Remove the item from the list
                        NotificationItem removedItem = notificationList.get(position);
                        notificationList.remove(position);
                        notificationAdapter.notifyItemRemoved(position);
                        updateNotificationView(view);

                        // Delete the item
                        NotificationHelper.deleteNotification(deviceId, removedItem, new NotificationHelper.Callback() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onNotificationsUpdated(List<NotificationItem> updatedNotificationList) {
                                notificationList.clear();
                                notificationList.addAll(updatedNotificationList); // Update the RecyclerView list
                                notificationAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(String error) {
                                Log.e("NotificationHelper", "Error: " + error);
                            }
                        });

                        Snackbar.make(notificationsListView, "Notification deleted", Snackbar.LENGTH_LONG)
                                .setAction("Undo", v -> {
                                    // Add the item back to the list
                                    notificationList.add(position, removedItem);
                                    notificationAdapter.notifyItemInserted(position);

                                    AppNotifications.sendNotification(deviceId, removedItem.getTitle(), removedItem.getBody(), removedItem.getFlag());
                                }).show();
                    }
                });

                itemTouchHelper.attachToRecyclerView(notificationsListView);
            } else {
                // Do something
            }
        });

        initializeToolbarButtons(view);

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


        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AppNotifications.getNotification(deviceId, requireContext());
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
    private void initializeToolbarButtons(View view) {
        profileButton = view.findViewById(R.id.toolbar_person);
        scannerButton = view.findViewById(R.id.toolbar_qrscanner);
        addButton = view.findViewById(R.id.toolbar_add);
        favouriteButton = view.findViewById(R.id.toolbar_favourite);

        profileButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_profileFragment);
        });

        scannerButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_qrFragment);
        });

        addButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_eventFragment);
        });

        favouriteButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_favouriteFragment);
        });

    }

    public void showNotificationDialog(@NonNull NotificationItem notificationItem, Context context) {
        View dialogView = getLayoutInflater().inflate(R.layout.notifications_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

    private void updateNotificationView(@NonNull View view) {
        TextView noNotifications = view.findViewById(R.id.noNotifications);

        if (notificationList.isEmpty()) {
            noNotifications.setVisibility(View.VISIBLE);
            notificationsListView.setVisibility(View.GONE);
        } else {
            noNotifications.setVisibility(View.GONE);
            notificationsListView.setVisibility(View.VISIBLE);
        }
    }

}