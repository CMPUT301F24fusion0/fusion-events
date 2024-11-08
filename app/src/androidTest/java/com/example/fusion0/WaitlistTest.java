package com.example.fusion0;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
public class WaitlistTest {
    private FirebaseFirestore db;
    private Waitlist waitlist;
    private String testEventId;
    private String testEntrantId;
    private String testEventId_T ;
    private String testEntrantId_T;
    private String exs_evID;

    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();
        waitlist = new Waitlist();
        testEventId = "testEvent123";
        testEntrantId = "testEntrant456";


        testEventId_T = "EVENT100";



        testEntrantId_T = "ENTRANT JOE C";
        exs_evID = "a8cb6aa0-29bd-4bdf-a2da-5d0cb5e33a5e";


        //waitlist.addEntrantToWaitingList(exs_evID, testEntrantId_T);
    }

    @After
    public void tearDown() {
        // Delete the test data after each test
        db.collection("events").document(testEventId).collection("waitingList").document(testEntrantId).delete();
    }


    @Test
    public void testAddEntrantToWaitingList() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        System.out.println("Adding entrant to waiting list...");
        waitlist.addEntrantToWaitingList(testEventId, testEntrantId);

        // Adding a short delay to allow Firebase to commit the document
        Thread.sleep(2000);

        db.collection("events").document(testEventId).collection("waitingList").document(testEntrantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    System.out.println("Document retrieved: " + documentSnapshot.getId());
                    assertTrue("Document does not exist as expected.", documentSnapshot.exists());
                    assertEquals("Status is not set to 'waiting' as expected.", "waiting", documentSnapshot.getString("status"));
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail("Firebase operation failed: " + e.getMessage());
                    latch.countDown();
                });

        latch.await();
    }


    @Test
    public void testRemoveEntrantFromWaitingList() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        System.out.println("Adding entrant to waiting list...");
        waitlist.addEntrantToWaitingList(testEventId, testEntrantId);

        // Adding a small wait before removing to ensure the document exists
        Thread.sleep(2000);

        System.out.println("Removing entrant from waiting list...");
        waitlist.removeEntrantFromWaitingList(testEventId, testEntrantId);

        // Adding another delay to ensure Firestore processes the delete/update request
        Thread.sleep(2000);

        db.collection("events").document(testEventId).collection("waitingList").document(testEntrantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    System.out.println("Document retrieved after removal: " + documentSnapshot.getId());
                    assertFalse("Document should not exist or should be marked as declined.",
                            documentSnapshot.exists() || "declined".equals(documentSnapshot.getString("status")));
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail("Firebase operation failed: " + e.getMessage());
                    latch.countDown();
                });

        latch.await();
    }


    @Test
    public void testSampleAttendees() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        // the number of entrants and number of attendees to select
        int numEntrants = 5;
        int numToSelect = 2;

        for (int i = 1; i <= numEntrants; i++) {
            String entrantId = "LtestEntrant" + i;
            waitlist.addEntrantToWaitingList(exs_evID, entrantId);
        }

        // Adding a delay to allow Firebase to commit the documents
        Thread.sleep(2000);

        // Perform the lottery sampling
        waitlist.sampleAttendees(exs_evID, numToSelect);

        // Adding another delay to ensure Firebase processes the update request
        Thread.sleep(2000);

        // Check if the correct number of attendees were chosen
        db.collection("events").document(exs_evID).collection("waitingList")
                .whereEqualTo("status", "chosen")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> chosenEntrants = queryDocumentSnapshots.getDocuments();
                    assertEquals("The number of chosen entrants is incorrect.", numToSelect, chosenEntrants.size());

                    for (DocumentSnapshot document : chosenEntrants) {
                        System.out.println("Chosen entrant: " + document.getId());
                        assertEquals("Status is not set to 'chosen' as expected.", "chosen", document.getString("status"));
                    }

                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail("Firebase operation failed: " + e.getMessage());
                    latch.countDown();
                });

        latch.await();
    }

}

