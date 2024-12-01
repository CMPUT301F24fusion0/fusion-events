package com.example.fusion0.fragments;


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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.fusion0.R;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.FacilitiesInfo;

import java.util.ArrayList;

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
        facilityNameTextView.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        addressTextView.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        facilityNameEditText.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        addressEditText.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        saveButton.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        cancelButton.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        editButton.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        deleteButton.setVisibility(isEditing ? View.GONE : View.VISIBLE);

    }
}
