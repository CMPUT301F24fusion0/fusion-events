package com.example.fusion0;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.List;
import java.util.Locale;


public class GeoLocation implements LocationListener {
    /**
     * The GeoLocation class manages all geolocation functionality, including permission requests,
     * location updates, and distance calculations for event registration.
     *
     * Flow:
     * 1. Call `getLocation()` to start tracking the user's location.
     * 2. `onLocationChanged` is automatically triggered with location updates, storing the latest
     *    location in `userLocation`.
     * 3. Use `getUserLocation()` to retrieve the most recent location after updates.
     * 4. Use 'canRegister()' after using getLocation()
     *
     * Usage:
     * - `requestLocationPermission()` checks and requests necessary permissions.
     * - `getLocation()` initiates location updates.
     * - `canRegister(Location userLocation)` checks if the user is within a specified radius of the event (5km radius constant).
     *
     * This class requires a Context to access system services and display notifications.
     */


    //Context logic was provided by chatGPT, "Orginally in my main I create geo location logic, I now need to move this to a different class besides MainActivity.java how do I alter the uses of MainActivity within the new class?" - 2024-10-27
    private final Context context;
    private LocationManager locationManager;
    private Location userLocation;
    private Location eventLocation;
    private double EVENT_RADIUS_KM = 5.0;

    public GeoLocation(Context context, Location eventLocation) {
        this.context = context;
        this.eventLocation = eventLocation;
    }

    //In the case you only want to get the users location not check if they're close to the event
    public GeoLocation(Context context) {
        this.context = context;
    }


    // The following code was provided by: "https://www.youtube.com/watch?v=qY-xFxZ7HKY" - 2024-10-27
    public void requestLocationPermission() {
        ActivityCompat.requestPermissions((AppCompatActivity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    }

    // The following code was provided by: "https://www.youtube.com/watch?v=qY-xFxZ7HKY" - 2024-10-27
    public void getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // The following code was provided by: "https://www.youtube.com/watch?v=qY-xFxZ7HKY" - 2024-10-27
    @Override
    public void onLocationChanged(@NonNull Location location) {
        userLocation = location;

        Toast.makeText(context, location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();

        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Checks if the user is within the event radius
    public boolean canRegister(Location userLocation) {
        double distance = userLocation.distanceTo(eventLocation);
        return distance <= EVENT_RADIUS_KM;
    }

    //Get the users location if needed
    public Location getUserLocation() {
        return userLocation;
    }




}