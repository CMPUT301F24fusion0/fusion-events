package com.example.fusion0;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.api.Places;
import com.google.zxing.WriterException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class EventActivity extends AppCompatActivity {
    private static final String TAG = "EventActivity";
    private EditText eventName;
    private TextView addFacilityText;
    private androidx.fragment.app.FragmentContainerView autocompletePlaceFragment;
    private EditText description;
    private Calendar startDateCalendar;
    private TextView dateRequirementsTextView;
    private TextView startDateTextView;
    private TextView startTimeTextView;
    private TextView endDateTextView;
    private TextView endTimeTextView;
    private EditText capacity;
    private Button addButton;
    private Button exitButton;
    private ImageView uploadedImageView;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Spinner spinnerFacilities;
    private OrganizerInfo organizer;
    private FacilitiesInfo facility;

    private String deviceID;
    private String address;
    private String facilityName;
    private Date startDate;
    private Date endDate;
    private String startTime;
    private String endTime;
    private Uri eventPoster;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

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

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        validateOrganizer();
        uploadPoster();
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
                        eventPoster = imageUri;
                    }
                }
        );

        uploadImageButton.setOnClickListener(v ->{
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);});
    }

    private void handleFacility(OrganizerInfo organizer){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, organizer.getFacilities());

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFacilities.setAdapter(adapter);

        spinnerFacilities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFacility = parent.getItemAtPosition(position).toString();
                if (selectedFacility.equals("Add Facility")){
                    addFacility();
                }else{
                    EventFirebase.findFacility(selectedFacility, new EventFirebase.FacilityCallback() {
                        @Override
                        public void onSuccess(FacilitiesInfo existingFacility) {
                            facility = existingFacility;
                            address = facility.getAddress();
                            facilityName = facility.getFacilityName();
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
            Places.initialize(getApplicationContext(), "AIzaSyDinZhBZ1IaUO8Rcxqq5Tsli7tKnsJhyzg");
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.FORMATTED_ADDRESS));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                address = place.getFormattedAddress();
                facilityName = place.getDisplayName();
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
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
                        eventPoster
                );
            } catch (WriterException e) {
                throw new RuntimeException(e);
            }

            EventFirebase.addEvent(newEvent);
            ArrayList<String> eventsList = organizer.getEvents();
            eventsList.add(newEvent.eventID);
            organizer.setEvents(eventsList);


            ArrayList<String> facilitiesList = organizer.getFacilities();
            facilitiesList.add(facilityName);
            organizer.setFacilities(facilitiesList);
        });
    }

    private void ExitButtonHandling() {
        exitButton.setOnClickListener(v -> finish());
    }
}