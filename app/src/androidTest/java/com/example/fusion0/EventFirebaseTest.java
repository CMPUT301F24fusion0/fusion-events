package com.example.fusion0;

import static org.junit.Assert.fail;

import androidx.test.core.app.ApplicationProvider;

import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.EventInfo;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

/**
 * Integration tests with firebase
 * @author Simon Haile
 */
public class EventFirebaseTest {
    private CollectionReference eventsRef;
    private EventFirebase eventFirebase;

    /**
     * Set up the required objects
     * @author Simon Haile
     */
    @Before
    public void setUp() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        eventFirebase = new EventFirebase();
    }

    /**
     * Helper method to create a new EventInfo object.
     * @author Simon Haile
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
     * @author Simon Haile
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
     * Tests if an event can be successfully added to Firestore.
     * @author Simon Haile
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
     * @author Simon Haile
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
     * @author Simon Haile
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
     * @author Simon Haile
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
}
