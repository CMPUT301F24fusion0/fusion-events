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
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class ProfileInstrumentalTest {

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule = new ActivityScenarioRule<>(ProfileActivity.class);

    @Before
    public void setUp() {
        // Initialize Intents for intent validation.
        Intents.init();
    }

    @After
    public void tearDown() {
        // Release Intents after each test to clean up.
        Intents.release();
    }

    /**
     * Test to verify that when the edit button is clicked,
     * the save button is displayed, indicating the profile is in edit mode.
     */
    @Test
    public void testEditProfile() {
        // Click on the edit button to switch to edit mode.
        onView(withId(R.id.editButton)).perform(click());

        // Check if the save button is now displayed.
        onView(withId(R.id.saveButton)).check(matches(isDisplayed()));
    }
}
