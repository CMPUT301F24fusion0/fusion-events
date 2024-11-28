package com.example.fusion0;

import static org.junit.Assert.fail;

import androidx.test.core.app.ApplicationProvider;

import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.FacilitiesInfo;
import com.example.fusion0.models.OrganizerInfo;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class EventFirebaseTest {
    private CollectionReference eventsRef;
    private CollectionReference facilitiesRef;
    private CollectionReference organizersRef;
    private EventFirebase eventFirebase;

    @Before
    public void setUp() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        facilitiesRef = db.collection("facilities");
        organizersRef = db.collection("organizers");
        eventFirebase = new EventFirebase();
    }

    /**
     * Helper method to create a new EventInfo object.
     */
    public EventInfo newEvent() throws WriterException {
        return new EventInfo(
                "Organizer1", "Sample Event", "123 Address", "facility1", "Sample Facility",
                "100", "0", "Test event", new java.util.Date(), new java.util.Date(),
                new java.util.Date(), "12:00", "14:00", "eventPosterUrl", false, 0.0, 0.0, 100
        );
    }



    /**
     * Ensure the collection exists and is accessible.
     */
    @Test
    public void collectionEventTest() {
        eventsRef
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        throw new IllegalArgumentException("This collection doesn't exist");
                    }
                });
    }

    /**
     * Helper method to create a new FacilitiesInfo object.
     */
    public FacilitiesInfo newFacility() {
        return new FacilitiesInfo("123 Address", "Facility 1", "Owner", 0.0, 0.0, "facilityImage");
    }

    /**
     * Ensure the collection exists and is accessible.
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

    public OrganizerInfo newOrganizer()  {
        OrganizerInfo organizer = new OrganizerInfo("device123");
        return organizer;
    }

    /**
     * Ensure the collection exists and is accessible.
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
     * Tests if an event can be successfully added to Firestore.
     */
    @Test
    public void addEventTest() throws WriterException {
        EventInfo event = newEvent();
        eventFirebase.addEvent(event);

        eventsRef
                .document(event.getEventID())
                .get()
                .addOnSuccessListener(task -> {
                    if (!task.exists()) {
                        fail("Event was not added to Firestore.");
                    }
                });
    }

    /**
     * Tests if an event can be successfully retrieved from Firestore.
     */
    @Test
    public void findEventTest() throws WriterException {
        EventInfo event = newEvent();
        eventFirebase.addEvent(event);

        eventFirebase.findEvent(event.getEventID(), new EventFirebase.EventCallback() {
            @Override
            public void onSuccess(EventInfo retrievedEvent) {
                if (!(Objects.equals(retrievedEvent.getEventID(), event.getEventID())) ||
                        !(Objects.equals(retrievedEvent.getEventName(), "Sample Event"))) {
                    fail("Event retrieved from Firestore does not match the expected values.");
                }
            }

            @Override
            public void onFailure(String error) {
                fail("Failed to retrieve event: " + error);
            }
        });
    }

    /**
     * Tests if an event's information can be successfully updated.
     */
    @Test
    public void editEventTest() throws WriterException {
        EventInfo event = newEvent();
        eventFirebase.addEvent(event); // Add event to Firestore first

        event.setEventName("Updated Event Name");
        eventFirebase.editEvent(event);

        eventsRef
                .document(event.getEventID())
                .get()
                .addOnSuccessListener(task -> {
                    if (task.exists()) {
                        if (!(Objects.equals(task.getString("eventName"), "Updated Event Name"))) {
                            fail("Event name was not updated correctly in Firestore.");
                        }
                    } else {
                        fail("Event document does not exist after update.");
                    }
                });
    }

    /**
     * Tests if an event can be successfully deleted from Firestore.
     */
    @Test
    public void deleteEventTest() throws WriterException {
        EventInfo event = newEvent();
        eventFirebase.addEvent(event);

        eventFirebase.deleteEvent(event.getEventID());

        eventsRef
                .document(event.getEventID())
                .get()
                .addOnSuccessListener(task -> {
                    if (task.exists()) {
                        fail("Event was not deleted from Firestore.");
                    }
                });
    }


    /**
     * Tests if a facility can be successfully added to Firestore.
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
    /**
     * Tests if an organizer can be successfully added to Firestore.
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
