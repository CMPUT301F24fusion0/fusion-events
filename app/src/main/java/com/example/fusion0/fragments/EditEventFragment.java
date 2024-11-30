package com.example.fusion0.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.fusion0.BuildConfig;
import com.example.fusion0.R;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.QRCode;
import com.example.fusion0.models.EventInfo;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment for editing an event's details such as name, description, capacity, geolocation,
 * start/end dates, event poster, and QR code. Users can upload/delete posters,
 * generate/delete QR codes, and save changes.
 */
public class EditEventFragment extends Fragment {

    private EditText eventName, description, capacity, radiusInput;
    private TextView startDateTextView, endDateTextView, locationTextView, radiusLabel, addPosterText;
    private ImageView uploadedPosterView, qrCodeImageView;
    private ImageButton editPosterButton, deletePosterButton;
    private Button saveButton, cancelButton, generateQrCodeButton, deleteQrCodeButton;
    private androidx.appcompat.widget.SwitchCompat geolocationSwitch;

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
    private EventFirebase eventFirebase = new EventFirebase();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_event, container, false);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        initializeViews(view);
        initializeImagePicker();
        initializeGooglePlaces();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        loadEventDetails();
        return view;
    }

    private void initializeViews(View view) {
        eventName = view.findViewById(R.id.EventName);
        description = view.findViewById(R.id.Description);
        capacity = view.findViewById(R.id.Capacity);
        radiusInput = view.findViewById(R.id.radius_input);
        radiusLabel = view.findViewById(R.id.radius_label);
        geolocationSwitch = view.findViewById(R.id.geolocation_switch);
        startDateTextView = view.findViewById(R.id.start_date_text);
        endDateTextView = view.findViewById(R.id.end_date_text);
        locationTextView = view.findViewById(R.id.location_text_view);
        uploadedPosterView = view.findViewById(R.id.uploaded_image_view);
        qrCodeImageView = view.findViewById(R.id.event_qr_code_image);
        addPosterText = view.findViewById(R.id.add_poster_text);
        editPosterButton = view.findViewById(R.id.upload_image_button);
        deletePosterButton = view.findViewById(R.id.delete_image_button);
        generateQrCodeButton = view.findViewById(R.id.generate_qr_code_button);
        deleteQrCodeButton = view.findViewById(R.id.delete_qr_code_button);
        saveButton = view.findViewById(R.id.add_button);
        cancelButton = view.findViewById(R.id.exit_button);

        radiusInput.setVisibility(View.GONE);
        radiusLabel.setVisibility(View.GONE);

        saveButton.setOnClickListener(v -> {
            saveEventDetails();
            Navigation.findNavController(v).navigateUp();
        });

        cancelButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());



        geolocationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleGeolocation(isChecked));

        editPosterButton.setOnClickListener(v -> pickImage());
        deletePosterButton.setOnClickListener(v -> removePoster());
        generateQrCodeButton.setOnClickListener(v -> generateQRCode());
        deleteQrCodeButton.setOnClickListener(v -> deleteQRCode());
    }

    private void initializeImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadedPosterView.setImageURI(imageUri);
                        uploadPosterToFirebase(imageUri);
                    }
                }
        );
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void initializeGooglePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.API_KEY);
        }

        // Retrieve the fragment by ID
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                requireActivity().getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment == null) {
            //Toast.makeText(requireContext(), "Error initializing Autocomplete fragment.", Toast.LENGTH_SHORT).show();
            return;
        }

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                eventAddress = place.getFormattedAddress();
                eventLatLng = place.getLatLng();
                locationTextView.setText(eventAddress);
                Toast.makeText(requireContext(), "Location updated to: " + eventAddress, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(requireContext(), "Error selecting place: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadEventDetails() {
        eventFirebase.findEvent(eventId, new EventFirebase.EventCallback() {
            @Override
            public void onSuccess(EventInfo eventInfo) {
                if (eventInfo != null) {
                    event = eventInfo;
                    populateFields(event);
                } else {
                    Toast.makeText(requireContext(), "Event not found.", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), "Failed to load event: " + error, Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

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

        if (event.getQrCode() != null) {
            try {
                Bitmap qrBitmap = new QRCode(event.getQrCode()).getQrImage();
                qrCodeImageView.setImageBitmap(qrBitmap);
                qrCodeImageView.setVisibility(View.VISIBLE);
                deleteQrCodeButton.setVisibility(View.VISIBLE);
            } catch (WriterException e) {
                Toast.makeText(getContext(), "Failed to load QR Code.", Toast.LENGTH_SHORT).show();
            }
        }

        startDate = event.getStartDate();
        endDate = event.getEndDate();
    }

    private void updatePosterVisibility(boolean hasPoster) {
        uploadedPosterView.setVisibility(hasPoster ? View.VISIBLE : View.GONE);
        addPosterText.setVisibility(hasPoster ? View.GONE : View.VISIBLE);
        deletePosterButton.setVisibility(hasPoster ? View.VISIBLE : View.GONE);
    }

    private void toggleGeolocation(boolean isEnabled) {
        geolocationEnabled = isEnabled;
        radiusInput.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
        radiusLabel.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
    }

    private void uploadPosterToFirebase(Uri imageUri) {
        StorageReference imageRef = storageRef.child("event_posters/" + eventId + ".jpg");
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    eventPosterUrl = uri.toString();
                    updatePosterVisibility(true);
                }))
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to upload poster.", Toast.LENGTH_SHORT).show());
    }

    private void removePoster() {
        if (eventPosterUrl != null && !eventPosterUrl.isEmpty()) {
            posterMarkedForDeletion = true;
            updatePosterVisibility(false);
        } else {
            Toast.makeText(requireContext(), "No poster to remove.", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateQRCode() {
        try {
            QRCode newQrCode = new QRCode(event.getEventID());
            event.setQrCode(newQrCode.getQrCode());
            Bitmap qrBitmap = newQrCode.getQrImage();
            qrCodeImageView.setImageBitmap(qrBitmap);
            qrCodeImageView.setVisibility(View.VISIBLE);
            deleteQrCodeButton.setVisibility(View.VISIBLE);
        } catch (WriterException e) {
            Toast.makeText(requireContext(), "Error generating QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteQRCode() {
        event.setQrCode(null);
        qrCodeImageView.setVisibility(View.GONE);
        deleteQrCodeButton.setVisibility(View.GONE);
    }

    private void saveEventDetails() {
        String updatedName = eventName.getText().toString().trim();
        String updatedDescription = description.getText().toString().trim();
        String updatedCapacity = capacity.getText().toString().trim();

        if (TextUtils.isEmpty(updatedName) || TextUtils.isEmpty(updatedDescription) || TextUtils.isEmpty(updatedCapacity)) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDate == null || endDate == null || endDate.before(startDate)) {
            Toast.makeText(requireContext(), "Please select valid start and end dates.", Toast.LENGTH_SHORT).show();
            return;
        }

        String radiusStr = radiusInput.getText().toString().trim();
        if (geolocationEnabled && (TextUtils.isEmpty(radiusStr) || !radiusStr.matches("\\d+"))) {
            Toast.makeText(requireContext(), "Please enter a valid radius.", Toast.LENGTH_SHORT).show();
            return;
        }

        eventRadius = geolocationEnabled ? Integer.parseInt(radiusStr) * 1000 : 0;

        if (posterMarkedForDeletion) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(eventPosterUrl);
            imageRef.delete().addOnSuccessListener(unused -> {
            }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to delete poster: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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

        eventFirebase.editEvent(event);
        Toast.makeText(requireContext(), "Event updated successfully.", Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager().popBackStack();
    }

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
