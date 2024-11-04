package com.example.fusion0;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class FavouriteActivity extends AppCompatActivity {
    private static final String TAG = "FavouriteActivity";
    private Button joinedEventsButton;
    private Button createdEventsButton;
    private Button facilitiesButton;
    private String deviceID;
    private OrganizerInfo organizer;
    private UserInfo user;
    private ListView joinedEventsList;
    private ListView createdEventsList;
    private ListView facilitiesList;
    private boolean isFacilitiesListVisible = false;
    private boolean isCreatedEventsListVisible = false;
    private boolean isJoinedEventsListVisible = false;

    private ImageButton profileButton;
    private ImageButton addButton;
    private ImageButton scannerButton;
    private ImageButton homeButton;





    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "Device ID: " + deviceID); // Log the device ID


        joinedEventsButton = findViewById(R.id.joined_events_view_button);
        createdEventsButton = findViewById(R.id.created_events_view_button);
        facilitiesButton = findViewById(R.id.facilities_view_button);
        joinedEventsList = findViewById(R.id.joined_events_list);
        createdEventsList = findViewById(R.id.created_events_list);
        facilitiesList = findViewById(R.id.facilities_list);

/*
        joinedEventsButton.setOnClickListener(view -> {UserFirestore.findUser(deviceID, new UserFirestore.Callback() {
                @Override
                public void onSuccess(UserInfo userInfo) {
                    if (userInfo == null) {
                        Toast.makeText(FavouriteActivity.this, "No Joined Events Available.", Toast.LENGTH_SHORT).show();
                        joinedEventsList.setVisibility(View.GONE);
                        joinedEventsButton.setText("View");
                    } else {
                        user = userInfo;

                        if (joinedEventsList.getAdapter() == null) {
                            ArrayAdapter<String> facilitiesAdapter = new ArrayAdapter<>(FavouriteActivity.this, android.R.layout.simple_list_item_1, user.getEventsNames());
                            joinedEventsList.setAdapter(facilitiesAdapter);
                        }

                        // Toggle visibility and button text
                        if (isCreatedEventsListVisible) {
                            joinedEventsList.setVisibility(View.GONE);
                            joinedEventsButton.setText("View");
                        } else {
                            joinedEventsList.setVisibility(View.VISIBLE);
                            joinedEventsButton.setText("Hide");
                        }
                        isCreatedEventsListVisible = !isCreatedEventsListVisible;
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Error fetching user: " + error);
                }
            });
        });
        */
        createdEventsButton.setOnClickListener(view -> {EventFirebase.findOrganizer(deviceID, new EventFirebase.OrganizerCallback() {
                @Override
                public void onSuccess(OrganizerInfo organizerInfo) {
                    if (organizerInfo == null) {
                        Toast.makeText(FavouriteActivity.this, "No Created Events Available.", Toast.LENGTH_SHORT).show();
                        createdEventsList.setVisibility(View.GONE);
                        createdEventsButton.setText("View");
                    } else {
                        organizer = organizerInfo;

                        if (createdEventsList.getAdapter() == null) {
                            ArrayAdapter<String> facilitiesAdapter = new ArrayAdapter<>(FavouriteActivity.this, android.R.layout.simple_list_item_1, organizer.getEventsNames());
                            createdEventsList.setAdapter(facilitiesAdapter);
                        }

                        // Toggle visibility and button text
                        if (isCreatedEventsListVisible) {
                            createdEventsList.setVisibility(View.GONE);
                            createdEventsButton.setText("View");
                        } else {
                            createdEventsList.setVisibility(View.VISIBLE);
                            createdEventsButton.setText("Hide");
                        }
                        isCreatedEventsListVisible = !isCreatedEventsListVisible;
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Error fetching organizer: " + error);
                }
            });
        });

        facilitiesButton.setOnClickListener(view -> {
            EventFirebase.findOrganizer(deviceID, new EventFirebase.OrganizerCallback() {
                @Override
                public void onSuccess(OrganizerInfo organizerInfo) {
                    if (organizerInfo == null) {
                        Toast.makeText(FavouriteActivity.this, "No facilities available.", Toast.LENGTH_SHORT).show();
                        facilitiesList.setVisibility(View.GONE);
                    } else {
                        organizer = organizerInfo;

                        if (facilitiesList.getAdapter() == null) {
                            if(organizer.getFacilitiesNames() != null) {
                                ArrayAdapter<String> facilitiesAdapter = new ArrayAdapter<>(FavouriteActivity.this, android.R.layout.simple_list_item_1, organizer.getFacilitiesNames());
                                facilitiesList.setAdapter(facilitiesAdapter);
                            }else{
                                Toast.makeText(FavouriteActivity.this, "No facilities available.", Toast.LENGTH_SHORT).show();
                                facilitiesButton.setEnabled(false);
                                facilitiesList.setVisibility(View.GONE);
                                return;
                            }
                        }
                        if (isFacilitiesListVisible) {
                            facilitiesList.setVisibility(View.GONE);
                            facilitiesButton.setText("View");
                        } else {
                            facilitiesList.setVisibility(View.VISIBLE);
                            facilitiesButton.setText("Hide");
                        }

                        isFacilitiesListVisible = !isFacilitiesListVisible; // Toggle the state
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Error fetching organizer: " + error);
                }
            });
        });


        scannerButton = findViewById(R.id.toolbar_qrscanner);
        scannerButton.setOnClickListener(view -> {
            Intent intent = new Intent(FavouriteActivity.this, QRActivity.class);
            startActivity(intent);
        });

        addButton = findViewById(R.id.toolbar_add);

        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(FavouriteActivity.this, EventActivity.class);
            startActivity(intent);
        });

        // Initialize profile button to navigate to ProfileActivity
        profileButton = findViewById(R.id.toolbar_person);

        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(FavouriteActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        homeButton = findViewById(R.id.toolbar_home);

        homeButton.setOnClickListener(view -> {
            Intent intent = new Intent(FavouriteActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
