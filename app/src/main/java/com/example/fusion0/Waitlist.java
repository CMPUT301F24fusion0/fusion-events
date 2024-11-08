package com.example.fusion0;

import android.util.Log;

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
    private final FirebaseFirestore db;
    static ArrayList<String> chosen;
    static ArrayList<String> cancel;
    CollectionReference eventsRef;


    public Waitlist() {
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        chosen = new ArrayList<String>();
        cancel = new ArrayList<String>();
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
        // Reference to the event's waiting list
        CollectionReference waitingListRef = db.collection("events")
                .document(eventId)
                .collection("waitingList");

        // Create entrant data with a "waiting" status
        HashMap<String, Object> entrantData = new HashMap<>();
        entrantData.put("status", "waiting");

        // Add entrant to the waiting list only if they are not already present
        getAll(eventId, all -> {
            if (!all.contains(entrantId)) {
                waitingListRef.document(entrantId).set(entrantData)
                        .addOnSuccessListener(aVoid -> {
                            System.out.println("Entrant added to waiting list");
                        })
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
                            .addOnSuccessListener(aVoid -> {
                                System.out.println("Entrant removed from waiting list");
                            })
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

                                        chosen.add(entrant.getId());
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
                                        chosen.add(chosenEntrant.getId());
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

    public void getAll(String eventId, AllCB allCB) {
        ArrayList<String> all = new ArrayList<>();
        CollectionReference waitingListRef = db.collection("events")
                .document(eventId)
                .collection("waitingList");

        waitingListRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                for (DocumentSnapshot doc : docs) {
                    all.add(doc.getId());
                }
                allCB.allDid(all);
            } else {
                Log.e("Error", "Error");
            }
        });
    }

    public interface AllCB {
        void allDid(ArrayList<String> all);
    }

    public void getCancel(String eventId) {
        CollectionReference waitingListRef = db.collection("events")
                .document(eventId)
                .collection("waitingList");

        waitingListRef.whereEqualTo("status", "declined").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                for (DocumentSnapshot doc : docs) {
                    cancel.add(doc.getId());
                }
            } else {
                Log.e("Error", "Error");
            }
        });
    }

    public void getChosen(String eventId) {
        CollectionReference waitingListRef = db.collection("events")
                .document(eventId)
                .collection("waitingList");

        waitingListRef.whereEqualTo("status", "chosen").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                for (DocumentSnapshot doc : docs) {
                    chosen.add(doc.getId());
                }
            } else {
                Log.e("Error", "Error");
            }
        });
    }

    public void allNotification(String eventId, String dID) {
        getAll(eventId, new AllCB() {
            @Override
            public void allDid(ArrayList<String> all) {
                for (String dID: all) {
                    AppNotifications.sendNotification(dID, "Welcome", "Welcome to the waiting list, the lottery" +
                            "will occur soon! Stay tuned for more information.");
                }
            }
        });
    }

    public void chosenNotification() {
        for (String dID: chosen) {
            AppNotifications.sendNotification(dID, "Congratulations", "You've been selected." +
                    "Please accept the in-app invite for the event to confirm your participation");
        }
    }

    public void cancelNotifications() {
        for (String dID: cancel) {
           AppNotifications.sendNotification(dID, "Declined", "You've rejected the invite for" +
                   "this event. You will no longer be able to join unless a spot opens up");

        }
    }

}
