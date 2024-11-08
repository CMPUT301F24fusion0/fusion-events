package com.example.fusion0;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

/**
 * GeoLocation class manages the user's geolocation, calculates distances
 * to an event location, and determines whether the user can register based
 * on an acceptable radius.
 */
public class GeoLocation implements LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private final Context context;
    private final FragmentActivity fragmentActivity;
    private LocationManager locationManager;
    private Location userLocation;
    private Location eventLocation;
    private double acceptableRadius;

    /**
     * Constructor for GeoLocation that sets an event location and acceptable radius.
     *
     * @param fragmentActivity The activity context to display dialogs.
     * @param context The context in which the location service is accessed.
     * @param eventLatitude The latitude of the event location.
     * @param eventLongitude The longitude of the event location.
     * @param acceptableRadius The radius within which the user can register.
     */
    public GeoLocation(FragmentActivity fragmentActivity, Context context, double eventLatitude, double eventLongitude, double acceptableRadius) {
        this.fragmentActivity = fragmentActivity;
        this.context = context;
        this.acceptableRadius = acceptableRadius;
        this.eventLocation = new Location("eventLocation");
        this.eventLocation.setLatitude(eventLatitude);
        this.eventLocation.setLongitude(eventLongitude);
        initializeLocationManager();
    }

    /**
     * Constructor for GeoLocation to compare a user location to an event location without a pre-set event location and already known user location (User setters).
     *
     * @param fragmentActivity The activity context to display dialogs.
     * @param context The context in which the location service is accessed.
     */
    public GeoLocation(FragmentActivity fragmentActivity, Context context) {
        this.fragmentActivity = fragmentActivity;
        this.context = context;
        initializeLocationManager();
    }


    //The follow 4 functions were implemented with the help of "https://www.youtube.com/watch?v=qY-xFxZ7HKY" - 2024-10-27
    //ChatGPT made changes to help improve the speed at which location was retrieved, "(Provided code in video in function form)  How can I make it so location is retrieved quicker to avoid issues?" - 2024-10-27

    /**
     * Initializes the LocationManager and sets the initial user location if permissions are granted.
     */
    private void initializeLocationManager() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (isLocationPermissionGranted()) {
            setLastKnownLocation();
        }
    }

    /**
     * Sets the last known location for a quicker initial value.
     */
    private void setLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                userLocation = lastKnownLocation;
            }
        }
    }

    /**
     * Callback method invoked when the user's location changes. Updates userLocation and displays a Toast message.
     *
     * @param location The new location of the user.
     */
    @Override
    public void onLocationChanged(@NonNull Location location) {
        userLocation = location;
    }

    /**
     * Checks if location permissions are granted.
     * Requests permissions if not already granted and returns false immediately.
     * The actual result (granted or denied) should be handled in onRequestPermissionsResult.
     *
     * @return true if location permission is already granted; false if permission is requested.
     */
    public boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests location permission from the user if it isn’t already granted.
     */
    public void requestLocationPermission() {
        if (!isLocationPermissionGranted()) {
            ActivityCompat.requestPermissions(fragmentActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Attempts to retrieve the user's current location.
     *
     * If location permissions are granted, it first tries to retrieve the last known location
     * from the GPS provider. If a last known location is available, it is returned immediately.
     *
     * If no last known location is available, it requests location updates from both the GPS
     * and Network providers to obtain the user's location. Location updates will trigger the
     * `onLocationChanged` method when a new location is available.
     *
     * If permissions are not granted or the user has denied the request, it will not attempt
     * to retrieve the location and returns null.
     *
     * @return the user's last known location if available, or null if no immediate location
     *         is available or permissions are not granted.
     */
    public Location getLocation() {
        //If location was already set return it
        if (userLocation != null) {
            return userLocation;
        }
        //If location was not yet set
        if (isLocationPermissionGranted()) {

            // Try to get the last known location
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                userLocation = lastKnownLocation;
                return userLocation;
            }

            // Request location updates if no immediate location is available
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        }
        return null;
    }




    /**
     * Determines if the user can register based on their distance from the event location.
     *
     * @return true if the user is within the acceptable radius from the event location; false otherwise.
     */
    public boolean canRegister() {
        if (userLocation != null && eventLocation != null) {
            double distance = userLocation.distanceTo(eventLocation);
            return distance <= acceptableRadius;
        }
        return false;
    }

    /**
     * Sets the event location based on provided latitude and longitude.
     *
     * @param latitude The latitude of the event location.
     * @param longitude The longitude of the event location.
     */
    public void setEventLocation(double latitude, double longitude) {
        eventLocation = new Location("eventLocation");
        eventLocation.setLatitude(latitude);
        eventLocation.setLongitude(longitude);
    }

    /**
     * Sets the user location based on provided latitude and longitude.
     *
     * @param latitude The latitude of the user location.
     * @param longitude The longitude of the user location.
     */
    public void setUserLocation(double latitude, double longitude) {
        userLocation = new Location("userLocation");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);
    }

    /**
     * Retrieves the user's current latitude.
     *
     * @return The latitude of the user's current location.
     */
    public double getUserLatitude() {
        return userLocation != null ? userLocation.getLatitude() : 0.0;
    }

    /**
     * Retrieves the user's current longitude.
     *
     * @return The longitude of the user's current location.
     */
    public double getUserLongitude() {
        return userLocation != null ? userLocation.getLongitude() : 0.0;
    }

    /**
     * Retrieves the event's current latitude.
     *
     * @return The latitude of the event location.
     */
    public double getEventLatitude() {
        return eventLocation != null ? eventLocation.getLatitude() : 0.0;
    }

    /**
     * Retrieves the event's current longitude.
     *
     * @return The longitude of the event location.
     */
    public double getEventLongitude() {
        return eventLocation != null ? eventLocation.getLongitude() : 0.0;
    }

    /**
     * Sets the event acceptable radius.
     *
     * @param radius The acceptable radius of the event.
     */
    public void setEventRadius(int radius){
        acceptableRadius = radius;
    }


    /**
     * Displays a map dialog showing user and event locations with an indicator of the acceptable radius.
     */
    public void showMapDialog() {
        if (eventLocation != null && userLocation != null) {
            double eventLat = eventLocation.getLatitude();
            double eventLng = eventLocation.getLongitude();
            double userLat = userLocation.getLatitude();
            double userLng = userLocation.getLongitude();

            MapDialogFragment mapDialogFragment = new MapDialogFragment(eventLat, eventLng, userLat, userLng, acceptableRadius);
            mapDialogFragment.show(fragmentActivity.getSupportFragmentManager(), "mapDialog");
        } else {
            Toast.makeText(context, "Locations not set", Toast.LENGTH_SHORT).show();
        }
    }
}
