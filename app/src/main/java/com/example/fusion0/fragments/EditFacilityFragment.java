package com.example.fusion0.fragments;


import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.fusion0.R;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.FacilitiesInfo;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Edit a facility
 * @author Simon Haile
 */
public class EditFacilityFragment extends Fragment {

    private String deviceID;
    private TextView facilityNameTextView, addressTextView, facilitiesEventsTextView;

    private EditText facilityNameEditText, addressEditText;
    private FacilitiesInfo facility;
    private ImageView facilityImageView;
    private Boolean isOwner = false;
    private LinearLayout toolbar;
    private ImageButton backButton;
    private Button editButton, saveButton, deleteButton, cancelButton;
    private ListView facilitiesEventsList;
    private EventFirebase eventFirebase = new EventFirebase();
    private StorageReference storageRef;


    /**
     * Initialize UI components
     * @author Simon Haile
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return views
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.facility_view, container, false);
        storageRef = FirebaseStorage.getInstance().getReference();

        // Initialize UI components
        facilityNameTextView = view.findViewById(R.id.facilityName);
        addressTextView = view.findViewById(R.id.address);
        facilityImageView = view.findViewById(R.id.facilityImage);

        facilitiesEventsTextView = view.findViewById(R.id.facilities_events_list_text);
        facilityNameEditText = view.findViewById(R.id.editFacilityName);
        addressEditText = view.findViewById(R.id.editAddress);
        toolbar = view.findViewById(R.id.toolbar);

        facilitiesEventsList = view.findViewById(R.id.facilities_events_list);

        backButton = view.findViewById(R.id.backButton);
        editButton = view.findViewById(R.id.edit_button);
        saveButton = view.findViewById(R.id.save_button);
        deleteButton = view.findViewById(R.id.delete_button);
        cancelButton = view.findViewById(R.id.cancel_button);

        // Get device ID
        deviceID = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up back button navigation
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());


        // Get facility ID from arguments
        if (getArguments() != null) {
            String facilityID = getArguments().getString("facilityID");
            fetchFacilityData(facilityID);
        } else {
            Toast.makeText(requireContext(), "No Facility ID provided.", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }

        return view;
    }

    /**
     * Get facility data
     * @author Simon Haile
     * @param facilityID id of facility
     */
    private void fetchFacilityData(String facilityID) {
        eventFirebase.findFacility(facilityID, new EventFirebase.FacilityCallback() {
            @Override
            public void onSuccess(FacilitiesInfo facilitiesInfo) {
                if (facilitiesInfo == null) {
                    Toast.makeText(requireContext(), "Facility Unavailable.", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                } else {
                    facility = facilitiesInfo;
                    populateFacilityDetails();
                }
            }

            @Override
            public void onFailure(String error) {
                //Log.e(TAG, "Error fetching facility data: " + error);

                Toast.makeText(requireContext(), "Failed to load facility data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Add in facility details
     * @author Simon Haile
     */
    private void populateFacilityDetails() {
        facilityNameTextView.setText(facility.getFacilityName());
        addressTextView.setText(facility.getAddress());

        if (facility.getFacilityImage() != null && !facility.getFacilityImage().isEmpty()) {
            Glide.with(requireContext())
                    .load(facility.getFacilityImage())
                    .into(facilityImageView);
            facilityImageView.setVisibility(View.VISIBLE);
        }

        checkOwnershipAndAdminStatus();

        ArrayList<String> eventNames = new ArrayList<>();
        ArrayAdapter<String> eventsAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, eventNames);
        facilitiesEventsList.setAdapter(eventsAdapter);

        if (facility.getEvents() != null && !facility.getEvents().isEmpty()) {
            ArrayList<String> filteredEvents = new ArrayList<>();
            for (String event : facility.getEvents()) {
                eventFirebase.findEvent(event, new EventFirebase.EventCallback() {
                    @Override
                    public void onSuccess(EventInfo eventInfo) {
                        if (eventInfo != null) {
                            filteredEvents.add(eventInfo.getEventID());
                            eventNames.add(eventInfo.getEventName());
                            eventsAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        //Log.e(TAG, "Error fetching event data: " + error);
                    }
                });
            }

            facility.setEvents(filteredEvents);

        } else {
            facilitiesEventsTextView.setVisibility(View.VISIBLE);
            facilitiesEventsList.setVisibility(View.GONE);
        }

        setupButtons();
    }

    /**
     * Check check if the user is admin
     * @author Simon Haile
     */
    private void checkOwnershipAndAdminStatus() {
        //isOwner = deviceID.equals(facility.getOwner()) || EventFirebase.isDeviceIDAdmin(deviceID);
        toolbar.setVisibility(View.VISIBLE);

    }

    /**
     * Setup buttons based on context
     * @author Simon Haile
     */
    private void setupButtons() {
        editButton.setOnClickListener(v -> {
                toggleEditMode(true);

        });

        saveButton.setOnClickListener(v -> {
                facility.setFacilityName(facilityNameEditText.getText().toString());
                facility.setAddress(addressEditText.getText().toString());
                eventFirebase.editFacility(facility);
                toggleEditMode(false);
                populateFacilityDetails();
                Toast.makeText(requireContext(), "Facility details updated.", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(v).navigateUp(); // Navigate up after deletion


        });

        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Facility")
                    .setMessage("Are you sure you want to delete this facility?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        eventFirebase.deleteFacility(facility.getFacilityID());
                        Toast.makeText(requireContext(), "Facility deleted successfully.", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(v).navigateUp(); // Navigate up after deletion
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });

        cancelButton.setOnClickListener(v -> toggleEditMode(false));
    }

    /**
     * Allow organizer to edit
     * @author Simon Haile
     * @param isEditing whether editing is allowed
     */
    private void toggleEditMode(boolean isEditing) {
        facilityNameEditText.setText(facilityNameTextView.getText());
        addressEditText.setText(addressTextView.getText());

        facilityNameTextView.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        addressTextView.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        facilityNameEditText.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        addressEditText.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        saveButton.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        cancelButton.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        editButton.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        deleteButton.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        facilityImageView.setOnClickListener(v -> {
            // Prompt user to delete image, if yes set image to null and hide image view
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this facility's image?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        Uri imageUri = drawableToUri(requireContext(), R.drawable.image_unavailable);

                        // Create a unique filename for Firebase Storage
                        String fileName = "facility_images/" + UUID.randomUUID().toString() + ".jpg";
                        StorageReference imageRef = storageRef.child(fileName);

                        imageRef.putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        facility.setFacilityImage(uri.toString());
                                        facilityImageView.setImageURI(uri);
                                        eventFirebase.editFacility(facility);
                                        facilityImageView.setVisibility(View.GONE);
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    // Handle errors in the file upload
                                    Log.e(TAG, "Upload failed", e);
                                    Toast.makeText(requireActivity().getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });
    }


    public Uri drawableToUri(Context context, int drawableResId) {
        // Get the drawable resource
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);

        // Create a bitmap from the drawable
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            // If the drawable is already a BitmapDrawable, extract the Bitmap directly
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof GradientDrawable) {
            // If the drawable is a GradientDrawable, create a Bitmap and draw the drawable onto it
            GradientDrawable gradientDrawable = (GradientDrawable) drawable;
            bitmap = Bitmap.createBitmap(gradientDrawable.getIntrinsicWidth(), gradientDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            gradientDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            gradientDrawable.draw(canvas);
        }

        if (bitmap == null) {
            throw new IllegalArgumentException("Unsupported drawable type");
        }

        // Create a file in the app's cache directory
        File cacheDir = context.getCacheDir();
        File file = new File(cacheDir, "image_unavailable_background.png");

        try (FileOutputStream out = new FileOutputStream(file)) {
            // Compress the bitmap and save it as a PNG file
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return the URI pointing to the saved file
        return Uri.fromFile(file);
    }
}
