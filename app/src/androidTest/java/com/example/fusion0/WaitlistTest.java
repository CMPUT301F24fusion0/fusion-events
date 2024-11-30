package com.example.fusion0;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.helpers.Waitlist;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WaitlistTest {
    private static CollectionReference eventRef;
    private static Waitlist waitlist;
    private static UserFirestore userFirestore;
    private static EventInfo event;

    /**
     * Initializes everything necessary for this class
     * @throws WriterException exception related to the QR code
     * @author Sehej Brar
     */
    @BeforeClass
    public static void firebase() throws WriterException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        waitlist =  new Waitlist();
        UserInfo user = new UserInfo(new ArrayList<>(), "first", "last", "email", "123", "did", new ArrayList<>());
        userFirestore = new UserFirestore();
        userFirestore.addUser(user, () -> {});
        event = new EventInfo(
                "Organizer1", "Sample Event", "123 Address", "facility1", "Sample Facility",
                "100", "0", "Test event", new java.util.Date(), new java.util.Date(),
                new java.util.Date(), "12:00", "14:00", "eventPosterUrl", false, 0.0, 0.0, 100
        );
        new EventFirebase().addEvent(event);
    }

    /**
     * Testing if conversions we done in Waitlist will work
     * @author Sehej Brar
     */
    @Test
    public void testAdd() {
        Map<String, String> user = new HashMap<>();
        user.put("did", "did");
        user.put("status", "waiting");
        eventRef.document(event.getEventID()).update("waitinglist", FieldValue.arrayUnion(user));
        eventRef.document(event.getEventID()).get().addOnSuccessListener(documentSnapshot -> {
            ArrayList<Map<String, String>> result  = (ArrayList<Map<String, String>>) documentSnapshot.get("waitlist");
            assertNotNull(result);
        });
    }

    /**
     * Checks if the changeStatus method correctly works
     * @author Sehej Brar
     */
    @Test
    public void testChangeStatus() {
        waitlist.changeStatus(event.getEventID(), "did", "chosen");
        eventRef.document(event.getEventID()).get().addOnSuccessListener(documentSnapshot -> {
            ArrayList<Map<String, String>> result  = (ArrayList<Map<String, String>>) documentSnapshot.get("waitlist");
            assertEquals(result.get(0).get("status"), "chosen");
        });
    }

    /**
     * Tests if getAll works
     * @author Sehej Brar
     */
    @Test
    public void testGetAll() {
        waitlist.getAll(event.getEventID(), all -> {
            assertEquals(all.get(0), "did");
        });
    }

    /**
     * Tests if getChosen works
     * @author Sehej Brar
     */
    @Test
    public void testGetChosen() {
        waitlist.getChosen(event.getEventID(), chosen -> {
            assertEquals(chosen.get(0), "did");
        });
    }
}
