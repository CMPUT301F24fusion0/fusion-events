package com.example.fusion0;

import static org.junit.Assert.fail;

import androidx.test.core.app.ApplicationProvider;

import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.OrganizerInfo;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

/**
 * Organizer integration tests with firebase
 * @author Simon Haile
 */
public class OrganizersTest {
    private EventFirebase eventFirebase;
    private CollectionReference organizersRef;

    @Before
    public void setUp() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        organizersRef = db.collection("organizers");
        eventFirebase = new EventFirebase();
    }

    /**
     * Initialize a new organizer
     * @author Simon Haile
     * @return organizer
     */
    public OrganizerInfo newOrganizer()  {
        return new OrganizerInfo("device123");
    }

    /**
     * Ensure the collection exists and is accessible.
     * @author Simon Haile
     */
    @Test
    public void collectionOrganizersTest() {
        organizersRef
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        throw new IllegalArgumentException("This collection doesn't exist");
                    }
                });
    }

    /**
     * Tests if an organizer can be successfully added to Firestore.
     * @author Simon Haile
     */
    @Test
    public void addOrganizerTest() {
        OrganizerInfo organizer = newOrganizer();
        eventFirebase.addOrganizer(organizer);

        organizersRef
                .document(organizer.deviceId)
                .get()
                .addOnSuccessListener(task -> {
                    if (!task.exists()) {
                        fail("Organizer was not added to Firestore.");
                    }
                });
    }

    /**
     * Tests if an organizer can be successfully retrieved from Firestore.
     * @author Simon Haile
     */
    @Test
    public void findOrganizerTest() {
        OrganizerInfo organizer = newOrganizer();
        eventFirebase.addOrganizer(organizer); // First, add the organizer to Firestore

        eventFirebase.findOrganizer(organizer.deviceId, new EventFirebase.OrganizerCallback() {
            @Override
            public void onSuccess(OrganizerInfo retrievedOrganizer) {
                if (!(Objects.equals(retrievedOrganizer.deviceId, organizer.deviceId))) {
                    fail("Organizer retrieved from Firestore does not match the expected values.");
                }
            }

            @Override
            public void onFailure(String error) {
                fail("Failed to retrieve organizer: " + error);
            }
        });
    }

    /**
     * Tests if an organizer's information can be successfully updated.
     * @author Simon Haile
     */
    @Test
    public void editOrganizerTest() {
        OrganizerInfo organizer = newOrganizer();
        eventFirebase.addOrganizer(organizer); // Add organizer to Firestore first

        // Update the organizer's deviceId or other details
        organizer.deviceId = "updatedDevice123";
        eventFirebase.editOrganizer(organizer);

        organizersRef
                .document(organizer.deviceId)
                .get()
                .addOnSuccessListener(task -> {
                    if (task.exists()) {
                        if (!(Objects.equals(task.getString("deviceId"), "updatedDevice123"))) {
                            fail("Organizer deviceId was not updated correctly in Firestore.");
                        }
                    } else {
                        fail("Organizer document does not exist after update.");
                    }
                });
    }

    /**
     * Tests if an organizer can be successfully deleted from Firestore.
     * @author Simon Haile
     */
    @Test
    public void deleteOrganizerTest() {
        OrganizerInfo organizer = newOrganizer();
        eventFirebase.addOrganizer(organizer); // Add organizer to Firestore first

        eventFirebase.deleteOrganizer(organizer.deviceId);

        organizersRef
                .document(organizer.deviceId)
                .get()
                .addOnSuccessListener(task -> {
                    if (task.exists()) {
                        fail("Organizer was not deleted from Firestore.");
                    }
                });
    }
}
