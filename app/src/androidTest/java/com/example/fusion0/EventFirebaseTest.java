package com.example.fusion0;

import static org.junit.Assert.fail;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

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
        ArrayList<String> events = new ArrayList<>(Arrays.asList("Event 1", "Event 2"));
        ArrayList<String> facilities = new ArrayList<>(Arrays.asList("Facility 1", "Facility 2"));
        return new OrganizerInfo(events, facilities, "1234");
    }

    @Test
    public void addOrganizerTest() {
        firebase.addOrganizer(newOrganizer());
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
    public void editTest() {
        HashMap<String, Object> organizer = new HashMap<>();
        organizer.put("events", "Event 3");
        organizer.put("facilities", Arrays.asList("Facility 1", "Facility 2"));
        organizer.put("deviceId", "1234");
        firebase.editOrganizer(newOrganizer(), organizer);
        organizersRef
                .document("1234")
                .get()
                .addOnSuccessListener(task -> {
                    if (task.exists()) {
                        if (!(Objects.equals(task.getString("events"), "Event 3"))) {
                            fail();
                        }
                    } else {
                        fail();
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
