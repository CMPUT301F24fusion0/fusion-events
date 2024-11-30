package com.example.fusion0;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.example.fusion0.activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;

public class FavouriteFragmentTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    /**
     * Checks to see if the required lists are on there
     * @author Sehej Brar
     */
    @Test
    public void testSeeLists() {
        Espresso.onView(withId(R.id.toolbar_favourite)).perform(click());
        Espresso.onView(withId(R.id.joined_eventTV)).check(ViewAssertions.matches(isDisplayed()));
        Espresso.onView(withId(R.id.created_eventsTV)).check(ViewAssertions.matches(isDisplayed()));
        Espresso.onView(withId(R.id.user_facilitiesTV)).check(ViewAssertions.matches(isDisplayed()));
    }

    /**
     * Test to see if we click to view a list then we will actually see he list
     * @author Sehej Brar
     */
    @Test
    public void testClickList() throws InterruptedException {
        Espresso.onView(withId(R.id.toolbar_favourite)).perform(click());
        Espresso.onView(withId(R.id.facilities_view_button)).perform(click());
    }
}
