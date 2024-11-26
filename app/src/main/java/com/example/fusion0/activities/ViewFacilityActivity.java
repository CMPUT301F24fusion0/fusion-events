package com.example.fusion0.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.ProfileManagement;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.FacilitiesInfo;
import com.example.fusion0.models.UserInfo;
import com.google.zxing.WriterException;
import com.example.fusion0.R;

import java.util.ArrayList;

/**
 * @author Simon Haile
 * This activity allows organizers to view their respective facilities and  edit the facility information
 */
public class ViewFacilityActivity extends AppCompatActivity {

    private String deviceID;
    private TextView facilityNameTextView, addressTextView, ownerTextView, facilitiesEventsTextView;
    private EditText facilityNameEditText, addressEditText;
    private FacilitiesInfo facility;
    private ImageView facilityImageView;
    private Boolean isOwner = false;
    private LinearLayout toolbar;
    private ImageButton backButton;
    private Button editButton, saveButton, deleteButton, cancelButton;
    private ListView facilitiesEventsList;
    private ProfileManagement profileManager;



    /**
     * Called when the activity is first created. This method sets up the user interface
     * and handles the logic for displaying, editing, and deleting facility details.
     * It initializes UI components, retrieves facility data from Firebase, and manages
     * user interaction for viewing and modifying the facility information.
     * @author Simon Haile
     *
     * @param savedInstanceState This Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle), otherwise null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facility_view);

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        facilityNameTextView = findViewById(R.id.facilityName);
        addressTextView = findViewById(R.id.address);
        facilityImageView = findViewById(R.id.facilityImage);
        ownerTextView = findViewById(R.id.owner);
        facilitiesEventsTextView= findViewById(R.id.facilities_events_list_text);
        facilityNameEditText = findViewById(R.id.editFacilityName);
        addressEditText = findViewById(R.id.editAddress);
        toolbar = findViewById(R.id.toolbar);


        facilitiesEventsList = findViewById(R.id.facilities_events_list);

        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.edit_button);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);
        cancelButton = findViewById(R.id.cancel_button);

        profileManager = new ProfileManagement();




        // Back button logic
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(ViewFacilityActivity.this, FavouriteActivity.class);
            startActivity(intent);
        });

        Intent intentReceived = getIntent();
        String facilityID = intentReceived.getStringExtra("facilityID");

        EventFirebase.findFacility(facilityID, new EventFirebase.FacilityCallback() {
            @Override
            public void onSuccess(FacilitiesInfo facilitiesInfo) {
                if (facilitiesInfo == null) {
                    Toast.makeText(ViewFacilityActivity.this, "Facility Unavailable.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    facility = facilitiesInfo;
                    facilityNameTextView.setText(facility.getFacilityName());
                    addressTextView.setText(facility.getAddress());

                    UserFirestore.findUser(deviceID, new UserFirestore.Callback() {
                        @Override
                        public void onSuccess(UserInfo user) {
                            String fullName = user.getFirstName() + ' ' + user.getLastName();
                            ownerTextView.setText(fullName);
                        }

                        @Override
                        public void onFailure(String error) {
                            ownerTextView.setText(facility.getOwner());

                        }
                    });
                    if (facility.getFacilityImage() != null && !facility.getFacilityImage().isEmpty()) {
                        Glide.with(ViewFacilityActivity.this)
                                .load(facility.getFacilityImage())
                                .into(facilityImageView);
                        facilityImageView.setVisibility(View.VISIBLE);
                    }

                    if (deviceID.equals(facility.getOwner()) || EventFirebase.isDeviceIDAdmin(deviceID)) {
                        if(EventFirebase.isDeviceIDAdmin(deviceID)){
                            Toast.makeText(ViewFacilityActivity.this, "You are an admin.", Toast.LENGTH_SHORT).show();
                        }
                        isOwner = true;
                        toolbar.setVisibility(View.VISIBLE);
                    }

                    ArrayList<String> eventNames = new ArrayList<>();
                    ArrayAdapter<String> eventsAdapter = new ArrayAdapter<>(ViewFacilityActivity.this, android.R.layout.simple_list_item_1, eventNames);
                    facilitiesEventsList.setAdapter(eventsAdapter);
                    if (facility.getEvents() != null) {
                        ArrayList<String> filteredEvents = new ArrayList<>();
                        for (String event : facility.getEvents()) {
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

                        facility.setEvents(filteredEvents);
                        facilitiesEventsList.setOnItemClickListener((parent, view1, position, id) -> {
                            String eventID = facility.getEvents().get(position);

                            Intent intent = new Intent(ViewFacilityActivity.this, ViewEventActivity.class);
                            intent.putExtra("eventID", eventID);
                            intent.putExtra("deviceID", deviceID);
                            startActivity(intent);
                        });

                    }else{
                        facilitiesEventsTextView.setVisibility(View.VISIBLE);
                        facilitiesEventsList.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error fetching facility data: " + error);
                Toast.makeText(ViewFacilityActivity.this, "Failed to load facility data.", Toast.LENGTH_SHORT).show();
            }
        });


        editButton.setOnClickListener(v -> {
            if (isOwner) {
                facilityNameTextView.setVisibility(View.GONE);
                addressTextView.setVisibility(View.GONE);

                facilityNameEditText.setVisibility(View.VISIBLE);
                addressEditText.setVisibility(View.VISIBLE);

                saveButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);


                facilityNameEditText.setText(facility.getFacilityName());
                addressEditText.setText(facility.getAddress());

                editButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);

            }
        });

        saveButton.setOnClickListener(v -> {
            if (isOwner) {
                facility.setFacilityName(facilityNameEditText.getText().toString());
                facility.setAddress(addressEditText.getText().toString());

                facilityNameTextView.setVisibility(View.VISIBLE);
                addressTextView.setVisibility(View.VISIBLE);
                ownerTextView.setVisibility(View.VISIBLE);

                facilityNameEditText.setVisibility(View.GONE);
                addressEditText.setVisibility(View.GONE);

                editButton.setVisibility(View.VISIBLE);

                saveButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.VISIBLE);

                facilityNameTextView.setText(facility.getFacilityName());
                addressTextView.setText(facility.getAddress());
                ownerTextView.setText(facility.getOwner());

                EventFirebase.editFacility(facility);
            }
        });

        deleteButton.setOnClickListener(view -> {
            if (isOwner) {
                new AlertDialog.Builder(ViewFacilityActivity.this)
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> EventFirebase.deleteFacility(facility.getFacilityID()))
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        cancelButton.setOnClickListener(view ->{
            facilityNameTextView.setVisibility(View.VISIBLE);
            addressTextView.setVisibility(View.VISIBLE);

            facilityNameEditText.setVisibility(View.GONE);
            addressEditText.setVisibility(View.GONE);

            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);

            saveButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);


            facilityNameTextView.setText(facility.getFacilityName());
            addressTextView.setText(facility.getAddress());
        });

    }
}
