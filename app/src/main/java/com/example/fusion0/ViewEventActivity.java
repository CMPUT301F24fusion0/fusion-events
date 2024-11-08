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

public class ViewEventActivity extends AppCompatActivity {

    private String deviceID;
    private Boolean isOwner = false;
    private Spinner eventFacility;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView, eventEndDateTextView, eventCapacityTextView, waitinglistFullTextView;
    private EditText eventNameEditText, eventDescriptionEditText, eventCapacityEditText;
    private ImageView eventPosterImageView, qrImageView;
    private ListView entrantsListView, chosenEntrantsListView, cancelledEntrantsListView;
    private Button startDateButton, endDateButton, editButton, deleteButton, joinButton, cancelButton, saveButton;
    private ImageButton backButton;
    private EventInfo event;
    private UserInfo user;
    private LinearLayout toolbar;


    private FusedLocationProviderClient fusedLocationClient;
    private Double longitude = null;
    private Double latitude = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_view);


        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        backButton = findViewById(R.id.backButton);
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

        if (eventID != null) {
            EventFirebase.findEvent(eventID, new EventFirebase.EventCallback() {
                @Override
                public void onSuccess(EventInfo eventInfo) throws WriterException {
                    if (eventInfo == null) {
                        Toast.makeText(ViewEventActivity.this, "Event Unavailable.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
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
                        }else{

                            editButton.setVisibility(View.GONE);
                            deleteButton.setVisibility(View.GONE);
                            cancelButton.setVisibility(View.GONE);
                            saveButton.setVisibility(View.GONE);

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

        joinButton.setOnClickListener(view -> {
            GeoLocation geoLocation = new GeoLocation(this, this, event.getLatitude(), event.getLongitude(), event.getRadius());
            Log.e("ViewEventActivity", "Radius: " + event.getRadius());
            if (!geoLocation.isLocationPermissionGranted()) {
                geoLocation.requestLocationPermission();
            }
            else {
                proceedWithJoin(geoLocation);
            }
        });



    }


    private void proceedWithJoin(GeoLocation geoLocation) {
        Location userLocation = geoLocation.getLocation();
        if (userLocation == null) {
            Toast.makeText(this, "Retrieving your location...", Toast.LENGTH_SHORT).show();
            return;
        }

        //Once the location is retrieved, proceed with registration check
        validateDistanceAndJoin(geoLocation, userLocation);
    }


    private void validateDistanceAndJoin(GeoLocation geoLocation, Location userLocation) {
        //geoLocation.setUserLocation(userLocation.getLatitude(), userLocation.getLongitude());
        if (geoLocation.canRegister()) {
            addUserToWaitingList(userLocation);
        }
        else {
            geoLocation.showMapDialog(); //Optionally show the user a map with the event location and radius
            Toast.makeText(this, "You are outside the acceptable radius to join this event.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserToWaitingList(Location userLocation) {
        ArrayList<EventInfo> eventsList = user.getEvents();
        eventsList.add(event);
        user.setEvents(eventsList);
        UserFirestore.editUserEvents(user);

        ArrayList<String> currentEntrants = event.getWaitinglist();
        String newEntrant = "[" + deviceID + ", " + userLocation.getLatitude() + ", " + userLocation.getLongitude() + "]";
        currentEntrants.add(newEntrant);
        event.setWaitinglist(currentEntrants);
        EventFirebase.editEvent(event); // Assuming editEvent updates the event in Firebase
        Toast.makeText(this, "Joined Waiting List Successfully.", Toast.LENGTH_SHORT).show();
    }
}