package com.example.fusion0.activities;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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

import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.fusion0.BuildConfig;
import com.example.fusion0.fragments.CancelledEntrants;
import com.example.fusion0.fragments.ChosenEntrants;
import com.example.fusion0.fragments.Registration;
import com.example.fusion0.fragments.WaitlistFragment;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.GeoLocation;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.helpers.Waitlist;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.FacilitiesInfo;
import com.example.fusion0.models.OrganizerInfo;
import com.example.fusion0.models.UserInfo;
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
import com.yalantis.ucrop.UCrop;
import com.example.fusion0.R;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
    private TextView eventNameTextView, eventFacilityTextView,addFacilityText, eventDescriptionTextView,registrationDateRequirementsTextView, registrationDateTextView, dateRequirementsTextView,  eventStartDateTextView, eventEndDateTextView,eventStartTimeTextView, eventEndTimeTextView, eventCapacityTextView, waitinglistFullTextView;
    private EditText eventNameEditText, eventDescriptionEditText, eventCapacityEditText;
    private ImageView eventPosterImageView, qrImageView;
    private Button startDateButton, endDateButton, registrationDateButton, editButton, deleteButton, joinButton, cancelButton, saveButton, waitinglistButton, cancelledEntrantsButton, chosenEntrantsButton, uploadImageButton, lotteryButton;
    private ImageButton backButton;
    private EventInfo event;
    private UserInfo user;
    private OrganizerInfo organizer;
    private LinearLayout toolbar;
    private Calendar startDateCalendar, registrationDateCalendar;
    private androidx.fragment.app.FragmentContainerView autocompletePlaceFragment;
    private Location userLocation;
    LinearLayout lists;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private FusedLocationProviderClient fusedLocationClient;

    private Double newLongitude = null;
    private Double newLatitude = null;
    private String newEventPoster, facility, address;
    private Date endDate, startDate, registrationDate;
    private FacilitiesInfo newFacility;

    public static Waitlist waitlist;

    private StorageReference storageRef;


    /**
     * @author Simon Haile, Sehej Brar
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
        registrationDateRequirementsTextView=findViewById(R.id.registration_date_requirements_text);
        registrationDateTextView = findViewById(R.id.registration_date_text);
        registrationDateButton = findViewById(R.id.registration_date_button);


        eventPosterImageView = findViewById(R.id.uploaded_image_view);
        uploadImageButton = findViewById(R.id.upload_image_button);

        qrImageView = findViewById(R.id.qrImage);
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
        lists = findViewById(R.id.lists);
        toolbar = findViewById(R.id.toolbar);

        backButton.setOnClickListener(view -> {
            finish();
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

                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                        String formattedStartDate = dateFormat.format(event.getStartDate());
                        String formattedEndDate = dateFormat.format(event.getEndDate());
                        String formattedRegistrationDate = dateFormat.format(event.getRegistrationDate());


                        eventStartDateTextView.setText(formattedStartDate);
                        eventEndDateTextView.setText(formattedEndDate);
                        eventStartTimeTextView.setText(event.getStartTime());
                        eventEndTimeTextView.setText(event.getEndTime());
                        registrationDateTextView.setText(formattedRegistrationDate);


                        endDate = event.getEndDate();
                        startDate =event.getStartDate();
                        registrationDate = event.getRegistrationDate();
                        facility = event.getFacilityName();

                        newEventPoster = event.getEventPoster();

                        if (newEventPoster != null && !newEventPoster.isEmpty()) {
                            Glide.with(ViewEventActivity.this)
                                    .load(newEventPoster)
                                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                    .into(new SimpleTarget<Drawable>() {
                                        @Override
                                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                            // Get the original image dimensions
                                            int originalWidth = resource.getIntrinsicWidth();
                                            int originalHeight = resource.getIntrinsicHeight();


                                            int newWidth = (int) (originalWidth / 1.5);
                                            int newHeight = (int) (originalHeight / 1.5);


                                            Glide.with(ViewEventActivity.this)
                                                    .load(newEventPoster)
                                                    .override(newWidth, newHeight)
                                                    .into(eventPosterImageView);


                                            // Set the visibility of the ImageView
                                            eventPosterImageView.setVisibility(View.VISIBLE);
                                        }
                                    });
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


                        } else {
                            lists.setVisibility(View.GONE);
                            editButton.setVisibility(View.GONE);
                            deleteButton.setVisibility(View.GONE);
                            cancelButton.setVisibility(View.GONE);
                            saveButton.setVisibility(View.GONE);

                            ArrayList<Map<String, String>> currentEntrants = event.getWaitinglist();
                            int capacity = Integer.parseInt(event.getCapacity());

                            if (currentEntrants.size() < capacity) {
                                joinButton.setVisibility(View.VISIBLE);
                                waitlist.getAll(eventID, all -> {
                                    if (!all.contains(deviceID)) {
                                        joinButton.setOnClickListener(view -> {
                                            UserFirestore.findUser(deviceID, new UserFirestore.Callback() {
                                                @Override
                                                public void onSuccess(UserInfo userInfo) {
                                                    if (userInfo != null) {
                                                        user = userInfo;
                                                        Log.d("Checkpoint", "the user is not null");
                                                        if (event.getGeolocation()) {
                                                            GeoLocation geoLocation = new GeoLocation(ViewEventActivity.this, ViewEventActivity.this, event.getLatitude(), event.getLongitude(), event.getRadius());
                                                            Log.d("ViewEventActivity", "Radius: " + event.getRadius());
                                                            if (!geoLocation.isLocationPermissionGranted()) {
                                                                geoLocation.requestLocationPermission();
                                                            } else {
                                                                proceedWithJoin(geoLocation);
                                                            }
                                                        } else {
                                                            addUserToWaitingList();
                                                        }
                                                    } else {
                                                        String activity = "ViewEventActivity";
                                                        Log.d("Checkpoint", "the user is null, we're going to registration");
                                                        Registration registration = new Registration();
                                                        Bundle bundle = new Bundle();
                                                        bundle.putString("eventID", eventID);
                                                        bundle.putString("activity", activity);
                                                        registration.setArguments(bundle);
                                                        getSupportFragmentManager()
                                                                .beginTransaction()
                                                                .replace(R.id.event_view, registration)
                                                                .addToBackStack(null)
                                                                .commit();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(String error) {
                                                    System.out.println("Failure" + error);
                                                }
                                            });
                                        });
                                    } else {
                                        joinButton.setText("Unjoin Waiting List");

                                        joinButton.setOnClickListener(view -> {

                                            Toast.makeText(ViewEventActivity.this, "You have left the waiting list", Toast.LENGTH_SHORT).show();

                                            // Remove it on the events collection
                                            ArrayList<Map<String, String>> newWaitingList = event.removeUserFromWaitingList(deviceID, event.getWaitinglist());
                                            event.setWaitinglist(newWaitingList);

                                            // Remove it on the user's collection
                                            waitlist.removeFromUserWL(deviceID, eventID, user);

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

                                            ArrayList<String> newEventsList = user.removeEventFromEventList(event.getEventID(), user.getEvents());
                                            user.setEvents(newEventsList);
                                            UserFirestore.editUserEvents(user);

                                            EventFirebase.editEvent(event);

                                            Intent intent = new Intent(ViewEventActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        });
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

        waitinglistButton.setOnClickListener(view -> {
            if (event.getWaitinglist().isEmpty()) {
                Toast.makeText(ViewEventActivity.this, "Waiting list is empty.", Toast.LENGTH_SHORT).show();
            }else{
                WaitlistFragment waitlistFragment = new WaitlistFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable("waitingListData", (ArrayList<Map<String, String>>) event.getWaitinglist());
                bundle.putString("eventCapacity", event.getCapacity());
                bundle.putString("eventID", event.getEventID());
                bundle.putSerializable("waitlist", waitlist);
                waitlistFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.event_view, waitlistFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        chosenEntrantsButton.setOnClickListener(view -> {
            waitlist.getChosen(eventID, chosen -> {
                if (chosen.isEmpty()) {
                    Toast.makeText(ViewEventActivity.this, "Chosen entrants list is empty.", Toast.LENGTH_SHORT).show();
                }else{
                    ChosenEntrants chosenEntrants = new ChosenEntrants();

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("chosenEntrantsData", chosen);
                    bundle.putString("eventID", event.getEventID());
                    bundle.putSerializable("waitlist", waitlist);
                    chosenEntrants.setArguments(bundle);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.event_view, chosenEntrants)
                            .addToBackStack(null)
                            .commit();
                }
            });
        });

        cancelledEntrantsButton.setOnClickListener(view -> {
            waitlist.getCancel(eventID, cancel -> {
                if (cancel.isEmpty()) {
                    Toast.makeText(ViewEventActivity.this, "Cancelled entrants list is empty.", Toast.LENGTH_SHORT).show();
                }else{
                    CancelledEntrants cancelledEntrants = new CancelledEntrants();

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("cancelledEntrantsData", cancel);
                    bundle.putString("eventID", event.getEventID());
                    bundle.putSerializable("waitlist", (Serializable) waitlist);
                    cancelledEntrants.setArguments(bundle);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.event_view, cancelledEntrants)
                            .addToBackStack(null)
                            .commit();
                }
            });
        });

        editButton.setOnClickListener(v -> {
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            uploadImageButton.setVisibility(View.VISIBLE);
            startDateButton.setVisibility(View.VISIBLE);
            endDateButton.setVisibility(View.VISIBLE);
            registrationDateButton.setVisibility(View.VISIBLE);



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
            registrationDateButton.setVisibility(View.GONE);
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
            event.setRegistrationDate(registrationDate);
            event.setEventPoster(newEventPoster);

            eventNameTextView.setText(newEventName);
            eventDescriptionTextView.setText(newDescription);
            eventCapacityTextView.setText(newEventCapacity);
            eventFacilityTextView.setText(facility);
            eventStartDateTextView.setText(dateFormat.format(startDate));
            eventEndDateTextView.setText(dateFormat.format(endDate));
            eventStartTimeTextView.setText(newStartTime);
            eventEndTimeTextView.setText(newEndTime);
            registrationDateTextView.setText(dateFormat.format(registrationDate));

            registrationDateTextView.setVisibility(View.VISIBLE);
            eventStartDateTextView.setVisibility(View.VISIBLE);
            eventEndDateTextView.setVisibility(View.VISIBLE);;
            eventStartTimeTextView.setVisibility(View.VISIBLE);
            eventEndTimeTextView.setVisibility(View.VISIBLE);

            registrationDateRequirementsTextView.setVisibility(View.GONE);
            dateRequirementsTextView.setVisibility(View.GONE);



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
            registrationDateButton.setVisibility(View.GONE);

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
                DatePickerDialog dialog = new DatePickerDialog(ViewEventActivity.this, new DatePickerDialog.OnDateSetListener() {
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

                            TimePickerDialog timePickerDialog = new TimePickerDialog(ViewEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

        registrationDateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(ViewEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        registrationDateCalendar = Calendar.getInstance();
                        registrationDateCalendar.set(selectedYear, selectedMonth, selectedDay);
                        Calendar currentDate = Calendar.getInstance();
                        if (startDate != null){
                            if (registrationDateCalendar.before(currentDate)) {
                                registrationDateRequirementsTextView.setText("Deadline Cannot Be Before Today.");
                                registrationDateRequirementsTextView.setVisibility(View.VISIBLE);
                                registrationDateTextView.setVisibility(View.GONE);
                                registrationDateCalendar = null;
                            }else if (startDate.before(registrationDateCalendar.getTime())) {
                                registrationDateRequirementsTextView.setText("Registration deadline must be before the event start date.");
                                registrationDateRequirementsTextView.setVisibility(View.VISIBLE);
                                registrationDateTextView.setVisibility(View.GONE);
                                registrationDateButton.setVisibility(View.VISIBLE);
                                registrationDateCalendar = null;
                            }else {
                                String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                                registrationDateTextView.setText(selectedDate);
                                registrationDateTextView.setVisibility(View.VISIBLE);
                                registrationDateRequirementsTextView.setVisibility(View.GONE);
                                registrationDate = registrationDateCalendar.getTime();
                            }
                        }else{
                            registrationDateRequirementsTextView.setText("Please Select Start Date.");
                            registrationDateRequirementsTextView.setVisibility(View.VISIBLE);
                            registrationDateTextView.setVisibility(View.GONE);
                            registrationDateCalendar = null;
                        }

                    }
                }, year, month, day);
                dialog.show();
            }
        });
    }

    /**
     * @author Derin Karas
     * Proceed with join after user's location is validated
     *
     * @param geoLocation Geolocation object of the user
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
     * Validate the user's location
     * @param geoLocation Geolocation object of the user
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
     */
    private void addUserToWaitingList() {
        ArrayList<String> eventsList = user.getEvents();
        eventsList.add(event.getEventID());
        waitlist.addToUserWL(deviceID, event.getEventID(), user);

        ArrayList<Map<String, String>> currentEntrants = event.getWaitinglist();

        if (event.getGeolocation()){
            Map<String, String> newEntrant = new HashMap<>();
            newEntrant.put("did", deviceID);
            newEntrant.put("latitude", String.valueOf(userLocation.getLatitude()));
            newEntrant.put("longitude", String.valueOf(userLocation.getLongitude()));
            newEntrant.put("status", "waiting");
            currentEntrants.add(newEntrant);
        } else {
            Map<String, String> newEntrant = new HashMap<>();
            newEntrant.put("did", deviceID);
            newEntrant.put("latitude", null);
            newEntrant.put("longitude", null);
            newEntrant.put("status", "waiting");
            currentEntrants.add(newEntrant);
            Log.d("Checkpoint", "Current Entrants: " + currentEntrants);
        }

        event.setWaitinglist(currentEntrants);


        EventFirebase.editEvent(event);
        Toast.makeText(this, "Joined Waiting List Successfully.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ViewEventActivity.this, FavouriteActivity.class);
        startActivity(intent);
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
                    newFacility = new FacilitiesInfo(address, facility, deviceID, newLatitude, newLongitude, newEventPoster);

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

                        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg"));

                        UCrop.of(imageUri, destinationUri)
                                .withAspectRatio(9, 16)
                                .withMaxResultSize(800, 1600)
                                .start(this);
                    }
                }
        );

        uploadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                eventPosterImageView.setVisibility(View.VISIBLE);
                eventPosterImageView.setImageURI(resultUri);

                StorageReference imageRef = storageRef.child("event_posters/" + UUID.randomUUID().toString() + ".jpg");

                imageRef.putFile(resultUri)
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

                eventPosterImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Log.e(TAG, "Crop error", cropError);
            }
        }
    }
}