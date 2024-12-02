package com.example.fusion0.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
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
import com.example.fusion0.helpers.Waitlist;
import com.example.fusion0.models.NotificationItem;
import com.example.fusion0.models.UserInfo;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Main fragment contains all the logic for the entire app
 * @author Everyone
 */
public class MainFragment extends Fragment {

    private LoginManagement loginManagement;
    private Boolean loginState;
    private String deviceId;


    private LinearLayout profileButton;
    private LinearLayout addButton;
    private LinearLayout homeButton;
    private LinearLayout scannerButton;
    private LinearLayout favouriteButton;

    private ImageButton profileImageButton;
    private ImageButton addImageButton;
    private ImageButton homeImageButton;
    private ImageButton scannerImageButton;
    private ImageButton favouriteImageButton;


    private TextView homeTextView;
    private TextView scannerTextView;
    private TextView addTextView;
    private TextView searchTextView;
    private TextView profileTextView;


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

    private Waitlist waitlist = new Waitlist();


    /**
     * Required empty public constructor
     */
    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Call the methods required for initially setting up the app
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @SuppressLint("HardwareIds")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        loginManagement = new LoginManagement(requireContext());
        notificationList = new ArrayList<>();
    }

    /**
     * Inflate the view
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    /**
     * Controls the accept/decline seen in the home page and checks if user is logged in or new
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();

        initializeToolbarButtons(view, context);

        FirebaseApp.initializeApp(context);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        Log.d("In", "task.isSuccessful");

                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                Timestamp registrationDeadline = document.getTimestamp("registrationDate");
                                Log.d("query", "snapshot is null");
                                if (registrationDeadline != null && !document.getBoolean("lotteryConducted")) {
                                    Date now = new Date();
                                    Date deadlineDate = registrationDeadline.toDate();
//                                    Calendar calNow = Calendar.getInstance();
//                                    calNow.setTime(now);
//                                    calNow.set(Calendar.HOUR_OF_DAY, 0);
//                                    calNow.set(Calendar.MINUTE, 0);
//                                    calNow.set(Calendar.SECOND, 0);
//                                    calNow.set(Calendar.MILLISECOND, 0);
//
//                                    Calendar calDeadline = Calendar.getInstance();
//                                    calDeadline.setTime(registrationDeadline.toDate());
//                                    calDeadline.set(Calendar.HOUR_OF_DAY, 0);
//                                    calDeadline.set(Calendar.MINUTE, 0);
//                                    calDeadline.set(Calendar.SECOND, 0);
//                                    calDeadline.set(Calendar.MILLISECOND, 0);

                                    Log.d("DateCheck", "calNow: " + now.getTime());
                                    Log.d("DateCheck", "calDeadline: " + deadlineDate.getTime());

                                    if (now.after(deadlineDate)) {
                                        Log.d("calNow", "after the calDeadline");
                                        String eventId = document.getId();
                                        runLottery(eventId, document);
                                        document.getReference().update("lotteryConducted", true);
                                    }
                                } else {
                                    Log.e("FirestoreError", "RegistrationFragment deadline not found for event: " + document.getId());
                                }
                            }
                        } else {
                            Log.e("FirestoreError", "QuerySnapshot is null.");
                        }
                    } else {
                        Log.e("FirestoreError", "Error getting events", task.getException());
                    }
                });


        AppNotifications.createChannel(context);

        notificationsListView = view.findViewById(R.id.notificationsList);
        notificationsListView.setLayoutManager(new LinearLayoutManager(context));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, LinearLayoutManager.VERTICAL);
        notificationsListView.addItemDecoration(dividerItemDecoration);

        // Retrieve login state
        loginManagement.isUserLoggedIn(isLoggedIn -> {
            if (isLoggedIn) {
                AppNotifications.permission(getActivity(), deviceId);
                new UserFirestore().findUser(deviceId, new UserFirestore.Callback() {
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

                notificationAdapter = new NotificationAdapter(context, notificationList, deviceId);
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

                                    AppNotifications.sendNotification(deviceId, removedItem.getTitle(), removedItem.getBody(), removedItem.getFlag(), removedItem.getEventId());
                                }).show();
                    }
                });

                itemTouchHelper.attachToRecyclerView(notificationsListView);

            } else {
                profileButton = view.findViewById(R.id.toolbar_person);
                scannerButton = view.findViewById(R.id.toolbar_qrscanner);
                addButton = view.findViewById(R.id.toolbar_add);
                favouriteButton = view.findViewById(R.id.toolbar_favourite);

                profileButton.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("destination", "profile");
                    Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_registrationFragment, bundle);
                });

                addButton.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("destination", "addEvent");
                    Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_registrationFragment, bundle);
                });

                scannerButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_qrFragment));

                favouriteButton.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("destination", "favourite");
                    Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_registrationFragment, bundle);
                });
            }
        });

    }

    /**
     * @author Sehej Brar
     * Decides whether the permission is granted and then sends them the notification.
     * If they refuse notifications, it will give them an alert dialog which takes them to the app's
     * notification settings in their phone's settings to change it should they choose to.
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
                new AlertDialog.Builder(requireContext())
                        .setTitle("Permission Denied")
                        .setMessage("Notifications is a permission needed for the app to function properly.")
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().getPackageName());
                                requireActivity().startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
            }
        } else {
            Log.d("Wrong", "Code");
        }
    }

    /**
     * Initializes the toolbar and sends them to the correct page if the button is clicked.
     */
    private void initializeToolbarButtons(View view, Context context) {
        homeButton = view.findViewById(R.id.toolbar_home);
        scannerButton = view.findViewById(R.id.toolbar_qrscanner);
        addButton = view.findViewById(R.id.toolbar_add);
        favouriteButton = view.findViewById(R.id.toolbar_favourite);
        profileButton = view.findViewById(R.id.toolbar_person);

        homeImageButton = view.findViewById(R.id.toolbar_home_image);
        scannerImageButton = view.findViewById(R.id.toolbar_qrscanner_image);
        addImageButton = view.findViewById(R.id.toolbar_add_image);
        favouriteImageButton = view.findViewById(R.id.toolbar_favourite_image);
        profileImageButton = view.findViewById(R.id.toolbar_person_image);

        homeTextView = view.findViewById(R.id.homeTextView);
        scannerTextView = view.findViewById(R.id.qrTextView);
        addTextView = view.findViewById(R.id.addTextView);
        searchTextView = view.findViewById(R.id.searchTextView);
        profileTextView = view.findViewById(R.id.profileTextView);

        // Set all buttons
        setAllButtonsInactive(context);
        setActiveButton(context, homeImageButton, homeTextView);

        profileButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_profileFragment));

        scannerButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_qrFragment));

        addButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_eventFragment));

        favouriteButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_favouriteFragment));
    }

    private void setAllButtonsInactive(Context context) {
        profileImageButton.setColorFilter(ContextCompat.getColor(context, R.color.grey));
        scannerImageButton.setColorFilter(ContextCompat.getColor(context, R.color.grey));
        addImageButton.setColorFilter(ContextCompat.getColor(context, R.color.grey));
        favouriteImageButton.setColorFilter(ContextCompat.getColor(context, R.color.grey));

        scannerTextView.setTextColor(ContextCompat.getColor(context, R.color.grey));
        addTextView.setTextColor(ContextCompat.getColor(context, R.color.grey));
        searchTextView.setTextColor(ContextCompat.getColor(context, R.color.grey));
        profileTextView.setTextColor(ContextCompat.getColor(context, R.color.grey));
    }

    private void setActiveButton(Context context, ImageButton activeButton, TextView activeTextView) {
        activeButton.setColorFilter(ContextCompat.getColor(context, R.color.royalBlue));
        activeTextView.setTextColor(ContextCompat.getColor(context, R.color.royalBlue));
    }

    /**
     * A notification pop up
     * @param notificationItem the custom notificationItem that contains the required info
     * @param context context from class
     */
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

    /**
     * Update notifications if their are none
     * @param view the current view
     */
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


    /**
     * Starting the lottery function and send notifications
     * @param eventId event id
     * @param eventDoc the document for the event
     */
    private void runLottery(String eventId, DocumentSnapshot eventDoc) {
        if (eventDoc != null) {
            if (!eventDoc.getString("lotteryCapacity").equals("0")) {
                waitlist.allNotification(eventId, "Lottery Starting",
                        "The lottery is not starting. Be on the look out for the results!", "0");
                waitlist.conductLottery(eventId, Integer.parseInt(eventDoc.getString("lotteryCapacity")), new Waitlist.LotteryCallback() {
                    @Override
                    public void onComplete() {
                    }
                });
                waitlist.conductLottery(eventId, Integer.parseInt(eventDoc.getString("lotteryCapacity")), () -> {});
//                waitlist.loseNotification(eventId, "Lottery Results", "Unfortunately, " +
//                        "you have lost the lottery. You may still receive an invite if someone declines their invitation.", "0");

                waitlist.getChosen(eventId, chosen -> {
                    if (!chosen.isEmpty()) {
                        ChosenEntrantsFragment chosenEntrants = new ChosenEntrantsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("chosenEntrantsData", chosen);
                        bundle.putString("eventID", eventId);
                        bundle.putSerializable("fragment_waitlist", waitlist);
                        chosenEntrants.setArguments(bundle);

                        // Replace fragment to show chosen entrants
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.event_view, chosenEntrants)
                                .addToBackStack(null)
                                .commit();
                    }
                });

            } else {
                Log.d("Lottery", "Lottery capacity is 0, skipping lottery.");
            }
        } else {
            Log.e("Lottery", "Event document is null.");
        }
    }

}
