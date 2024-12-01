package com.example.fusion0.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fusion0.adapters.AddEventPageAdapter;
import com.example.fusion0.helpers.AddEventHelper;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.AddEventViewModel;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.FacilitiesInfo;
import com.example.fusion0.models.OrganizerInfo;
import com.example.fusion0.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.zxing.WriterException;

import java.util.ArrayList;

/**
 * @author Simon Haile
 * This activity allows users to create events
 */
public class AddEventFragment extends Fragment {

    private AddEventViewModel viewModel;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

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

    private ImageButton addEventButton;
    private EventFirebase eventFirebase =new EventFirebase();

    public AddEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Shared ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(AddEventViewModel.class);
        // Initialize default values for AddEventHelper if needed
        viewModel.initializeHelper(new AddEventHelper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();

        // Initialize ViewPager2 and TabLayout
        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);

        addEventButton = view.findViewById(R.id.save_button);

        initializeToolbarButtons(view, context);

        // Set up ViewPager2 and TabLayout
        viewPager.setAdapter(new AddEventPageAdapter(this));

        // Link TabLayout and ViewPager2
        @SuppressLint("SetTextI18n") TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            View customTabView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_tab, null);
            TextView textView = customTabView.findViewById(R.id.tab_title);
            if (position == 0) {
                textView.setText("INFO");
            } else if (position == 1) {
                textView.setText("FACILITY");
            }
            tab.setCustomView(customTabView);
        });
        tabLayoutMediator.attach();

        AddEvent(context, view);
    }


    /**
     * @author Simon Haile
     * Handles the addition of a new event using helper values from ViewModel.
     * Validates user input fields and creates a new event if valid.
     * The event is added to Firebase and associated organizer and facility records.
     */
    private void AddEvent(Context context, View view) {

        addEventButton.setOnClickListener(v -> {
            AddEventHelper helper = viewModel.getHelper(); // Retrieve the helper from ViewModel

            System.out.println(helper.toString());

            // Validate fields retrieved from helper
            if (TextUtils.isEmpty(helper.getEventName())) {
                Toast.makeText(context, "Event name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(helper.getWaitlistCapacity())) {
                Toast.makeText(context, "Capacity is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(helper.getLotteryCapacity()) ||
                    Integer.parseInt(helper.getLotteryCapacity()) >= Integer.parseInt(helper.getWaitlistCapacity())) {
                Toast.makeText(context, "Lottery Capacity must be less than Waitlist Capacity", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(helper.getDescription())) {
                Toast.makeText(context, "Description is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (helper.getStartDate() == null || helper.getEndDate() == null) {
                Toast.makeText(context, "Start or End date is missing", Toast.LENGTH_SHORT).show();
                return;
            }
            if (helper.getRegistrationDate() == null) {
                Toast.makeText(context, "Registration deadline is missing", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(helper.getStartTime()) || TextUtils.isEmpty(helper.getEndTime())) {
                Toast.makeText(context, "Start and End times are required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(helper.getEventPoster())) {
                Toast.makeText(context, "Event poster is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(helper.getFacilityName()) || helper.getFacility() == null) {
                Toast.makeText(context, "Facility is missing", Toast.LENGTH_SHORT).show();
                return;
            }

            EventInfo newEvent;
            try {
                newEvent = new EventInfo(
                        helper.getDeviceID(),
                        helper.getEventName(),
                        helper.getAddress(),
                        helper.getFacilityID(),
                        helper.getFacilityName(),
                        helper.getWaitlistCapacity(),
                        helper.getLotteryCapacity(),
                        helper.getDescription(),
                        helper.getStartDate(),
                        helper.getEndDate(),
                        helper.getRegistrationDate(),
                        helper.getStartTime(),
                        helper.getEndTime(),
                        helper.getEventPoster(),
                        helper.getGeolocation(),
                        helper.getLongitude(),
                        helper.getLatitude(),
                        helper.getGeolocationRadius() // Convert km to meters
                );
            } catch (WriterException e) {
                throw new RuntimeException(e);
            }

            // Add facility if it's a new one
            OrganizerInfo organizer = helper.getOrganizer();
            if (helper.getNewFacility() != null) {
                eventFirebase.addFacility(helper.getNewFacility());
                ArrayList<FacilitiesInfo> facilitiesList = organizer.getFacilities();
                facilitiesList.add(helper.getNewFacility());
                organizer.setFacilities(facilitiesList);
                eventFirebase.editOrganizer(organizer);
            }

            // Add the event to Firebase
            eventFirebase.addEvent(newEvent);

            // Update organizer's list of events
            ArrayList<EventInfo> eventsList = organizer.getEvents();
            eventsList.add(newEvent);
            organizer.setEvents(eventsList);
            eventFirebase.editOrganizer(organizer);

            // Update facility's list of events
            ArrayList<String> facilityEventsList = helper.getFacility().getEvents();
            facilityEventsList.add(newEvent.eventID);
            helper.getFacility().setEvents(facilityEventsList);
            eventFirebase.editFacility(helper.getFacility());

            Toast.makeText(context, "Event Added Successfully!", Toast.LENGTH_SHORT).show();

            // Navigate back to the main fragment
            Navigation.findNavController(view).navigate(R.id.action_eventFragment_to_mainFragment);
        });
    }

    /**
     * Initializes the toolbar and sends them to the correct page if the button is clicked.
     */
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
        setActiveButton(context, addImageButton, addTextView);

        homeButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_eventFragment_to_mainFragment));

        profileButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_eventFragment_to_profileFragment));

        scannerButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_eventFragment_to_qrFragment));

        favouriteButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_eventFragment_to_favouriteFragment));
    }

    private void setAllButtonsInactive(Context context) {
        profileImageButton.setColorFilter(ContextCompat.getColor(context, R.color.grey));
        scannerImageButton.setColorFilter(ContextCompat.getColor(context, R.color.grey));
        homeImageButton.setColorFilter(ContextCompat.getColor(context, R.color.grey));
        favouriteImageButton.setColorFilter(ContextCompat.getColor(context, R.color.grey));

        scannerTextView.setTextColor(ContextCompat.getColor(context, R.color.grey));
        homeTextView.setTextColor(ContextCompat.getColor(context, R.color.grey));
        searchTextView.setTextColor(ContextCompat.getColor(context, R.color.grey));
        profileTextView.setTextColor(ContextCompat.getColor(context, R.color.grey));
    }

    private void setActiveButton(Context context, ImageButton activeButton, TextView activeTextView) {
        activeButton.setColorFilter(ContextCompat.getColor(context, R.color.royalBlue));
        activeTextView.setTextColor(ContextCompat.getColor(context, R.color.royalBlue));
    }
}