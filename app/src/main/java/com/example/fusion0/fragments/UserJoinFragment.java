package com.example.fusion0.fragments;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.fusion0.BuildConfig;
import com.example.fusion0.R;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.GeoLocation;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.helpers.Waitlist;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.FacilitiesInfo;
import com.example.fusion0.models.OrganizerInfo;
import com.example.fusion0.models.UserInfo;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserJoinFragment extends Fragment {
    private String deviceID;
    private Boolean isOwner = false;

    private Spinner eventFacility;

    // Text related views
    private TextView eventNameTextView;
    private TextView eventFacilityTextView;
    private TextView eventDescriptionTextView;
    private TextView eventStartDateTextView;
    private TextView eventEndDateTextView;
    private TextView eventStartTimeTextView;
    private TextView eventEndTimeTextView;
    private TextView registrationDateTextView;
    private TextView registrationDateRequirementsTextView;
    private TextView registrationPassedFullTextView;
    private TextView waitinglistFullTextView;
    private TextView dateRequirementsTextView;
    private TextView eventWaitlistCapacityTextView;
    private TextView eventLotteryCapacityTextView;
    private TextView startMonth;
    private TextView startDateTextView;

    // Image related views and buttons
    private ImageView eventPosterImageView;
    private ImageView qrImageView;
    private ImageView facilityButton;
    private ImageButton backButton;

    private FloatingActionButton joinButton;

    // Manipulative objects
    private EventInfo event;
    private EventFirebase eventFirebase;
    private UserInfo user;
    private OrganizerInfo organizer;
    private TextView organizerName;
    private Calendar startDateCalendar;
    private Calendar registrationDateCalendar;
    private FacilitiesInfo newFacility;
    private  Waitlist waitlist = new Waitlist();
    private StorageReference storageRef;

    private String newEventPoster;
    private String facility;
    private String address;
    private String facilityID;
    private Location userLocation;

    private Date endDate;
    private Date startDate;
    private Date registrationDate;

    private ShimmerFrameLayout userJoinEventSkeletonLayout;
    private ScrollView scrollContainer;

    public UserJoinFragment() {
        // Required empty public constructor
    }

    @SuppressLint("HardwareIds")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eventFirebase = new EventFirebase();
        storageRef = FirebaseStorage.getInstance().getReference();
        deviceID = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_join, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();

        userJoinEventSkeletonLayout = view.findViewById(R.id.userJoinEventSkeletonLayout);
        scrollContainer = view.findViewById(R.id.scroll_container);

        backButton = view.findViewById(R.id.backButton);
        eventNameTextView = view.findViewById(R.id.EventName);
        eventDescriptionTextView = view.findViewById(R.id.description);
        eventFacility = view.findViewById(R.id.spinner_facilities);
        eventFacilityTextView = view.findViewById(R.id.facilityName);
        facilityButton = view.findViewById(R.id.facility_view_button);
        eventStartDateTextView = view.findViewById(R.id.start_date_text);
        eventEndDateTextView = view.findViewById(R.id.end_date_text);
        eventEndTimeTextView = view.findViewById(R.id.end_time_text);
        eventStartTimeTextView = view.findViewById(R.id.start_time_text);
        eventWaitlistCapacityTextView = view.findViewById(R.id.capacity);
        eventLotteryCapacityTextView = view.findViewById(R.id.lotteryCapacity);
        dateRequirementsTextView = view.findViewById(R.id.date_requirements_text);
        registrationDateRequirementsTextView = view.findViewById(R.id.registrationDateRequirementsTextView);
        registrationDateTextView = view.findViewById(R.id.registration_date_text);
        registrationPassedFullTextView = view.findViewById(R.id.registration_passed_text_view);
        waitinglistFullTextView = view.findViewById(R.id.waitinglist_full_text_view);
        eventPosterImageView = view.findViewById(R.id.uploaded_image_view);
        qrImageView = view.findViewById(R.id.qrImage);
        startMonth = view.findViewById(R.id.startMonth);
        startDateTextView = view.findViewById(R.id.startDateTextView);
        organizerName = view.findViewById(R.id.organizerName);
        joinButton = view.findViewById(R.id.join_button);

        loadScreen();

        backButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_userJoinFragment_to_mainFragment);
        });

        facilityButton.setOnClickListener(v -> {
            Bundle userJoinedBundle = new Bundle();
            userJoinedBundle.putString("facilityID", event.getFacilityID());
            userJoinedBundle.putString("eventID", event.getEventID());
            userJoinedBundle.putString("ID", "userJoinedEvent");
            Navigation.findNavController(view).navigate(R.id.action_userJoinFragment_to_viewFacilityFragment, userJoinedBundle);
        });

        Bundle bundle = getArguments();
        assert bundle != null;
        String eventID = bundle.getString("eventID");


        if (eventID != null) {
            eventFirebase.findEvent(eventID, new EventFirebase.EventCallback() {
                @Override
                public void onSuccess(EventInfo eventInfo) throws WriterException {
                    if (eventInfo == null) {
                        Toast.makeText(context, "Event Unavailable.", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(view).navigate(R.id.action_userJoinFragment_to_mainFragment);
                    } else {

                        event = eventInfo;

                        new UserFirestore().findUser(eventInfo.getOrganizer(), new UserFirestore.Callback() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onSuccess(UserInfo userInfo) {
                                Log.d("Event Organizer:", eventInfo.getOrganizer());
                                String firstName = userInfo.getFirstName();
                                String lastName = userInfo.getLastName();
                                organizerName.setText(firstName + " " + lastName);
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e("ViewEventFragment", "Error fetching user: " + error);
                            }
                        });

                        eventNameTextView.setText(event.getEventName());
                        eventDescriptionTextView.setText(event.getDescription());
                        eventWaitlistCapacityTextView.setText(event.getCapacity());
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

                        // Set the card view
                        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                        int monthIndex = calendar.get(Calendar.MONTH);
                        String monthAbbreviation = new DateFormatSymbols().getShortMonths()[monthIndex].toUpperCase();

                        startMonth.setText(monthAbbreviation);
                        startDateTextView.setText(day);

                        newEventPoster = event.getEventPoster();

                        Double latitudeSet = event.getLatitude();
                        Double longitudeSet = event.getLongitude();

                        displayMapView(facilityButton, latitudeSet, longitudeSet, context);

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
                            Bundle bundle = new Bundle();
                            bundle.putString("eventID", eventID);
                            Navigation.findNavController(view).navigate(R.id.action_userJoinFragment_to_viewEventFragment, bundle);
                        }
                        populateScreen();
                    }


                    ArrayList<Map<String, String>> currentEntrants = event.getWaitinglist();
                    int capacity = Integer.parseInt(event.getCapacity());

                    waitlist.getAll(eventID, all -> {
                        Calendar calendar = Calendar.getInstance();
                        Date currentDate = calendar.getTime();
                        if (!all.contains(deviceID) & (currentEntrants.size() < capacity) & !(event.getRegistrationDate().before(currentDate))) {
                            joinButton.setVisibility(View.VISIBLE);
                            joinButton.setOnClickListener(view -> {
                                new UserFirestore().findUser(deviceID, new UserFirestore.Callback() {
                                    @Override
                                    public void onSuccess(UserInfo userInfo) {
                                        if (userInfo != null) {
                                            user = userInfo;
                                            if (event.getGeolocation()) {
                                                GeoLocation geoLocation = new GeoLocation(getActivity(), context, event.getLatitude(), event.getLongitude(), event.getRadius());
                                                Log.d("ViewEventFragment", "Radius: " + event.getRadius());
                                                if (!geoLocation.isLocationPermissionGranted()) {
                                                    geoLocation.requestLocationPermission();
                                                } else {
                                                    proceedWithJoin(geoLocation, view, context);
                                                }
                                            } else {
                                                addUserToWaitingList(view, context);
                                            }
                                        } else {
                                            Bundle bundle = new Bundle();
                                            bundle.putString("destination", "userjoin");
                                            bundle.putString("eventIDJ", eventID);
                                            Navigation.findNavController(view).navigate(R.id.action_userJoinFragment_to_registrationFragment, bundle);
                                        }
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        System.out.println("Failure" + error);
                                    }
                                });
                            });
                        } else if (!all.contains(deviceID) & event.getRegistrationDate().before(currentDate)) {
                            showWithTransition(registrationPassedFullTextView);
                            joinButton.setOnClickListener(v->{
                                Toast.makeText(context, "Registration Deadline has passed.", Toast.LENGTH_SHORT).show();

                            });

                        } else if (!all.contains(deviceID) & (currentEntrants.size() >= capacity)) {
                            showWithTransition(waitinglistFullTextView);
                            joinButton.setOnClickListener(v->{
                                Toast.makeText(context, "Waiting List is full.", Toast.LENGTH_SHORT).show();

                            });


                        } else if (all.contains(deviceID)) {
                            joinButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_minus));
                            joinButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.red, context.getTheme()));
                            joinButton.setOnClickListener(view -> {
                                Toast.makeText(context, "You have left the waiting list", Toast.LENGTH_SHORT).show();

                                // Remove it on the events collection
                                ArrayList<Map<String, String>> newWaitingList = event.removeUserFromWaitingList(deviceID, event.getWaitinglist());
                                event.setWaitinglist(newWaitingList);

                                // Remove it on the user's collection
                                waitlist.removeFromUserWL(deviceID, eventID, user);

                                eventFirebase.editEvent(event);

                                new UserFirestore().findUser(deviceID, new UserFirestore.Callback() {
                                    @Override
                                    public void onSuccess(UserInfo userInfo) {
                                        user = userInfo;
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Log.e("JoinedEventFragment", "Error fetching user: " + error);
                                    }
                                });

                                ArrayList<String> newEventsList = user.removeEventFromEventList(event.getEventID(), user.getEvents());
                                user.setEvents(newEventsList);
                                new UserFirestore().editUserEvents(user);

                                eventFirebase.editEvent(event);

                                Navigation.findNavController(view).navigate(R.id.action_userJoinFragment_to_favouriteFragment);
                            });
                        }
                    });

                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Error fetching new facility: " + error);
                }
            });
        } else {
            Toast.makeText(context, "Invalid Event ID.", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigate(R.id.action_viewEventFragment_to_mainFragment);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        eventPosterImageView.setOnClickListener(v -> {
            View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_image_view, null);

            ImageView fullScreenImageView = popupView.findViewById(R.id.full_screen_image_view);

            Glide.with(getActivity())
                    .load(Uri.parse(event.getEventPoster()))
                    .into(fullScreenImageView);

            // Get the exit button from the popup layout
            Button exitButton = popupView.findViewById(R.id.exit_button);

            // Create a PopupWindow to display the custom popup layout
            PopupWindow dialog = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            dialog.setOutsideTouchable(true);
            dialog.setFocusable(true);

            dialog.showAtLocation(eventPosterImageView, Gravity.CENTER, 0, 0);

            // Set up the exit button to dismiss the popup
            exitButton.setOnClickListener(v1 -> {
                dialog.dismiss();
            });
        });
        qrImageView.setOnClickListener(v -> {
            View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_image_view, null);

            ImageView fullScreenImageView = popupView.findViewById(R.id.full_screen_image_view);

            String qrCode = event.getQrCode();

            if (qrCode != null && !qrCode.isEmpty()) {
                Bitmap qrBitmap = null;
                try {
                    qrBitmap = event.generateQRCodeImage(500, 500, qrCode);
                } catch (WriterException e) {
                    throw new RuntimeException(e);
                }
                fullScreenImageView.setImageBitmap(qrBitmap);
            }
            // Get the exit button from the popup layout
            Button exitButton = popupView.findViewById(R.id.exit_button);

            // Create a PopupWindow to display the custom popup layout
            PopupWindow dialog = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            dialog.setOutsideTouchable(true);
            dialog.setFocusable(true);

            dialog.showAtLocation(qrImageView, Gravity.CENTER, 0, 0);

            // Set up the exit button to dismiss the popup
            exitButton.setOnClickListener(v1 -> {
                dialog.dismiss();
            });
        });
    }


    /**
     * @param geoLocation Geolocation object of the user
     * @author Derin Karas
     * Proceed with join after user's location is validated
     */
    private void proceedWithJoin(GeoLocation geoLocation, View view, Context context) {
        userLocation = geoLocation.getLocation();
        if (userLocation == null) {
            Toast.makeText(context, "Retrieving your location...", Toast.LENGTH_SHORT).show();
            userLocation = geoLocation.getLocation();
        }

        //Once the location is retrieved, proceed with registration check
        validateDistanceAndJoin(geoLocation, view, context);
    }

    /**
     * @param geoLocation Geolocation object of the user
     * @author Derin Karas
     * Validate the user's location
     */
    private void validateDistanceAndJoin(GeoLocation geoLocation, View view, Context context) {
        //geoLocation.setUserLocation(userLocation.getLatitude(), userLocation.getLongitude());
        if (geoLocation.canRegister()) {
            addUserToWaitingList(view, context);
        } else {
            geoLocation.showMapDialog(); //Optionally show the user a map with the event location and radius
            Toast.makeText(context, "You are outside the acceptable radius to join this event.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @author Simon Haile, Derin Karas, Sehej Brar
     * Adds the joined event to the user attribute 'events' and adds user to the event waitinglist
     */
    private void addUserToWaitingList(View view, Context context) {
        ArrayList<String> eventsList = user.getEvents();
        eventsList.add(event.getEventID());
        waitlist.addToUserWL(deviceID, event.getEventID(), user);

        ArrayList<Map<String, String>> currentEntrants = event.getWaitinglist();

        if (event.getGeolocation()) {
            Map<String, String> newEntrant = new HashMap<>();
            newEntrant.put("did", deviceID);
            newEntrant.put("latitude", String.valueOf(userLocation.getLatitude()));
            newEntrant.put("longitude", String.valueOf(userLocation.getLongitude()));
            newEntrant.put("status", "waiting");
            currentEntrants.add(newEntrant);
        } else {
            Map<String, String> newEntrant = new HashMap<>();
            newEntrant.put("did", deviceID);
            newEntrant.put("latitude", null);
            newEntrant.put("longitude", null);
            newEntrant.put("status", "waiting");
            currentEntrants.add(newEntrant);
            Log.d("Checkpoint", "Current Entrants: " + currentEntrants);
        }

        event.setWaitinglist(currentEntrants);


        eventFirebase.editEvent(event);
        Toast.makeText(context, "Joined Waiting List Successfully.", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(view).navigate(R.id.action_userJoinFragment_to_favouriteFragment);
    }

    /**
     * Show view with transition
     * @author Nimi Akinroye
     * @param view
     */
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

    /**
     * Display map of entrants
     * @author Nimi Akinroye
     * @param mapView map image
     * @param latitude latitude
     * @param longitude longitude
     * @param context context
     */
    private void displayMapView(ImageView mapView, Double latitude, Double longitude, Context context) {
        String staticMapUrl;
        if (latitude > longitude) {
            staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap?center="
                    + latitude + "," + longitude
                    + "&zoom=15&size=640x480&maptype=roadmap"
                    + "&markers=color:red%7Clabel:F%7C" + latitude + "," + longitude
                    + "&key=" + BuildConfig.API_KEY;
        } else {
            staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap?center="
                    + longitude + "," + latitude
                    + "&zoom=15&size=640x480&maptype=roadmap"
                    + "&markers=color:red%7Clabel:F%7C" + longitude + "," + latitude
                    + "&key=" + BuildConfig.API_KEY;
        }

        Log.d("StaticMapURL", staticMapUrl); // Log the URL for debugging

        mapView.setVisibility(View.VISIBLE);
        Glide.with(context)
                .load(staticMapUrl)
                .into(mapView);
    }

    /**
     * Load the screen
     * @author Nimi Akinroye
     */
    private void loadScreen() {
        scrollContainer.setVisibility(View.GONE);
        eventPosterImageView.setVisibility(View.GONE);

        userJoinEventSkeletonLayout.startShimmerAnimation();
        userJoinEventSkeletonLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Populate the screen
     * @author Nimi Akinroye
     */
    private void populateScreen() {
        userJoinEventSkeletonLayout.stopShimmerAnimation();
        userJoinEventSkeletonLayout.setVisibility(View.GONE);

        eventPosterImageView.setVisibility(View.VISIBLE);
        scrollContainer.setVisibility(View.VISIBLE);
    }


}