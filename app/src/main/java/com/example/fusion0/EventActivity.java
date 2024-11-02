package com.example.fusion0;


import java.util.HashMap;




//import java.awt.Image;
//import android.media.Image;


import java.util.Date;
import java.sql.Time;
import java.util.ArrayList;




public class EventActivity {
    private String organizer;
    private String eventName;
    private String address;
    private String facilityName;
    private Integer capacity;
    //private Image eventPoster;
    private float distance;
    private Date startDate;
    private Date endDate;
    private Time startTime;
    private Time endTime;
    ArrayList<String> entrants;


    public EventActivity(String organizer, String eventName, String address, String facilityName, Integer capacity, float distance, Date startDate, Date endDate, Time startTime, Time endTime, ArrayList<String> entrants) {
        this.organizer = organizer;
        this.eventName = eventName;
        this.address = address;
        this.facilityName = facilityName;
        this.capacity = capacity;
        this.distance = distance;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.entrants = entrants;
    }


    public HashMap<String, Object> event() {
        HashMap<String, Object> event = new HashMap<>();


        event.put("organizer", this.organizer);
        event.put("eventName", this.eventName);
        event.put("address", this.address);
        event.put("facilityName", this.facilityName);
        event.put("capacity", this.capacity);
        event.put("distance", this.distance);
        event.put("startDate", this.startDate);
        event.put("endDate", this.endDate);
        event.put("startTime", this.startTime);
        event.put("endTime", this.endTime);
        event.put("entrants", this.entrants);


        return event;
    }


    public String getOrganizer() {
        return organizer;
    }


    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }


    public String getEventName() {


        return eventName;
    }


    public void setEventName(String eventName) {


        this.eventName = eventName;
    }


    public String getAddress() {


        return address;
    }


    public void setAddress(String address) {


        this.address = address;
    }


    public String getFacilityName() {


        return facilityName;
    }


    public void setFacilityName(String facilityName) {


        this.facilityName = facilityName;
    }


    public Integer getCapacity() {


        return capacity;
    }


    public void setCapacity(Integer capacity) {


        this.capacity = capacity;
    }


    public float getDistance() {


        return distance;
    }


    public void setDistance(float distance) {


        this.distance = distance;
    }


    public Date getStartDate() {


        return startDate;
    }


    public void setStartDate(Date startDate) {


        this.startDate = startDate;
    }


    public Date getEndDate() {


        return endDate;
    }


    public void setEndDate(Date endDate) {


        this.endDate = endDate;
    }


    public Time getStartTime() {


        return startTime;
    }
    public void setStartTime(Time startTime) {


        this.startTime = startTime;
    }


    public Time getEndTime() {


        return endTime;
    }


    public void setEndTime(Time endTime) {


        this.endTime = endTime;
    }




    public ArrayList<String> getEntrants() {
        return entrants;
    }


    public void setEntrants(ArrayList<String> entrants) {
        this.entrants = entrants;
    }


}

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import android.widget.TextView;
import android.widget.TimePicker;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventActivity extends AppCompatActivity{
    private static final String TAG = "EventActivity";

    private EditText eventName;
    private EditText description;
    private Calendar startDateCalendar;
    private static final String TAG = "EventActivity";
    private TextView dateRequirementsTextView;
    private TextView startDateTextView;
    private TextView startTimeTextView;
    private TextView endDateTextView;
    private TextView endTimeTextView;
    private static final String TAG = "EventActivity";
    private EditText capacity;
    private Button addButton;
    private Button exitButton;
    private ImageView uploadedImageView;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);


        eventName = findViewById(R.id.EventName);
        uploadedImageView = findViewById(R.id.uploaded_image_view);

        eventName = findViewById(R.id.EventName);

        description = findViewById(R.id.Description);
        dateRequirementsTextView = findViewById(R.id.date_requirements_text);
        startDateTextView = findViewById(R.id.start_date_text);
        startTimeTextView = findViewById(R.id.start_time_text);
        endDateTextView = findViewById(R.id.end_date_text);
        endTimeTextView = findViewById(R.id.end_time_text);
        capacity = findViewById(R.id.Capacity);
        addButton = findViewById(R.id.add_button);
        exitButton = findViewById(R.id.exit_button);


        uploadImage();
        newFacility();
        StartDateButtonHandling();
        EndDateButtonHandling();
        AddEvent();
        ExitButtonHandling();
    }

    private void uploadImage(){
        Button uploadImageButton = findViewById(R.id.upload_image_button);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadedImageView.setVisibility(View.VISIBLE);
                        uploadedImageView.setImageURI(imageUri);
                    }
                }
        );

        uploadImageButton.setOnClickListener(v ->{
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);});
    }

    private void newFacility(){
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "API KEY");
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
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

                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            TimePickerDialog timePickerDialog = new TimePickerDialog(com.example.fusion0.EventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                    startDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    startDateCalendar.set(Calendar.MINUTE, selectedMinute);
                                    // Check if start time is before current time
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

                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            TimePickerDialog timePickerDialog = new TimePickerDialog(com.example.fusion0.EventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                    endDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    endDateCalendar.set(Calendar.MINUTE, selectedMinute);
                                    // Check if end time is after or equal to start time
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
    }
    private void ExitButtonHandling() {
        exitButton.setOnClickListener(v -> finish());
    }
}

