package com.example.fusion0.activitiyTest;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.fusion0.R;
import com.example.fusion0.activities.JoinedEventActivity;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.UserInfo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;

@RunWith(AndroidJUnit4.class)
public class JoinedEventActivityTest {

    @Rule
    public ActivityScenarioRule<JoinedEventActivity> activityRule =
            new ActivityScenarioRule<>(JoinedEventActivity.class);

    @Mock
    EventFirebase mockEventFirebase;
    @Mock
    UserFirestore mockUserFirestore;
    @Mock
    UserInfo mockUserInfo;

    @Mock
    Context mockContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        doAnswer(invocation -> {
            EventFirebase.EventCallback callback = invocation.getArgument(1);
            EventInfo mockEventInfo = mock(EventInfo.class);
            when(mockEventInfo.getEventName()).thenReturn("Test Event");
            when(mockEventInfo.getDescription()).thenReturn("Test Description");
            when(mockEventInfo.getFacilityName()).thenReturn("Test Facility");
            when(mockEventInfo.getStartDate()).thenReturn(new java.util.Date());
            when(mockEventInfo.getEndDate()).thenReturn(new java.util.Date());
            when(mockEventInfo.getRegistrationDate()).thenReturn(new java.util.Date());
            callback.onSuccess(mockEventInfo);
            return null;
        }).when(mockEventFirebase).findEvent(anyString(), any(EventFirebase.EventCallback.class));
    }

    @Test
    public void testEventDetailsDisplayed() {
        // Simulate data returned by Firebase
        EventInfo mockEventInfo = mock(EventInfo.class);
        when(mockEventInfo.getEventName()).thenReturn("Test Event");
        when(mockEventInfo.getDescription()).thenReturn("Test Description");
        when(mockEventInfo.getFacilityName()).thenReturn("Test Facility");
        when(mockEventInfo.getStartDate()).thenReturn(new java.util.Date());
        when(mockEventInfo.getEndDate()).thenReturn(new java.util.Date());
        when(mockEventInfo.getRegistrationDate()).thenReturn(new java.util.Date());

        // Simulate Firebase callback
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                EventFirebase.EventCallback callback = invocation.getArgument(1);
                callback.onSuccess(mockEventInfo);  // Simulate a successful response with mock event data
                return null;  // No return value needed for void methods
            }
        }).when(mockEventFirebase).findEvent(mockEventInfo.getEventID(), any());

        // Assuming a valid eventID was passed to the activity
        Intent intent = new Intent();
        intent.putExtra("eventID", "test_event_id");
        intent.putExtra("deviceID", "test_device_id");
        activityRule.getScenario().onActivity(activity -> {
            // Inject the mocked dependencies into the activity
            activity.eventFirebase = mockEventFirebase;
            activity.userFirestore = mockUserFirestore;
            activity.setIntent(intent);
        });

        // Verify that the data is displayed correctly in the UI
        Espresso.onView(ViewMatchers.withId(R.id.EventName)).check(ViewAssertions.matches(ViewMatchers.withText("Test Event")));
        Espresso.onView(ViewMatchers.withId(R.id.description)).check(ViewAssertions.matches(ViewMatchers.withText("Test Description")));
        Espresso.onView(ViewMatchers.withId(R.id.facilityName)).check(ViewAssertions.matches(ViewMatchers.withText("Test Facility")));
    }

    @Test
    public void testGlideImageLoading() {
        String imageUrl = "https://example.com/event_image.jpg";

        // Mock Glide loading behavior
        Glide.with(mockContext)
                .load(imageUrl)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        // Image loaded successfully
                    }
                });

        // Verify that the image view is populated with the image
        Espresso.onView(ViewMatchers.withId(R.id.uploaded_image_view)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testBackButton() {
        Espresso.onView(ViewMatchers.withId(R.id.backButton)).perform(ViewActions.click());
        // Verify if the activity finishes
        Espresso.onView(ViewMatchers.withId(R.id.backButton)).check(ViewAssertions.doesNotExist());
    }

    @Test
    public void testUnjoinButton() {
        // Mock the behavior of unjoining the event
        doNothing().when(mockEventFirebase).editEvent(any(EventInfo.class));
        doNothing().when(mockUserFirestore).editUserEvents(any(UserInfo.class));

        // Simulate the user clicking the "unjoin" button
        Espresso.onView(ViewMatchers.withId(R.id.unjoin_button)).perform(ViewActions.click());

        // Verify that the event and user events are updated correctly
        verify(mockEventFirebase, times(1)).editEvent(any(EventInfo.class));
        verify(mockUserFirestore, times(1)).editUserEvents(any(UserInfo.class));

        Espresso.onView(ViewMatchers.withId(R.id.unjoin_button)).check(ViewAssertions.doesNotExist());
    }
}
