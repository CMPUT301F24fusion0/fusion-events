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

    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();
        waitlist = new Waitlist();
        testEventId = "testEvent123";
        testEntrantId = "testEntrant456";
    }

    @After
    public void tearDown() {
        // Optionally, delete test data created during tests
        db.collection("events").document(testEventId).collection("waitingList").document(testEntrantId).delete();
    }

    // Write test methods below
    @Test
    public void testAddEntrantToWaitingList() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        waitlist.addEntrantToWaitingList(testEventId, testEntrantId);

        db.collection("events").document(testEventId).collection("waitingList").document(testEntrantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    assertTrue(documentSnapshot.exists());
                    assertEquals("waiting", documentSnapshot.getString("status"));
                    latch.countDown();
                })
                .addOnFailureListener(e -> fail("Firebase operation failed: " + e.getMessage()));

        latch.await();
    }


    @Test
    public void testRemoveEntrantFromWaitingList() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        waitlist.addEntrantToWaitingList(testEventId, testEntrantId);
        waitlist.removeEntrantFromWaitingList(testEventId, testEntrantId);

        db.collection("events").document(testEventId).collection("waitingList").document(testEntrantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    assertFalse(documentSnapshot.exists() || "declined".equals(documentSnapshot.getString("status")));
                    latch.countDown();
                })
                .addOnFailureListener(e -> fail("Firebase operation failed: " + e.getMessage()));

        latch.await();
    }

}
