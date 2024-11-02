package com.example.geolocation;

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
    private double acceptableRadius;

    /**
     * Constructor for GeoLocation.
     *
     * @param context The context in which the location service is accessed.
     * @param acceptableRadius The radius within which the user can register.
     */
    public GeoLocation(Context context, double acceptableRadius) {
        this.context = context;
        this.acceptableRadius = acceptableRadius;
        initializeLocationManager();
    }

    /**
     * Initializes the LocationManager and sets the initial user location if
     * permissions are granted.
     *
     * The following code was provided by: "https://www.youtube.com/watch?v=qY-xFxZ7HKY" - 2024-10-27.
     * The following was edited by ChatGPT to improve location retrieval speed.
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
     * Requests location updates from GPS and Network providers.
     *
     * @return true if permissions are granted and location updates are requested, false otherwise.
     *
     * The following code was provided by: "https://www.youtube.com/watch?v=qY-xFxZ7HKY" - 2024-10-27.
     * The following was edited by ChatGPT to improve location retrieval speed.
     */
    public boolean getLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Callback method invoked when the user's location changes.
     * Updates userLocation and displays a Toast message.
     *
     * @param location The new location of the user.
     *
     * The following code was provided by: "https://www.youtube.com/watch?v=qY-xFxZ7HKY" - 2024-10-27.
     * The following was edited by ChatGPT to improve location retrieval speed.
     */
    @Override
    public void onLocationChanged(@NonNull Location location) {
        userLocation = location;
        Log.d("GeoLocation", "User location updated: " + location.getLatitude() + ", " + location.getLongitude());
        Toast.makeText(context, "User Location Updated ", Toast.LENGTH_SHORT).show();
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
}
