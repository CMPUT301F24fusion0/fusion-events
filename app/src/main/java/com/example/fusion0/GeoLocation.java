package com.example.fusion0;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

//Javadocs provided by chatGPT

/**
 * GeoLocation class manages the user's geolocation, calculates distances
 * to an event location, and determines whether the user can register based
 * on an acceptable radius.
 */
public class GeoLocation implements LocationListener {
    private final Context context;
    private LocationManager locationManager;
    private Location userLocation;
    private Location eventLocation;
    private float acceptableRadius;
    private final FragmentActivity fragmentActivity;

    /**
     * Constructor for GeoLocation that sets an event location and acceptable radius.
     *
     * @param fragmentActivity The activity context to display dialogs.
     * @param context The context in which the location service is accessed.
     * @param eventLatitude The latitude of the event location.
     * @param eventLongitude The longitude of the event location.
     * @param acceptableRadius The radius within which the user can register.
     */
    public GeoLocation(FragmentActivity fragmentActivity, Context context, double eventLatitude, double eventLongitude, float acceptableRadius) {
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








    //The follow 3 functions were implemented with the help of "https://www.youtube.com/watch?v=qY-xFxZ7HKY" - 2024-10-27
    //ChatGPT made changes to help improve the speed at which location was retrieved, "(Provided 3 functions original version) How can I make it so location is retrieved quicker to avoid issue?" - 2024-10-27

    /**
     * Initializes the LocationManager and sets the initial user location if permissions are granted.
     */
    private void initializeLocationManager() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get the last known location for a quicker initial value
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                userLocation = lastKnownLocation;
                Log.d("GeoLocation", "Initial user location set from last known location");
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
        Log.d("GeoLocation", "User location updated: " + location.getLatitude() + ", " + location.getLongitude());
        //Toast.makeText(context, "User Location Updated ", Toast.LENGTH_SHORT).show();
    }

    /**
     * Requests location updates from GPS and Network providers.
     *
     * @return true if permissions are granted and location updates are requested, false otherwise.
     */
    public boolean getLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
            return true;
        }
        else {
            return false;
        }
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
        return userLocation.getLatitude();
    }

    /**
     * Retrieves the user's current longitude.
     *
     * @return The longitude of the user's current location.
     */
    public double getUserLongitude() {
        return userLocation.getLongitude();
    }

    /**
     * Retrieves the event's current latitude.
     *
     * @return The latitude of the event location.
     */
    public double getEventLatitude() {
        return eventLocation.getLatitude();
    }

    /**
     * Retrieves the event's current longitude.
     *
     * @return The longitude of the event location.
     */
    public double getEventLongitude() {
        return eventLocation.getLongitude();
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
