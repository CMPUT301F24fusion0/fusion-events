package com.example.fusion0;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

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

    @Test
    public void addOrganizerTest() {

    }
}
