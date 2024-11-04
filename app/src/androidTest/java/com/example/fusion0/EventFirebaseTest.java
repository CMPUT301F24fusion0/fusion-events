package com.example.fusion0;


import static org.junit.Assert.fail;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class EventFirebaseTest {
    private CollectionReference organizersRef, facilitiesRef, eventsRef;
    private EventFirebase firebase;

    @Before
    public void firebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        organizersRef = db.collection("organizers");
        facilitiesRef = db.collection("facilities");
        eventsRef = db.collection("facilities");
        firebase = new EventFirebase();
    }

    public OrganizerInfo newOrganizer() {
        ArrayList<EventInfo> events = new ArrayList<>();
        ArrayList<FacilitiesInfo> facilities = new ArrayList<>();
        return new OrganizerInfo("1234");
    }

    @Test
    public void addOrganizerTest() {
        EventFirebase.addOrganizer(newOrganizer());
        organizersRef
                .document("1234")
                .get()
                .addOnSuccessListener(task -> {
                    if (!task.exists()) {
                        fail();
                    }
                });
    }

    @Test
    public void editTest() throws WriterException {
        OrganizerInfo organizer = new OrganizerInfo("1234");

        Date startDate = new Date();
        Date endDate = new Date();
        EventInfo event = new EventInfo("1234", "Event1", "address", "facility1", "100", "description", startDate, endDate, "12", "2", "eventposter");
        ArrayList<EventInfo> newEventList = organizer.getEvents();
        newEventList.add(event);
        organizer.setEvents(newEventList);

        FacilitiesInfo facility = new FacilitiesInfo("address", "facility1", "1234");
        ArrayList<FacilitiesInfo> newFacilitiesList = organizer.getFacilities();
        newFacilitiesList.add(facility);
        organizer.setFacilities(newFacilitiesList);

        EventFirebase.editOrganizer(organizer);
        organizersRef
                .document("1234")
                .get()
                .addOnSuccessListener(task -> {
                    if (task.exists()) {
                        if (!(Objects.equals(task.getString("events"), "Event 1"))) { // Check the correct event name
                            fail("Expected event name not found");
                        }
                    } else {
                        fail("Organizer document does not exist");
                    }
                });
    }

    @Test
    public void deleteTest() {
        firebase.deleteOrganizer("1234");
        organizersRef
                .document("1234")
                .get()
                .addOnSuccessListener(task -> {
                    if (task.exists()) {
                        fail();
                    }
                });

    }
}

    }

