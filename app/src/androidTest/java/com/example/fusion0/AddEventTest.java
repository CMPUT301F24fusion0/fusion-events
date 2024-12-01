package com.example.fusion0;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.widget.DatePicker;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.example.fusion0.activities.MainActivity;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

public class AddEventTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    @Test
    public void testGoAddEvent() {
        Espresso.onView(withId(R.id.toolbar_person)).perform(click());
        Espresso.onView(withId(R.id.createEventHeader)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void fillOutForm() {
        Espresso.onView(withId(R.id.EventName)).perform(replaceText("Event Name"), closeSoftKeyboard());
        Espresso.onView(withId(R.id.Description)).perform(replaceText("Event Description"), closeSoftKeyboard());
        Espresso.onView(withId(R.id.save_button)).perform(click());
        Espresso.onView(withId(R.id.createEventHeader)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
