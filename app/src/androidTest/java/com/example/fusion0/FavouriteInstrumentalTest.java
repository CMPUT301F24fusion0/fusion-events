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

/**
 * Instrumented test class for the FavouriteActivity.
 * This test class includes setup and teardown methods, and a test to verify the display
 * of the "Joined Events" button.
 */
@RunWith(AndroidJUnit4.class)
public class FavouriteInstrumentalTest {
    /**
     * Rule to launch FavouriteActivity before each test in this class.
     */
    @Rule
    public ActivityScenarioRule<FavouriteActivity> activityRule = new ActivityScenarioRule<>(FavouriteActivity.class);

    /**
     * Initializes Espresso Intents before each test to track and validate interactions
     * that start new activities or intents.
     */
    @Before
    public void setUp() {
        Intents.init();
    }

    /**
     * Releases resources used by Espresso Intents after each test completes,
     * ensuring no interference with other tests.
     */
    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Verifies that the "Joined Events" button is visible on the FavouriteActivity screen.
     * Ensures the button is correctly displayed when the activity launches.
     */
    @Test
    public void testJoinedEventsButtonDisplayed() {
        onView(withId(R.id.joined_events_view_button)).check(matches(isDisplayed()));
    }
}
