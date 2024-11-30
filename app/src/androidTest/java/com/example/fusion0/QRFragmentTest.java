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

public class QRFragmentTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.CAMERA, android.Manifest.permission.POST_NOTIFICATIONS);

    /**
     * Checks to see if we're on the QR Fragment
     * @author Sehej Brar
     */
    @Test
    public void testToQR() {
        Espresso.onView(withId(R.id.toolbar_qrscanner)).perform(click());
        Espresso.onView(withId(R.id.instruction_text)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Tests whether cancel will take us back to home screen
     * @author Sehej Brar
     */
    @Test
    public void testCancel() {
        Espresso.onView(withId(R.id.toolbar_qrscanner)).perform(click());
        Espresso.onView(withId(R.id.cancel_button)).perform(click());
        Espresso.onView(withId(R.id.toolbar)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

}
