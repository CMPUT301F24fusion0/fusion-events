package com.example.fusion0.fragments;

import static android.content.ContentValues.TAG;

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
 * Fragment for editing and managing facility details.
 */
public class EditFacilityFragment extends Fragment {

    private String deviceID;
    private TextView facilityNameTextView, addressTextView, ownerTextView, facilitiesEventsTextView;
    private EditText facilityNameEditText, addressEditText;
    private FacilitiesInfo facility;
    private ImageView facilityImageView;
    private Boolean isOwner = false;
    private LinearLayout toolbar;
    private ImageButton backButton;
    private Button editButton, saveButton, deleteButton, cancelButton;
    private ListView facilitiesEventsList;
    private EventFirebase eventFirebase = new EventFirebase();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.facility_view, container, false);

        // Initialize UI components
        facilityNameTextView = view.findViewById(R.id.facilityName);
        addressTextView = view.findViewById(R.id.address);
        facilityImageView = view.findViewById(R.id.facilityImage);
        ownerTextView = view.findViewById(R.id.owner);
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
        backButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

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
                Log.e(TAG, "Error fetching facility data: " + error);
                Toast.makeText(requireContext(), "Failed to load facility data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFacilityDetails() {
        facilityNameTextView.setText(facility.getFacilityName());
        addressTextView.setText(facility.getAddress());
        ownerTextView.setVisibility(View.GONE);

        if (facility.getFacilityImage() != null && !facility.getFacilityImage().isEmpty()) {
            Glide.with(requireContext())
                    .load(facility.getFacilityImage())
                    .into(facilityImageView);
            facilityImageView.setVisibility(View.VISIBLE);
        }

        if (deviceID.equals(facility.getOwner()) || EventFirebase.isDeviceIDAdmin(deviceID)) {
            isOwner = true;
            toolbar.setVisibility(View.VISIBLE);
        }

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
                    }
                });
            }

            facility.setEvents(filteredEvents);
            facilitiesEventsList.setOnItemClickListener((parent, view, position, id) -> {
                Toast.makeText(requireContext(), "Event: " + filteredEvents.get(position), Toast.LENGTH_SHORT).show();
            });

        } else {
            facilitiesEventsTextView.setVisibility(View.VISIBLE);
            facilitiesEventsList.setVisibility(View.GONE);
        }

        setupButtons();
    }

    private void setupButtons() {
        facilityImageView.setOnClickListener(v -> {
            if (isOwner) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Image")
                        .setMessage("Are you sure you want to delete this facility's image?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            facility.setFacilityImage(null);
                            eventFirebase.editFacility(facility);
                            facilityImageView.setVisibility(View.GONE);
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        editButton.setOnClickListener(v -> {
            if (isOwner) {
                toggleEditMode(true);
            }
        });

        saveButton.setOnClickListener(v -> {
            if (isOwner) {
                facility.setFacilityName(facilityNameEditText.getText().toString());
                facility.setAddress(addressEditText.getText().toString());
                eventFirebase.editFacility(facility);
                toggleEditMode(false);
                populateFacilityDetails();
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (facility.getEvents() == null || facility.getEvents().isEmpty()) {
                if (isOwner) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Delete Facility")
                            .setMessage("Are you sure you want to delete this facility?")
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                eventFirebase.deleteFacility(facility.getFacilityID());
                                requireActivity().onBackPressed();
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
            } else {
                Toast.makeText(requireContext(), "Update the locations of the facility's events.", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> toggleEditMode(false));
    }

    private void toggleEditMode(boolean isEditing) {
        if (isEditing) {
            facilityNameTextView.setVisibility(View.GONE);
            addressTextView.setVisibility(View.GONE);
            facilityNameEditText.setVisibility(View.VISIBLE);
            addressEditText.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        } else {
            facilityNameTextView.setVisibility(View.VISIBLE);
            addressTextView.setVisibility(View.VISIBLE);
            facilityNameEditText.setVisibility(View.GONE);
            addressEditText.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
        }
    }
}
