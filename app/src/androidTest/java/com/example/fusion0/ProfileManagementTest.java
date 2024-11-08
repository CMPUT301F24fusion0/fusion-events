package com.example.fusion0;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

/**
 * Instrumented test class for the ProfileManagement class.
 * This class tests user data retrieval from Firebase Firestore.
 */
@RunWith(AndroidJUnit4.class)
public class ProfileManagementTest {

    private ProfileManagement profileManagement;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String testDeviceId;

    /**
     * Sets up the test environment before each test is executed.
     * Initializes Firebase instances, ProfileManagement object, and sets up a test user in Firestore.
     */
    @Before
    public void setUp() {
        profileManagement = new ProfileManagement();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Use a test device ID (update as needed)
        testDeviceId = "test_device_id";

        ArrayList<String> notifications = new ArrayList<>();

        // Set up a test user in Firestore for the success scenario
        UserInfo testUser = new UserInfo(notifications, "Alice", "Smith", "alicesmith@gmail.com", "12345678", "test_deviceId", new ArrayList<EventInfo>());
        db.collection("users").document(testDeviceId).set(testUser)
                .addOnSuccessListener(aVoid -> Log.d("ProfileManagementTest", "Test user set up successfully"))
                .addOnFailureListener(e -> Log.e("ProfileManagementTest", "Failed to set up test user: " + e.getMessage()));
    }

    /**
     * Tests the successful retrieval of user data from Firestore.
     * Verifies that the correct user data is retrieved based on the test device ID.
     */
    @Test
    public void testGetUserData_Success() {
        profileManagement.getUserData(testDeviceId, new ProfileManagement.UserDataCallback() {
            @Override
            public void onUserDataReceived(UserInfo user) {
                assertNotNull("User data retrieved successfully", user);
                assertTrue("Correct user name", "Alice Smith".equals(user.firstName + " " + user.lastName));
                assertTrue("Correct user email", "alice@gmail.com".equals(user.getEmail()));
                assertTrue("Correct user phone number", "123-456-789".equals(user.getPhoneNumber()));
            }

            @Override
            public void onDataNotAvailable() {
                fail("User data should be available for the test device ID");
            }

            @Override
            public void onError(Exception e) {
                fail("Error retrieving user data: " + e.getMessage());
            }
        });
    }

    /**
     * Tests the retrieval of user data for a non-existent user (failure scenario).
     * Verifies that the callback correctly handles the case where the user data does not exist.
     */
    @Test
    public void testGetUserData_Failure() {
        String nonExistentDeviceId = "non_existent_device_id";

        profileManagement.getUserData(nonExistentDeviceId, new ProfileManagement.UserDataCallback() {
            @Override
            public void onUserDataReceived(UserInfo user) {
                fail("User data should not be retrieved for a non-existent device ID");
            }

            @Override
            public void onDataNotAvailable() {
                assertTrue("Data not available for non-existent user", true);
            }

            @Override
            public void onError(Exception e) {
                fail("Unexpected error occurred: " + e.getMessage());
            }
        });
    }

    /**
     * Cleans up after each test by deleting the test user from Firestore.
     * Ensures no leftover test data remains in the Firestore database.
     */
    @After
    public void tearDown() {
        db.collection("users").document(testDeviceId).delete()
                .addOnSuccessListener(aVoid -> Log.d("ProfileManagementTest", "Test user deleted successfully"))
                .addOnFailureListener(e -> Log.e("ProfileManagementTest", "Failed to delete test user: " + e.getMessage()));
    }
}