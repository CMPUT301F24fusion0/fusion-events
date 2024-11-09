package com.example.fusion0;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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


import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ViewEventActivity extends AppCompatActivity {

    private String deviceID;
    private Boolean isOwner = false;
    private Spinner eventFacilitySpinner;
    private TextView eventNameTextView, eventDescriptionTextView,eventFacilityTextView, eventStartDateTextView, eventEndDateTextView,eventStartTimeTextView, eventEndTimeTextView, eventCapacityTextView, waitinglistFullTextView;
    private EditText eventNameEditText, eventDescriptionEditText, eventCapacityEditText;
    private ImageView eventPosterImageView, qrImageView;
    private ListView waitinglistListView, chosenEntrantsListView, cancelledEntrantsListView;
    private Button startDateButton, endDateButton, editButton, deleteButton, joinButton, cancelButton, saveButton, waitinglistButton, cancelledEntrantsButton, chosenEntrantsButton, uploadImageButton;
    private ImageButton backButton;
    private EventInfo event;
    private UserInfo user;
    private LinearLayout toolbar;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private FusedLocationProviderClient fusedLocationClient;

    private Double newLongitude = null;
    private Double newLatitude = null;
    private String newEventPoster;
    private Date newStartDate;
    private Date newEndDate;

    private StorageReference storageRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_view);

        storageRef = FirebaseStorage.getInstance().getReference();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        eventPosterImageView.setImageURI(imageUri);

                        StorageReference imageRef = storageRef.child("event_posters/" + UUID.randomUUID().toString() + ".jpg");

                        imageRef.putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        newEventPoster = uri.toString();
                                    }).addOnFailureListener(e -> {
                                        Log.e(TAG, "Error getting download URL", e);
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Upload failed", e);
                                });
                    }
                }
        );

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        backButton = findViewById(R.id.backButton);
        eventNameTextView = findViewById(R.id.EventName);
        eventDescriptionTextView = findViewById(R.id.description);
        eventFacilityTextView = findViewById(R.id.facilityName);
        eventFacilitySpinner= findViewById(R.id.spinner_facilities);
        eventStartDateTextView = findViewById(R.id.start_date_text);
        eventEndDateTextView = findViewById(R.id.end_date_text);
        eventEndTimeTextView = findViewById(R.id.end_time_text);
        eventStartTimeTextView = findViewById(R.id.start_time_text);
        eventCapacityTextView = findViewById(R.id.capacityTextView);
        eventNameEditText = findViewById(R.id.editEventName);
        eventDescriptionEditText = findViewById(R.id.description_edit);
        eventCapacityEditText = findViewById(R.id.editCapacity);

        eventPosterImageView = findViewById(R.id.uploaded_image_view);
        uploadImageButton = findViewById(R.id.upload_image_button);


        eventPosterImageView = findViewById(R.id.uploaded_image_view);

        qrImageView = findViewById(R.id.qrImage);
        waitinglistListView = findViewById(R.id.waitinglistListView);
        chosenEntrantsListView = findViewById(R.id.chosenEntrantsListView);
        cancelledEntrantsListView = findViewById(R.id.cancelledEntrantsListView);
        waitinglistButton = findViewById(R.id.waitinglistButton);
        chosenEntrantsButton = findViewById(R.id.chosenEntrantsButton);
        cancelledEntrantsButton = findViewById(R.id.cancelledEntrantsButton);

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
                        eventCapacityTextView.setText(event.getCapacity());
                        //eventFacilityTextView.setText(event.getFacilityName());

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


        waitinglistButton.setOnClickListener(view -> {
            if (event.getWaitinglist().isEmpty()) {
                Toast.makeText(ViewEventActivity.this, "Waiting list is empty.", Toast.LENGTH_SHORT).show();
            } else {
                if (waitinglistListView.getVisibility() == View.GONE) {
                    waitinglistListView.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewEventActivity.this,
                            android.R.layout.simple_list_item_1, event.getWaitinglist());
                    waitinglistListView.setAdapter(adapter);

                    waitinglistButton.setText("Hide Waitinglist");
                } else {
                    waitinglistListView.setVisibility(View.GONE);
                    waitinglistButton.setText("Show Waitinglist");
                }
            }
        });

        chosenEntrantsButton.setOnClickListener(view -> {
            if (event.getChosenEntrants().isEmpty()) {
                Toast.makeText(ViewEventActivity.this, "Chosen entrants list is empty.", Toast.LENGTH_SHORT).show();
            } else {
                if (chosenEntrantsListView.getVisibility() == View.GONE) {
                    chosenEntrantsListView.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewEventActivity.this,
                            android.R.layout.simple_list_item_1, event.getChosenEntrants());
                    chosenEntrantsListView.setAdapter(adapter);

                    chosenEntrantsButton.setText("Hide Chosen Entrants");
                } else {
                    chosenEntrantsListView.setVisibility(View.GONE);
                    chosenEntrantsButton.setText("Show Chosen Entrants");
                }
            }
        });

        cancelledEntrantsButton.setOnClickListener(view -> {
            if (event.getCancelledEntrants().isEmpty()) {
                Toast.makeText(ViewEventActivity.this, "Cancelled entrants list is empty.", Toast.LENGTH_SHORT).show();
            } else {
                if (cancelledEntrantsListView.getVisibility() == View.GONE) {
                    cancelledEntrantsListView.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewEventActivity.this,
                            android.R.layout.simple_list_item_1, event.getCancelledEntrants());
                    cancelledEntrantsListView.setAdapter(adapter);

                    cancelledEntrantsButton.setText("Hide Cancelled Entrants");
                } else {
                    cancelledEntrantsListView.setVisibility(View.GONE);
                    cancelledEntrantsButton.setText("Show Cancelled Entrants");
                }
            }
        });



        editButton.setOnClickListener(v -> {
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            uploadImageButton.setVisibility(View.VISIBLE);

            editEventName();
            editDescription();
            //editFacility();
            editImage();
            //editStartDate();
            //editEndDate();
            editCapacity();
        });

        deleteButton.setOnClickListener(v -> {if (isOwner) {
            new android.app.AlertDialog.Builder(ViewEventActivity.this)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this event?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        EventFirebase.deleteEvent(event.getEventID());
                        finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        } else {
            Toast.makeText(ViewEventActivity.this, "You are not the organizer, cannot delete event.", Toast.LENGTH_SHORT).show();
        }
        });

        cancelButton.setOnClickListener(view ->{

        });

        saveButton.setOnClickListener(view ->{
            String newEventName = eventNameEditText.getText().toString();
            String newDescription = eventDescriptionEditText.getText().toString();
            String newEventCapacity = eventCapacityEditText.getText().toString();

            String selectedFacility = eventFacilitySpinner.getSelectedItem().toString();

            String newStartTime = eventStartTimeTextView.getText().toString();
            String newEndTime = eventEndTimeTextView.getText().toString();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            event.setEventName(newEventName);
            event.setDescription(newDescription);
            event.setCapacity(newEventCapacity);
            event.setFacilityName(selectedFacility);
            event.setLatitude(newLatitude);
            event.setLongitude(newLongitude);
            event.setStartDate(newStartDate);
            event.setEndDate(newEndDate);
            event.setStartTime(newStartTime);
            event.setEndTime(newEndTime);
            event.setEventPoster(newEventPoster);

            eventNameTextView.setText(newEventName);
            eventDescriptionTextView.setText(newDescription);
            eventCapacityTextView.setText(newEventCapacity);
            //eventFacility.setSelection(getFacilityPosition(selectedFacility));
            eventStartDateTextView.setText(dateFormat.format(newStartDate));
            eventEndDateTextView.setText(dateFormat.format(newEndDate));
            eventStartTimeTextView.setText(newStartTime);
            eventEndTimeTextView.setText(newEndTime);

            eventNameTextView.setVisibility(View.VISIBLE);
            eventNameEditText.setVisibility(View.GONE);

            eventDescriptionTextView.setVisibility(View.VISIBLE);
            eventDescriptionEditText.setVisibility(View.GONE);

            eventCapacityTextView.setVisibility(View.VISIBLE);
            eventCapacityEditText.setVisibility(View.GONE);

            eventStartDateTextView.setVisibility(View.VISIBLE);
            startDateButton.setVisibility(View.GONE);

            eventEndDateTextView.setVisibility(View.VISIBLE);
            endDateButton.setVisibility(View.GONE);

            eventStartTimeTextView.setVisibility(View.VISIBLE);

            eventEndTimeTextView.setVisibility(View.VISIBLE);

            saveButton.setVisibility(View.GONE);

            EventFirebase.editEvent(event);
            Toast.makeText(ViewEventActivity.this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
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
        LoginManagement login = new LoginManagement(this);
        login.isUserLoggedIn(isLoggedIn -> {
            if (isLoggedIn) {
                UserFirestore.findUser(deviceID, new UserFirestore.Callback() {
                    @Override
                    public void onSuccess(UserInfo userInfo) {
                        user = userInfo;

                        ArrayList<EventInfo> eventsList = user.getEvents();
                        eventsList.add(event);
                        user.setEvents(eventsList);
                        UserFirestore.editUserEvents(user);

                        ArrayList<String> currentEntrants = event.getWaitinglist();
                        String newEntrant = "[" + deviceID + ", " + userLocation.getLatitude() + ", " + userLocation.getLongitude() + "]";
                        currentEntrants.add(newEntrant);
                        event.setWaitinglist(currentEntrants);
                        EventFirebase.editEvent(event);
                        Toast.makeText(ViewEventActivity.this, "Joined Waiting List Successfully.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("ViewEventActivity", "Error fetching user: " + error);
                    }
                });

            } else {
                Registration registration = new Registration();
                Bundle bundle = new Bundle();
                bundle.putString("eventID", event.getEventID());
                registration.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.event_view, registration)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }


    private void editEventName(){
        eventNameTextView.setVisibility(View.GONE);
        eventNameEditText.setVisibility(View.VISIBLE);
        eventNameEditText.setText(event.getEventName());
    }
    private void editDescription(){
        eventDescriptionTextView.setVisibility(View.GONE);
        eventDescriptionEditText.setVisibility(View.VISIBLE);
        eventDescriptionEditText.setText(event.getDescription());

    }
    private void editCapacity(){
        eventCapacityTextView.setVisibility(View.GONE);
        eventCapacityEditText.setVisibility(View.VISIBLE);
        eventCapacityEditText.setText(event.getCapacity());
    }


    private void editImage() {
        uploadImageButton.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }


    private void editStartDate(){

    }
    private void editEndDate(){

    }
    private void editFacility(){

    }



}