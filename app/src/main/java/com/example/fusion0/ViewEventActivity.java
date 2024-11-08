package com.example.fusion0;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


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
    private LinearLayout toolbar;


    private FusedLocationProviderClient fusedLocationClient;
    private Double longitude = null;
    private Double latitude = null;

    private Waitlist waitlist;


    /**
     * Creates the activity for viewing one event
     * @param savedInstanceState saved from last instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_view);

        waitlist = new Waitlist();

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
                        } else {
                            editButton.setVisibility(View.GONE);
                            deleteButton.setVisibility(View.GONE);
                            cancelButton.setVisibility(View.GONE);
                            saveButton.setVisibility(View.GONE);

                            ArrayList<String> currentEntrants = event.getWaitinglist();
                            int capacity = Integer.parseInt(event.getCapacity());

                            if (currentEntrants.size() < capacity) {
                                joinButton.setVisibility(View.VISIBLE);
                            } else {
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
                    if (event.getGeolocation()) {
                        getCurrentLocation();
                    }
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

    /**
     * Gets location of user trying to sign up for event
     */
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            resultLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        });
    }

    /**
     * Asks for permission for location from the user
     */
    private final ActivityResultLauncher<String> resultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            getCurrentLocation();
        }
    });

}