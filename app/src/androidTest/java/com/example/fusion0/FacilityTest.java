package com.example.fusion0;

import static org.junit.Assert.fail;

import androidx.test.core.app.ApplicationProvider;

import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.FacilitiesInfo;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

public class FacilityTest {
    private CollectionReference facilitiesRef;
    private EventFirebase eventFirebase;

    @Before
    public void initialize() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        facilitiesRef = db.collection("facilities");
        eventFirebase = new EventFirebase();
    }

    /**
     * Helper method to create a new FacilitiesInfo object.
     * @author Simon Haile
     */
    public FacilitiesInfo newFacility() {
        return new FacilitiesInfo("123 Address", "Facility 1", "Owner", 0.0, 0.0, "facilityImage");
    }

    /**
     * Ensure the collection exists and is accessible.
     * @author Simon Haile
     */
    @Test
    public void collectionFacilitiesTest() {
        facilitiesRef
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        throw new IllegalArgumentException("This collection doesn't exist");
                    }
                });
    }

    /**
     * Tests if a facility can be successfully added to Firestore.
     * @author Simon Haile
     */
    @Test
    public void addFacilityTest() {
        FacilitiesInfo facility = newFacility();
        eventFirebase.addFacility(facility);

        facilitiesRef
                .document(facility.getFacilityID())
                .get()
                .addOnSuccessListener(task -> {
                    if (!task.exists()) {
                        fail("Facility was not added to Firestore.");
                    }
                });
    }

    /**
     * Tests if a facility can be successfully retrieved from Firestore.
     * @author Simon Haile
     */
    @Test
    public void findFacilityTest() {
        FacilitiesInfo facility = newFacility();
        eventFirebase.addFacility(facility); // First, add the facility to Firestore

        eventFirebase.findFacility(facility.getFacilityID(), new EventFirebase.FacilityCallback() {
            @Override
            public void onSuccess(FacilitiesInfo retrievedFacility) {
                if (!(Objects.equals(retrievedFacility.getFacilityID(), facility.getFacilityID())) ||
                        !(Objects.equals(retrievedFacility.getFacilityName(), "Facility 1"))) {
                    fail("Facility retrieved from Firestore does not match the expected values.");
                }
            }

            @Override
            public void onFailure(String error) {
                fail("Failed to retrieve facility: " + error);
            }
        });
    }

    /**
     * Tests if a facility's information can be successfully updated.
     * @author Simon Haile
     */
    @Test
    public void editFacilityTest() {
        FacilitiesInfo facility = newFacility();
        eventFirebase.addFacility(facility); // Add facility to Firestore first

        facility.setFacilityName("Updated Facility Name");
        eventFirebase.editFacility(facility);

        facilitiesRef
                .document(facility.getFacilityID())
                .get()
                .addOnSuccessListener(task -> {
                    if (task.exists()) {
                        if (!(Objects.equals(task.getString("facilityName"), "Updated Facility Name"))) {
                            fail("Facility name was not updated correctly in Firestore.");
                        }
                    } else {
                        fail("Facility document does not exist after update.");
                    }
                });
    }

    /**
     * Tests if a facility can be successfully deleted from Firestore.
     * @author Simon Haile
     */
    @Test
    public void deleteFacilityTest() {
        FacilitiesInfo facility = newFacility();
        eventFirebase.addFacility(facility); // Add facility to Firestore first

        eventFirebase.deleteFacility(facility.getFacilityID());

        facilitiesRef
                .document(facility.getFacilityID())
                .get()
                .addOnSuccessListener(task -> {
                    if (task.exists()) {
                        fail("Facility was not deleted from Firestore.");
                    }
                });
    }
}
