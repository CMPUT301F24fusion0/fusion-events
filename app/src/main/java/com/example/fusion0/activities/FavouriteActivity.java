package com.example.fusion0.activities;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.fusion0.fragments.ViewEventFragment;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.FacilitiesInfo;
import com.example.fusion0.models.OrganizerInfo;
import com.example.fusion0.models.UserInfo;
import com.example.fusion0.R;
import com.example.fusion0.fragments.QRFragment;
import com.example.fusion0.fragments.ProfileFragment;
import com.google.firebase.firestore.auth.User;
import com.google.zxing.WriterException;

import java.util.ArrayList;

/**
 * @author Simon Haile
 * This activity allows organizers users to view and select their created events, joined events and
 * facilities.
 */
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




    /**
     * Called when the activity is first created. This method initializes the activity's user
     * interface elements, sets up the button click listeners, and handles the logic for
     * displaying and interacting with different sections of the user's favourites, including
     * joined events, created events, and facilities.
     * The method also interacts with Firebase to retrieve the user's data, events, and facilities,
     * and provides the user with the option to view, create, or manage events and facilities.
     *
     * @param savedInstanceState This Bundle contains the data it most recently supplied in
     *      * onSaveInstanceState(Bundle), otherwise null.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        joinedEventsButton = findViewById(R.id.joined_events_view_button);
        createdEventsButton = findViewById(R.id.created_events_view_button);
        facilitiesButton = findViewById(R.id.facilities_view_button);
        joinedEventsList = findViewById(R.id.joined_events_list);
        createdEventsList = findViewById(R.id.created_events_list);
        facilitiesList = findViewById(R.id.facilities_list);


        joinedEventsButton.setOnClickListener(view -> {
            new UserFirestore().findUser(deviceID, new UserFirestore.Callback() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                if (userInfo == null) {
                    Toast.makeText(FavouriteActivity.this, "User data is not available.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (userInfo.getEvents() == null) {
                    Toast.makeText(FavouriteActivity.this, "No Joined Events Available.", Toast.LENGTH_SHORT).show();
                    joinedEventsList.setVisibility(View.GONE);
                    joinedEventsButton.setText("View");
                } else {
                    user = userInfo;
                    ArrayList<String> eventNames = new ArrayList<>();
                    ArrayAdapter<String> eventsAdapter = new ArrayAdapter<>(FavouriteActivity.this, android.R.layout.simple_list_item_1, eventNames);
                    joinedEventsList.setAdapter(eventsAdapter);
                    if (user.getEvents() != null) {
                        ArrayList<String> filteredEvents = new ArrayList<>();
                        for (String event : user.getEvents()) {
                            EventFirebase.findEvent(event, new EventFirebase.EventCallback() {
                                @Override
                                public void onSuccess(EventInfo eventInfo) throws WriterException {
                                    if (eventInfo != null) {
                                        filteredEvents.add(eventInfo.getEventID());
                                        eventNames.add(eventInfo.getEventName());
                                        eventsAdapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onFailure(String error) {

                                }
                            });
                        }

                        user.setEvents(filteredEvents);
                        joinedEventsList.setOnItemClickListener((parent, view1, position, id) -> {
                            String eventID = user.getEvents().get(position);

                            Intent intent = new Intent(FavouriteActivity.this, ViewEventFragment.class);
                            intent.putExtra("eventID", eventID);
                            intent.putExtra("deviceID", deviceID);
                            startActivity(intent);
                        });
                    }

                    if (isJoinedEventsListVisible) {
                        joinedEventsList.setVisibility(View.GONE);
                        joinedEventsButton.setText("View");
                    } else {
                        joinedEventsList.setVisibility(View.VISIBLE);
                        joinedEventsButton.setText("Hide");
                    }
                    isJoinedEventsListVisible = !isJoinedEventsListVisible;
                }
            }


            @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Error fetching user: " + error);
                }
            });

            joinedEventsList.setOnItemClickListener((parent, view1, position, id) -> {
                String eventID = user.getEvents().get(position);

                Intent intent = new Intent(FavouriteActivity.this, JoinedEventActivity.class);
                intent.putExtra("eventID", eventID);
                intent.putExtra("deviceID", deviceID);
                startActivity(intent);
            });
        });

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
            createdEventsList.setOnItemClickListener((parent, view1, position, id) -> {
                EventInfo event = organizer.getEvents().get(position);
                String eventID = event.getEventID();

                Intent intent = new Intent(FavouriteActivity.this, ViewEventFragment.class);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
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
            facilitiesList.setOnItemClickListener((parent, view1, position, id) -> {
                FacilitiesInfo facility = organizer.getFacilities().get(position);
                String facilityID = facility.getFacilityID();

                Intent intent = new Intent(FavouriteActivity.this, ViewFacilityActivity.class);
                intent.putExtra("facilityID", facilityID);
                startActivity(intent);
            });
        });


        scannerButton = findViewById(R.id.toolbar_qrscanner);
        scannerButton.setOnClickListener(view -> {
            Intent intent = new Intent(FavouriteActivity.this, QRFragment.class);
            startActivity(intent);
        });

        addButton = findViewById(R.id.toolbar_add);

        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(FavouriteActivity.this, EventActivity.class);
            startActivity(intent);
        });

        // Initialize profile button to navigate to ProfileFragment
        profileButton = findViewById(R.id.toolbar_person);

        profileButton.setOnClickListener(view -> {
            ProfileFragment profileFragment = new ProfileFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_favourite, profileFragment)
                    .addToBackStack(null)
                    .commit();

//            Intent intent = new Intent(FavouriteActivity.this, ProfileActivity.class);
//            startActivity(intent);
        });

        homeButton = findViewById(R.id.toolbar_home);

        homeButton.setOnClickListener(view -> {
            Intent intent = new Intent(FavouriteActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
