package com.example.fusion0.fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
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
import android.widget.ScrollView;
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
import com.example.fusion0.helpers.ProfileManagement;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.FacilitiesInfo;
import com.example.fusion0.models.OrganizerInfo;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.zxing.WriterException;

import java.util.ArrayList;

public class ViewFacilityFragment extends Fragment {

    private String deviceID;
    private String ID;
    private String eventID;
    private String facilityID;
    
    private TextView facilityNameTextView;
    private TextView addressTextView;
    private TextView ownerTextView;
    private TextView facilitiesEventsTextView;
    
    private EditText facilityNameEditText;
    private EditText addressEditText;
    
    private FacilitiesInfo facility;
    private ImageView facilityImageView;
    private Boolean isOwner = false;
    private LinearLayout toolbar;

    private ImageButton backButton;
    private ImageButton editButton;
    private ImageButton deleteButton;

    private Button saveButton;
    private Button cancelButton;
    
    private ListView facilitiesEventsList;
    private ProfileManagement profileManager;
    private EventFirebase eventFirebase = new EventFirebase();

    private ShimmerFrameLayout viewFacilitySkeletonLayout;
    private ScrollView scrollContainer;

    public ViewFacilityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        deviceID = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_facility, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();

        facilityNameTextView = view.findViewById(R.id.facilityName);
        addressTextView = view.findViewById(R.id.address);
        facilityImageView = view.findViewById(R.id.facilityImage);
        ownerTextView = view.findViewById(R.id.owner);
        facilitiesEventsTextView= view.findViewById(R.id.facilities_events_list_text);
        facilityNameEditText = view.findViewById(R.id.editFacilityName);
        addressEditText = view.findViewById(R.id.editAddress);
        toolbar = view.findViewById(R.id.toolbar);

        facilitiesEventsList = view.findViewById(R.id.facilities_events_list);

        backButton = view.findViewById(R.id.backButton);
        editButton = view.findViewById(R.id.edit_button);
        saveButton = view.findViewById(R.id.save_button);
        deleteButton = view.findViewById(R.id.delete_button);
        cancelButton = view.findViewById(R.id.cancel_button);

        profileManager = new ProfileManagement();

        viewFacilitySkeletonLayout = view.findViewById(R.id.viewFacilitySkeletonLayout);
        scrollContainer = view.findViewById(R.id.scroll_container);

        loadScreen();
        Bundle bundle = getArguments();
        if (bundle != null) {
            facilityID  = bundle.getString("facilityID");
            eventID = bundle.getString("eventID");
            ID = bundle.getString("ID");
        }

        backButton.setOnClickListener(v -> {
            if ("favourite".equals(ID)) {
                Navigation.findNavController(view).navigate(R.id.action_viewFacilityFragment_to_favouriteFragment);
            } else if ("viewEvent".equals(ID)) {
                Bundle eventBundle = new Bundle();
                eventBundle.putString("eventID", eventID);
                Navigation.findNavController(view).navigate(R.id.action_viewFacilityFragment_to_viewEventFragment, eventBundle);
            } else if ("joinedEvent".equals(ID)) {
                Bundle joinedBundle = new Bundle();
                joinedBundle.putString("eventID", eventID);
                Navigation.findNavController(view).navigate(R.id.action_viewFacilityFragment_to_userJoinFragment, joinedBundle);
            } else if ("userJoinedEvent".equals(ID)) {
                Bundle userJoinedBundle = new Bundle();
                userJoinedBundle.putString("eventID", eventID);
                Navigation.findNavController(view).navigate(R.id.action_viewFacilityFragment_to_joinedEventsFragment, userJoinedBundle);
            }
        });

        eventFirebase.findFacility(facilityID, new EventFirebase.FacilityCallback() {
            @Override
            public void onSuccess(FacilitiesInfo facilitiesInfo) {
                if (facilitiesInfo == null) {
                    Toast.makeText(context, "Facility Unavailable.", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(R.id.action_viewFacilityFragment_to_viewEventFragment);
                } else {
                    facility = facilitiesInfo;
                    facilityNameTextView.setText(facility.getFacilityName());
                    addressTextView.setText(facility.getAddress());
                    ownerTextView.setVisibility(View.GONE);

                    /*
                    new UserFirestore().findUser(deviceID, new UserFirestore.Callback() {
                        @Override
                        public void onSuccess(UserInfo user) {
                            String fullName = user.getFirstName() + ' ' + user.getLastName();
                            ownerTextView.setText(fullName);
                        }

                        @Override
                        public void onFailure(String error) {
                            ownerTextView.setText(facility.getOwner());

                        }
                    });

                     */
                    if (facility.getFacilityImage() != null && !facility.getFacilityImage().isEmpty()) {
                        Glide.with(context)
                                .load(facility.getFacilityImage())
                                .into(facilityImageView);
                        facilityImageView.setVisibility(View.VISIBLE);
                    }

                    if (deviceID.equals(facility.getOwner()) || EventFirebase.isDeviceIDAdmin(deviceID)) {
                        if(EventFirebase.isDeviceIDAdmin(deviceID)){
                            Toast.makeText(context, "You are an admin.", Toast.LENGTH_SHORT).show();
                        }
                        isOwner = true;
                        editButton.setVisibility(View.VISIBLE);
                    }

                    ArrayList<String> eventNames = new ArrayList<>();
                    ArrayAdapter<String> eventsAdapter = new ArrayAdapter<>(context, R.layout.spinner_dropdown_item, eventNames);
                    facilitiesEventsList.setAdapter(eventsAdapter);


                    if ((facility.getEvents() != null) && !(facility.getEvents().isEmpty())) {
                        ArrayList<String> filteredEvents = new ArrayList<>();
                        for (String event : facility.getEvents()) {
                            eventFirebase.findEvent(event, new EventFirebase.EventCallback() {
                                @Override
                                public void onSuccess(EventInfo eventInfo) throws WriterException {
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
                        populateScreen();
                        facilitiesEventsList.setOnItemClickListener((parent, view1, position, id) -> {
                            ViewEventFragment eventFragment = new ViewEventFragment();

                            Bundle bundle = new Bundle();
                            bundle.putString("eventID", filteredEvents.get(position));
                            bundle.putString("deviceID", deviceID);
                            eventFragment.setArguments(bundle);

                            Navigation.findNavController(view).navigate(R.id.action_viewFacilityFragment_to_viewEventFragment, bundle);
                        });

                    }else {
                        facilitiesEventsTextView.setVisibility(View.VISIBLE);
                        facilitiesEventsList.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error fetching facility data: " + error);
                Toast.makeText(context, "Failed to load facility data.", Toast.LENGTH_SHORT).show();
            }
        });

        editButton.setOnClickListener(v -> {
            if (isOwner) {
                facilityNameTextView.setVisibility(View.GONE);
                addressTextView.setVisibility(View.GONE);

                facilityNameEditText.setVisibility(View.VISIBLE);
                addressEditText.setVisibility(View.VISIBLE);

                toolbar.setVisibility(View.VISIBLE);


                facilityNameEditText.setText(facility.getFacilityName());
                addressEditText.setText(facility.getAddress());

                editButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.VISIBLE);

            }
        });

        saveButton.setOnClickListener(v -> {
            if (isOwner) {
                facility.setFacilityName(facilityNameEditText.getText().toString());
                facility.setAddress(addressEditText.getText().toString());

                facilityNameTextView.setVisibility(View.VISIBLE);
                addressTextView.setVisibility(View.VISIBLE);
                ownerTextView.setVisibility(View.VISIBLE);

                facilityNameEditText.setVisibility(View.GONE);
                addressEditText.setVisibility(View.GONE);

                editButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);

                facilityNameTextView.setText(facility.getFacilityName());
                addressTextView.setText(facility.getAddress());
                ownerTextView.setText(facility.getOwner());

                eventFirebase.editFacility(facility);
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (facility.getEvents()==null || facility.getEvents().isEmpty()){
                if (isOwner) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete entry")
                            .setMessage("Are you sure you want to delete this entry?")
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                eventFirebase.findOrganizer(facility.getOwner(), new EventFirebase.OrganizerCallback() {
                                    @Override
                                    public void onSuccess(OrganizerInfo organizerInfo) {
                                        ArrayList<FacilitiesInfo> organizerFacilities = organizerInfo.getFacilities();

                                        for (int i = 0; i < organizerFacilities.size(); i++) {
                                            FacilitiesInfo currentFacility = organizerFacilities.get(i);
                                            if (currentFacility.getFacilityID().equals(facilityID)) {
                                                organizerFacilities.remove(i);
                                                break;
                                            }
                                        }
                                        organizerInfo.setFacilities(organizerFacilities);
                                        eventFirebase.editOrganizer(organizerInfo);
                                    }

                                    @Override
                                    public void onFailure(String error) {

                                    }
                                });

                                eventFirebase.deleteFacility(facility.getFacilityID());
                                Navigation.findNavController(view).navigate(R.id.action_viewFacilityFragment_to_favouriteFragment);
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
            } else {
                Toast.makeText(context, "Update the locations of the facility's events.", Toast.LENGTH_SHORT).show();
            }

        });

        cancelButton.setOnClickListener(v ->{
            facilityNameTextView.setVisibility(View.VISIBLE);
            addressTextView.setVisibility(View.VISIBLE);

            facilityNameEditText.setVisibility(View.GONE);
            addressEditText.setVisibility(View.GONE);

            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.GONE);

            toolbar.setVisibility(View.GONE);


            facilityNameTextView.setText(facility.getFacilityName());
            addressTextView.setText(facility.getAddress());
        });

    }

    private void loadScreen() {
        scrollContainer.setVisibility(View.GONE);

        viewFacilitySkeletonLayout.startShimmerAnimation();
        viewFacilitySkeletonLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Populate the screen
     * @author Nimi Akinroye
     */
    private void populateScreen() {
        viewFacilitySkeletonLayout.stopShimmerAnimation();
        viewFacilitySkeletonLayout.setVisibility(View.GONE);

        scrollContainer.setVisibility(View.VISIBLE);
    }
}