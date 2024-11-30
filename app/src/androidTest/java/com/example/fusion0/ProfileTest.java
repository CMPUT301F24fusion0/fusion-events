package com.example.fusion0;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import com.example.fusion0.activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;

public class ProfileTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    /**
     * Checks if we manage to profile from home screen
     * @author Sehej Brar
     */
    @Test
    public void testToProfile() {
        Espresso.onView(withId(R.id.toolbar_person)).perform(click());
        Espresso.onView(withId(R.id.profileHeader)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Ensures we can successfully move to the notification settings page and turn them off
     * @author Sehej Brar
     */
    @Test
    public void testCheckSettings() {
        Espresso.onView(withId(R.id.toolbar_person)).perform(click());
        Espresso.onView(withId(R.id.settingsButton)).perform(click());
        Espresso.onView(withId(R.id.titleName)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(withId(R.id.switchNotifications)).perform(click());
    }

    /**
     * See if we can go back to the profile page using the back arrow
     * @author Sehej Brar
     */
    @Test
    public void testGoBack() {
        Espresso.onView(withId(R.id.toolbar_person)).perform(click());
        Espresso.onView(withId(R.id.settingsButton)).perform(click());
        Espresso.onView(withId(R.id.backButton)).perform(click());
        Espresso.onView(withId(R.id.profileHeader)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

}
