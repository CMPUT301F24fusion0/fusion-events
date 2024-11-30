package com.example.fusion0.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.fusion0.R;
import com.example.fusion0.activities.JoinedEventActivity;
import com.example.fusion0.activities.ViewFacilityActivity;
import com.example.fusion0.helpers.AnimationHelper;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.FacilitiesInfo;
import com.example.fusion0.models.OrganizerInfo;
import com.example.fusion0.models.UserInfo;
import com.google.zxing.WriterException;

import java.util.ArrayList;

public class FavouriteFragment extends Fragment {


    private static final String TAG = "FavouriteFragment";
    private ImageButton joinedEventsButton;
    private ImageButton createdEventsButton;
    private ImageButton facilitiesButton;
    private String deviceID;
    private OrganizerInfo organizer;
    private UserInfo user;
    private ListView joinedEventsList;
    private ListView createdEventsList;
    private ListView facilitiesList;
    private boolean isFacilitiesListVisible = false;
    private boolean isCreatedEventsListVisible = false;
    private boolean isJoinedEventsListVisible = false;


    private LinearLayout profileButton;
    private LinearLayout addButton;
    private LinearLayout homeButton;
    private LinearLayout scannerButton;
    private LinearLayout favouriteButton;

    private ImageButton profileImageButton;
    private ImageButton addImageButton;
    private ImageButton homeImageButton;
    private ImageButton scannerImageButton;
    private ImageButton favouriteImageButton;

    private TextView homeTextView;
    private TextView scannerTextView;
    private TextView addTextView;
    private TextView searchTextView;
    private TextView profileTextView;

    private View joinedEventsListDivider;
    private View createdEventsListDivider;
    private View facilitiesListDivider;

    private TextView emptyJoinedEventsList;
    private TextView emptyCreatedEventsList;
    private TextView emptyFacilitiesList;

    EventFirebase eventFirebase = new EventFirebase();


    public FavouriteFragment() {
        // Required empty public constructor
    }


    @SuppressLint("HardwareIds")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deviceID = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favourite, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Context context = requireContext();

        initializeToolbarButtons(view, context);


        joinedEventsButton = view.findViewById(R.id.joined_events_view_button);
        createdEventsButton = view.findViewById(R.id.created_events_view_button);
        facilitiesButton = view.findViewById(R.id.facilities_view_button);
        joinedEventsList = view.findViewById(R.id.joined_events_list);
        createdEventsList = view.findViewById(R.id.created_events_list);
        facilitiesList = view.findViewById(R.id.facilities_list);
        createdEventsList = view.findViewById(R.id.created_events_list);

        joinedEventsListDivider = view.findViewById(R.id.joined_events_list_divider);
        createdEventsListDivider = view.findViewById(R.id.created_events_list_divider);
        facilitiesListDivider = view.findViewById(R.id.facilities_list_divider);

        emptyJoinedEventsList = view.findViewById(R.id.emptyJoinedEventsList);
        emptyCreatedEventsList = view.findViewById(R.id.emptyCreatedEventsList);
        emptyFacilitiesList = view.findViewById(R.id.emptyFacilitiesList);

        joinedEventsButton.setOnClickListener(v -> {
            AnimationHelper.rotateView(joinedEventsButton, 45f, 300);
            new UserFirestore().findUser(deviceID, new UserFirestore.Callback() {
                @Override
                public void onSuccess(UserInfo userInfo) {
                    if (userInfo == null) {
                        Toast.makeText(context, "User data is not available.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (userInfo.getEvents() == null || userInfo.getEvents().isEmpty()) {
                        Toast.makeText(context, "No Joined Events Available.", Toast.LENGTH_SHORT).show();
                        joinedEventsList.setVisibility(View.GONE);

                        if (isJoinedEventsListVisible) {
                            AnimationHelper.fadeOutView(joinedEventsListDivider, 75);
                            AnimationHelper.fadeOutView(emptyJoinedEventsList, 225);
                            isJoinedEventsListVisible = !isJoinedEventsListVisible;
                        } else {
                            AnimationHelper.fadeInView(joinedEventsListDivider, 75);
                            AnimationHelper.fadeInView(emptyJoinedEventsList, 225);
                            isJoinedEventsListVisible = !isJoinedEventsListVisible;
                        }
                    } else {
                        user = userInfo;
                        ArrayList<String> events = user.getEvents();
                        ArrayList<String> eventNames = new ArrayList<>();

                        final int totalEvents = events.size();
                        final int[] eventsFetchedCount = {0};

                        for (String eventId : events) {
                            EventFirebase.findEvent(eventId, new EventFirebase.EventCallback() {
                                @Override
                                public void onSuccess(EventInfo eventInfo) throws WriterException {
                                    if (eventInfo != null) {
                                        eventNames.add(eventInfo.getEventName());
                                    }
                                    eventsFetchedCount[0]++;

                                    if (eventsFetchedCount[0] == totalEvents) {
                                        ArrayAdapter<String> eventsAdapter = new ArrayAdapter<>(context, R.layout.spinner_dropdown_item, eventNames);
                                        joinedEventsList.setAdapter(eventsAdapter);
                                    }
                                }

                                @Override
                                public void onFailure(String error) {
                                    Log.e(TAG, "Error fetching event: " + error);
                                }
                            });
                        }


                        if (isJoinedEventsListVisible) {
                            AnimationHelper.fadeOutView(joinedEventsListDivider, 75);
                            AnimationHelper.fadeOutView(joinedEventsList, 225);
                        } else {
                            AnimationHelper.fadeInView(joinedEventsListDivider, 75);
                            AnimationHelper.fadeInView(joinedEventsList, 225);
                        }
                        isJoinedEventsListVisible = !isJoinedEventsListVisible;
                    }
                }


                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Error fetching user: " + error);
                }
            });


            joinedEventsList.setOnItemClickListener((parent, view1, position, id) -> {
                String event = user.getEvents().get(position);


                Intent intent = new Intent(requireActivity(), JoinedEventActivity.class);
                intent.putExtra("eventID", event);
                intent.putExtra("deviceID", deviceID);
                startActivity(intent);
            });
        });


        createdEventsButton.setOnClickListener(v -> {
            AnimationHelper.rotateView(createdEventsButton, 45f, 300);
            EventFirebase.findOrganizer(deviceID, new EventFirebase.OrganizerCallback() {
                @Override
                public void onSuccess(OrganizerInfo organizerInfo) {
                    // Null check for organizerInfo
                    if (organizerInfo != null) {
                        // Proceed only if the organizerInfo is valid
                        if (organizerInfo.getEvents() != null) {
                            // Handle empty events list
                            if (organizerInfo.getEvents().isEmpty()) {
                                Toast.makeText(context, "No Created Events Available.", Toast.LENGTH_SHORT).show();
                                createdEventsList.setVisibility(View.GONE);

                                if (isCreatedEventsListVisible) {
                                    AnimationHelper.fadeOutView(createdEventsListDivider, 75);
                                    AnimationHelper.fadeOutView(emptyCreatedEventsList, 225);
                                    isCreatedEventsListVisible = !isCreatedEventsListVisible;
                                } else {
                                    AnimationHelper.fadeInView(createdEventsListDivider, 75);
                                    AnimationHelper.fadeInView(emptyCreatedEventsList, 225);
                                    isCreatedEventsListVisible = !isCreatedEventsListVisible;
                                }

                            } else {
                                organizer = organizerInfo;
                                ArrayAdapter<String> eventsAdapter = new ArrayAdapter<>(context, R.layout.spinner_dropdown_item , organizer.getEventsNames());
                                createdEventsList.setAdapter(eventsAdapter);


                                // Toggle visibility of the created events list
                                if (isCreatedEventsListVisible) {
                                    AnimationHelper.fadeOutView(createdEventsListDivider, 75);
                                    AnimationHelper.fadeOutView(createdEventsList, 250);
                                } else {
                                    AnimationHelper.fadeInView(createdEventsListDivider, 75);
                                    AnimationHelper.fadeInView(createdEventsList, 250);
                                }
                                isCreatedEventsListVisible = !isCreatedEventsListVisible;
                            }
                        } else {
                            // If no events list is found
                            Toast.makeText(context, "No Created Events Available.", Toast.LENGTH_SHORT).show();
                            createdEventsList.setVisibility(View.GONE);
                            isCreatedEventsListVisible = false;
                        }
                    } else {
                        // If organizerInfo itself is null
                        Toast.makeText(context, "You have created no events.", Toast.LENGTH_SHORT).show();
                        createdEventsList.setVisibility(View.GONE);
                        isCreatedEventsListVisible = false;
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Error fetching organizer: " + error);
                    // Handle failure case here (e.g., show a Toast or other fallback UI)
                }
            });


            // Set up the item click listener for the created events list
            createdEventsList.setOnItemClickListener((parent, view1, position, id) -> {
                if (organizer != null && organizer.getEvents() != null && position >= 0 && position < organizer.getEvents().size()) {
                    EventInfo event = organizer.getEvents().get(position);
                    String eventID = event.getEventID();


                    Bundle bundle = new Bundle();
                    bundle.putString("eventID", eventID);

                    Navigation.findNavController(view1).navigate(R.id.action_favouriteFragment_to_viewEventFragment, bundle);
                } else {
                    Toast.makeText(context, "Invalid event selected.", Toast.LENGTH_SHORT).show();
                }
            });
        });




        facilitiesButton.setOnClickListener(v -> {
            AnimationHelper.rotateView(facilitiesButton, 45f, 300);
            eventFirebase.findOrganizer(deviceID, new EventFirebase.OrganizerCallback() {
                @Override
                public void onSuccess(OrganizerInfo organizerInfo) {
                    if (organizerInfo.getFacilities() == null || organizerInfo.getFacilities().isEmpty()) {
                        Toast.makeText(context, "No facilities available.", Toast.LENGTH_SHORT).show();
                        facilitiesList.setVisibility(View.GONE);

                        if (isFacilitiesListVisible) {
                            AnimationHelper.fadeOutView(facilitiesListDivider, 75);
                            AnimationHelper.fadeOutView(emptyFacilitiesList, 225);
                            isFacilitiesListVisible = !isFacilitiesListVisible;
                        } else {
                            AnimationHelper.fadeInView(facilitiesListDivider, 75);
                            AnimationHelper.fadeInView(emptyFacilitiesList, 225);
                            isFacilitiesListVisible = !isFacilitiesListVisible;
                        }

                    } else {
                        organizer = organizerInfo;


                        if (facilitiesList.getAdapter() == null) {
                            if (organizer.getFacilitiesNames() != null) {
                                ArrayAdapter<String> facilitiesAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, organizer.getFacilitiesNames());
                                facilitiesList.setAdapter(facilitiesAdapter);
                            } else {
                                Toast.makeText(context, "No facilities available.", Toast.LENGTH_SHORT).show();
                                facilitiesButton.setEnabled(false);
                                facilitiesList.setVisibility(View.GONE);
                                return;
                            }
                        }
                        if (isFacilitiesListVisible) {
                            AnimationHelper.fadeOutView(facilitiesListDivider, 75);
                            AnimationHelper.fadeOutView(facilitiesList, 250);
                        } else {
                            AnimationHelper.fadeInView(facilitiesListDivider, 75);
                            AnimationHelper.fadeInView(facilitiesList, 250);
                        }
                        isFacilitiesListVisible = !isFacilitiesListVisible;
                    }
                }


                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Error fetching organizer: " + error);
                }
            });
            facilitiesList.setOnItemClickListener((parent, view1, position, id) -> {
                FacilitiesInfo facility = organizer.getFacilities().get(position);
                String facilityID = facility.getFacilityID();

                Intent intent = new Intent(requireActivity(), ViewFacilityActivity.class);
                intent.putExtra("facilityID", facilityID);
                startActivity(intent);
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateJoinedEventsList();
        updateCreatedEventsList();
        updateFacilitiesList();
    }




    private void updateJoinedEventsList() {
        joinedEventsList.setVisibility(View.GONE);
        if (user != null && user.getEvents() != null && !user.getEvents().isEmpty()) {
            ArrayList<String> eventNames = new ArrayList<>();
            int totalEvents = user.getEvents().size();
            int[] eventsFetchedCount = {0};


            for (String eventId : user.getEvents()) {
                eventFirebase.findEvent(eventId, new EventFirebase.EventCallback() {
                    @Override
                    public void onSuccess(EventInfo eventInfo) throws WriterException {
                        if (eventInfo != null) {
                            eventNames.add(eventInfo.getEventName());
                        }
                        eventsFetchedCount[0]++;
                        // Once all events are fetched, update the adapter
                        if (eventsFetchedCount[0] == totalEvents) {
                            ArrayAdapter<String> eventsAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, eventNames);
                            joinedEventsList.setAdapter(eventsAdapter);
                        }
                    }


                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Error fetching event: " + error);
                    }
                });
            }
        }
    }





    private void updateCreatedEventsList() {
        createdEventsList.setVisibility(View.GONE);
        if (organizer != null && organizer.getEvents() != null && !organizer.getEvents().isEmpty()) {
            ArrayAdapter<String> eventsAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, organizer.getEventsNames());
            createdEventsList.setAdapter(eventsAdapter);
        }
    }


    private void updateFacilitiesList() {
        facilitiesList.setVisibility(View.GONE);
        if (organizer != null && organizer.getFacilities() != null && !organizer.getFacilities().isEmpty()) {
            ArrayAdapter<String> facilitiesAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, organizer.getFacilitiesNames());
            facilitiesList.setAdapter(facilitiesAdapter);
        }
    }

    private void initializeToolbarButtons(View view, Context context) {
        homeButton = view.findViewById(R.id.toolbar_home);
        scannerButton = view.findViewById(R.id.toolbar_qrscanner);
        addButton = view.findViewById(R.id.toolbar_add);
        favouriteButton = view.findViewById(R.id.toolbar_favourite);
        profileButton = view.findViewById(R.id.toolbar_person);

        homeImageButton = view.findViewById(R.id.toolbar_home_image);
        scannerImageButton = view.findViewById(R.id.toolbar_qrscanner_image);
        addImageButton = view.findViewById(R.id.toolbar_add_image);
        favouriteImageButton = view.findViewById(R.id.toolbar_favourite_image);
        profileImageButton = view.findViewById(R.id.toolbar_person_image);

        homeTextView = view.findViewById(R.id.homeTextView);
        scannerTextView = view.findViewById(R.id.qrTextView);
        addTextView = view.findViewById(R.id.addTextView);
        searchTextView = view.findViewById(R.id.searchTextView);
        profileTextView = view.findViewById(R.id.profileTextView);

        // Set all buttons
        setAllButtonsInactive(context);
        setActiveButton(context, favouriteImageButton, searchTextView);


        homeButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_favouriteFragment_to_mainFragment);
        });


        scannerButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_favouriteFragment_to_qrFragment);
        });


        addButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_favouriteFragment_to_eventFragment);
        });


        profileButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_favouriteFragment_to_profileFragment);
        });
    }

    private void setAllButtonsInactive(Context context) {
        profileImageButton.setColorFilter(ContextCompat.getColor(context, R.color.grey));
        scannerImageButton.setColorFilter(ContextCompat.getColor(context, R.color.grey));
        homeImageButton.setColorFilter(ContextCompat.getColor(context, R.color.grey));
        addImageButton.setColorFilter(ContextCompat.getColor(context, R.color.grey));

        scannerTextView.setTextColor(ContextCompat.getColor(context, R.color.grey));
        homeTextView.setTextColor(ContextCompat.getColor(context, R.color.grey));
        addTextView.setTextColor(ContextCompat.getColor(context, R.color.grey));
        profileTextView.setTextColor(ContextCompat.getColor(context, R.color.grey));
    }

    private void setActiveButton(Context context, ImageButton activeButton, TextView activeTextView) {
        activeButton.setColorFilter(ContextCompat.getColor(context, R.color.royalBlue));
        activeTextView.setTextColor(ContextCompat.getColor(context, R.color.royalBlue));
    }

    private void fadeInView(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(300) // Set duration for smoothness
                .setListener(null);
    }
}


