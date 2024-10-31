package com.example.fusion0;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * The {@code Waitlist} class provides methods to manage and interact with the
 * waiting list for events. It allows adding entrants to the waiting list and
 * randomly selecting a specified number of attendees from the waiting list
 * for an event.
 *
 * <p>This class is intended to handle all waiting list-related operations,
 * separate from user management and other event functionalities.</p>
 *
 * <p>Usage example:
 * <pre>
 *     Waitlist waitlist = new Waitlist();
 *     waitlist.addEntrantToWaitingList("eventId123", "entrantId456");
 *     waitlist.sampleAttendees("eventId123", 5);
 * </pre>
 * </p>
 */
public class Waitlist {


    /**
     * Adds an entrant to the waiting list for a specific event.
     *
     * @param eventId   The unique identifier of the event.
     * @param entrantId The unique identifier of the entrant to be added to the waiting list.
     *
     * This method checks if the entrant already exists in the waiting list.
     * If not, it adds the entrant with a status of "waiting".
     */
    public void addEntrantToWaitingList(String eventId, String entrantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the event's waiting list
        CollectionReference waitingListRef = db.collection("events")
                .document(eventId)
                .collection("waitingList");

        // Create entrant data with a "waiting" status
        HashMap<String, Object> entrantData = new HashMap<>();
        entrantData.put("status", "waiting");

        // Add entrant to the waiting list only if they are not already present
        waitingListRef.document(entrantId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().exists()) {
                waitingListRef.document(entrantId).set(entrantData)
                        .addOnSuccessListener(aVoid -> System.out.println("Entrant added to waiting list"))
                        .addOnFailureListener(e -> System.out.println("Error adding entrant: " + e));
            } else {
                System.out.println("Entrant already exists in the waiting list");
            }
        });
    }



    /**
     * Samples a specified number of attendees from the waiting list for a specific event.
     *
     * @param eventId      The unique identifier of the event.
     * @param numToSelect  The number of attendees to be randomly selected from the waiting list.
     *
     * This method retrieves all entrants with a status of "waiting" and randomly selects
     * a specified number of them. The selected entrants' statuses are updated to "chosen".
     * This method does not return any values, but updates Firestore with the chosen entrants.
     */
    public void sampleAttendees(String eventId, int numToSelect) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(eventId).collection("waitingList")
                .whereEqualTo("status", "waiting") // only users who are waiting
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> entrants = task.getResult().getDocuments();
                        Collections.shuffle(entrants); // Randomize order for lottery

                        List<DocumentSnapshot> chosenEntrants = entrants.subList(0, Math.min(numToSelect, entrants.size()));
                        for (DocumentSnapshot entrant : chosenEntrants) {
                            entrant.getReference().update("status", "chosen");
                            //ADD notification helper function or Firebase Cloud Messaging
                        }

                        System.out.println(numToSelect + " entrants chosen for event " + eventId);
                    } else {
                        System.out.println("Error sampling attendees: " + task.getException());
                    }
                });

}
