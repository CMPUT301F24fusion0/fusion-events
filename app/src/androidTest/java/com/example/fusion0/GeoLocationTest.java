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

import com.example.fusion0.fragments.TestFragmentActivity;
import com.example.fusion0.helpers.GeoLocation;

/**
 * GeoLocationTest
 *
 * This class tests the functionality of the GeoLocation class. It uses Android's instrumented
 * testing framework to validate the behavior of GeoLocation with and without event data.
 * The tests cover methods for setting and retrieving event and user locations, as well as
 * determining whether a user can register based on proximity to the event location.
 * @author Derin Karas
 */



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

    // The following test setUp() was provided by chatGPT, "How do I set up my geolocations first and second constructor (Provided the two constructors) in a test class?" - 2024 - 11 -07
    // Made me create TestFragmentActivity.java

    /**
     * Sets up the test environment.
     * Initializes GeoLocation objects with and without event data by launching TestFragmentActivity
     */
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

    /**
     * Tests the constructor that initializes GeoLocation with event data.
     * Validates that event latitude, longitude, and radius are correctly set.
     */
    @Test
    public void testConstructorWithEventLocation() {
        assertEquals(EVENT_LATITUDE, geoLocationWithEvent.getEventLatitude(), 0.001);
        assertEquals(EVENT_LONGITUDE, geoLocationWithEvent.getEventLongitude(), 0.001);
        assertEquals(ACCEPTABLE_RADIUS, geoLocationWithEvent.getEventRadius(), 0.001);
    }

    /**
     * Tests the constructor that initializes GeoLocation without event data.
     * Validates that default values (0.0) are assigned to event latitude and longitude.
     */
    @Test
    public void testConstructorWithoutEventLocation() {
        assertEquals(0.0, geoLocationWithoutEvent.getEventLatitude(), 0.001);
        assertEquals(0.0, geoLocationWithoutEvent.getEventLongitude(), 0.001);
    }

    /**
     * Tests the setEventLocation method.
     * Verifies that the event location latitude and longitude are correctly updated.
     */
    @Test
    public void testSetEventLocation() {
        geoLocationWithoutEvent.setEventLocation(EVENT_LATITUDE, EVENT_LONGITUDE);
        assertEquals(EVENT_LATITUDE, geoLocationWithoutEvent.getEventLatitude(), 0.001);
        assertEquals(EVENT_LONGITUDE, geoLocationWithoutEvent.getEventLongitude(), 0.001);
    }

    /**
     * Tests the setUserLocation method.
     * Validates that the user location latitude and longitude are correctly set.
     */
    @Test
    public void testSetUserLocation() {
        geoLocationWithEvent.setUserLocation(USER_LATITUDE, USER_LONGITUDE);
        assertEquals(USER_LATITUDE, geoLocationWithEvent.getUserLatitude(), 0.001);
        assertEquals(USER_LONGITUDE, geoLocationWithEvent.getUserLongitude(), 0.001);
    }

    /**
     * Tests the setEventRadius method.
     * Verifies that the event radius is updated to the specified value.
     */
    @Test
    public void testSetEventRadius() {
        geoLocationWithEvent.setEventRadius(200);
        assertEquals(200, geoLocationWithEvent.getEventRadius(), 0.001);
    }

    /**
     * Tests user location retrieval when permissions are granted.
     * Verifies that user location latitude and longitude are correctly retrieved.
     */
    @Test
    public void testGetLocationWhenPermissionGranted() {
        geoLocationWithoutEvent.setUserLocation(USER_LATITUDE, USER_LONGITUDE);
        assertEquals(USER_LATITUDE, geoLocationWithoutEvent.getUserLatitude(), 0.001);
        assertEquals(USER_LONGITUDE, geoLocationWithoutEvent.getUserLongitude(), 0.001);
    }

    /**
     * Tests the onLocationChanged method.
     * Verifies that the user's location is updated when the location changes.
     */
    @Test
    public void testOnLocationChangedUpdatesUserLocation() {
        Location newLocation = new Location("gps");
        newLocation.setLatitude(37.7750);
        newLocation.setLongitude(-122.4195);
        geoLocationWithEvent.onLocationChanged(newLocation);

        assertEquals(37.7750, geoLocationWithEvent.getUserLatitude(), 0.0001);
        assertEquals(-122.4195, geoLocationWithEvent.getUserLongitude(), 0.0001);
    }

    /**
     * Tests the canRegister method when the user is within the acceptable radius.
     * Validates that the method returns true.
     */
    @Test
    public void testCanRegisterWithinRadius() {
        geoLocationWithEvent.setUserLocation(USER_LATITUDE, USER_LONGITUDE);
        geoLocationWithEvent.setEventLocation(EVENT_LATITUDE, EVENT_LONGITUDE);
        assertTrue(geoLocationWithEvent.canRegister());
    }

    /**
     * Tests the canRegister method when the user is outside the acceptable radius.
     * Validates that the method returns false.
     */
    @Test
    public void testCanRegisterOutsideRadius() {
        geoLocationWithEvent.setUserLocation(54.0, -114.0); // Far from the event location
        geoLocationWithEvent.setEventLocation(EVENT_LATITUDE, EVENT_LONGITUDE);
        assertFalse(geoLocationWithEvent.canRegister());
    }
}
