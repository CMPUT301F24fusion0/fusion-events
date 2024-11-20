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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import com.example.fusion0.activities.QRActivity;


/**
 * Instrumented test for the {@link QRActivity}, verifying that the QR code scanner UI elements
 * are properly displayed when the activity is launched.
 * This test initializes the {@link Intents} framework to handle activity intents and checks
 * the display of the QR scanner view.
 *
 */
@RunWith(AndroidJUnit4.class)
public class QRInstrumentalTest {

    /**
     * Rule to launch the QRActivity in a test scenario,
     * providing a controlled lifecycle for the activity.
     */
    @Rule
    public ActivityScenarioRule<QRActivity> activityRule = new ActivityScenarioRule<>(QRActivity.class);



    // Sets up the test by initializing Espresso Intents, which allows validation of activity intents.
    @Before
    public void setUp() {
        Intents.init();
    }

    // Cleans up after each test by releasing Espresso Intents resources.
    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Tests if the QR code scanner view, identified by {@code R.id.camera_preview},
     * is displayed when the activity is launched.
     */
    @Test
    public void testQRScannerDisplayed() {
        onView(withId(R.id.camera_preview)).check(matches(isDisplayed()));
    }
}
