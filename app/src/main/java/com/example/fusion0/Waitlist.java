package com.example.fusion0;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author : Ali Abouei, Sehej Brar
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
    CollectionReference eventsRef;
    UserFirestore userFirestore;


    public Waitlist() {
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        userFirestore = new UserFirestore();
    }


    /**
     * @author : Ali Abouei
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
                long capacity;
                long acceptedCount;

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
                System.out.println("Event not found for event ID: " + eventId);
            }
        }).addOnFailureListener(e -> System.out.println("Error fetching event details: " + e.getMessage()));
    }


    /**
     * @author : Ali Abouei
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

    /**
     * @author Sehej Brar
     * Add user to their waitlist
     * @param entrantId entrant id
     * @param eventId event id
     */
    public void addToUserWL(String entrantId, String eventId) {
        db.collection("users").document(entrantId)
                .update("events", FieldValue.arrayUnion(eventId))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Good", "Task");
                    } else {
                        Log.e("Fail", "Fail to update");
                    }
                });
    }

    /**
     * Remove event from user's waitlist
     * @author Sehej Brar
     * @param entrantId entrant id
     * @param eventId event id
     */
    public void removeFromUserWL(String entrantId, String eventId) {
        db.collection("users").document(entrantId)
                .update("events", FieldValue.arrayRemove(eventId))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Good", "Task");
                    } else {
                        Log.e("Fail", "Fail to update");
                    }
                });
    }

    /**
     * Interface for all waitlist entrants
     * @author Sehej Brar
     */
    public interface AllCB {
        void allDid(ArrayList<String> all);
    }

    /**
     * Gets all those on waitlist
     * @author Sehej Brar
     * @param eventId event id
     * @param allCB a callback for all entrants on waitlist
     */
    public void getAll(String eventId, AllCB allCB) {
        ArrayList<String> all = new ArrayList<>();
        DocumentReference waitingListDoc = db.collection("events")
                .document(eventId);

        waitingListDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    ArrayList<ArrayList<String>> all_waitingList = (ArrayList<ArrayList<String>>) doc.get("waitinglist");

                    if (all_waitingList != null) {
                        for (ArrayList<String> user: all_waitingList) {
                            all.add(user.get(0));
                        }
                    }
                }
                allCB.allDid(all);
            } else {
                Log.e("Error", "Error");
            }
        });
    }

    /**
     * Interface for all entrants who cancelled after being chosen
     * @author Sehej Brar
     */
    public interface CancelCB {
        void cancelDid(ArrayList<String> cancel);
    }

    /**
     * Gets those that cancel
     * @author Sehej Brar
     * @param eventId event id
     * @param cancelCB callback for people who cancelled
     */
    public void getCancel(String eventId, CancelCB cancelCB) {
        ArrayList<String> cancel = new ArrayList<>();
        DocumentReference waitingListDoc = db.collection("events")
                .document(eventId);

        waitingListDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    ArrayList<ArrayList<String>> all_waitingList = (ArrayList<ArrayList<String>>) doc.get("waitinglist");

                    if (all_waitingList != null) {
                        for (ArrayList<String> user: all_waitingList) {
                            if (Objects.equals(user.get(3), "cancel")) {
                                cancel.add(user.get(0));
                            }
                        }
                    }
                }
                cancelCB.cancelDid(cancel);
            } else {
                Log.e("Error", "Error");
            }
        });
    }

    /**
     * Interface for all chosen entrants
     * @author Sehej Brar
     */
    public interface ChosenCB {
        void ChosenDid(ArrayList<String> chosen);
    }

    /**
     * Gets the people chosen from the lottery
     * @author Sehej Brar
     * @param eventId event id
     * @param chosenCB callback to get the chosen people
     */
    public void getChosen(String eventId, ChosenCB chosenCB) {
        ArrayList<String> chosen = new ArrayList<>();
        DocumentReference waitingListDoc = db.collection("events")
                .document(eventId);

        waitingListDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    ArrayList<ArrayList<String>> all_waitingList = (ArrayList<ArrayList<String>>) doc.get("waitinglist");

                    if (all_waitingList != null) {
                        for (ArrayList<String> user: all_waitingList) {
                            if (Objects.equals(user.get(3), "chosen")) {
                                chosen.add(user.get(0));
                            }
                        }
                    }
                }
                chosenCB.ChosenDid(chosen);
            } else {
                Log.e("Error", "Error");
            }
        });
    }

    /**
     * Sends a notification to everyone on the waitlist
     * @author Sehej Brar
     * @param eventId event id
     * @param title title of notification
     * @param message message of notification
     */
    public void allNotification(String eventId, String title, String message, String flag) {
        getAll(eventId, all -> {
            for (String dID: all) {
                AppNotifications.sendNotification(dID, title, message, flag);
            }
        });
    }

    /**
     * Sends notification to the ones chosen in the lottery
     * @author Sehej Brar
     * @param eventId event id
     * @param title title of notification
     * @param message message of notification
     */
    public void chosenNotification(String eventId, String title, String message, String flag) {
        getChosen(eventId, chosen -> {
            for (String dID: chosen) {
                AppNotifications.sendNotification(dID, title, message, flag);
            }
        });
    }

    /**
     * Sends notification to the ones not chosen in the lottery
     * @author Sehej Brar
     * @param eventId event id
     * @param title title of notification
     * @param message message of notification
     */
    public void loseNotification(String eventId, String title, String message, String flag) {
        getAll(eventId, all -> getChosen(eventId, chosen -> {
            all.removeAll(chosen);
            for (String dID: all) {
                AppNotifications.sendNotification(dID, title, message, flag);
            }
        }));
    }

    /**
     * Sends notification to the ones who cancelled their invite after the lottery
     * @author Sehej Brar
     * @param eventId event id
     * @param title title of notification
     * @param message message of notification
     */
    public void cancelNotifications(String eventId, String title, String message, String flag) {
        getCancel(eventId, cancel -> {
            for (String dID: cancel) {
                AppNotifications.sendNotification(dID, title, message, flag);
            }
        });
    }
}
