package com.example.fusion0.fragments;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.fusion0.BuildConfig;
import com.example.fusion0.R;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.helpers.Waitlist;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.FacilitiesInfo;
import com.example.fusion0.models.OrganizerInfo;
import com.example.fusion0.models.UserInfo;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AuthorAttributions;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Copy of View Event Fragment
 */
public class TestViewEventFragment extends Fragment {
    private String deviceID;
    private Boolean isOwner = false;
    private Spinner eventFacility;
    private TextView eventNameTextView, eventFacilityTextView, addFacilityText, eventDescriptionTextView, registrationDateRequirementsTextView, registrationDateTextView, registrationPassedFullTextView, dateRequirementsTextView, eventStartDateTextView, eventEndDateTextView, eventStartTimeTextView, eventEndTimeTextView, eventCapacityTextView, eventLotteryCapacityTextView, waitinglistFullTextView, startMonth, startDateTextView;
    private EditText eventNameEditText, eventDescriptionEditText, eventCapacityEditText, eventLotteryCapacityEditText;
    private ImageView eventPosterImageView, qrImageView, facilityButton;
    private Button joinButton, cancelButton, saveButton;
    private ImageButton backButton, uploadImageButton,editButton, deleteButton, lotteryButton;;
    private EventInfo event;
    private EventFirebase eventFirebase;
    private UserInfo user;
    private OrganizerInfo organizer;
    private TextView organizerName;
    private LinearLayout toolbar;
    private Calendar startDateCalendar, registrationDateCalendar;
    private androidx.fragment.app.FragmentContainerView autocompletePlaceFragment;
    private Location userLocation;
    LinearLayout lists, waitinglistButton, cancelledEntrantsButton, chosenEntrantsButton, editStateButtons;
    ConstraintLayout startDateButton, endDateButton, registrationDateButton;

    private View eventNameBar, descriptionBar, startDateBar, endDateBar, registrationDateBar, waitlistBar, lotteryBar;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private FusedLocationProviderClient fusedLocationClient;

    private Double newLongitude = null;
    private Double newLatitude = null;
    private String newEventPoster, facility, address, facilityID;
    private Date endDate, startDate, registrationDate;
    private FacilitiesInfo newFacility;

    public static Waitlist waitlist;

    private StorageReference storageRef;

    public TestViewEventFragment() {
        // Required empty public constructor
    }

    @SuppressLint("HardwareIds")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storageRef = FirebaseStorage.getInstance().getReference();
        waitlist = new Waitlist();
        deviceID = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test_view_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();
        Activity activity = requireActivity();

        backButton = view.findViewById(R.id.backButton);
        eventNameTextView = view.findViewById(R.id.EventName);
        eventDescriptionTextView = view.findViewById(R.id.description);
        eventFacility = view.findViewById(R.id.spinner_facilities);
        eventFacilityTextView = view.findViewById(R.id.facilityName);
        addFacilityText = view.findViewById(R.id.add_facility_text);
        facilityButton = view.findViewById(R.id.facility_view_button);
        autocompletePlaceFragment = view.findViewById(R.id.autocomplete_fragment);
        eventStartDateTextView = view.findViewById(R.id.start_date_text);
        eventEndDateTextView = view.findViewById(R.id.end_date_text);
        eventEndTimeTextView = view.findViewById(R.id.end_time_text);
        eventStartTimeTextView = view.findViewById(R.id.start_time_text);
        eventCapacityTextView = view.findViewById(R.id.capacity);
        eventLotteryCapacityTextView = view.findViewById(R.id.lotteryCapacity);
        eventNameEditText = view.findViewById(R.id.editEventName);
        eventDescriptionEditText = view.findViewById(R.id.description_edit);
        eventCapacityEditText = view.findViewById(R.id.editCapacity);
        eventLotteryCapacityEditText = view.findViewById(R.id.editLotteryCapacity);
        dateRequirementsTextView = view.findViewById(R.id.date_requirements_text);
        registrationDateRequirementsTextView = view.findViewById(R.id.registrationDateRequirementsTextView);
        registrationDateTextView = view.findViewById(R.id.registration_date_text);
        registrationDateButton = view.findViewById(R.id.registration_date_button);

        eventPosterImageView = view.findViewById(R.id.uploaded_image_view);
        uploadImageButton = view.findViewById(R.id.upload_image_button);

        qrImageView = view.findViewById(R.id.qrImage);
        waitinglistButton = view.findViewById(R.id.waitinglistButton);
        chosenEntrantsButton = view.findViewById(R.id.chosenEntrantsButton);
        cancelledEntrantsButton = view.findViewById(R.id.cancelledEntrantsButton);

        startDateButton = view.findViewById(R.id.start_date_button);
        endDateButton = view.findViewById(R.id.end_date_button);
        editButton = view.findViewById(R.id.edit_button);
        deleteButton = view.findViewById(R.id.delete_button);

//        joinButton = view.findViewById(R.id.join_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        saveButton = view.findViewById(R.id.save_button);

        editStateButtons = view.findViewById(R.id.editStateButtons);
        waitinglistFullTextView = view.findViewById(R.id.waitinglist_full_text_view);
        registrationPassedFullTextView = view.findViewById(R.id.registration_passed_text_view);
        lists = view.findViewById(R.id.lists);
        toolbar = view.findViewById(R.id.toolbar);

        eventNameBar = view.findViewById(R.id.eventNameBar);
        descriptionBar = view.findViewById(R.id.descriptionBar);
        startDateBar = view.findViewById(R.id.startDateBar);
        endDateBar = view.findViewById(R.id.endDateBar);
        waitlistBar = view.findViewById(R.id.waitlistBar);
        lotteryBar = view.findViewById(R.id.lotteryBar);
        registrationDateBar = view.findViewById(R.id.registrationDateBar);

        startMonth = view.findViewById(R.id.startMonth);
        startDateTextView = view.findViewById(R.id.startDateTextView);
        organizerName = view.findViewById(R.id.organizerName);

        backButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_viewEventFragment_to_mainFragment);
        });

        Bundle bundle = getArguments();
        assert bundle != null;
        String eventID = bundle.getString("eventID");

        new UserFirestore().findUser(deviceID, new UserFirestore.Callback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(UserInfo userInfo) {
                user = userInfo;
                String firstName = user.getFirstName();
                String lastName = user.getLastName();
                organizerName.setText(firstName + " " + lastName);
            }

            @Override
            public void onFailure(String error) {
                Log.e("ViewEventFragment", "Error fetching user: " + error);
            }
        });

        if (eventID != null) {
            eventFirebase.findEvent(eventID, new EventFirebase.EventCallback() {
                @Override
                public void onSuccess(EventInfo eventInfo) throws WriterException {
                    if (eventInfo == null) {
                        Toast.makeText(context, "Event Unavailable.", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(view).navigate(R.id.action_viewEventFragment_to_mainFragment);
                    } else {

                        event = eventInfo;

                        eventNameTextView.setText(event.getEventName());
                        eventDescriptionTextView.setText(event.getDescription());
                        eventCapacityTextView.setText(event.getCapacity());
                        eventLotteryCapacityTextView.setText(event.getLotteryCapacity());
                        eventFacilityTextView.setText(event.getFacilityName());

                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                        String formattedStartDate = dateFormat.format(event.getStartDate());
                        String formattedEndDate = dateFormat.format(event.getEndDate());
                        String formattedRegistrationDate = dateFormat.format(event.getRegistrationDate());


                        eventStartDateTextView.setText(formattedStartDate);
                        eventEndDateTextView.setText(formattedEndDate);
                        eventStartTimeTextView.setText(event.getStartTime());
                        eventEndTimeTextView.setText(event.getEndTime());
                        registrationDateTextView.setText(formattedRegistrationDate);

                        endDate = event.getEndDate();
                        startDate = event.getStartDate();
                        registrationDate = event.getRegistrationDate();
                        facility = event.getFacilityName();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(startDate);

                        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                        int monthIndex = calendar.get(Calendar.MONTH);

                        String monthAbbreviation = new DateFormatSymbols().getShortMonths()[monthIndex].toUpperCase();

                        startMonth.setText(monthAbbreviation);
                        startDateTextView.setText(day);

                        newEventPoster = event.getEventPoster();

                        if (newEventPoster != null && !newEventPoster.isEmpty()) {
                            Glide.with(context)
                                    .load(newEventPoster)
                                    .centerCrop()
                                    .into(eventPosterImageView);
                        }

                        String qrCode = event.getQrCode();

                        if (qrCode != null && !qrCode.isEmpty()) {
                            Bitmap qrBitmap = event.generateQRCodeImage(500, 500, qrCode);
                            qrImageView.setImageBitmap(qrBitmap);
                        }

                        if (deviceID.equals(event.getOrganizer())) {
                            eventFirebase.findOrganizer(event.getOrganizer(), new EventFirebase.OrganizerCallback() {
                                @Override
                                public void onSuccess(OrganizerInfo organizerInfo) {
                                    organizer = organizerInfo;
                                }

                                @Override
                                public void onFailure(String error) {
                                    Log.e(TAG, "Unable to find organizer.");
                                }
                            });

                            isOwner = true;
                        }
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e("ViewEventFragment", "Error fetching event: " + error);
                    Toast.makeText(context, "Failed to load event details.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, "Invalid Event ID.", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigate(R.id.action_viewEventFragment_to_mainFragment);
        }

        waitinglistButton.setOnClickListener(v -> {
            waitlist.getWait(eventID, wait -> {
                if (wait.isEmpty()) {
                    Toast.makeText(context, "Waiting list is empty.", Toast.LENGTH_SHORT).show();
                } else {
                    ArrayList<Map<String, String>> fullWaitingListEntrants = new ArrayList<>();

                    if (event.getWaitinglist() != null && !event.getWaitinglist().isEmpty()) {
                        for (Map<String, String> user : event.getWaitinglist()) {
                            if (wait.contains(user.get("did")) && "waiting".equals(user.get("status"))) {
                                fullWaitingListEntrants.add(user);
                            }
                        }
                    }
                    WaitlistFragment waitlistFragment = new WaitlistFragment();

                    Bundle newBundle = new Bundle();
                    newBundle.putSerializable("waitingListData", fullWaitingListEntrants);
                    newBundle.putString("eventCapacity", event.getCapacity());
                    newBundle.putString("eventID", event.getEventID());
                    newBundle.putSerializable("fragment_waitlist", waitlist);
                    waitlistFragment.setArguments(newBundle);

                    Navigation.findNavController(view).navigate(R.id.action_viewEventFragment_to_waitlistFragment, bundle);
                }
            });
        });

        chosenEntrantsButton.setOnClickListener(v -> {
            waitlist.getChosen(eventID, chosen -> {
                ArrayList<Map<String, String>> fullChosenEntrants = new ArrayList<>();


                if (event.getWaitinglist() != null && !event.getWaitinglist().isEmpty()) {
                    for (Map<String, String> user : event.getWaitinglist()) {
                        if (chosen.contains(user.get("did")) && "chosen".equals(user.get("status"))) {
                            fullChosenEntrants.add(user);
                        }
                    }
                }
                ChosenEntrantsFragment chosenEntrants = new ChosenEntrantsFragment();

                Bundle newBundle = new Bundle();
                newBundle.putSerializable("chosenEntrantsData", fullChosenEntrants);
                newBundle.putString("eventID", event.getEventID());
                newBundle.putSerializable("fragment_waitlist", waitlist);
                newBundle.putString("lotteryCapacity", event.getLotteryCapacity());
                chosenEntrants.setArguments(newBundle);

                // Launch the ChosenEntrantsFragment fragment with the filtered data
                Navigation.findNavController(view).navigate(R.id.action_viewEventFragment_to_chosenEntrantsFragment, bundle);
            });
        });

        cancelledEntrantsButton.setOnClickListener(v -> {
            waitlist.getCancel(eventID, cancel -> {
                if (cancel.isEmpty()) {
                    Toast.makeText(context, "Cancelled entrants list is empty.", Toast.LENGTH_SHORT).show();
                } else {
                    ArrayList<Map<String, String>> fullCancelledEntrants = new ArrayList<>();


                    if (event.getWaitinglist() != null && !event.getWaitinglist().isEmpty()) {
                        for (Map<String, String> user : event.getWaitinglist()) {
                            if (cancel.contains(user.get("did")) && "cancel".equals(user.get("status"))) {
                                fullCancelledEntrants.add(user);
                            }
                        }
                    }

                    CancelledEntrantsFragment cancelledEntrantsFragment = new CancelledEntrantsFragment();

                    Bundle newBundle = new Bundle();
                    newBundle.putSerializable("cancelledEntrantsData", fullCancelledEntrants);
                    newBundle.putString("eventID", event.getEventID());
                    newBundle.putSerializable("fragment_waitlist", waitlist);
                    cancelledEntrantsFragment.setArguments(newBundle);

                    Navigation.findNavController(view).navigate(R.id.action_viewEventFragment_to_cancelledEntrantsFragment, bundle);
                }
            });
        });

        editButton.setOnClickListener(v -> {
            editButton.setVisibility(View.GONE);

            deleteButton.setVisibility(View.VISIBLE);
            editStateButtons.setVisibility(View.VISIBLE);
            uploadImageButton.setVisibility(View.VISIBLE);

            // Make the date related buttons clickable and focusable
            startDateButton.setClickable(true);
            startDateButton.setFocusable(true);
            startDateBar.setVisibility(View.VISIBLE);

            endDateButton.setClickable(true);
            endDateButton.setFocusable(true);
            endDateBar.setVisibility(View.VISIBLE);

            registrationDateButton.setClickable(true);
            registrationDateButton.setFocusable(true);
            registrationDateBar.setVisibility(View.VISIBLE);

            editEventName();
            editDescription();
            editFacility(organizer, context);
            editCapacity();
            editLotteryCapacity();

        });

        deleteButton.setOnClickListener(v -> {
            if (isOwner) {
                new android.app.AlertDialog.Builder(context)
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete this event?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            ArrayList<String> eventNames = organizer.getEventsNames();
                            eventNames.remove(event.getFacilityName());
                            organizer.setEventsNames(eventNames);

                            ArrayList<EventInfo> events = organizer.getEvents();
                            for (int i = 0; i < events.size(); i++) {
                                EventInfo currentEvent = events.get(i);
                                if (currentEvent.getEventID().equals(event.getEventID())) {
                                    events.remove(i);
                                    break;
                                }
                            }
                            organizer.setEvents(events);
                            eventFirebase.deleteEvent(event.getEventID());
                            eventFirebase.editOrganizer(organizer);
                            Navigation.findNavController(view).navigate(R.id.action_viewEventFragment_to_favouriteFragment);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .create()
                        .show();
            } else {
                Toast.makeText(context, "You are not the organizer, cannot delete event.", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> {
            editButton.setVisibility(View.VISIBLE);

            deleteButton.setVisibility(View.GONE);
            editStateButtons.setVisibility(View.GONE);

            uploadImageButton.setVisibility(View.GONE);

            startDateButton.setClickable(false);
            startDateButton.setFocusable(false);
            startDateBar.setVisibility(View.GONE);

            endDateButton.setClickable(false);
            endDateButton.setFocusable(false);
            endDateBar.setVisibility(View.GONE);

            registrationDateButton.setClickable(false);
            registrationDateButton.setFocusable(false);
            registrationDateBar.setVisibility(View.GONE);

            eventNameTextView.setVisibility(View.VISIBLE);
            eventNameEditText.setVisibility(View.GONE);
            eventNameBar.setVisibility(View.GONE);

            eventDescriptionTextView.setVisibility(View.VISIBLE);

            eventDescriptionEditText.setVisibility(View.GONE);
            descriptionBar.setVisibility(View.GONE);

            eventCapacityTextView.setVisibility(View.VISIBLE);
            eventCapacityEditText.setVisibility(View.GONE);
            waitlistBar.setVisibility(View.GONE);

            eventLotteryCapacityTextView.setVisibility(View.VISIBLE);
            eventLotteryCapacityEditText.setVisibility(View.GONE);
            lotteryBar.setVisibility(View.GONE);
        });

        saveButton.setOnClickListener(v -> {
            String newEventName = eventNameEditText.getText().toString();
            String newDescription = eventDescriptionEditText.getText().toString();

            String newEventCapacity = eventCapacityEditText.getText().toString();
            String newEventLotteryCapacity = eventLotteryCapacityEditText.getText().toString();

            if (Integer.parseInt(newEventLotteryCapacity) >= Integer.parseInt(newEventCapacity)) {
                Toast.makeText(context, "Lottery capacity must be less than wishlist!", Toast.LENGTH_SHORT).show();
                return;
            }

            String newStartTime = eventStartTimeTextView.getText().toString();
            String newEndTime = eventEndTimeTextView.getText().toString();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            event.setEventName(newEventName);
            event.setDescription(newDescription);
            event.setCapacity(newEventCapacity);
            event.setLotteryCapacity(newEventLotteryCapacity);
            if (newFacility != null) {
                eventFirebase.addFacility(newFacility);
                Log.d("AddEventFragment", newFacility.getFacilityName());
                ArrayList<FacilitiesInfo> facilitiesList = organizer.getFacilities();
                facilitiesList.add(newFacility);
                organizer.setFacilities(facilitiesList);
                eventFirebase.editOrganizer(organizer);
            }

            Log.d("Current FacilityID", event.getFacilityID());
            Log.d("Logged FacilityID", facilityID);

            if (!event.getFacilityID().equals(facilityID)) {
                eventFirebase.findFacility(event.getFacilityID(), new EventFirebase.FacilityCallback() {
                    @Override
                    public void onSuccess(FacilitiesInfo facilitiesInfo) {
                        if (facilitiesInfo != null) {
                            ArrayList<String> facilityEvents = facilitiesInfo.getEvents();
                            Log.e(TAG, " old facility: " + facilityEvents);

                            facilityEvents.remove(event.getEventID());
                            Log.e(TAG, " old facility updated: " + facilityEvents);

                            facilitiesInfo.setEvents(facilityEvents);

                            String facilityID = facilitiesInfo.getFacilityID();
                            Log.e(TAG, "Facility ID: " + facilityID);

                            if (facilityID != null && !facilityID.isEmpty()) {
                                eventFirebase.editFacility(facilitiesInfo);
                            } else {
                                Log.e(TAG, "Invalid facility ID, cannot update facility.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Error fetching old facility: " + error);
                    }
                });

                eventFirebase.findFacility(facilityID, new EventFirebase.FacilityCallback() {
                    @Override
                    public void onSuccess(FacilitiesInfo newFacility) {
                        if (newFacility != null) {
                            ArrayList<String> newFacilityEvents = newFacility.getEvents();
                            Log.e(TAG, " new facility: " + newFacilityEvents);

                            newFacilityEvents.add(event.getEventID());
                            Log.e(TAG, " new facility updated: " + newFacilityEvents);

                            newFacility.setEvents(newFacilityEvents);

                            eventFirebase.editFacility(newFacility);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Error fetching new facility: " + error);
                    }
                });

                event.setFacilityID(facilityID);
            }

            ArrayList<String> eventNames = organizer.getEventsNames();
            eventNames.remove(event.getFacilityName());
            event.setFacilityName(facility);
            eventNames.add(facility);
            organizer.setEventsNames(eventNames);
            event.setAddress(address);
            event.setLatitude(newLatitude);
            event.setLongitude(newLongitude);
            event.setStartDate(startDate);
            event.setEndDate(endDate);
            event.setStartTime(newStartTime);
            event.setEndTime(newEndTime);
            event.setRegistrationDate(registrationDate);
            event.setEventPoster(newEventPoster);

            eventNameTextView.setText(newEventName);
            eventDescriptionTextView.setText(newDescription);
            eventCapacityTextView.setText(newEventCapacity);
            eventLotteryCapacityTextView.setText(newEventLotteryCapacity);
            eventFacilityTextView.setText(facility);
            eventStartDateTextView.setText(dateFormat.format(startDate));
            eventEndDateTextView.setText(dateFormat.format(endDate));
            eventStartTimeTextView.setText(newStartTime);
            eventEndTimeTextView.setText(newEndTime);
            registrationDateTextView.setText(dateFormat.format(registrationDate));

            registrationDateTextView.setVisibility(View.VISIBLE);
            eventStartDateTextView.setVisibility(View.VISIBLE);
            eventEndDateTextView.setVisibility(View.VISIBLE);
            ;
            eventStartTimeTextView.setVisibility(View.VISIBLE);
            eventEndTimeTextView.setVisibility(View.VISIBLE);

            registrationDateRequirementsTextView.setVisibility(View.GONE);
            dateRequirementsTextView.setVisibility(View.GONE);

            eventNameTextView.setVisibility(View.VISIBLE);
            eventNameEditText.setVisibility(View.GONE);

            eventDescriptionTextView.setVisibility(View.VISIBLE);
            eventDescriptionEditText.setVisibility(View.GONE);

            eventCapacityTextView.setVisibility(View.VISIBLE);
            eventCapacityEditText.setVisibility(View.GONE);

            eventLotteryCapacityTextView.setVisibility(View.VISIBLE);
            eventLotteryCapacityEditText.setVisibility(View.GONE);

            autocompletePlaceFragment.setVisibility(View.GONE);
            eventFacilityTextView.setVisibility(View.VISIBLE);
            eventFacility.setVisibility(View.GONE);

            if (newFacility != null) {
                eventFirebase.addFacility(newFacility);
                ArrayList<FacilitiesInfo> facilitiesList = organizer.getFacilities();
                facilitiesList.add(newFacility);
                organizer.setFacilities(facilitiesList);
                eventFirebase.editOrganizer(organizer);
            }

            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.GONE);
            editStateButtons.setVisibility(View.GONE);
            uploadImageButton.setVisibility(View.GONE);

            startDateButton.setClickable(false);
            startDateButton.setFocusable(false);
            startDateBar.setVisibility(View.GONE);

            endDateButton.setClickable(false);
            endDateButton.setFocusable(false);
            endDateBar.setVisibility(View.GONE);

            registrationDateButton.setClickable(false);
            registrationDateButton.setFocusable(false);
            registrationDateBar.setVisibility(View.GONE);

            ArrayList<EventInfo> events = organizer.getEvents();
            for (int i = 0; i < events.size(); i++) {
                EventInfo currentEvent = events.get(i);
                // Check if the event ID matches the edited event's ID
                if (currentEvent.getEventID().equals(event.getEventID())) {
                    events.set(i, event);
                    break;
                }
            }
            organizer.setEvents(events);
            eventFirebase.editOrganizer(organizer);
            eventFirebase.editEvent(event);

            Toast.makeText(context, "Event updated successfully!", Toast.LENGTH_SHORT).show();
        });

        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        startDateCalendar = Calendar.getInstance();
                        startDateCalendar.set(selectedYear, selectedMonth, selectedDay);
                        Calendar currentDate = Calendar.getInstance();
                        if (startDateCalendar.before(currentDate)) {
                            dateRequirementsTextView.setText("Date Must Be Today or Later.");
                            showWithTransition(dateRequirementsTextView);
                            eventStartDateTextView.setText("MM / DD / YYYY");
                            eventStartTimeTextView.setText("HH / MM");
                            eventStartDateTextView.setTextColor(getResources().getColor(R.color.red));
                            startDateCalendar = null;
                        } else {
                            String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                            eventStartDateTextView.setText(selectedDate);
                            eventStartDateTextView.setTextColor(getResources().getColor(R.color.black));
                            startDate = startDateCalendar.getTime();

                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                    startDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    startDateCalendar.set(Calendar.MINUTE, selectedMinute);

                                    Calendar currentTime = Calendar.getInstance();
                                    if (startDateCalendar.before(currentTime)) {
                                        dateRequirementsTextView.setText("Start Time Must Be Now or Later.");
                                        showWithTransition(dateRequirementsTextView);
                                        eventStartDateTextView.setText("MM / DD / YYYY");
                                        eventStartTimeTextView.setText("HH / MM");
                                        eventStartTimeTextView.setTextColor(getResources().getColor(R.color.red));
                                    } else {
                                        String selectedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                                        eventStartTimeTextView.setText(selectedTime);
                                        eventStartTimeTextView.setTextColor(getResources().getColor(R.color.black));
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

        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
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
                            showWithTransition(dateRequirementsTextView);
                            eventEndDateTextView.setText("MM / DD / YYYY");
                            eventEndTimeTextView.setText("HH / MM");
                            eventStartDateTextView.setTextColor(getResources().getColor(R.color.red));
                            eventStartTimeTextView.setTextColor(getResources().getColor(R.color.red));
                        } else if (endDateCalendar.before(currentDate)) {
                            dateRequirementsTextView.setText("End Date Must Be Today or Later.");
                            showWithTransition(dateRequirementsTextView);
                            eventEndDateTextView.setText("MM / DD / YYYY");
                            eventEndTimeTextView.setText("HH / MM");
                            eventEndDateTextView.setTextColor(getResources().getColor(R.color.red));
                        } else if (endDateCalendar.before(startDateCalendar)) {
                            dateRequirementsTextView.setText("End Date Must Be On or After Start Date.");
                            showWithTransition(dateRequirementsTextView);
                            eventEndDateTextView.setText("MM / DD / YYYY");
                            eventEndTimeTextView.setText("HH / MM");
                            eventEndDateTextView.setTextColor(getResources().getColor(R.color.red));
                        } else {
                            String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                            eventEndDateTextView.setText(selectedDate);
                            eventEndDateTextView.setTextColor(getResources().getColor(R.color.black));

                            endDate = endDateCalendar.getTime();

                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                    endDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    endDateCalendar.set(Calendar.MINUTE, selectedMinute);

                                    if (endDateCalendar.before(startDateCalendar)) {
                                        dateRequirementsTextView.setText("End Time Must Be After Start Time.");
                                        showWithTransition(dateRequirementsTextView);
                                        eventEndDateTextView.setText("MM / DD / YYYY");
                                        eventEndTimeTextView.setText("HH / MM");
                                        eventEndTimeTextView.setTextColor(getResources().getColor(R.color.red));
                                    } else {
                                        String selectedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                                        eventEndTimeTextView.setText(selectedTime);
                                        eventEndTimeTextView.setTextColor(getResources().getColor(R.color.black));
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

        registrationDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view1, int selectedYear, int selectedMonth, int selectedDay) {
                    registrationDateCalendar = Calendar.getInstance();
                    registrationDateCalendar.set(selectedYear, selectedMonth, selectedDay);
                    Calendar currentDate = Calendar.getInstance();
                    if (startDate != null){
                        if (registrationDateCalendar.before(currentDate)) {
                            registrationDateRequirementsTextView.setText("Deadline Cannot Be Before Today.");
                            showWithTransition(registrationDateRequirementsTextView);
                            registrationDateTextView.setText("MM / DD / YYYY");
                            registrationDateTextView.setTextColor(getResources().getColor(R.color.red));
                            registrationDateCalendar = null;
                        }else if (startDateCalendar.before(registrationDateCalendar)) {
                            registrationDateRequirementsTextView.setText("RegistrationFragment deadline must be before the event start date.");
                            showWithTransition(registrationDateRequirementsTextView);
                            registrationDateTextView.setText("MM / DD / YYYY");
                            registrationDateTextView.setTextColor(getResources().getColor(R.color.red));
                            registrationDateCalendar = null;
                        }else {
                            String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                            registrationDateTextView.setText(selectedDate);
                            registrationDateTextView.setTextColor(getResources().getColor(R.color.black));
                            registrationDate = registrationDateCalendar.getTime();
                        }
                    }else{
                        registrationDateRequirementsTextView.setText("Please Select Start Date.");
                        showWithTransition(registrationDateRequirementsTextView);
                        registrationDateTextView.setText("MM / DD / YYYY");
                        registrationDateTextView.setTextColor(getResources().getColor(R.color.red));
                        registrationDateCalendar = null;
                    }

                }
            }, year, month, day);
            dialog.show();
        });
        
    }

    /**
     * @author Simon Haile
     * Displays an editable text field for the event name by hiding the
     * event name text view and showing the corresponding EditText.
     * Sets the EditText's content to the current event name.
     */
    private void editEventName() {
        eventNameTextView.setVisibility(View.GONE);
        eventNameEditText.setVisibility(View.VISIBLE);
        eventNameBar.setVisibility(View.VISIBLE);
        eventNameEditText.setText(event.getEventName());
    }

    /**
     * @author Simon Haile
     * Displays an editable text field for the description by hiding the
     * description text view and showing the corresponding EditText.
     * Sets the EditText's content to the current description.
     */
    private void editDescription() {
        eventDescriptionTextView.setVisibility(View.GONE);
        eventDescriptionEditText.setVisibility(View.VISIBLE);
        descriptionBar.setVisibility(View.VISIBLE);
        eventDescriptionEditText.setText(event.getDescription());

    }

    /**
     * @author Simon Haile
     * Displays an editable text field for the capacity by hiding the
     * capacity text view and showing the corresponding EditText.
     * Sets the EditText's content to the current capacity.
     */
    private void editCapacity() {
        eventCapacityTextView.setVisibility(View.GONE);
        eventCapacityEditText.setVisibility(View.VISIBLE);
        waitlistBar.setVisibility(View.VISIBLE);
        eventCapacityEditText.setText(event.getCapacity());
    }

    private void editLotteryCapacity() {
        eventLotteryCapacityTextView.setVisibility(View.GONE);
        eventLotteryCapacityEditText.setVisibility(View.VISIBLE);
        lotteryBar.setVisibility(View.VISIBLE);
        eventLotteryCapacityEditText.setText(event.getLotteryCapacity());
    }

    /**
     * @param organizer The organizer's information used to retrieve their facilities.
     * @author Simon Haile
     * Displays a spinner to allow the event organizer to choose or add a facility for the event.
     * The spinner is populated with existing facilities, and the "Add Facility" option is added at the end.
     * If a facility is selected, its details are fetched from Firebase. If "Add Facility" is selected,
     * the user can add a new facility using a place autocomplete fragment.
     */
    private void editFacility(OrganizerInfo organizer, Context context) {
        eventFacility.setVisibility(View.VISIBLE);
        eventFacilityTextView.setVisibility(View.GONE);

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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                R.layout.spinner_item, facilityNames);

        // Set drop-down view resource
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        // Set the adapter to the spinner
        eventFacility.setAdapter(adapter);

        // Set the item selection listener for the spinner
        eventFacility.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFacility = parent.getItemAtPosition(position).toString();

                // Check if "Add Facility" is selected
                if (selectedFacility.equals("Add Facility")) {
                    addFacility(facilityNames, adapter, context); // Pass the adapter so we can update it
                } else {
                    // If the selected facility exists, proceed with fetching it
                    facilityID = organizer.getFacilityIdByName(selectedFacility);
                    Log.d("Current Facility ID", facilityID);
                    eventFirebase.findFacility(facilityID, new EventFirebase.FacilityCallback() {
                        @Override
                        public void onSuccess(FacilitiesInfo existingFacility) {
                            address = existingFacility.getAddress();
                            facility = existingFacility.getFacilityName();
                            newLongitude = existingFacility.getLongitude();
                            newLatitude = existingFacility.getLatitude();
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
     * @param facilityNames The list of existing facility names.
     * @param adapter       The adapter used for the facility spinner to update the displayed options.
     * @author Simon Haile
     * Allows the user to add a new facility to the list by using a place autocomplete fragment.
     * The fragment allows the user to select a place, which is then added as a new facility.
     * The facility details (address, name, and coordinates) are captured and added to the facility list.
     * If the facility already exists in the list, a message is displayed to the user.
     */
    private void addFacility(ArrayList<String> facilityNames, ArrayAdapter<String> adapter, Context context) {
        autocompletePlaceFragment.setVisibility(View.VISIBLE);

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(requireContext(), BuildConfig.API_KEY);
        }

        PlacesClient placesClient = Places.createClient(context);

        // Initialize the AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LAT_LNG));

        // Set up the PlaceSelectionListener
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                address = place.getFormattedAddress();
                facility = place.getDisplayName();

                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    newLatitude = latLng.latitude;
                    newLongitude = latLng.longitude;
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
                                    String facilityImage = photoUri.toString();

                                    if (facilityNames.contains(facility)) {
                                        Toast.makeText(context.getApplicationContext(), "This facility has already been added.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        newFacility = new FacilitiesInfo(address, facility, deviceID, newLatitude, newLongitude, facilityImage);
                                        facilityID = newFacility.getFacilityID();
                                        Log.d("New Facility ID One", facilityID);
                                        facilityNames.add(facility);

                                        // Notify the adapter that the data has changed
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
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
     * Initializes the image upload process by setting up an image picker and handling the image
     * upload to Firebase Storage.
     * This method sets up an `ActivityResultLauncher` to handle the image selection from the device.
     * When an image is selected, it is displayed in an `ImageView` (`eventPosterImageView`).
     * The image is then uploaded to Firebase Storage under the "event_posters"
     * directory, with a unique file name generated using `UUID`. Once the upload is successful,
     * the download URL for the uploaded image
     * is stored for later use.
     */
    private void uploadNewPoster(View view, Context context) {
        Button uploadImageButton = view.findViewById(R.id.upload_image_button);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        eventPosterImageView.setVisibility(View.VISIBLE);
                        eventPosterImageView.setImageURI(imageUri);

                        Uri destinationUri = Uri.fromFile(new File(context.getCacheDir(), "cropped_image.jpg"));

                        UCrop.of(imageUri, destinationUri)
                                .withAspectRatio(9, 16)
                                .withMaxResultSize(800, 1600)
                                .start(context, this);
                    }
                }
        );

        uploadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }

    ;

    /**
     * Checks results after coming back from another activity
     *
     * @param requestCode see if the activity we came back from was correct
     * @param resultCode  whether the activity finished correctly
     * @param data        the data obtained from the activity
     * @author Simon Haile
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                eventPosterImageView.setVisibility(View.VISIBLE);
                eventPosterImageView.setImageURI(resultUri);

                StorageReference imageRef = storageRef.child("event_posters/" + UUID.randomUUID().toString() + ".jpg");

                imageRef.putFile(resultUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                newEventPoster = uri.toString();
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "Error getting download URL", e);
                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Upload failed", e);
                        });

                eventPosterImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Log.e(TAG, "Crop error", cropError);
            }
        }
    }

    public void showWithTransition(View view) {
        // Fade in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500); // Duration for fade-in (500ms)
        fadeIn.setFillAfter(true);

        // Fade out animation
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500); // Duration for fade-out (500ms)
        fadeOut.setFillAfter(true);

        // Make the view visible and start fade-in
        view.setVisibility(View.VISIBLE);
        view.startAnimation(fadeIn);

        // Delay for 4 seconds, then start fade-out and hide the view
        new Handler().postDelayed(() -> {
            view.startAnimation(fadeOut);
            new Handler().postDelayed(() -> view.setVisibility(View.GONE), fadeOut.getDuration());
        }, 4000);
    }


}