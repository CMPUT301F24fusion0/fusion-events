package com.example.fusion0.activitiyTest;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.assertion.ViewAssertions;

import com.example.fusion0.R;
import com.example.fusion0.activities.ViewFacilityActivity;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.FacilitiesInfo;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ViewFacilityActivityTest {
    private CollectionReference facilitiesRef;
    private EventFirebase eventFirebase;
    final CountDownLatch latch = new CountDownLatch(1);
    FacilitiesInfo mockFacility = new FacilitiesInfo("123 Address", "Facility 1", "Owner", 0.0, 0.0, "facilityImage");

    @Rule
    public ActivityScenarioRule<ViewFacilityActivity> activityRule = new ActivityScenarioRule<>(ViewFacilityActivity.class);


    @Before
    public void setUp() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        facilitiesRef = db.collection("facilities");
        eventFirebase = new EventFirebase();
        eventFirebase.addFacility(mockFacility);

        facilitiesRef.document(mockFacility.getFacilityID()).set(mockFacility).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Facility successfully added to Firestore with ID: " + mockFacility.getFacilityID());
                latch.countDown();  // Release the latch when Firestore operation completes
            } else {
                Log.e(TAG, "Failed to add facility to Firestore: ", task.getException());
                latch.countDown();  // Make sure to release latch even in failure to avoid deadlock
            }
        });
    }

    @Test
    public void testFacilityDetailsDisplayed() throws InterruptedException {
        String facilityID = mockFacility.getFacilityID();

        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ViewFacilityActivity.class);
        intent.putExtra("facilityID", facilityID);

        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "Failed to add facility: " + facilityID);

        // Launch the activity with the intent
        try (ActivityScenario<ViewFacilityActivity> scenario = ActivityScenario.launch(intent)) {
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Espresso.onView(ViewMatchers.withId(R.id.facilityName))
                    .check(ViewAssertions.matches(ViewMatchers.withText("Facility 1")));

            Espresso.onView(ViewMatchers.withId(R.id.address))
                    .check(ViewAssertions.matches(ViewMatchers.withText("123 Address")));
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testEditButtonFunctionality() {
        String facilityID = mockFacility.getFacilityID();

        // Create an intent with the facilityID to pass to the activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ViewFacilityActivity.class);
        intent.putExtra("facilityID", facilityID);

        // Launch the activity with the intent
        try (ActivityScenario<ViewFacilityActivity> scenario = ActivityScenario.launch(intent)) {
            // Espresso will automatically wait for the UI to be idle
            Espresso.onView(ViewMatchers.withId(R.id.edit_button))
                    .perform(ViewActions.click());

            // Check if the facility name and address are populated in EditText fields
            Espresso.onView(ViewMatchers.withId(R.id.editFacilityName))
                    .check(ViewAssertions.matches(ViewMatchers.withText("Test Facility")));

            Espresso.onView(ViewMatchers.withId(R.id.editAddress))
                    .check(ViewAssertions.matches(ViewMatchers.withText("123 Test St")));

            // Check if the save and cancel buttons are visible
            Espresso.onView(ViewMatchers.withId(R.id.save_button))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            Espresso.onView(ViewMatchers.withId(R.id.cancel_button))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }


    @Test
    public void testSaveButtonFunctionality() {
        String facilityID = mockFacility.getFacilityID();

        // Create an intent with the facilityID to pass to the activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ViewFacilityActivity.class);
        intent.putExtra("facilityID", facilityID);

        // Launch the activity with the intent
        try (ActivityScenario<ViewFacilityActivity> scenario = ActivityScenario.launch(intent)) {
            // Click Edit button to enter edit mode
            Espresso.onView(ViewMatchers.withId(R.id.edit_button)).perform(ViewActions.click());

            // Simulate user input in the EditText fields
            Espresso.onView(ViewMatchers.withId(R.id.editFacilityName))
                    .perform(ViewActions.replaceText("Updated Facility"));

            Espresso.onView(ViewMatchers.withId(R.id.editAddress))
                    .perform(ViewActions.replaceText("456 Updated St"));

            // Click Save button
            Espresso.onView(ViewMatchers.withId(R.id.save_button)).perform(ViewActions.click());

            // Check if the updated values are reflected in the TextViews
            Espresso.onView(ViewMatchers.withId(R.id.facilityName))
                    .check(ViewAssertions.matches(ViewMatchers.withText("Updated Facility")));

            Espresso.onView(ViewMatchers.withId(R.id.address))
                    .check(ViewAssertions.matches(ViewMatchers.withText("456 Updated St")));
        }
    }
}
/*
    @Test
    public void testCancelButtonFunctionality() {
        FacilitiesInfo mockFacility = new FacilitiesInfo("123 Address", "Facility 1", "Owner", 0.0, 0.0, "facilityImage");
        eventFirebase.addFacility(mockFacility);

        facilitiesRef.document(mockFacility.getFacilityID()).set(mockFacility).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String facilityID = mockFacility.getFacilityID();

                activityRule.getScenario().onActivity(activity -> {
                    Intent intent = new Intent();
                    intent.putExtra("facilityID", facilityID);
                    activity.setIntent(intent);
                });

                try {
                    latch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // Click Edit button to enter edit mode
                Espresso.onView(ViewMatchers.withId(R.id.edit_button)).perform(ViewActions.click());

                // Simulate user input in the EditText fields
                Espresso.onView(ViewMatchers.withId(R.id.editFacilityName))
                        .perform(ViewActions.replaceText("Updated Facility"));

                Espresso.onView(ViewMatchers.withId(R.id.editAddress))
                        .perform(ViewActions.replaceText("456 Updated St"));

                // Click Cancel button
                Espresso.onView(ViewMatchers.withId(R.id.cancel_button)).perform(ViewActions.click());

                // Check if the TextViews are unchanged (i.e., original data is restored)
                Espresso.onView(ViewMatchers.withId(R.id.facilityName))
                        .check(ViewAssertions.matches(ViewMatchers.withText("Test Facility")));

                Espresso.onView(ViewMatchers.withId(R.id.address))
                        .check(ViewAssertions.matches(ViewMatchers.withText("123 Test St")));
            }
        });
    }


    @Test
    public void testDeleteButtonFunctionality() {
        FacilitiesInfo mockFacility = new FacilitiesInfo("123 Address", "Facility 1", "Owner", 0.0, 0.0, "facilityImage");
        eventFirebase.addFacility(mockFacility);

        facilitiesRef.document(mockFacility.getFacilityID()).set(mockFacility).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String facilityID = mockFacility.getFacilityID();

                activityRule.getScenario().onActivity(activity -> {
                    Intent intent = new Intent();
                    intent.putExtra("facilityID", facilityID);
                    activity.setIntent(intent);
                });

                try {
                    latch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Click Delete button
                Espresso.onView(ViewMatchers.withId(R.id.delete_button)).perform(ViewActions.click());

                // Check if the Delete confirmation dialog appears
                Espresso.onView(ViewMatchers.withText("Are you sure you want to delete this entry?"))
                        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

                // Confirm deletion (Yes button)
                Espresso.onView(ViewMatchers.withText("Yes")).perform(ViewActions.click());

                facilitiesRef.document(facilityID).delete().addOnCompleteListener(deleteTask -> {
                    if (deleteTask.isSuccessful()) {
                        // Verify that the facility has been deleted by checking Firestore
                        facilitiesRef.document(facilityID).get().addOnCompleteListener(getTask -> {
                            if (getTask.isSuccessful() && !getTask.getResult().exists()) {
                                // Assert the facility is deleted
                                latch.countDown();
                            }
                        });
                    }
                });

                try {
                    latch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}

 */

