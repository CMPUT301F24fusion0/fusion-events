package com.example.fusion0;


import android.content.Context;
import android.location.Location;
import androidx.fragment.app.FragmentActivity;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.*;

import com.example.fusion0.helpers.GeoLocation;

import com.example.fusion0.fragments.TestFragmentActivity;

@RunWith(AndroidJUnit4.class)
public class GeoLocationTest {


    @Mock
    private FragmentActivity mockFragmentActivity;


    private GeoLocation geoLocationWithEvent;
    private GeoLocation geoLocationWithoutEvent;
    private GeoLocation geoLocation;
    private Context context;


    private static final double EVENT_LATITUDE = 53.523435760301595;
    private static final double EVENT_LONGITUDE = -113.52631860110789;
    private static final double ACCEPTABLE_RADIUS = 100.0;
    private static final double USER_LATITUDE = 53.523435760301595;
    private static final double USER_LONGITUDE = -113.52631860110789;


    //The following test setUp() was provided by chatGPT, "How do I set up my geolocations first and second constructor (Provided the two constructors) in a test class?" - 2024 - 11 -07
    //Made me create TestFragmentActivity.java
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();


        // Launch TestFragmentActivity using ActivityScenario and initialize geoLocation objects
        ActivityScenario<TestFragmentActivity> scenario = ActivityScenario.launch(TestFragmentActivity.class);
        scenario.onActivity(activity -> {
            geoLocationWithEvent = new GeoLocation(activity, context, EVENT_LATITUDE, EVENT_LONGITUDE, ACCEPTABLE_RADIUS);
            geoLocationWithoutEvent = new GeoLocation(activity, context);
        });
    }


    @Test
    public void testConstructorWithEventLocation() {
        assertEquals(EVENT_LATITUDE, geoLocationWithEvent.getEventLatitude(), 0.001);
        assertEquals(EVENT_LONGITUDE, geoLocationWithEvent.getEventLongitude(), 0.001);
        assertEquals(ACCEPTABLE_RADIUS, geoLocationWithEvent.getEventRadius(), 0.001);
    }


    @Test
    public void testConstructorWithoutEventLocation() {
        assertEquals(0.0, geoLocationWithoutEvent.getEventLatitude(), 0.001);
        assertEquals(0.0, geoLocationWithoutEvent.getEventLongitude(), 0.001);
    }


    @Test
    public void testSetEventLocation() {
        geoLocationWithoutEvent.setEventLocation(EVENT_LATITUDE, EVENT_LONGITUDE);
        assertEquals(EVENT_LATITUDE, geoLocationWithoutEvent.getEventLatitude(), 0.001);
        assertEquals(EVENT_LONGITUDE, geoLocationWithoutEvent.getEventLongitude(), 0.001);
    }


    @Test
    public void testSetUserLocation() {
        geoLocationWithEvent.setUserLocation(USER_LATITUDE, USER_LONGITUDE);
        assertEquals(USER_LATITUDE, geoLocationWithEvent.getUserLatitude(), 0.001);
        assertEquals(USER_LONGITUDE, geoLocationWithEvent.getUserLongitude(), 0.001);
    }


    @Test
    public void testSetEventRadius() {
        geoLocationWithEvent.setEventRadius(200);
        assertEquals(200, geoLocationWithEvent.getEventRadius(), 0.001);
    }


    @Test
    public void testGetLocationWhenPermissionGranted() {
        geoLocationWithoutEvent.setUserLocation(USER_LATITUDE, USER_LONGITUDE);
        assertEquals(USER_LATITUDE, geoLocationWithoutEvent.getUserLatitude(), 0.001);
        assertEquals(USER_LONGITUDE, geoLocationWithoutEvent.getUserLongitude(), 0.001);
    }


    @Test
    public void testOnLocationChangedUpdatesUserLocation() {
        Location newLocation = new Location("gps");
        newLocation.setLatitude(37.7750);
        newLocation.setLongitude(-122.4195);
        geoLocationWithEvent.onLocationChanged(newLocation);


        assertEquals(37.7750, geoLocationWithEvent.getUserLatitude(), 0.0001);
        assertEquals(-122.4195, geoLocationWithEvent.getUserLongitude(), 0.0001);
    }


    @Test
    public void testCanRegisterWithinRadius() {
        geoLocationWithEvent.setUserLocation(USER_LATITUDE, USER_LONGITUDE);
        geoLocationWithEvent.setEventLocation(EVENT_LATITUDE, EVENT_LONGITUDE);
        assertTrue(geoLocationWithEvent.canRegister());
    }


    @Test
    public void testCanRegisterOutsideRadius() {
        geoLocationWithEvent.setUserLocation(54.0, -114.0); //Far from the event location
        geoLocationWithEvent.setEventLocation(EVENT_LATITUDE, EVENT_LONGITUDE);
        assertFalse(geoLocationWithEvent.canRegister());
    }
}
