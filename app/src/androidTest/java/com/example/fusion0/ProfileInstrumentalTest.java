package com.example.fusion0;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for the ProfileActivity.
 * These tests verify the functionality and UI behavior of the Profile screen using Espresso.
 *
 * @author Malshaan Kodithuwakku
 */
@RunWith(AndroidJUnit4.class)
public class ProfileInstrumentalTest {

    /**
     * Launches the ProfileActivity for each test scenario.
     */
    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule = new ActivityScenarioRule<>(ProfileActivity.class);

    /**
     * Sets up the test environment before each test case is executed.
     * Initializes Intents for validating intents during the test.
     */
    @Before
    public void setUp() {
        Intents.init();
    }

    /**
     * Cleans up the test environment after each test case is executed.
     * Releases Intents to prevent memory leaks.
     */
    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Test case to verify the functionality of the edit button in ProfileActivity.
     * When the edit button is clicked, the save button should be displayed,
     * indicating that the profile has entered edit mode.
     */
    @Test
    public void testEditProfile() {
        // Click on the edit button to switch to edit mode.
        onView(withId(R.id.editButton)).perform(click());

        // Check if the save button is now displayed.
        onView(withId(R.id.saveButton)).check(matches(isDisplayed()));
    }
}