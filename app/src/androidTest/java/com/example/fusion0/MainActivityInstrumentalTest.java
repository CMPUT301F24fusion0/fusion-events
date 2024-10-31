package com.example.fusion0;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.intent.Intents;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


/**
 * Instrumentation test class for MainActivity.
 * This class includes UI tests to validate the visibility of toolbar buttons
 * and navigation functionality in MainActivity using Espresso and Intent matchers.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentalTest {


    // Launches MainActivity before each test
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    // Sets up the Intents framework before each test, allowing verification of activity navigation.
    @Before
    public void setUp() {
        Intents.init();
    }

    // Releases the Intents framework after each test.
    @After
    public void tearDown() {
        Intents.release();
    }

    // Verifies that all toolbar buttons in MainActivity are displayed on the screen.
    @Test
    public void testToolbarButtonsDisplayed() {
        onView(withId(R.id.toolbar_home)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbar_camera)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbar_add)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbar_favourite)).check(matches(isDisplayed()));
        onView(withId(R.id.toolbar_person)).check(matches(isDisplayed()));
    }


    /**
     * Tests navigation to ProfileActivity by clicking the profile toolbar button.
     * Verifies that clicking on the profile button (toolbar_person) opens ProfileActivity.
     */
    @Test
    public void testOpenProfileActivity() {
        onView(withId(R.id.toolbar_person)).perform(click());
        Intents.intended(hasComponent(ProfileActivity.class.getName()));
    }

    /**
     * Tests navigation to EventActivity by clicking the browse events button.
     * Verifies that clicking on the browse events button (browse_events_button) opens EventActivity.
     */
    @Test
    public void testOpenEventActivity() {
        onView(withId(R.id.browse_events_button)).perform(click());
        Intents.intended(hasComponent(EventActivity.class.getName()));
    }

    /**
     * Tests navigation to QRActivity by clicking the scan QR button.
     * Verifies that clicking on the scan QR button (scan_qr_button) opens QRActivity.
     */
    @Test
    public void testOpenQRActivity() {
        onView(withId(R.id.scan_qr_button)).perform(click());
        Intents.intended(hasComponent(QRActivity.class.getName()));
    }
}