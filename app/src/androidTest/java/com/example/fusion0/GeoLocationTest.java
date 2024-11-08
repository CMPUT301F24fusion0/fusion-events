package com.example.fusion0;

import android.content.Context;
import android.location.Location;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.example.fusion0.GeoLocation;
import com.example.fusion0.TestFragmentActivity;

@RunWith(AndroidJUnit4.class)
public class GeoLocationTest {

    private GeoLocation geoLocation;
    private Context context;

    @Before
    //The following test setUp was provided by chatGPT, "How do I set up my geolocations first constructor (Provided the first constructor) in a test class?" - 2024 - 11 -07
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();

        ActivityScenario<TestFragmentActivity> scenario = ActivityScenario.launch(TestFragmentActivity.class);
        scenario.onActivity(activity -> {
            geoLocation = new GeoLocation(activity, context, 37.7749, -122.4194, 1000); // Event location set with a radius of 1000 meters
        });
    }

    @Test
    public void testRequestLocationPermissionWhenNotGranted() {
        assertFalse(geoLocation.isLocationPermissionGranted());
    }

    @Test
    public void testGetLocationWhenPermissionGranted() {
        Location mockLocation = new Location("mockProvider");
        mockLocation.setLatitude(37.7749);
        mockLocation.setLongitude(-122.4194);
        geoLocation.setUserLocation(mockLocation.getLatitude(), mockLocation.getLongitude());

        Location location = geoLocation.getLocation();
        assertEquals(37.7749, location.getLatitude(), 0.0001);
        assertEquals(-122.4194, location.getLongitude(), 0.0001);
    }


    @Test
    public void testOnLocationChangedUpdatesUserLocation() {
        Location newLocation = new Location("gps");
        newLocation.setLatitude(37.7750);
        newLocation.setLongitude(-122.4195);

        geoLocation.onLocationChanged(newLocation);
        assertEquals(37.7750, geoLocation.getUserLatitude(),0.0001);
        assertEquals(-122.4195, geoLocation.getUserLongitude(),0.0001);
    }

    @Test
    public void testCanRegisterWithinRadius() {
        geoLocation.setUserLocation(37.7750, -122.4195);
        assertTrue(geoLocation.canRegister());
    }

    @Test
    public void testCanRegisterOnRadiusBoundary() {
        geoLocation.setUserLocation(37.7749, -122.4194);
        assertTrue(geoLocation.canRegister());
    }

    @Test
    public void testCanRegisterOutsideRadius() {
        geoLocation.setUserLocation(37.8044, -122.2711);
        assertFalse(geoLocation.canRegister());
    }





}
