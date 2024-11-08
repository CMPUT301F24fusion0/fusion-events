package com.example.fusion0;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;


public class EventActivity extends AppCompatActivity {
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private static final String TAG = "EventActivity";
    private EditText eventName,description, capacity, radius;
    private androidx.fragment.app.FragmentContainerView autocompletePlaceFragment;
    private Calendar startDateCalendar;
    private TextView addFacilityText,dateRequirementsTextView, startDateTextView, startTimeTextView, endDateTextView, endTimeTextView, geolocationTextView, radiusText;
    private Button addButton, exitButton;
    private ImageView uploadedImageView;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Spinner spinnerFacilities;
    private SwitchCompat geolocationSwitchCompact;



    private OrganizerInfo organizer;
    private FacilitiesInfo facility;
    private FacilitiesInfo newFacility = null;


    private String deviceID;
    private String address;
    private String facilityName;
    private Date startDate;
    private Date endDate;
    private String eventPoster;
    private Double latitude;
    private Double longitude;
    private Boolean geolocation = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        eventName = findViewById(R.id.EventName);
        uploadedImageView = findViewById(R.id.uploaded_image_view);
        spinnerFacilities = findViewById(R.id.spinner_facilities);
        addFacilityText = findViewById(R.id.add_facility_text);
        autocompletePlaceFragment = findViewById(R.id.autocomplete_fragment);
        description = findViewById(R.id.Description);
        dateRequirementsTextView = findViewById(R.id.date_requirements_text);
        startDateTextView = findViewById(R.id.start_date_text);
        startTimeTextView = findViewById(R.id.start_time_text);
        endDateTextView = findViewById(R.id.end_date_text);
        endTimeTextView = findViewById(R.id.end_time_text);
        capacity = findViewById(R.id.Capacity);
        addButton = findViewById(R.id.add_button);
        exitButton = findViewById(R.id.exit_button);
        geolocationTextView = findViewById(R.id.geolocation_text);
        geolocationSwitchCompact = findViewById(R.id.geolocation_switchcompat);
        radius = findViewById(R.id.radius);
        radiusText = findViewById(R.id.radius_text);


        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        validateOrganizer();

        uploadPoster();

        geolocationHandling();

        StartDateButtonHandling();
        EndDateButtonHandling();

        AddEvent();
        ExitButtonHandling();
    }



    private void validateOrganizer() {
        EventFirebase.findOrganizer(deviceID, new EventFirebase.OrganizerCallback() {
            @Override
            public void onSuccess(OrganizerInfo organizerInfo) {
                if (organizerInfo == null) {
                    organizer = new OrganizerInfo(deviceID);
                    EventFirebase.addOrganizer(organizer);
                } else {
                    organizer = organizerInfo;
                }
                handleFacility(organizer);
            }
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error fetching organizer: " + error);
            }
        });
    }


    private void uploadPoster(){
        Button uploadImageButton = findViewById(R.id.upload_image_button);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadedImageView.setVisibility(View.VISIBLE);
                        uploadedImageView.setImageURI(imageUri);

                        StorageReference imageRef = storageRef.child("event_posters/" + UUID.randomUUID().toString() + ".jpg");

                        imageRef.putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        eventPoster = uri.toString();
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
    }

    private void handleFacility(OrganizerInfo organizer){
        ArrayList<String> facilityNames = new ArrayList<>();

        if (organizer.getFacilities() != null){
            ArrayList<FacilitiesInfo> facilities = organizer.getFacilities();
            for (FacilitiesInfo facility : facilities) {
                if (facility != null) {
                    facilityNames.add(facility.getFacilityName());
                } else {
                    Log.e(TAG, "Found a null facility in the list.");
                }
            }
        }

        facilityNames.add("Add Facility");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, facilityNames);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFacilities.setAdapter(adapter);

        spinnerFacilities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFacility = parent.getItemAtPosition(position).toString();
                if (selectedFacility.equals("Add Facility")){
                    addFacility();
                }else{
                    String facilityID = organizer.getFacilityIdByName(selectedFacility);
                    EventFirebase.findFacility(facilityID, new EventFirebase.FacilityCallback() {
                        @Override
                        public void onSuccess(FacilitiesInfo existingFacility) {
                            facility = existingFacility;
                            address = facility.getAddress();
                            facilityName = facility.getFacilityName();
                            longitude = facility.getLongitude();
                            latitude = facility.getLatitude();
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

            }
        });

    }

    private void addFacility(){
        autocompletePlaceFragment.setVisibility(View.VISIBLE);
        addFacilityText.setVisibility(View.VISIBLE);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                address = place.getFormattedAddress();
                facilityName = place.getDisplayName();

                LatLng latLng = place.getLatLng();

                if (latLng != null) {
                    latitude = latLng.latitude;
                    longitude = latLng.longitude;
                }

                newFacility = new FacilitiesInfo(address, facilityName, deviceID, latitude, longitude);
                facility = newFacility;
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void geolocationHandling(){
        geolocationSwitchCompact.setOnCheckedChangeListener((buttonView, isChecked) -> {
            geolocation = isChecked;
            radiusText.setVisibility(View.VISIBLE);
            radius.setVisibility(View.VISIBLE);
        });
    }

    private void StartDateButtonHandling() {
        Button startDateButton = findViewById(R.id.start_date_button);
        TextView startDateTextView = findViewById(R.id.start_date_text);
        TextView startTimeTextView = findViewById(R.id.start_time_text);
        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(com.example.fusion0.EventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        startDateCalendar = Calendar.getInstance();
                        startDateCalendar.set(selectedYear, selectedMonth, selectedDay);
                        Calendar currentDate = Calendar.getInstance();
                        if (startDateCalendar.before(currentDate)) {
                            dateRequirementsTextView.setText("Date Must Be Today or Later.");
                            dateRequirementsTextView.setVisibility(View.VISIBLE);
                            startDateTextView.setVisibility(View.GONE);
                            startDateCalendar = null;
                        } else {
                            String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                            startDateTextView.setText(selectedDate);
                            startDateTextView.setVisibility(View.VISIBLE);
                            dateRequirementsTextView.setVisibility(View.GONE);

                            startDate = startDateCalendar.getTime();


                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            TimePickerDialog timePickerDialog = new TimePickerDialog(com.example.fusion0.EventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                    startDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    startDateCalendar.set(Calendar.MINUTE, selectedMinute);

                                    Calendar currentTime = Calendar.getInstance();
                                    if (startDateCalendar.before(currentTime)) {
                                        dateRequirementsTextView.setText("Start Time Must Be Now or Later.");
                                        dateRequirementsTextView.setVisibility(View.VISIBLE);
                                        startTimeTextView.setVisibility(View.GONE);
                                    } else {
                                        String selectedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                                        startTimeTextView.setText(selectedTime);
                                        startTimeTextView.setVisibility(View.VISIBLE);
                                        dateRequirementsTextView.setVisibility(View.GONE);
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
    }
    private void EndDateButtonHandling() {
        Button endDateButton = findViewById(R.id.end_date_button);
        TextView endDateTextView = findViewById(R.id.end_date_text);
        TextView endTimeTextView = findViewById(R.id.end_time_text);
        // End Date Button
        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(com.example.fusion0.EventActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                            endDateTextView.setVisibility(View.GONE);
                            endTimeTextView.setVisibility(View.GONE);
                        } else if (endDateCalendar.before(currentDate)) {
                            dateRequirementsTextView.setText("End Date Must Be Today or Later.");
                            dateRequirementsTextView.setVisibility(View.VISIBLE);
                            endDateTextView.setVisibility(View.GONE);
                            endTimeTextView.setVisibility(View.GONE);
                        } else if (endDateCalendar.before(startDateCalendar)) {
                            endDateTextView.setVisibility(View.GONE);
                            endTimeTextView.setVisibility(View.GONE);
                            dateRequirementsTextView.setText("End Date Must Be On or After Start Date.");
                            dateRequirementsTextView.setVisibility(View.VISIBLE);
                        } else {
                            String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                            endDateTextView.setText(selectedDate);
                            endDateTextView.setVisibility(View.VISIBLE);
                            dateRequirementsTextView.setVisibility(View.GONE);

                            endDate = endDateCalendar.getTime();

                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            TimePickerDialog timePickerDialog = new TimePickerDialog(com.example.fusion0.EventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                    endDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    endDateCalendar.set(Calendar.MINUTE, selectedMinute);

                                    if (endDateCalendar.before(startDateCalendar)) {
                                        dateRequirementsTextView.setText("End Time Must Be After Start Time.");
                                        dateRequirementsTextView.setVisibility(View.VISIBLE);
                                        endTimeTextView.setVisibility(View.GONE);
                                    } else {
                                        String selectedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                                        endTimeTextView.setText(selectedTime);
                                        endTimeTextView.setVisibility(View.VISIBLE);
                                        dateRequirementsTextView.setVisibility(View.GONE);
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
    }
    private void setDateRequirements(String message, TextView textView, boolean hideOtherTextViews) {
        dateRequirementsTextView.setText(message);
        dateRequirementsTextView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
        if (hideOtherTextViews) {
            startTimeTextView.setVisibility(View.GONE);
            endTimeTextView.setVisibility(View.GONE);
        }
    }


    private void AddEvent(){
        addButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(eventName.getText().toString())) {
                eventName.setError("Event name is required");
                Toast.makeText(EventActivity.this, "Event name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(capacity.getText().toString())) {
                capacity.setError("Capacity is required");
                Toast.makeText(EventActivity.this, "Capacity is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(description.getText().toString())) {
                description.setError("Description is required");
                Toast.makeText(EventActivity.this, "Description is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(startTimeTextView.getText().toString())) {
                startTimeTextView.setError("Start time is required");
                Toast.makeText(EventActivity.this, "Start time is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(endTimeTextView.getText().toString())) {
                endTimeTextView.setError("End time is required");
                Toast.makeText(EventActivity.this, "End time is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startDate == null || endDate == null) {
                Toast.makeText(EventActivity.this, "Start or End date is missing", Toast.LENGTH_SHORT).show();
                return;
            }
            /*if (eventPoster == null) {
                Toast.makeText(EventActivity.this, "Event poster is missing", Toast.LENGTH_SHORT).show();
                return;
            }
*/
            if (facilityName == null || facility == null) {
                Toast.makeText(EventActivity.this, "Facility name or facility is missing", Toast.LENGTH_SHORT).show();
                return;
            }

            EventInfo newEvent = null;

            try {
                newEvent = new EventInfo(
                        deviceID,
                        eventName.getText().toString(),
                        address,
                        facilityName,
                        capacity.getText().toString(),
                        description.getText().toString(),
                        startDate,
                        endDate,
                        startTimeTextView.getText().toString(),
                        endTimeTextView.getText().toString(),
                        eventPoster,
                        geolocation,
                        longitude,
                        latitude,
                        Integer.parseInt(radius.getText().toString()) * 1000
                );
            } catch (WriterException e) {
                throw new RuntimeException(e);
            }

            if (newFacility != null){
                EventFirebase.addFacility(newFacility);
            }

            EventFirebase.addEvent(newEvent);

            ArrayList<EventInfo> eventsList = organizer.getEvents();
            eventsList.add(newEvent);
            organizer.setEvents(eventsList);
            EventFirebase.editOrganizer(organizer);


            ArrayList<FacilitiesInfo> facilitiesList = organizer.getFacilities();
            facilitiesList.add(facility);
            organizer.setFacilities(facilitiesList);
            EventFirebase.editOrganizer(organizer);

            ArrayList<String> facilityEventsList = facility.getEvents();
            facilityEventsList.add(newEvent.eventID);
            facility.setEvents(facilityEventsList);
            EventFirebase.editFacility(facility);

            Intent intent = new Intent(EventActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void ExitButtonHandling() {

        exitButton.setOnClickListener(v -> finish());
    }
}