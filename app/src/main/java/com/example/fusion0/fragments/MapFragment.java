package com.example.fusion0.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.fusion0.BuildConfig;
import com.example.fusion0.R;
import com.example.fusion0.helpers.AddEventHelper;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.AddEventViewModel;
import com.example.fusion0.models.FacilitiesInfo;
import com.example.fusion0.models.OrganizerInfo;
import com.example.fusion0.models.SimpleTextWatcher;
import com.google.android.gms.common.api.Status;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MapFragment extends Fragment {

    private static final String TAG = "AddEventFragment";
    private TextView geolocationTextView, radiusText;
    private androidx.fragment.app.FragmentContainerView autocompletePlaceFragment;

    private EventFirebase eventFirebase = new EventFirebase();
    private EditText radius;

    private Spinner spinnerFacilities;
    private SwitchCompat geolocationSwitchCompact;

    private FacilitiesInfo facility;
    private FacilitiesInfo newFacility = null;
    private String address;
    private String facilityID;
    private String facilityName;
    private String facilityImage;

    private Double latitude;
    private Double longitude;
    private Boolean geolocation = false;

    private FirebaseStorage storage;
    private OrganizerInfo organizer;
    private AddEventViewModel viewModel;

    private String deviceID;
    private LottieAnimationView imageLoadingSpinner;
    private ImageView mapImageView;
    private View lineView;

    public MapFragment() {
        // Required empty public constructor
    }

    @SuppressLint("HardwareIds")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deviceID = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context mapContext = requireContext();
        Activity activity = requireActivity();

        viewModel = new ViewModelProvider(requireActivity()).get(AddEventViewModel.class);
        AddEventHelper helper = viewModel.getHelper();

        spinnerFacilities = view.findViewById(R.id.spinner_facilities);
        autocompletePlaceFragment = view.findViewById(R.id.autocomplete_fragment);

        mapImageView = view.findViewById(R.id.mapImageView);

        geolocationTextView = view.findViewById(R.id.geolocation_text);
        geolocationSwitchCompact = view.findViewById(R.id.geolocation_switchcompat);
        radius = view.findViewById(R.id.radius);
        radiusText = view.findViewById(R.id.radius_text);
        lineView = view.findViewById(R.id.lineView);

        helper.setGeolocation(false);

        radius.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (radius != null && !radius.getText().toString().equals("")) {
                    helper.setGeolocationRadius(Integer.parseInt(radius.getText().toString()) * 1000);
                }
            }
        });

        // Main Processes
        validateOrganizer(mapContext, helper);

        geolocationHandling(helper);
    }

    /**
     * @author Simon Haile
     * Validates and retrieves the organizer associated with the current device ID.
     * This method checks if an organizer already exists in the database based on the device ID.
     * If the organizer is found, it is assigned to the `organizer` variable.
     * If no organizer is found, a new `OrganizerInfo` object is created and added to the database.
     */
    private void validateOrganizer(Context context, AddEventHelper helper) {
        eventFirebase.findOrganizer(deviceID, new EventFirebase.OrganizerCallback() {
            @Override
            public void onSuccess(OrganizerInfo organizerInfo) {
                if (organizerInfo == null) {
                    organizer = new OrganizerInfo(deviceID);
                    eventFirebase.addOrganizer(organizer);
                    helper.setOrganizer(organizer);
                } else {
                    organizer = organizerInfo;
                    helper.setOrganizer(organizer);
                }

                handleFacility(organizer, context, helper);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error fetching organizer: " + error);
            }
        });
    }

    /**
     * @param organizer The organizer's information used to retrieve their facilities.
     * @author Simon Haile
     * Displays a spinner to allow the event organizer to choose or add a facility for the event.
     * The spinner is populated with existing facilities, and the "Add Facility" option is added at the end.
     * If a facility is selected, its details are fetched from Firebase. If "Add Facility" is selected,
     * the user can add a new facility using a place autocomplete fragment.
     */
    private void handleFacility(OrganizerInfo organizer, Context context, AddEventHelper helper) {
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_dropdown_item, facilityNames);

        // Set drop-down view resource
        adapter.setDropDownViewResource(R.layout.spinner_item);

        // Set the adapter to the spinner
        imageLoadingSpinner = requireView().findViewById(R.id.previewAnimation);
        imageLoadingSpinner.playAnimation();
        spinnerFacilities.setAdapter(adapter);

        // Set the item selection listener for the spinner
        spinnerFacilities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFacility = parent.getItemAtPosition(position).toString();

                // Check if "Add Facility" is selected
                if (selectedFacility.equals("Add Facility")) {
                    addFacility(facilityNames, adapter, context, helper); // Pass the adapter so we can update it
                    helper.setFacilityName(selectedFacility);
                } else {
                    // If the selected facility exists, proceed with fetching it
                    facilityID = organizer.getFacilityIdByName(selectedFacility);
                    helper.setFacilityID(facilityID);
                    eventFirebase.findFacility(facilityID, new EventFirebase.FacilityCallback() {
                        @Override
                        public void onSuccess(FacilitiesInfo existingFacility) {
                            facility = existingFacility;
                            address = facility.getAddress();
                            facilityName = facility.getFacilityName();
                            longitude = facility.getLongitude();
                            latitude = facility.getLatitude();

                            helper.setFacility(facility);
                            helper.setAddress(address);
                            helper.setFacilityName(facilityName);
                            helper.setLatitude(latitude);
                            helper.setLongitude(longitude);
                            displayMapView(mapImageView, latitude , longitude, context, imageLoadingSpinner);
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
    private void addFacility(ArrayList<String> facilityNames, ArrayAdapter<String> adapter, Context context, AddEventHelper helper) {
        Activity activity = requireActivity();

        autocompletePlaceFragment.setVisibility(View.VISIBLE);

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(requireContext(), BuildConfig.API_KEY);
        }

        // Initialize PlacesClient after the API is enabled
        PlacesClient placesClient = Places.createClient(context);

        // Initialize the AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return
        assert autocompleteFragment != null;
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS));

        // Set up the PlaceSelectionListener
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                address = place.getFormattedAddress();
                facilityName = place.getDisplayName();

                helper.setAddress(address);
                helper.setFacilityName(facilityName);

                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    latitude = latLng.latitude;
                    longitude = latLng.longitude;

                    helper.setLatitude(latitude);
                    helper.setLongitude(longitude);
                }

                final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);

                final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(Objects.requireNonNull(place.getId()), fields);
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
                                    facilityImage = photoUri.toString();

                                    // Check if the facility name already exists in the list of facility names
                                    if (facilityNames.contains(facilityName)) {
                                        Toast.makeText(activity.getApplicationContext(), "This facility has already been added.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        newFacility = new FacilitiesInfo(address, facilityName, deviceID, latitude, longitude, facilityImage);
                                        facility = newFacility;
                                        facilityNames.add(facilityName);


                                        helper.setFacility(facility);
                                        helper.setNewFacility(newFacility);
                                        helper.setFacilityID(newFacility.getFacilityID());

                                        // Notify the adapter that the data has changed
                                        adapter.notifyDataSetChanged();

                                        // Optionally, show a toast indicating the facility was added
                                        Toast.makeText(activity.getApplicationContext(), "New facility added: " + facilityName, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.w(TAG, "Fetched photo URI is null.");
                                }
                            })
                            .addOnFailureListener(exception -> {
                                Log.e(TAG, "Error fetching photo URI: " + exception.getMessage());
                            });
                }).addOnFailureListener(exception -> {
                    Log.e(TAG, "Error fetching place details: " + exception.getMessage());
                });
                displayMapView(mapImageView, latitude, longitude, context, imageLoadingSpinner);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    /**
     * @author Simon Haile
     * Handles geolocation switch compact. Sets geolocation to true if switch compact is checked.
     */
    private void geolocationHandling(AddEventHelper helper) {
        geolocationSwitchCompact.setOnCheckedChangeListener((buttonView, isChecked) -> {
            geolocation = isChecked;
            helper.setGeolocation(geolocation);
            if (isChecked) {
                radiusText.setVisibility(View.VISIBLE);
                radius.setVisibility(View.VISIBLE);
                lineView.setVisibility(View.VISIBLE);
            } else {
                radiusText.setVisibility(View.GONE);
                radius.setVisibility(View.GONE);
                lineView.setVisibility(View.GONE);
                radius.setText("0");
                helper.setGeolocationRadius(Integer.parseInt(radius.getText().toString()) * 1000);
            }
        });
    }

    private void displayMapView(ImageView mapView, Double latitude, Double longitude, Context context, LottieAnimationView imageLoadingSpinner) {
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

        imageLoadingSpinner.setVisibility(View.GONE);
        mapView.setVisibility(View.VISIBLE);
        Glide.with(context)
                .load(staticMapUrl)
                .into(mapView);
    }
}