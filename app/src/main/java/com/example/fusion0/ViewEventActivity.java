package com.example.fusion0;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;


/**
 * @author Simon Haile
 * This activity allows organizers to view the selected joined event and users that have scanned
 * a qr code to view the scanned event
 */
public class ViewEventActivity extends AppCompatActivity {

    private String deviceID;
    private Boolean isOwner = false;
    private Spinner eventFacility;
    private TextView eventNameTextView, eventFacilityTextView,addFacilityText, eventDescriptionTextView, dateRequirementsTextView,  eventStartDateTextView, eventEndDateTextView,eventStartTimeTextView, eventEndTimeTextView, eventCapacityTextView, waitinglistFullTextView;
    private EditText eventNameEditText, eventDescriptionEditText, eventCapacityEditText;
    private ImageView eventPosterImageView, qrImageView;
    private ListView waitinglistListView, chosenEntrantsListView, cancelledEntrantsListView;
    private Button startDateButton, endDateButton, editButton, deleteButton, joinButton, cancelButton, saveButton, waitinglistButton, cancelledEntrantsButton, chosenEntrantsButton, uploadImageButton, lotteryButton;
    private ImageButton backButton;
    private EventInfo event;
    private UserInfo user;
    private OrganizerInfo organizer;
    private LinearLayout toolbar;
    private Calendar startDateCalendar;
    private androidx.fragment.app.FragmentContainerView autocompletePlaceFragment;
    private Location userLocation;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private FusedLocationProviderClient fusedLocationClient;

    private Double newLongitude = null;
    private Double newLatitude = null;
    private String newEventPoster, facility, address;
    private Date endDate, startDate;
    private FacilitiesInfo newFacility;

    Waitlist waitlist;

    private StorageReference storageRef;


    /**
     * @author Simon Haile
     * Initializes the activity by setting up the user interface, loading event details,
     * fetching user and event information, and handling user interactions. This method
     * is called when the activity is created.
     *
     * @param savedInstanceState A Bundle object containing the activity's previously saved state,
     *  or null if the activity is being created for the first time.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_view);

        storageRef = FirebaseStorage.getInstance().getReference();
        uploadNewPoster();


        waitlist = new Waitlist();

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        backButton = findViewById(R.id.backButton);
        lotteryButton = findViewById(R.id.lottery_button);
        eventNameTextView = findViewById(R.id.EventName);
        eventDescriptionTextView = findViewById(R.id.description);
        eventFacility = findViewById(R.id.spinner_facilities);
        eventFacilityTextView = findViewById(R.id.facilityName);
        addFacilityText = findViewById(R.id.add_facility_text);
        autocompletePlaceFragment = findViewById(R.id.autocomplete_fragment);
        eventStartDateTextView = findViewById(R.id.start_date_text);
        eventEndDateTextView = findViewById(R.id.end_date_text);
        eventEndTimeTextView = findViewById(R.id.end_time_text);
        eventStartTimeTextView = findViewById(R.id.start_time_text);
        eventCapacityTextView = findViewById(R.id.capacityTextView);
        eventNameEditText = findViewById(R.id.editEventName);
        eventDescriptionEditText = findViewById(R.id.description_edit);
        eventCapacityEditText = findViewById(R.id.editCapacity);
        dateRequirementsTextView = findViewById(R.id.date_requirements_text);

        eventPosterImageView = findViewById(R.id.uploaded_image_view);
        uploadImageButton = findViewById(R.id.upload_image_button);

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
                        eventFacilityTextView.setText(event.getFacilityName());


                        eventStartDateTextView.setText(event.getStartDate().toString());
                        eventEndDateTextView.setText(event.getEndDate().toString());
                        eventStartTimeTextView.setText(event.getStartTime());
                        eventEndTimeTextView.setText(event.getEndTime());

                        endDate = event.getEndDate();
                        startDate =event.getStartDate();
                        facility = event.getFacilityName();

                        newEventPoster = event.getEventPoster();

                        if (newEventPoster != null && !newEventPoster.isEmpty()) {
                            Glide.with(ViewEventActivity.this)
                                    .load(newEventPoster)
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
                            EventFirebase.findOrganizer(event.getOrganizer(), new EventFirebase.OrganizerCallback() {
                                @Override
                                public void onSuccess(OrganizerInfo organizerInfo) {
                                    organizer = organizerInfo;
                                }

                                @Override
                                public void onFailure(String error) {
                                    Log.e(TAG, "Unable to find organizer.");
                                }
                            });

                            isOwner = true;

                            int lotteryCapacity = 10;

                           lotteryButton.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View view) {
                                   waitlist.conductLottery(eventID, lotteryCapacity);
                               }
                           });

                        } else {
                            editButton.setVisibility(View.GONE);
                            deleteButton.setVisibility(View.GONE);
                            cancelButton.setVisibility(View.GONE);
                            saveButton.setVisibility(View.GONE);

                            ArrayList<ArrayList<String>> currentEntrants = event.getWaitinglist();
                            int capacity = Integer.parseInt(event.getCapacity());

                            if (currentEntrants.size() < capacity) {
                                joinButton.setVisibility(View.VISIBLE);
                                waitinglistButton.setOnClickListener(view -> {
                                    if (event.getWaitinglist().isEmpty()) {
                                        Toast.makeText(ViewEventActivity.this, "Waiting list is empty.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (waitinglistListView.getVisibility() == View.GONE) {
                                            waitinglistListView.setVisibility(View.VISIBLE);
                                            ArrayList<String> flatList = new ArrayList<String>();

                                            for (ArrayList<String> user: event.getWaitinglist()) {
                                                flatList.add("[" +  user.get(0) + ", " + user.get(1) + ", " + user.get(2));
                                            }

                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(ViewEventActivity.this,
                                                    android.R.layout.simple_list_item_1, flatList);
                                            waitinglistListView.setAdapter(adapter);

                                            waitinglistButton.setText("Hide Waitinglist");
                                        } else {
                                            waitinglistListView.setVisibility(View.GONE);
                                            waitinglistButton.setText("Show Waitinglist");
                                        }
                                    }
                                });
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
            startDateButton.setVisibility(View.VISIBLE);
            endDateButton.setVisibility(View.VISIBLE);


            editEventName();
            editDescription();
            editFacility(organizer);
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
            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            uploadImageButton.setVisibility(View.GONE);
            startDateButton.setVisibility(View.GONE);
            endDateButton.setVisibility(View.GONE);
            addFacilityText.setVisibility(View.GONE);
            autocompletePlaceFragment.setVisibility(View.GONE);
            eventFacilityTextView.setVisibility(View.VISIBLE);
            eventFacility.setVisibility(View.GONE);

            eventNameTextView.setVisibility(View.VISIBLE);
            eventNameEditText.setVisibility(View.GONE);

            eventDescriptionTextView.setVisibility(View.VISIBLE);
            eventDescriptionEditText.setVisibility(View.GONE);

            eventCapacityTextView.setVisibility(View.VISIBLE);
            eventCapacityEditText.setVisibility(View.GONE);

        });

        saveButton.setOnClickListener(view ->{
            String newEventName = eventNameEditText.getText().toString();
            String newDescription = eventDescriptionEditText.getText().toString();
            String newEventCapacity = eventCapacityEditText.getText().toString();
            String newStartTime = eventStartTimeTextView.getText().toString();
            String newEndTime = eventEndTimeTextView.getText().toString();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            event.setEventName(newEventName);
            event.setDescription(newDescription);
            event.setCapacity(newEventCapacity);
            event.setFacilityName(facility);
            event.setAddress(address);
            event.setLatitude(newLatitude);
            event.setLongitude(newLongitude);
            event.setStartDate(startDate);
            event.setEndDate(endDate);
            event.setStartTime(newStartTime);
            event.setEndTime(newEndTime);
            event.setEventPoster(newEventPoster);

            eventNameTextView.setText(newEventName);
            eventDescriptionTextView.setText(newDescription);
            eventCapacityTextView.setText(newEventCapacity);
            eventFacilityTextView.setText(facility);
            eventStartDateTextView.setText(dateFormat.format(startDate));
            eventEndDateTextView.setText(dateFormat.format(endDate));
            eventStartTimeTextView.setText(newStartTime);
            eventEndTimeTextView.setText(newEndTime);

            eventNameTextView.setVisibility(View.VISIBLE);
            eventNameEditText.setVisibility(View.GONE);

            eventDescriptionTextView.setVisibility(View.VISIBLE);
            eventDescriptionEditText.setVisibility(View.GONE);

            eventCapacityTextView.setVisibility(View.VISIBLE);
            eventCapacityEditText.setVisibility(View.GONE);

            addFacilityText.setVisibility(View.GONE);
            autocompletePlaceFragment.setVisibility(View.GONE);
            eventFacilityTextView.setVisibility(View.VISIBLE);
            eventFacility.setVisibility(View.GONE);

            if (newFacility != null){
                EventFirebase.addFacility(newFacility);
                ArrayList<FacilitiesInfo> facilitiesList = organizer.getFacilities();
                facilitiesList.add(newFacility);
                organizer.setFacilities(facilitiesList);
                EventFirebase.editOrganizer(organizer);
            }

            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            uploadImageButton.setVisibility(View.GONE);
            startDateButton.setVisibility(View.GONE);
            endDateButton.setVisibility(View.GONE);

            EventFirebase.editEvent(event);
            Toast.makeText(ViewEventActivity.this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
        });

        startDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(ViewEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                    startDateCalendar = Calendar.getInstance();
                    startDateCalendar.set(selectedYear, selectedMonth, selectedDay);
                    Calendar currentDate = Calendar.getInstance();
                    if (startDateCalendar.before(currentDate)) {
                        dateRequirementsTextView.setText("Date Must Be Today or Later.");
                        dateRequirementsTextView.setVisibility(View.VISIBLE);
                        eventStartDateTextView.setVisibility(View.GONE);
                        startDateCalendar = null;
                    } else {
                        String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                        eventStartDateTextView.setText(selectedDate);
                        eventStartDateTextView.setVisibility(View.VISIBLE);
                        dateRequirementsTextView.setVisibility(View.GONE);

                        Date newStartDate = startDateCalendar.getTime();

                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(ViewEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                startDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                startDateCalendar.set(Calendar.MINUTE, selectedMinute);

                                Calendar currentTime = Calendar.getInstance();

                                if (startDateCalendar.before(currentTime)) {
                                    dateRequirementsTextView.setText("Start Time Must Be Now or Later.");
                                    dateRequirementsTextView.setVisibility(View.VISIBLE);
                                    eventStartTimeTextView.setVisibility(View.GONE);
                                } else {
                                    String selectedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                                    eventStartTimeTextView.setText(selectedTime);
                                    eventStartTimeTextView.setVisibility(View.VISIBLE);
                                    dateRequirementsTextView.setVisibility(View.GONE);
                                    startDate = newStartDate;
                                }
                            }
                        }, hour, minute, true);
                        timePickerDialog.show();
                    }
                }
            }, year, month, day);
            dialog.show();
        });

        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(com.example.fusion0.ViewEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        Calendar endDateCalendar = Calendar.getInstance();
                        endDateCalendar.set(selectedYear, selectedMonth, selectedDay);

                        // Copy time from startDateCalendar if the dates are the same
                        if (startDateCalendar != null &&
                                endDateCalendar.get(Calendar.YEAR) == startDateCalendar.get(Calendar.YEAR) &&
                                endDateCalendar.get(Calendar.MONTH) == startDateCalendar.get(Calendar.MONTH) &&
                                endDateCalendar.get(Calendar.DAY_OF_MONTH) == startDateCalendar.get(Calendar.DAY_OF_MONTH)) {
                            // Set the end time to the start time
                            endDateCalendar.set(Calendar.HOUR_OF_DAY, startDateCalendar.get(Calendar.HOUR_OF_DAY));
                            endDateCalendar.set(Calendar.MINUTE, startDateCalendar.get(Calendar.MINUTE));
                        }
                        Calendar currentDate = Calendar.getInstance();

                        if (startDateCalendar == null) {
                            dateRequirementsTextView.setText("Please select a Start Date first.");
                            dateRequirementsTextView.setVisibility(View.VISIBLE);
                            eventEndDateTextView.setVisibility(View.GONE);
                            eventEndTimeTextView.setVisibility(View.GONE);
                        } else if (endDateCalendar.before(currentDate)) {
                            dateRequirementsTextView.setText("End Date Must Be Today or Later.");
                            dateRequirementsTextView.setVisibility(View.VISIBLE);
                            eventEndDateTextView.setVisibility(View.GONE);
                            eventEndTimeTextView.setVisibility(View.GONE);
                        } else if (endDateCalendar.before(startDateCalendar)) {
                            eventEndDateTextView.setVisibility(View.GONE);
                            eventEndTimeTextView.setVisibility(View.GONE);
                            dateRequirementsTextView.setText("End Date Must Be On or After Start Date.");
                            dateRequirementsTextView.setVisibility(View.VISIBLE);
                        } else {
                            String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                            eventEndDateTextView.setText(selectedDate);
                            eventEndDateTextView.setVisibility(View.VISIBLE);
                            dateRequirementsTextView.setVisibility(View.GONE);

                            Date newEndDate = endDateCalendar.getTime();

                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);

                            TimePickerDialog timePickerDialog = new TimePickerDialog(com.example.fusion0.ViewEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                    endDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    endDateCalendar.set(Calendar.MINUTE, selectedMinute);

                                    if (endDateCalendar.before(startDateCalendar)) {
                                        dateRequirementsTextView.setText("End Time Must Be After Start Time.");
                                        dateRequirementsTextView.setVisibility(View.VISIBLE);
                                        eventEndTimeTextView.setVisibility(View.GONE);
                                    } else {
                                        String selectedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                                        eventEndTimeTextView.setText(selectedTime);
                                        eventEndTimeTextView.setVisibility(View.VISIBLE);
                                        dateRequirementsTextView.setVisibility(View.GONE);
                                        endDate = newEndDate;
                                    }
                                }
                            }, hour, minute, true);
                            timePickerDialog.show();
                        }
                    }
                }, year, month, day);
                dialog.show();
            }
        });

        waitlist.getAll(eventID, all -> {
            if (!all.contains(deviceID)) {
                joinButton.setOnClickListener(view -> {
                    if (event.getGeolocation()) {
                        GeoLocation geoLocation = new GeoLocation(this, this, event.getLatitude(), event.getLongitude(), event.getRadius());
                        Log.e("ViewEventActivity", "Radius: " + event.getRadius());
                        if (!geoLocation.isLocationPermissionGranted()) {
                            geoLocation.requestLocationPermission();
                        } else {
                            proceedWithJoin(geoLocation);
                        }
                    } else {
                        addUserToWaitingList();
                    }
                });
            } else {
                joinButton.setText("Unjoin Waiting List");

                joinButton.setOnClickListener(view -> {

                    Toast.makeText(ViewEventActivity.this, "You have left the waiting list", Toast.LENGTH_SHORT).show();

                    // Remove it on the events collection
                    ArrayList<ArrayList<String>> newWaitingList = event.removeUserFromWaitingList(deviceID, event.getWaitinglist());
                    event.setWaitinglist(newWaitingList);

                    // Remove it on the user's collection
                    waitlist.removeFromUserWL(deviceID, eventID);

                    EventFirebase.editEvent(event);

                    UserFirestore.findUser(deviceID, new UserFirestore.Callback() {
                        @Override
                        public void onSuccess(UserInfo userInfo) {
                            user = userInfo;
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("JoinedEventActivity", "Error fetching user: " + error);
                        }
                    });

                    ArrayList<EventInfo> newEventsList = user.removeEventFromEventList(event, user.getEvents());
                    user.setEvents(newEventsList);
                    UserFirestore.editUserEvents(user);

                    EventFirebase.editEvent(event);

                    Intent intent = new Intent(ViewEventActivity.this, MainActivity.class);
                    startActivity(intent);
                });
            }
        });

    }

    /**
     * @author Derin Karas
     *
     *
     * @param geoLocation
     */
    private void proceedWithJoin(GeoLocation geoLocation) {
        userLocation = geoLocation.getLocation();
        if (userLocation == null) {
            Toast.makeText(this, "Retrieving your location...", Toast.LENGTH_SHORT).show();
            return;
        }

        //Once the location is retrieved, proceed with registration check
        validateDistanceAndJoin(geoLocation);
    }


    /**
     * @author Derin Karas
     *
     *
     * @param geoLocation
     */
    private void validateDistanceAndJoin(GeoLocation geoLocation) {
        //geoLocation.setUserLocation(userLocation.getLatitude(), userLocation.getLongitude());
        if (geoLocation.canRegister()) {
            addUserToWaitingList();
        }
        else {
            geoLocation.showMapDialog(); //Optionally show the user a map with the event location and radius
            Toast.makeText(this, "You are outside the acceptable radius to join this event.", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * @author Simon Haile, Derin Karas, Sehej Brar
     * Adds the joined event to the user attribute 'events' and adds user to the event waitinglist
     *
     */
    private void addUserToWaitingList() {
        ArrayList<EventInfo> eventsList = user.getEvents();
        eventsList.add(event);
//        user.setEvents(eventsList);
//        UserFirestore.editUserEvents(user);
        waitlist.addToUserWL(deviceID, event.getEventID());

        ArrayList<ArrayList<String>> currentEntrants = event.getWaitinglist();

        if (event.getGeolocation()){
            ArrayList<String> newEntrant = new ArrayList<>(Arrays.asList(deviceID,
                    String.valueOf(userLocation.getLatitude()),
                    String.valueOf(userLocation.getLatitude()), "waiting"));
            currentEntrants.add(newEntrant);
        } else {
            currentEntrants.add(new ArrayList<String>(Arrays.asList(deviceID, null, null, "waiting")));
        }

        event.setWaitinglist(currentEntrants);
        EventFirebase.editEvent(event);
        Toast.makeText(this, "Joined Waiting List Successfully.", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * @author Simon Haile
     * Displays an editable text field for the event name by hiding the
     * event name text view and showing the corresponding EditText.
     * Sets the EditText's content to the current event name.
     */
    private void editEventName(){
        eventNameTextView.setVisibility(View.GONE);
        eventNameEditText.setVisibility(View.VISIBLE);
        eventNameEditText.setText(event.getEventName());
    }

    /**
     * @author Simon Haile
     * Displays an editable text field for the description by hiding the
     * description text view and showing the corresponding EditText.
     * Sets the EditText's content to the current description.
     */
    private void editDescription(){
        eventDescriptionTextView.setVisibility(View.GONE);
        eventDescriptionEditText.setVisibility(View.VISIBLE);
        eventDescriptionEditText.setText(event.getDescription());

    }

    /**
     * @author Simon Haile
     * Displays an editable text field for the capacity by hiding the
     * capacity text view and showing the corresponding EditText.
     * Sets the EditText's content to the current capacity.
     */
    private void editCapacity(){
        eventCapacityTextView.setVisibility(View.GONE);
        eventCapacityEditText.setVisibility(View.VISIBLE);
        eventCapacityEditText.setText(event.getCapacity());
    }

    /**
     * @author Simon Haile
     * Displays a spinner to allow the event organizer to choose or add a facility for the event.
     * The spinner is populated with existing facilities, and the "Add Facility" option is added at the end.
     * If a facility is selected, its details are fetched from Firebase. If "Add Facility" is selected,
     * the user can add a new facility using a place autocomplete fragment.
     *
     * @param organizer The organizer's information used to retrieve their facilities.
     */
    private void editFacility(OrganizerInfo organizer){
        eventFacility.setVisibility(View.VISIBLE);
        eventFacilityTextView.setVisibility(View.GONE);

        ArrayList<String> facilityNames = new ArrayList<>();

        // Add existing facilities to the facilityNames list
        if (organizer.getFacilities() != null) {
            ArrayList<FacilitiesInfo> facilities = organizer.getFacilities();
            for (FacilitiesInfo facility : facilities) {
                if (facility != null) {
                    facilityNames.add(facility.getFacilityName());
                } else {
                    Log.e(TAG, "Found a null facility in the list.");
                }
            }
        }

        // Add the "Add Facility" option at the end
        facilityNames.add("Add Facility");

        // Create the ArrayAdapter for the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, facilityNames);

        // Set drop-down view resource
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the spinner
        eventFacility.setAdapter(adapter);

        // Set the item selection listener for the spinner
        eventFacility.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFacility = parent.getItemAtPosition(position).toString();

                // Check if "Add Facility" is selected
                if (selectedFacility.equals("Add Facility")) {
                    addFacility(facilityNames, adapter); // Pass the adapter so we can update it
                } else {
                    // If the selected facility exists, proceed with fetching it
                    String facilityID = organizer.getFacilityIdByName(selectedFacility);
                    EventFirebase.findFacility(facilityID, new EventFirebase.FacilityCallback() {
                        @Override
                        public void onSuccess(FacilitiesInfo existingFacility) {
                            address = existingFacility.getAddress();
                            facility = existingFacility.getFacilityName();
                            newLongitude = existingFacility.getLongitude();
                            newLatitude = existingFacility.getLatitude();
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e(TAG, "Error fetching facility: " + error);
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle nothing selected case if necessary
            }
        });
    }


    /**
     * @author Simon Haile
     * Allows the user to add a new facility to the list by using a place autocomplete fragment.
     * The fragment allows the user to select a place, which is then added as a new facility.
     * The facility details (address, name, and coordinates) are captured and added to the facility list.
     * If the facility already exists in the list, a message is displayed to the user.
     *
     * @param facilityNames The list of existing facility names.
     * @param adapter The adapter used for the facility spinner to update the displayed options.
     */
    private void addFacility(ArrayList<String> facilityNames, ArrayAdapter<String> adapter) {
        autocompletePlaceFragment.setVisibility(View.VISIBLE);
        addFacilityText.setVisibility(View.VISIBLE);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        }

        // Initialize the AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LAT_LNG));

        // Set up the PlaceSelectionListener
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                address = place.getFormattedAddress();
                facility = place.getDisplayName();

                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    newLatitude = latLng.latitude;
                    newLongitude = latLng.longitude;
                }

                // Check if the facility name already exists in the list of facility names
                if (facilityNames.contains(facility)) {
                    Log.i(TAG, "Facility already exists: " + facility);
                    // Optionally show a message to the user
                    Toast.makeText(getApplicationContext(), "This facility has already been added.", Toast.LENGTH_SHORT).show();
                } else {
                    // Create new facility and proceed
                    newFacility = new FacilitiesInfo(address, facility, deviceID, newLatitude, newLongitude);

                    // Add the new facility name to the facilityNames list
                    facilityNames.add(facility);

                    // Notify the adapter that the data has changed
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }


    /**
     * @author Simon Haile
     * Initializes the image upload process by setting up an image picker and handling the image
     * upload to Firebase Storage.
     * This method sets up an `ActivityResultLauncher` to handle the image selection from the device.
     * When an image is selected, it is displayed in an `ImageView` (`eventPosterImageView`).
     * The image is then uploaded to Firebase Storage under the "event_posters"
     * directory, with a unique file name generated using `UUID`. Once the upload is successful,
     * the download URL for the uploaded image
     * is stored for later use.
     */
    private void uploadNewPoster(){
        Button uploadImageButton = findViewById(R.id.upload_image_button);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        eventPosterImageView.setVisibility(View.VISIBLE);
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

        uploadImageButton.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    };
}