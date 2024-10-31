package com.example.fusion0;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.UUID;

/*
 * Java docs provided by chatgpt.
 * Youtube Firebase tutorial: https://www.youtube.com/watch?v=Eg2aGWblQvk&list=PLjnZx7KD6m-ySasHtRKIX-04VuOYmGBmr
 * UUID resource: https://codegym.cc/groups/posts/guide-to-uuid-in-java
 */

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.UUID;

/**
 * The Event class represents an event that is stored in the events Firestore collection.
 * Each event has a unique event ID, a name, and a date.
 * This class provides methods to store and delete event data in Firebase Firestore.
 */
public class Event {
    private String eventId;
    private String eventName;
    private String eventDate;

    // Reference to the Firestore "events" collection
    private final CollectionReference eventsRef;

    /**
     * Constructor to initialize a new Event object.
     * It generates a unique event ID using UUID and sets the event name and date.
     *
     * @param eventName The name of the event.
     * @param eventDate The date of the event.
     */
    public Event(String eventName, String eventDate) {
        this.eventId = UUID.randomUUID().toString();
        this.eventName = eventName;
        this.eventDate = eventDate;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
    }

    /**
     * Stores the event in Firestore along with its QR code data.
     * This method saves the event details and the associated QR code to
     * the "events" collection in Firestore.
     *
     * @param qrCode The QRCode object representing the QR code of the event.
     */
    public void storeEvent(QRCode qrCode) {
        HashMap<String, Object> event = this.eventData();
        event.put("qrCode", qrCode.getQrCode());  // Include QR code data in event

        // Store event in Firestore
        eventsRef.document(eventId).set(event)
                .addOnSuccessListener(aVoid -> System.out.println("Event added successfully"))
                .addOnFailureListener(e -> System.out.println("Error adding event: " + e.getMessage()));
    }


}
