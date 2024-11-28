package com.example.fusion0.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fusion0.BuildConfig;
import com.example.fusion0.R;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.EventInfo;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for editing event details such as name, description, capacity, geolocation,
 * start/end dates, and poster. Includes functionality for uploading, updating, and
 * removing the event poster.
 */
public class EditEventActivity extends AppCompatActivity {

    private EditText eventName, description, capacity, radiusInput;
    private TextView startDateTextView, endDateTextView, locationTextView, radiusLabel, addPosterText;
    private ImageView uploadedPosterView;
    private ImageButton editPosterButton, deletePosterButton;
    private Button saveButton, cancelButton;
    private SwitchCompat geolocationSwitch;

    private EventInfo event;
    private String eventId, eventAddress;
    private LatLng eventLatLng;
    private Date startDate, endDate;
    private String eventPosterUrl;
    private Integer eventRadius;
    private boolean geolocationEnabled = false;
    private boolean posterMarkedForDeletion = false;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    /**
     * Called when the activity is created. Initializes UI components and sets up
     * event handling for buttons and switches.
     *
     * @param savedInstanceState The saved instance state of the activity.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        eventName = findViewById(R.id.EventName);
        description = findViewById(R.id.Description);
        capacity = findViewById(R.id.Capacity);
        radiusInput = findViewById(R.id.radius_input);
        radiusLabel = findViewById(R.id.radius_label);
        geolocationSwitch = findViewById(R.id.geolocation_switch);
        startDateTextView = findViewById(R.id.start_date_text);
        endDateTextView = findViewById(R.id.end_date_text);
        locationTextView = findViewById(R.id.location_text_view);
        uploadedPosterView = findViewById(R.id.uploaded_image_view);
        addPosterText = findViewById(R.id.add_poster_text);
        editPosterButton = findViewById(R.id.upload_image_button);
        deletePosterButton = findViewById(R.id.delete_image_button);
        saveButton = findViewById(R.id.add_button);
        cancelButton = findViewById(R.id.exit_button);

        radiusInput.setVisibility(View.GONE);
        radiusLabel.setVisibility(View.GONE);

        if (getIntent() != null) {
            eventId = getIntent().getStringExtra("eventId");
        }

        loadEventDetails();

        saveButton.setOnClickListener(v -> saveEventDetails());
        cancelButton.setOnClickListener(v -> finish());
        initializePosterEdit();
        initializePosterDelete();
        initializeGooglePlaces();

        geolocationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleGeolocation(isChecked));
    }

    /**
     * Loads event details from Firebase and populates the UI.
     */
    private void loadEventDetails() {
        EventFirebase.findEvent(eventId, new EventFirebase.EventCallback() {
            @Override
            public void onSuccess(EventInfo eventInfo) {
                if (eventInfo != null) {
                    event = eventInfo;
                    populateFields(event);
                } else {
                    Toast.makeText(EditEventActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(EditEventActivity.this, "Failed to load event: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Populates UI fields with event data.
     *
     * @param event The event data to populate.
     */
    private void populateFields(EventInfo event) {
        eventName.setText(event.getEventName());
        description.setText(event.getDescription());
        capacity.setText(event.getCapacity());
        startDateTextView.setText(formatDateTime(event.getStartDate()));
        endDateTextView.setText(formatDateTime(event.getEndDate()));

        eventAddress = event.getAddress();
        eventLatLng = new LatLng(event.getLatitude(), event.getLongitude());
        locationTextView.setText(eventAddress != null ? eventAddress : "No location set");

        geolocationEnabled = event.getGeolocation() != null && event.getGeolocation();
        geolocationSwitch.setChecked(geolocationEnabled);
        eventRadius = event.getRadius();
        if (eventRadius != null) {
            radiusInput.setText(String.valueOf(eventRadius / 1000));
        }

        toggleGeolocation(geolocationEnabled);

        eventPosterUrl = event.getEventPoster();
        if (eventPosterUrl != null) {
            Glide.with(this).load(eventPosterUrl).into(uploadedPosterView);
            updatePosterVisibility(true);
        } else {
            updatePosterVisibility(false);
        }

        startDate = event.getStartDate();
        endDate = event.getEndDate();
    }

    /**
     * Toggles visibility of geolocation-related inputs based on the geolocation switch state.
     *
     * @param isEnabled True if geolocation is enabled, false otherwise.
     */
    private void toggleGeolocation(boolean isEnabled) {
        geolocationEnabled = isEnabled;
        radiusInput.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
        radiusLabel.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
    }

    /**
     * Updates visibility of poster-related UI elements.
     *
     * @param hasPoster True if a poster is present, false otherwise.
     */
    private void updatePosterVisibility(boolean hasPoster) {
        uploadedPosterView.setVisibility(hasPoster ? View.VISIBLE : View.GONE);
        addPosterText.setVisibility(hasPoster ? View.GONE : View.VISIBLE);
        deletePosterButton.setVisibility(hasPoster ? View.VISIBLE : View.GONE);
    }

    /**
     * Initializes Google Places autocomplete fragment for location selection.
     */
    private void initializeGooglePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.API_KEY);
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                eventAddress = place.getFormattedAddress();
                eventLatLng = place.getLatLng();
                locationTextView.setText(eventAddress);
                Toast.makeText(EditEventActivity.this, "Location updated to: " + eventAddress, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(EditEventActivity.this, "Error selecting place: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Initializes functionality for editing (uploading) the poster.
     */
    private void initializePosterEdit() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadedPosterView.setImageURI(imageUri);

                        StorageReference imageRef = storageRef.child("event_posters/" + eventId + ".jpg");
                        imageRef.putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    eventPosterUrl = uri.toString();
                                    updatePosterVisibility(true);
                                }))
                                .addOnFailureListener(e -> Toast.makeText(EditEventActivity.this, "Failed to upload poster.", Toast.LENGTH_SHORT).show());
                    }
                }
        );

        editPosterButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }

    /**
     * Initializes functionality for marking a poster for deletion.
     */
    private void initializePosterDelete() {
        deletePosterButton.setOnClickListener(v -> removePoster());
    }

    /**
     * Marks the poster for deletion. The deletion is finalized when the event is saved.
     */
    private void removePoster() {
        if (eventPosterUrl != null && !eventPosterUrl.isEmpty()) {
            posterMarkedForDeletion = true;
            updatePosterVisibility(false);
        } else {
            Toast.makeText(this, "No poster to remove.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
 * Saves updated event details, including handling of poster deletion.
     */
    private void saveEventDetails() {
        String updatedName = eventName.getText().toString().trim();
        String updatedDescription = description.getText().toString().trim();
        String updatedCapacity = capacity.getText().toString().trim();

        if (TextUtils.isEmpty(updatedName) || TextUtils.isEmpty(updatedDescription) || TextUtils.isEmpty(updatedCapacity)) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDate == null || endDate == null || endDate.before(startDate)) {
            Toast.makeText(this, "Please select valid start and end dates.", Toast.LENGTH_SHORT).show();
            return;
        }

        String radiusStr = radiusInput.getText().toString().trim();
        if (geolocationEnabled && (TextUtils.isEmpty(radiusStr) || !radiusStr.matches("\\d+"))) {
            Toast.makeText(this, "Please enter a valid radius.", Toast.LENGTH_SHORT).show();
            return;
        }

        eventRadius = geolocationEnabled ? Integer.parseInt(radiusStr) * 1000 : 0;

        if (posterMarkedForDeletion) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(eventPosterUrl);
            imageRef.delete().addOnSuccessListener(unused -> {
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to delete poster: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
            eventPosterUrl = null;
        }

        event.setEventName(updatedName);
        event.setDescription(updatedDescription);
        event.setCapacity(updatedCapacity);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setAddress(eventAddress);
        event.setLatitude(eventLatLng != null ? eventLatLng.latitude : null);
        event.setLongitude(eventLatLng != null ? eventLatLng.longitude : null);
        event.setEventPoster(eventPosterUrl);
        event.setRadius(eventRadius);
        event.setGeolocation(geolocationEnabled);

        EventFirebase.editEvent(event);
        Toast.makeText(this, "Event updated successfully.", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Formats a Date object into a readable string.
     *
     * @param date The date to format.
     * @return A formatted date string.
     */
    private String formatDateTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return String.format(Locale.US, "%d/%d/%d %02d:%02d",
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }
}
