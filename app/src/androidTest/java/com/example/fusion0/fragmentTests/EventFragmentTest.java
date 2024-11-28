/*package com.example.fusion0.fragmentTests;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;
import org.junit.Test;
import java.util.Calendar;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import com.example.fusion0.BuildConfig;
import com.example.fusion0.R;
import com.example.fusion0.activities.MainActivity;
import com.example.fusion0.fragments.EventFragment;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.FacilitiesInfo;
import com.example.fusion0.models.OrganizerInfo;
import com.google.android.libraries.places.api.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.yalantis.ucrop.UCrop;


import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Calendar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventFragmentTest {

    @Mock
    private EventFirebase eventFirebase;

    @InjectMocks
    private EventFragment eventFragment;

    @Mock
    private Context mockContext;

    @Mock
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Mock
    private ArrayAdapter<String> mockAdapter;

    @Mock
    private OrganizerInfo mockOrganizer;

    @Mock
    private FacilitiesInfo mockFacility;

    @Mock
    private PlacesClient mockPlacesClient;

    @Mock
    private Place mockPlace;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockContext = ApplicationProvider.getApplicationContext();

        if (!Places.isInitialized()) {
            Places.initialize(mockContext, BuildConfig.API_KEY);
        }
        mockPlacesClient = Places.createClient(mockContext);

        FragmentScenario<EventFragment> fragmentScenario = FragmentScenario.launchInContainer(EventFragment.class);
        fragmentScenario.onFragment(fragment -> {
            fragment.eventFirebase = eventFirebase;
        });
    }

    @Test
    public void testViewsAreInitialized() {
        onView(withId(R.id.add_event_fragment)).check(matches(isDisplayed()));
        onView(withId(R.id.EventName)).check(matches(isDisplayed()));
        onView(withId(R.id.uploaded_image_view)).check(matches(isDisplayed()));
        onView(withId(R.id.spinner_facilities)).check(matches(isDisplayed()));
        onView(withId(R.id.add_facility_text)).check(matches(isDisplayed()));
        onView(withId(R.id.autocomplete_fragment)).check(matches(isDisplayed()));
        onView(withId(R.id.Description)).check(matches(isDisplayed()));
    }

    @Test
    public void testExitButtonNavigation() {
        // Perform click on exit button
        onView(withId(R.id.exit_button)).perform(click());
        // Verify navigation to main fragment (using the navigation action ID)
        onView(withId(R.id.mainFragment)).check(matches(isDisplayed()));
    }

    @Test
    public void testValidateOrganizer_Success() {
        OrganizerInfo mockOrganizer = new OrganizerInfo("mock_device_id");

        doAnswer(invocation -> {
            EventFirebase.OrganizerCallback callback = invocation.getArgument(1);
            callback.onSuccess(mockOrganizer);
            return null;
        }).when(eventFirebase).findOrganizer(anyString(), any(EventFirebase.OrganizerCallback.class));

        eventFragment.validateOrganizer(mockContext);

        assertNotNull(eventFragment.organizer);
        assertEquals("mock_device_id", eventFragment.organizer.getDeviceId());
    }

    @Test
    public void testUploadImage() {
        Uri mockUri = Uri.parse("content://mockuri");
        Intent resultIntent = new Intent();
        resultIntent.setData(mockUri);

        doAnswer(invocation -> {
            imagePickerLauncher.launch(resultIntent);
            return null;
        }).when(imagePickerLauncher).launch(any(Intent.class));

        // Launch the activity that contains the fragment
        activityScenarioRule.getScenario().onActivity(activity -> {
            EventFragment fragment = new EventFragment();
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.add_event_fragment, fragment)
                    .commit();

            // Interact with the fragment's UI (click the upload button)
            onView(withId(R.id.upload_image_button)).perform(click());

            // Simulate the result from the image picker (trigger onActivityResult)
            fragment.onActivityResult(UCrop.REQUEST_CROP, Activity.RESULT_OK, resultIntent);

            // Verify the ImageView is updated with the image URI
            onView(withId(R.id.uploaded_image_view)).check(matches(isDisplayed()));
        });
    }

    @Test
    public void testHandleFacility_populatesSpinnerWithFacilities() {
        ArrayList<FacilitiesInfo> mockFacilities = new ArrayList<>();
        mockFacilities.add(mockFacility);
        when(mockOrganizer.getFacilities()).thenReturn(mockFacilities);
        when(mockFacility.getFacilityName()).thenReturn("Test Facility");

        eventFragment.handleFacility(mockOrganizer, mockContext);

        verify(mockAdapter).add("Test Facility");
        verify(mockAdapter).add("Add Facility");
    }

    @Test
    public void testHandleFacility_addFacilityIsSelected() {
        ArrayList<FacilitiesInfo> mockFacilities = new ArrayList<>();
        mockFacilities.add(mockFacility);
        when(mockOrganizer.getFacilities()).thenReturn(mockFacilities);
        when(mockFacility.getFacilityName()).thenReturn("Test Facility");

        eventFragment.handleFacility(mockOrganizer, mockContext);

        AdapterView.OnItemSelectedListener listener = mock(AdapterView.OnItemSelectedListener.class);
        listener.onItemSelected(mock(Spinner.class), null, 0, 0);

        verify(eventFragment).addFacility(any(), any(), eq(mockContext));
    }

    @Test
    public void testAddFacility_existingFacility() {
        ArrayList<String> mockFacilityNames = new ArrayList<>();
        mockFacilityNames.add("Existing Facility");

        eventFragment.addFacility(mockFacilityNames, mockAdapter, mockContext);
    }

    @Test
    public void testAddFacility_addNewFacility() {
        ArrayList<String> mockFacilityNames = new ArrayList<>();
        mockFacilityNames.add("Existing Facility");

        when(mockPlace.getDisplayName()).thenReturn("New Facility");
        when(mockPlace.getFormattedAddress()).thenReturn("123 New Facility St.");
        when(mockPlace.getLatLng()).thenReturn(new LatLng(40.748817, -73.985428));

        eventFragment.addFacility(mockFacilityNames, mockAdapter, mockContext);

        verify(mockAdapter).notifyDataSetChanged();
        assertTrue(mockFacilityNames.contains("New Facility"));
    }

    @Test
    public void testFacilityFetch_onSuccess() {
        when(mockOrganizer.getFacilityIdByName(anyString())).thenReturn("facility_id");
        eventFragment.handleFacility(mockOrganizer, mockContext);

        FacilitiesInfo fetchedFacility = new FacilitiesInfo("New Address", "New Facility", "deviceID", 40.748817, -73.985428, "mock_uri");
        eventFirebase.findFacility("facility_id", new EventFirebase.FacilityCallback() {
            @Override
            public void onSuccess(FacilitiesInfo facilityInfo) {
                assertNotNull(facilityInfo);
                assertEquals("New Facility", facilityInfo.getFacilityName());
            }

            @Override
            public void onFailure(String error) {
                fail("Fetching facility should not fail.");
            }
        });
    }

    @Test
    public void testGeolocationHandling_checkedState() {
        onView(withId(R.id.geolocation_switchcompat)).perform(click());

        onView(withId(R.id.radius_text)).check(matches(isDisplayed()));
        onView(withId(R.id.radius)).check(matches(isDisplayed()));
    }

    @Test
    public void testGeolocationHandling_uncheckedState() {
        onView(withId(R.id.geolocation_switchcompat)).perform(click());
        onView(withId(R.id.geolocation_switchcompat)).perform(click());

        onView(withId(R.id.radius_text)).check(matches(withText("0")));
        onView(withId(R.id.radius)).check(matches(withText("0")));
    }

    @Test
    public void testStartDateButtonHandling_validDateTime() {
        onView(withId(R.id.start_date_button)).perform(click());

        onView(withId(R.id.start_date_text)).check(matches(isDisplayed()));
        onView(withId(R.id.start_time_text)).check(matches(isDisplayed()));
    }

    @Test
    public void testStartDateButtonHandling_invalidDate() throws InterruptedException {
        onView(withId(R.id.start_date_button)).perform(ViewActions.click());

        // Use UiAutomator to interact with the system DatePicker dialog
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Wait for the DatePicker to appear
        device.wait(Until.hasObject(By.desc("Date Picker")), 5000);

        // Set the date to a past date (e.g., January 1st, 2022)
        device.findObject(By.desc("Year")).setText("2022");
        device.findObject(By.desc("Month")).setText("January");
        device.findObject(By.desc("Day")).setText("1");

        // Simulate clicking "OK" on the DatePicker dialog (the "OK" button's resource ID is typically android.R.id.button1)
        onView(allOf(withId(android.R.id.button1), isDisplayed())).perform(ViewActions.click());

        // Verify the validation message for the invalid date selection
        onView(withId(R.id.date_requirements_text))
                .check(matches(ViewMatchers.withText("Date Must Be Today or Later.")));
    }

    @Test
    public void testEndDateButtonHandling_validDateTime() {
        // Simulate clicking the end date button
        onView(withId(R.id.end_date_button)).perform(click());

        // Assume user selects a valid end date and time (later than the start date)
        Calendar validEndDate = Calendar.getInstance();
        validEndDate.add(Calendar.DAY_OF_MONTH, 1);  // Set a valid end date (1 day later)

        // Simulate selecting a valid end time (later than start time)
        onView(withId(R.id.end_date_text)).check(matches(isDisplayed()));
        onView(withId(R.id.end_time_text)).check(matches(isDisplayed()));
    }

    @Test
    public void testEndDateButtonHandling_invalidDate() {
        // Simulate clicking the end date button
        onView(withId(R.id.end_date_button)).perform(click());

        // Simulate selecting an end date before the start date
        Calendar invalidEndDate = Calendar.getInstance();
        invalidEndDate.add(Calendar.DAY_OF_MONTH, -1);  // Set end date to a day before the start date

        // Verify that the date requirements text is shown and the end date/time is hidden
        onView(withId(R.id.date_requirements_text)).check(matches(withText("End Date Must Be On or After Start Date.")));
        onView(withId(R.id.end_date_text)).check(matches(not(isDisplayed())));
        onView(withId(R.id.end_time_text)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testRegistrationDateButtonHandling_validDate() {
        // Simulate clicking the registration date button
        onView(withId(R.id.registration_date_button)).perform(click());

        // Assume user selects a valid registration date (not before today and before start date)
        Calendar validRegDate = Calendar.getInstance();
        validRegDate.add(Calendar.DAY_OF_MONTH, 2);  // Set to a valid future date

        // Simulate selecting a valid registration date
        onView(withId(R.id.registration_date_text)).check(matches(isDisplayed()));
        onView(withId(R.id.registrationDateRequirementsTextView)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testRegistrationDateButtonHandling_invalidDate() {
        onView(withId(R.id.registration_date_button)).perform(click());

        Calendar invalidRegDate = Calendar.getInstance();
        invalidRegDate.add(Calendar.DAY_OF_MONTH, -1);

        onView(withId(R.id.registrationDateRequirementsTextView)).check(matches(withText("Deadline Cannot Be Before Today.")));
        onView(withId(R.id.registration_date_text)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testRegistrationDateButtonHandling_afterStartDate() {
        onView(withId(R.id.registration_date_button)).perform(click());

        Calendar invalidRegDate = Calendar.getInstance();
        invalidRegDate.add(Calendar.DAY_OF_MONTH, 1);

        onView(withId(R.id.registrationDateRequirementsTextView)).check(matches(withText("Registration deadline must be before the event start date.")));
        onView(withId(R.id.registration_date_text)).check(matches(not(isDisplayed())));
    }


}

*/