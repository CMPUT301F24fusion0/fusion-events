package com.example.fusion0;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.example.fusion0.activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;

/**
 * Tests for the admin class
 * @author Sehej Brar
 */
public class AdminTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    /**
     * Check if admin appears to admins
     * @author Sehej Brar
     */
    @Test
    public void testGoAdmin() {
        Espresso.onView(withId(R.id.toolbar_person)).perform(click());
        Espresso.onView(withId(R.id.adminFeatures)).perform(click());
        Espresso.onView(withId(R.id.adminFeaturesContainer)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    /**
     * See if the admin can browse facilities
     * @author Sehej Brar
     */
    @Test
    public void browseFacilities() {
        Espresso.onView(withId(R.id.toolbar_person)).perform(click());
        Espresso.onView(withId(R.id.adminFeatures)).perform(click());
        Espresso.onView(withId(R.id.browseFacilitiesButton)).perform(click());
        Espresso.onView(withId(R.id.adminFeaturesTitle)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
