package com.example.fusion0;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.zxing.WriterException;

import java.util.ArrayList;

/**
 * @author Sehej Brar, Simon Haile, and Malshaan K.
 * View the individual event.
 */
public class ViewEventActivity extends AppCompatActivity {

    private String deviceID;
    private Boolean isOwner = false;
    private Spinner eventFacility;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView, eventEndDateTextView, eventCapacityTextView, waitinglistFullTextView;
    private EditText eventNameEditText, eventDescriptionEditText, eventCapacityEditText;
    private ImageView eventPosterImageView, qrImageView;
    private ListView entrantsListView, chosenEntrantsListView, cancelledEntrantsListView;
    private Button startDateButton, endDateButton, editButton, deleteButton, joinButton, cancelButton, saveButton, lotteryButton;
    private ImageButton backButton;
    private EventInfo event;
    private UserInfo user;
    private LinearLayout toolbar;

    Waitlist waitlist;

    private FusedLocationProviderClient fusedLocationClient;
    private Double longitude = null;
    private Double latitude = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_view);

        waitlist = new Waitlist();

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        backButton = findViewById(R.id.backButton);
        lotteryButton = findViewById(R.id.lottery_button);
        eventNameTextView = findViewById(R.id.EventName);
        eventDescriptionTextView = findViewById(R.id.Description);
        eventFacility= findViewById(R.id.spinner_facilities);
        eventStartDateTextView = findViewById(R.id.start_date_text);
        eventEndDateTextView = findViewById(R.id.end_date_text);
        eventCapacityTextView = findViewById(R.id.editCapacity);
        eventNameEditText = findViewById(R.id.editEventName);
        eventDescriptionEditText = findViewById(R.id.Description);

        eventPosterImageView = findViewById(R.id.uploaded_image_view);
        qrImageView = findViewById(R.id.qrImage);
        entrantsListView = findViewById(R.id.waitinglistListView);
        chosenEntrantsListView = findViewById(R.id.chosenEntrantsListView);
        cancelledEntrantsListView = findViewById(R.id.cancelledEntrantsListView);
        startDateButton = findViewById(R.id.start_date_button);
        endDateButton = findViewById(R.id.end_date_button);
        editButton = findViewById(R.id.edit_button);
        deleteButton = findViewById(R.id.delete_button);

        joinButton = findViewById(R.id.join_button);
        cancelButton = findViewById(R.id.cancel_button);
        saveButton = findViewById(R.id.save_button);
        waitinglistFullTextView = findViewById(R.id.waitinglist_full_text_view);

        toolbar = findViewById(R.id.toolbar);


        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(ViewEventActivity.this, FavouriteActivity.class);
            startActivity(intent);
        });

        Intent intentReceived = getIntent();
        String eventID = intentReceived.getStringExtra("eventID");
/*
        UserFirestore.findUser(deviceID,new UserFirestore.Callback(){
            @Override
            public void onSuccess(UserInfo userInfo) {
                user = userInfo;
            }

            @Override
            public void onFailure(String error) {
                Log.e("ViewEventActivity", "Error fetching user: " + error);
            }
        });
*/
        if (eventID != null) {
            EventFirebase.findEvent(eventID, new EventFirebase.EventCallback() {
                @Override
                public void onSuccess(EventInfo eventInfo) throws WriterException {
                    if (eventInfo == null) {
                        Toast.makeText(ViewEventActivity.this, "Event Unavailable.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {

                        event = eventInfo;

                        eventNameTextView.setText(event.getEventName());
                        eventDescriptionTextView.setText(event.getDescription());
                        eventCapacityTextView.setText(String.valueOf(event.getCapacity()));


                        String eventPoster = event.getEventPoster();
                        if (eventPoster != null && !eventPoster.isEmpty()) {
                            Glide.with(ViewEventActivity.this)
                                    .load(eventPoster)
                                    .into(eventPosterImageView);
                            eventPosterImageView.setVisibility(View.VISIBLE);
                        }

                        String qrcode = event.getQrCode();
                        if (qrcode != null && !qrcode.isEmpty()) {
                            Bitmap qrBitmap = event.generateQRCodeImage(500, 500, qrcode);
                            qrImageView.setImageBitmap(qrBitmap);
                        }

                        toolbar.setVisibility(View.VISIBLE);

                        if (deviceID.equals(event.getOrganizer())) {
                            isOwner = true;
                            lotteryButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    int capacity = 1;

                                    try {
                                        capacity = Integer.parseInt(event.getCapacity());
                                    } catch (NumberFormatException e) {
                                        Log.e("Error", "Capacity is not an integer");
                                    }
                                    waitlist.sampleAttendees(eventID, capacity);

                                    waitlist.allNotification(eventID, "Lottery",
                                            "The lottery has started. Please be on the lookout for results.", "0");

                                    waitlist.chosenNotification(eventID, "Congratulations",
                                            "You've won the lottery! Please login and accept the invitation", "1");

                                    waitlist.cancelNotifications(eventID, "Cancel Confirmation", "You have cancelled" +
                                            "the invitation for the event.", "0");

                                    waitlist.loseNotification(eventID, "Lottery Lose", "Unfortunately, you've lost the lottery." +
                                            "Better luck next time!", "0");
                                }
                            });
                        } else {

                            editButton.setVisibility(View.GONE);
                            deleteButton.setVisibility(View.GONE);
                            cancelButton.setVisibility(View.GONE);
                            saveButton.setVisibility(View.GONE);
                            lotteryButton.setVisibility(View.GONE);

                            ArrayList<String> currentEntrants = event.getWaitinglist();
                            int capacity = Integer.parseInt(event.getCapacity());
                            if (currentEntrants.size() < capacity) {
                                joinButton.setVisibility(View.VISIBLE);
                            }else{
                                waitinglistFullTextView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e("ViewEventActivity", "Error fetching event: " + error);
                    Toast.makeText(ViewEventActivity.this, "Failed to load event details.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ViewEventActivity.this, "Invalid Event ID.", Toast.LENGTH_SHORT).show();
            finish();
        }

        editButton.setOnClickListener(v -> {

        });

        deleteButton.setOnClickListener(v -> {
        });

        waitlist.getAll(eventID, all -> {
            if (!all.contains(deviceID)) {
                joinButton.setOnClickListener(view -> {
                    GeoLocation geoLocation = new GeoLocation(this, this, event.getLatitude(), event.getLongitude(), event.getRadius());
                    Log.e("ViewEventActivity", "Radius: " + event.getRadius());
                    if (!geoLocation.isLocationPermissionGranted()) {
                        geoLocation.requestLocationPermission();
                    }
                    else {
                        proceedWithJoin(geoLocation, true, eventID);
                    }
                });
            } else {
                joinButton.setText(R.string.unjoin);
                joinButton.setOnClickListener(view -> {
                    waitlist.removeEntrantFromWaitingList(eventID, deviceID);
                    Toast.makeText(ViewEventActivity.this, "You have left the waiting list", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ViewEventActivity.this, MainActivity.class);
                    startActivity(intent);
                });
            }
        });

    }


    private void proceedWithJoin(GeoLocation geoLocation, boolean geoEnabled, String eventID) {
        if (geoEnabled) {
            // Only retrieve location if geolocation check is enabled
            Location userLocation = geoLocation.getLocation();
            if (userLocation == null) {
                Toast.makeText(this, "Retrieving your location...", Toast.LENGTH_SHORT).show();
                return;
            }
            // Once location is retrieved, validate distance to event
            validateDistanceAndJoin(geoLocation, userLocation, eventID);
        } else {
            // No geolocation needed; directly add user to the waiting list
            addUserToWaitingList(null, eventID); // Pass null for userLocation since it's not required
        }
    }


    private void validateDistanceAndJoin(GeoLocation geoLocation, Location userLocation, String eventId) {
        geoLocation.setUserLocation(userLocation.getLatitude(), userLocation.getLongitude());
        if (geoLocation.canRegister()) {
            addUserToWaitingList(userLocation, eventId); // Geolocation is enabled, and user is within radius
        } else {
            geoLocation.showMapDialog(); // Optionally show map dialog if outside acceptable radius
            Toast.makeText(this, "You are outside the acceptable radius to join this event.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserToWaitingList(Location userLocation, String eventID) {
        /*ArrayList<EventInfo> eventsList = user.getEvents();
        eventsList.add(event);
        user.setEvents(eventsList);
        UserFirestore.editUserEvents(user);*/
        LoginManagement login = new LoginManagement(this);
        login.isUserLoggedIn(isLoggedIn -> {
            if (isLoggedIn) {
                Toast.makeText(ViewEventActivity.this, "Joined Waiting List Successfully.", Toast.LENGTH_SHORT).show();
                ArrayList<String> currentEntrants = event.getWaitinglist();
                String newEntrant = "[" + deviceID + ", " + latitude + ", " + longitude + "]";
                currentEntrants.add(newEntrant);
                event.setWaitinglist(currentEntrants);
                waitlist.addEntrantToWaitingList(eventID, deviceID);
                EventFirebase.editEvent(event);
                Intent intent = new Intent(ViewEventActivity.this, MainActivity.class);
                startActivity(intent);

            } else {
                Registration registration = new Registration();
                Bundle bundle = new Bundle();
                bundle.putString("eventID", eventID);
                registration.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.event_view, registration)
                        .addToBackStack(null)
                        .commit();
            }
        });
//        ArrayList<String> currentEntrants = event.getWaitinglist();
//        String newEntrant = "[" + deviceID + ", " + userLocation.getLatitude() + ", " + userLocation.getLongitude() + "]";
//        currentEntrants.add(newEntrant);
//        event.setWaitinglist(currentEntrants);
//        EventFirebase.editEvent(event); // Assuming editEvent updates the event in Firebase
//        Toast.makeText(this, "Joined Waiting List Successfully.", Toast.LENGTH_SHORT).show();
    }
}