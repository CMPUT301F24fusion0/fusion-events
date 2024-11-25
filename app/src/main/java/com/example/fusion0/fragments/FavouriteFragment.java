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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.fusion0.R;
import com.example.fusion0.activities.JoinedEventActivity;
import com.example.fusion0.activities.ViewEventActivity;
import com.example.fusion0.activities.ViewFacilityActivity;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.FacilitiesInfo;
import com.example.fusion0.models.OrganizerInfo;
import com.example.fusion0.models.UserInfo;
import com.google.zxing.WriterException;

import java.util.ArrayList;

public class FavouriteFragment extends Fragment {

    private static final String TAG = "FavouriteActivity";
    private Button joinedEventsButton;
    private Button createdEventsButton;
    private Button facilitiesButton;
    private String deviceID;
    private OrganizerInfo organizer;
    private UserInfo user;
    private ListView joinedEventsList;
    private ListView createdEventsList;
    private ListView facilitiesList;
    private boolean isFacilitiesListVisible = false;
    private boolean isCreatedEventsListVisible = false;
    private boolean isJoinedEventsListVisible = false;

    private ImageButton profileButton;
    private ImageButton addButton;
    private ImageButton scannerButton;
    private ImageButton homeButton;

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

        joinedEventsButton = view.findViewById(R.id.joined_events_view_button);
        createdEventsButton = view.findViewById(R.id.created_events_view_button);
        facilitiesButton = view.findViewById(R.id.facilities_view_button);
        joinedEventsList = view.findViewById(R.id.joined_events_list);
        createdEventsList = view.findViewById(R.id.created_events_list);
        facilitiesList = view.findViewById(R.id.facilities_list);
        createdEventsList = view.findViewById(R.id.created_events_list);

        joinedEventsButton.setOnClickListener(v -> {
            new UserFirestore().findUser(deviceID, new UserFirestore.Callback() {
                @Override
                public void onSuccess(UserInfo userInfo) {
                    if (userInfo == null) {
                        Toast.makeText(context, "User data is not available.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (userInfo.getEvents() == null) {
                        Toast.makeText(context, "No Joined Events Available.", Toast.LENGTH_SHORT).show();
                        joinedEventsList.setVisibility(View.GONE);
                        joinedEventsButton.setText("View");
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
                                        ArrayAdapter<String> eventsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, eventNames);
                                        joinedEventsList.setAdapter(eventsAdapter);
                                    }
                                }

                                @Override
                                public void onFailure(String error) {
                                    Log.e(TAG, "Error fetching event: " + error);
                                }
                            });
                        }

                        ArrayAdapter<String> eventsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, eventNames);
                            joinedEventsList.setAdapter(eventsAdapter);
                        }

                        if (isJoinedEventsListVisible) {
                            joinedEventsList.setVisibility(View.GONE);
                            joinedEventsButton.setText("View");
                        } else {
                            joinedEventsList.setVisibility(View.VISIBLE);
                            joinedEventsButton.setText("Hide");
                        }
                        isJoinedEventsListVisible = !isJoinedEventsListVisible;
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
            EventFirebase.findOrganizer(deviceID, new EventFirebase.OrganizerCallback() {
                @Override
                public void onSuccess(OrganizerInfo organizerInfo) {
                    if (organizerInfo == null) {
                        Toast.makeText(context, "No Created Events Available.", Toast.LENGTH_SHORT).show();
                        createdEventsList.setVisibility(View.GONE);
                        createdEventsButton.setText("View");
                    } else {
                        organizer = organizerInfo;

                        if (createdEventsList.getAdapter() == null) {
                            ArrayAdapter<String> facilitiesAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, organizer.getEventsNames());
                            createdEventsList.setAdapter(facilitiesAdapter);
                        }

                        if (isCreatedEventsListVisible) {
                            createdEventsList.setVisibility(View.GONE);
                            createdEventsButton.setText("View");
                        } else {
                            createdEventsList.setVisibility(View.VISIBLE);
                            createdEventsButton.setText("Hide");
                        }
                        isCreatedEventsListVisible = !isCreatedEventsListVisible;

                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Error fetching organizer: " + error);
                }
            });
            createdEventsList.setOnItemClickListener((parent, view1, position, id) -> {
                EventInfo event = organizer.getEvents().get(position);
                String eventID = event.getEventID();

                Intent intent = new Intent(requireActivity(), ViewEventActivity.class);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
            });
        });

        facilitiesButton.setOnClickListener(v -> {
            EventFirebase.findOrganizer(deviceID, new EventFirebase.OrganizerCallback() {
                @Override
                public void onSuccess(OrganizerInfo organizerInfo) {
                    if (organizerInfo == null) {
                        Toast.makeText(context, "No facilities available.", Toast.LENGTH_SHORT).show();
                        facilitiesList.setVisibility(View.GONE);
                    } else {
                        organizer = organizerInfo;

                        if (facilitiesList.getAdapter() == null) {
                            if (organizer.getFacilitiesNames() != null) {
                                ArrayAdapter<String> facilitiesAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, organizer.getFacilitiesNames());
                                facilitiesList.setAdapter(facilitiesAdapter);
                            } else {
                                Toast.makeText(context, "No facilities available.", Toast.LENGTH_SHORT).show();
                                facilitiesButton.setEnabled(false);
                                facilitiesList.setVisibility(View.GONE);
                                return;
                            }
                        }
                        if (isFacilitiesListVisible) {
                            facilitiesList.setVisibility(View.GONE);
                            facilitiesButton.setText("View");
                        } else {
                            facilitiesList.setVisibility(View.VISIBLE);
                            facilitiesButton.setText("Hide");
                        }

                        isFacilitiesListVisible = !isFacilitiesListVisible; // Toggle the state
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

        initializeToolbarButtons(view);

    }

    private void initializeToolbarButtons(View view) {
        homeButton = view.findViewById(R.id.toolbar_home);
        scannerButton = view.findViewById(R.id.toolbar_qrscanner);
        addButton = view.findViewById(R.id.toolbar_add);
        profileButton = view.findViewById(R.id.toolbar_person);

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
}