package com.example.fusion0;

import android.app.Dialog;
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
import android.location.Location;

//The following class was helped by ChatGPT, "following the code given by: "https://www.youtube.com/watch?v=JzxjNNCYt_o (I implemented and provided my code from this video) How do I make a dialog fragment which uses google maps within?"
//ChatGPT provided me everything following "Set up the map fragment within the dialog"

public class MapDialogFragment extends DialogFragment {

    private double eventLat;
    private double eventLng;
    private double userLat;
    private double userLng;
    private double allowedRadius;

    public MapDialogFragment(double eventLat, double eventLng, double userLat, double userLng, double allowedRadius) {
        this.eventLat = eventLat;
        this.eventLng = eventLng;
        this.userLat = userLat;
        this.userLng = userLng;
        this.allowedRadius = allowedRadius;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_map, container, false);

        Button backButton = view.findViewById(R.id.backButton);
        TextView distanceText = view.findViewById(R.id.distanceText);

        Location eventLocation = new Location("Event");
        eventLocation.setLatitude(eventLat);
        eventLocation.setLongitude(eventLng);

        Location userLocation = new Location("User");
        userLocation.setLatitude(userLat);
        userLocation.setLongitude(userLng);

        float distanceInMeters = userLocation.distanceTo(eventLocation);

        // heck if the user is within the allowed radius and update the TextView
        if (distanceInMeters <= allowedRadius) {
            distanceText.setText("Within the event radius.");
        } else {
            distanceText.setText("Outside the event radius.");
        }

        //Back click listener to dismiss the dialog
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
                LatLng userLocation = new LatLng(userLat, userLng);

                googleMap.addMarker(new MarkerOptions().position(eventLocation).title("Event Location"));
                googleMap.addMarker(new MarkerOptions().position(userLocation).title("User Location"));

                googleMap.addCircle(new CircleOptions()
                        .center(eventLocation)
                        .radius(allowedRadius)
                        .strokeColor(0xFFFF0000)
                        .fillColor(0x44FF0000)
                        .strokeWidth(2));

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(eventLocation);
                builder.include(userLocation);
                LatLngBounds bounds = builder.build();

                int padding = 200;
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            }
        });

        return view;
    }
}

