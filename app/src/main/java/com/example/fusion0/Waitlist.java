package com.example.fusion0;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Author : Ali Abouei
 * The {@code Waitlist} class provides methods to manage and interact with the
 * waiting list for events. It allows adding and removing entrants to the waiting list,
 * selecting a specified number of attendees from the waiting list, and offering
 * another chance to join the event if an entrant declines.
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
    private FirebaseFirestore db;

    public Waitlist() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Adds an entrant to the waiting list for a specific event.
     *
     * @param eventId   The unique identifier of the event.
     * @param entrantId The unique identifier of the entrant to be added to the waiting list.
     *                  <p>
     *                  This method checks if the entrant already exists in the waiting list.
     *                  If not, it adds the entrant with a status of "waiting".
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
     * Removes an entrant from the waiting list or marks them as declined if they were chosen.
     *
     * @param eventId   The unique identifier of the event.
     * @param entrantId The unique identifier of the entrant to be removed or marked as declined.
     *                  <p>
     *                  This method updates the entrant's status to "declined" if they were previously
     *                  chosen and decreases the accepted count in the event document if needed.
     */
    public void removeEntrantFromWaitingList(String eventId, String entrantId) {

        CollectionReference eventsRef = db.collection("events");

        // Reference to the event's waiting list
        CollectionReference waitingListRef = db.collection("events")
                .document(eventId)
                .collection("waitingList");

        waitingListRef.document(entrantId).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                String status = document.getString("status");

                if ("chosen".equals(status)) {
                    // Mark entrant as declined
                    document.getReference().update("status", "declined");


                    // Decrement acceptedCount in the event document
                    eventsRef.document(eventId).get().addOnSuccessListener(eventDoc -> {
                        Long acceptedCount = eventDoc.getLong("acceptedCount");
                        if (acceptedCount != null && acceptedCount > 0) {
                            eventsRef.document(eventId).update("acceptedCount", acceptedCount - 1);
                        }
                    });
                } else {
                    // Remove the entrant if they're still on the waiting list
                    waitingListRef.document(entrantId).delete()
                            .addOnSuccessListener(aVoid -> System.out.println("Entrant removed from waiting list"))
                            .addOnFailureListener(e -> System.out.println("Error removing entrant: " + e));
                }
            } else {
                System.out.println("Entrant not found in waiting list.");
            }
        });
    }


    /**
     * Samples a specified number of attendees from the waiting list for a specific event.
     *
     * @param eventId     The unique identifier of the event.
     * @param numToSelect The number of attendees to be randomly selected from the waiting list.
     *                    This method retrieves all entrants with a status of "waiting" and randomly selects
     *                    a specified number of them. The selected entrants' statuses are updated to "chosen",
     *                    and the accepted count in the event document is incremented.
     */
    public void sampleAttendees(String eventId, int numToSelect) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsRef = db.collection("events");

        // Fetch event details to get capacity and current acceptedCount
        eventsRef.document(eventId).get().addOnSuccessListener(eventDoc -> {
            if (eventDoc.exists()) {
                Long capacity;
                Long acceptedCount;

                // Check if capacity is stored as a number or a string, then convert
                Object capacityField = eventDoc.get("capacity");
                Object acceptedCountField = eventDoc.get("acceptedCount");

                // Handle capacity conversion
                if (capacityField instanceof Number) {
                    capacity = ((Number) capacityField).longValue();
                } else if (capacityField instanceof String) {
                    try {
                        capacity = Long.parseLong((String) capacityField);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Capacity is not a valid number.");
                        return;
                    }
                } else {
                    System.out.println("Error: Capacity is missing or invalid.");
                    return;
                }

                // Handle acceptedCount conversion
                if (acceptedCountField instanceof Number) {
                    acceptedCount = ((Number) acceptedCountField).longValue();
                } else if (acceptedCountField instanceof String) {
                    try {
                        acceptedCount = Long.parseLong((String) acceptedCountField);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: AcceptedCount is not a valid number.");
                        return;
                    }
                } else {
                    System.out.println("Error: AcceptedCount is missing or invalid.");
                    return;
                }

                if (capacity != null && acceptedCount != null) {
                    // Calculate the number of entrants we actually need
                    int spotsRemaining = (int) (capacity - acceptedCount);
                    int finalNumToSelect = Math.min(numToSelect, spotsRemaining);
                    db.collection("events").document(eventId).collection("waitingList")
                            .whereEqualTo("status", "waiting")
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    List<DocumentSnapshot> entrants = task.getResult().getDocuments();
                                    Collections.shuffle(entrants);

                                    List<DocumentSnapshot> chosenEntrants = entrants.subList(0, Math.min(finalNumToSelect, entrants.size()));

                                    System.out.println("Chosen entrants count: " + chosenEntrants.size());

                                    if (chosenEntrants.isEmpty()) {
                                        System.out.println("No entrants to select.");
                                    }

                                    for (DocumentSnapshot entrant : chosenEntrants) {
                                        entrant.getReference().update("status", "chosen")
                                                .addOnSuccessListener(aVoid -> System.out.println("Successfully updated entrant status to 'chosen'"))
                                                .addOnFailureListener(e -> System.out.println("Failed to update entrant status: " + e.getMessage()));
                                    }

                                    System.out.println(finalNumToSelect + " entrants chosen for event " + eventId);
                                } else {
                                    System.out.println("Error sampling attendees: " + task.getException());
                                }
                            });

                } else {
                    System.out.println("Event details are incomplete.");
                }
            } else {
                System.out.println("Event not found for event ID: " + eventId);
            }
        }).addOnFailureListener(e -> System.out.println("Error fetching event details: " + e.getMessage()));
    }


    /**
     * Offers another chance to an entrant if a previously chosen entrant declines and more spots are needed.
     *
     * @param eventId The unique identifier of the event.
     *                <p>
     *                This method selects an entrant from the waiting list to replace a declined participant,
     *                only if the accepted count is still below the event's capacity.
     */
    public void offerAnotherChance(String eventId) {
        CollectionReference eventsRef = db.collection("events");

        eventsRef.document(eventId).get().addOnSuccessListener(eventDoc -> {
            if (eventDoc.exists()) {
                Long capacity = eventDoc.getLong("capacity");
                Long acceptedCount = eventDoc.getLong("acceptedCount");

                if (capacity != null && acceptedCount != null && acceptedCount < capacity) {
                    db.collection("events").document(eventId).collection("waitingList")
                            .whereEqualTo("status", "waiting")
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    List<DocumentSnapshot> waitingEntrants = task.getResult().getDocuments();

                                    if (!waitingEntrants.isEmpty()) {
                                        DocumentSnapshot chosenEntrant = waitingEntrants.get(0);
                                        chosenEntrant.getReference().update("status", "chosen");

                                        // Increment acceptedCount in the event document
                                        eventsRef.document(eventId).update("acceptedCount", acceptedCount + 1);
                                        System.out.println("Another entrant selected for event " + eventId);
                                        // ADD notification helper function or Firebase Cloud Messaging here
                                    } else {
                                        System.out.println("No waiting entrants left to offer another chance.");
                                    }
                                } else {
                                    System.out.println("Error querying waiting entrants: " + task.getException());
                                }
                            });
                } else {
                    System.out.println("Event is already at capacity or does not require more participants.");
                }
            } else {
                System.out.println("Event document not found for event ID: " + eventId);
            }
        }).addOnFailureListener(e -> System.out.println("Error fetching event details: " + e.getMessage()));
    }
}
