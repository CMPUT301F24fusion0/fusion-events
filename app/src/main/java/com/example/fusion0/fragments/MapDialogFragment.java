package com.example.fusion0.fragments;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.fusion0.R;

import java.util.List;

//The following class was helped by ChatGPT, "following the code given by: https://www.youtube.com/watch?v=JzxjNNCYt_o
//(I implemented and provided my code from this video). How do I make a dialog fragment which uses Google Maps within?"
//ChatGPT provided me everything following "Set up the map fragment within the dialog".

public class MapDialogFragment extends DialogFragment {

    private double eventLat;
    private double eventLng;
    private double userLat; //Latitude for a single user
    private double userLng; //Longitude for a single user
    private List<double[]> userLatLngList; //List of user locations for multiple users
    private double allowedRadius;
    private boolean isSingleUser; //Flag to indicate single or multiple user mode

    //Constructor for single user
    public MapDialogFragment(double eventLat, double eventLng, double userLat, double userLng, double allowedRadius) {
        this.eventLat = eventLat;
        this.eventLng = eventLng;
        this.userLat = userLat;
        this.userLng = userLng;
        this.allowedRadius = allowedRadius;
        this.isSingleUser = true; //Single user mode
    }

    //Constructor for multiple users
    public MapDialogFragment(double eventLat, double eventLng, List<double[]> userLatLngList, double allowedRadius) {
        this.eventLat = eventLat;
        this.eventLng = eventLng;
        this.userLatLngList = userLatLngList;
        this.allowedRadius = allowedRadius;
        this.isSingleUser = false; //Multiple user mode
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_map, container, false);

        Button backButton = view.findViewById(R.id.backButton);
        TextView distanceText = view.findViewById(R.id.distanceText);

        //For single user mode, calculate and display distance
        if (isSingleUser) {
            Location eventLocation = new Location("Event");
            eventLocation.setLatitude(eventLat);
            eventLocation.setLongitude(eventLng);

            Location userLocation = new Location("User");
            userLocation.setLatitude(userLat);
            userLocation.setLongitude(userLng);

            float distanceInMeters = userLocation.distanceTo(eventLocation);

            if (distanceInMeters <= allowedRadius) {
                distanceText.setText("Within the event radius.");
            } else {
                distanceText.setText("Outside the event radius.");
            }
        }
        else {
            distanceText.setText("Displaying multiple user locations.");
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        //Set up the map fragment within the dialog
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = new SupportMapFragment();
            getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                LatLng eventLocation = new LatLng(eventLat, eventLng);

                //Add a marker for the event location
                googleMap.addMarker(new MarkerOptions().position(eventLocation).title("Event Location"));

                //Add a circle for the allowed radius
                googleMap.addCircle(new CircleOptions()
                        .center(eventLocation)
                        .radius(allowedRadius)
                        .strokeColor(0xFFFF0000)
                        .fillColor(0x44FF0000)
                        .strokeWidth(2));

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(eventLocation);

                if (isSingleUser) {
                    LatLng userLocation = new LatLng(userLat, userLng);
                    googleMap.addMarker(new MarkerOptions().position(userLocation).title("User Location"));
                    builder.include(userLocation);
                }
                else {
                    //Multiple users logic
                    for (double[] latLng : userLatLngList) {
                        LatLng userLocation = new LatLng(latLng[0], latLng[1]);
                        googleMap.addMarker(new MarkerOptions().position(userLocation).title("User Location"));
                        builder.include(userLocation);
                    }
                }

                LatLngBounds bounds = builder.build();
                int padding = 200;
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            }
        });

        return view;
    }
}
