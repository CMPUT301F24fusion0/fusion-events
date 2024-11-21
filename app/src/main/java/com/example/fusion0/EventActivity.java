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
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AuthorAttributions;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * @author Simon Haile
 * This activity allows users to create events
 */
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
    private String facilityImage;
    private Date startDate;
    private Date endDate;
    private String eventPoster;
    private Double latitude;
    private Double longitude;
    private Boolean geolocation = false;


    /**
     * Initializes the activity when it is first created. This method sets up the user interface
     * and prepares the necessary components for the event creation process. It handles view initialization,
     * Firebase setup, and event handling methods for the user to create an event.
     * @param savedInstanceState A Bundle object containing the activity's previously saved state,
     * or null if the activity is being created for the first time.
     */
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
        radius.setText("0");
        radiusText = findViewById(R.id.radius_text);


        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        validateUser();
        validateOrganizer();

        uploadPoster();

        geolocationHandling();

        StartDateButtonHandling();
        EndDateButtonHandling();

        AddEvent();

        exitButton.setOnClickListener(v -> finish());
    }


    private void validateUser() {
        LoginManagement login = new LoginManagement(this);
        login.isUserLoggedIn(isLoggedIn -> {
            if (!isLoggedIn) {
                Registration registration = new Registration();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.activity_add_event, registration)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

        /**
         * @author Simon Haile
         * Validates and retrieves the organizer associated with the current device ID.
         * This method checks if an organizer already exists in the database based on the device ID.
         * If the organizer is found, it is assigned to the `organizer` variable.
         * If no organizer is found, a new `OrganizerInfo` object is created and added to the database.
         */
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
    private void uploadPoster() {
        Button uploadImageButton = findViewById(R.id.upload_image_button);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadedImageView.setVisibility(View.VISIBLE);
                        uploadedImageView.setImageURI(imageUri);

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                uploadedImageView.setVisibility(View.VISIBLE);
                uploadedImageView.setImageURI(resultUri);
                uploadedImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Log.e(TAG, "Crop error", cropError);
            }
        }
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
    private void handleFacility(OrganizerInfo organizer) {
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
        spinnerFacilities.setAdapter(adapter);

        // Set the item selection listener for the spinner
        spinnerFacilities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
            Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), BuildConfig.API_KEY);
        }

        // Initialize PlacesClient after the API is enabled
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS));

        // Set up the PlaceSelectionListener
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

                final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);

                final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(place.getId(), fields);
                placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
                    Place placeDetails = response.getPlace();

                    // Get photo metadata
                    List<PhotoMetadata> metadata = placeDetails.getPhotoMetadatas();
                    if (metadata == null || metadata.isEmpty()) {
                        Log.w(TAG, "No photo metadata available for this place.");
                        return;
                    }

                    // Fetch photo URI
                    PhotoMetadata photoMetadata = metadata.get(0);
                    String attributions = photoMetadata.getAttributions();
                    AuthorAttributions authorAttributions = photoMetadata.getAuthorAttributions();

                    // Create and send photo request
                    FetchResolvedPhotoUriRequest photoRequest =
                            FetchResolvedPhotoUriRequest.builder(photoMetadata)
                                    .setMaxWidth(500)
                                    .setMaxHeight(300)
                                    .build();

                    placesClient.fetchResolvedPhotoUri(photoRequest)
                            .addOnSuccessListener((photoUriResponse) -> {
                                Uri photoUri = photoUriResponse.getUri();
                                if (photoUri != null) {
                                    Log.d(TAG, "Fetched photo URI: " + photoUri.toString());
                                    facilityImage = photoUri.toString();

                                    // Check if the facility name already exists in the list of facility names
                                    if (facilityNames.contains(facilityName)) {
                                        Log.i(TAG, "Facility already exists: " + facilityName);
                                        // Optionally show a message to the user
                                        Toast.makeText(getApplicationContext(), "This facility has already been added.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.d(TAG, " fetching photo URI: " + facilityImage);
                                        newFacility = new FacilitiesInfo(address, facilityName, deviceID, latitude, longitude, facilityImage);
                                        facility = newFacility;

                                        // Add the new facility name to the facilityNames list
                                        facilityNames.add(facilityName);

                                        // Notify the adapter that the data has changed
                                        adapter.notifyDataSetChanged();

                                        // Optionally, show a toast indicating the facility was added
                                        Toast.makeText(getApplicationContext(), "New facility added: " + facilityName, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.w(TAG, "Fetched photo URI is null.");
                                }
                            })
                            .addOnFailureListener(exception -> {
                                Log.e(TAG, "Error fetching photo URI: " + exception.getMessage());
                            });

                }).addOnFailureListener(exception -> {
                    Log.e(TAG, "Error fetching place details: " + exception.getMessage());
                });

            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    /**
     * @author Simon Haile
     * Handles geolocation switch compact. Sets geolocation to true if switch compact is checked.
     */
    private void geolocationHandling(){
        geolocationSwitchCompact.setOnCheckedChangeListener((buttonView, isChecked) -> {
            geolocation = isChecked;
            if (isChecked) {
                radiusText.setVisibility(View.VISIBLE);
                radius.setVisibility(View.VISIBLE);
            } else {
                radiusText.setVisibility(View.GONE);
                radius.setVisibility(View.GONE);
                radius.setText("0");
            }
        });
    }

    /**
     * @author Simon Haile
     * Handles the user interaction with the Start Date button. This method sets up a date picker dialog for selecting
     * the start date and time. It validates the selected date and time to ensure they are not in the past, and displays
     * appropriate error messages when necessary.
     */
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

    /**
     * @author Simon Haile
     * Handles the user interaction with the End Date button. This method sets up a date picker dialog for selecting
     * the end date and time. It ensures that the end date is not earlier than the start date and that the selected
     * end time is not before the start time.
     */
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

    /**
     * @author Simon Haile
     * Handles the addition of a new event. It validates the user input fields for event name, capacity, description,
     * start time, end time, and other required fields. If all fields are valid, the event is created and added to the
     * organizer's list of events and the facility's list of events. The event is also added to Firebase.
     */
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
            if (eventPoster == null) {
                Toast.makeText(EventActivity.this, "Event poster is missing", Toast.LENGTH_SHORT).show();
                return;
            }

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
                ArrayList<FacilitiesInfo> facilitiesList = organizer.getFacilities();
                facilitiesList.add(facility);
                organizer.setFacilities(facilitiesList);
                EventFirebase.editOrganizer(organizer);
            }

            EventFirebase.addEvent(newEvent);

            ArrayList<EventInfo> eventsList = organizer.getEvents();
            eventsList.add(newEvent);
            organizer.setEvents(eventsList);
            EventFirebase.editOrganizer(organizer);



            ArrayList<String> facilityEventsList = facility.getEvents();
            facilityEventsList.add(newEvent.eventID);
            facility.setEvents(facilityEventsList);
            EventFirebase.editFacility(facility);

            Toast.makeText(EventActivity.this, "Event Added Successfully!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(EventActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}