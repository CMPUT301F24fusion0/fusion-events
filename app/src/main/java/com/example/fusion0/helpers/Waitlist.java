package com.example.fusion0.helpers;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.fusion0.models.UserInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author Sehej Brar
 * The {@code Waitlist} class provides methods to manage and interact with the
 * waiting list for events. It allows CRUD operations.
 * </p>
 */

public class Waitlist {
    private final FirebaseFirestore db;
    CollectionReference eventsRef;
    UserFirestore userFirestore;


    /**
     * Constructor for Waitlist
     * @author Sehej Brar
     */
    public Waitlist() {
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        userFirestore = new UserFirestore();
    }


    /**
     * @author Sehej Brar
     * Samples a specified number of entrants from the waiting list for a specific event. Also
     * can be used to re-sample entrants.
     *
     * @param eventId The unique identifier of the event.
     * @param numToSelect The number of attendees to be randomly selected from the waiting list.
     */
    public void conductLottery(String eventId, int numToSelect) {
        // Fetch event details to get capacity and current acceptedCount
        eventsRef.document(eventId).get().addOnSuccessListener(eventDoc -> {
            if (eventDoc.exists()) {
                int capacity;
                int acceptedCount;

                // Check if capacity is stored as a number or a string, then convert
                Object capacityField = eventDoc.get("capacity");
                Object acceptedCountField = eventDoc.get("acceptedCount");

                // Handle capacity conversion
                if (capacityField instanceof Number && acceptedCountField instanceof String) {
                    try {
                        capacity = (int) capacityField;
                        acceptedCount = Integer.parseInt((String) acceptedCountField);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Accepted Count or Capacity is not a number.");

                    }
                } else {
                    throw new IllegalArgumentException("Capacity is not a number or Accepted Count is not a string");
                }

                // Calculate the number of entrants we actually need
                int spotsRemaining = capacity - acceptedCount;
                int finalNumToSelect = Math.min(numToSelect, spotsRemaining);

                getWait(eventId, wait -> {
                    Collections.shuffle(wait);

                    // Hashset allows for faster lookup
                    ArrayList<String> winners_set = new ArrayList<>(wait.subList(0, finalNumToSelect));

                    DocumentReference documentReference = eventsRef.document(eventId);

                    documentReference.get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot doc = task.getResult();
                                    if (doc.exists()) {
                                        ArrayList<Map<String, String>> waitList = (ArrayList<Map<String, String>>) doc.get("waitinglist");

                                        if (waitList != null) {
                                            // If the winners are in the waiting list then their new status is chosen
                                            for (Map<String, String> user : waitList) {
                                                // Can't select chosen entrants nor cancelled entrants
                                                if (winners_set.contains(user.get("did")) && !Objects.equals(user.get("status"), "chosen") && !Objects.equals(user.get("status"), "cancel")) {
                                                    user.put("status", "chosen");
                                                }
                                            }
                                            // Update the waiting list
                                            documentReference.update("waitinglist", waitList);
                                        }
                                    }
                                }
                            });
                });
            }
        });
    }

    /**
     * Allows the organizer to cancel a user's invitation after the lottery has been conducted.
     * @author Sehej Brar
     * @param eventID event's unique id
     * @param userID user's unique id
     */
    public void organizerCancel(String eventID, String userID) {
        getChosen(eventID, chosen -> {
            if (!chosen.isEmpty()) {
                for (String did: chosen) {
                    changeStatus(eventID, did, "cancel");
                }
            }
        });
    }

    /**
     * Changes the user's waiting list status
     * @author Sehej Brar
     * @param eventID event id
     * @param userID user's unique id
     * @param newStatus the status to change the user to
     */
    public void changeStatus(String eventID, String userID, String newStatus) {
        ArrayList<String> allStatus = new ArrayList<>(Arrays.asList("chosen", "waiting", "cancel", "chosen"));

        if (userID == null || !allStatus.contains(newStatus.toLowerCase())) {
            throw new IllegalArgumentException("The argument provided is not valid");
        }

        DocumentReference documentReference = eventsRef.document(eventID);

        documentReference.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            ArrayList<Map<String, String>> waitList = (ArrayList<Map<String, String>>) doc.get("waitinglist");

                            if (waitList != null) {
                                for (Map<String, String> user : waitList) {
                                    if (Objects.equals(user.get("did"), userID)) {
                                        user.put("status", newStatus);
                                    }
                                }

                                documentReference.update("waitinglist", waitList);

                            }
                        }
                    }
                });
    }

    /**
     * @author Sehej Brar
     * Add user to their waitlist
     * @param entrantId entrant id
     * @param eventId event id
     */
    public void addToUserWL(String entrantId, String eventId, UserInfo user) {
        db.collection("users").document(entrantId)
                .update("events", FieldValue.arrayUnion(eventId))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> userEvents = user.getEvents();
                        userEvents.add(eventId);
                        user.setEvents(userEvents);
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
    public void removeFromUserWL(String entrantId, String eventId, UserInfo user) {
        db.collection("users").document(entrantId)
                .update("events", FieldValue.arrayRemove(eventId))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> userEvents = user.getEvents();
                        userEvents.remove(eventId);
                        user.setEvents(userEvents);
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
                    ArrayList<Map<String, String>> all_waitingList = (ArrayList<Map<String, String>>) doc.get("waitinglist");

                    if (all_waitingList != null) {
                        for (Map<String, String> user: all_waitingList) {
                            all.add(user.get("did"));
                        }
                    }
                }
                Log.d("Checkpoint", "lah" + all);
                allCB.allDid(all);
            } else {
                Log.e("Error", "Error");
            }
        });
    }

    /**
     * Interface for all waitlist entrants
     * @author Sehej Brar
     */
    public interface WaitingCB {
        void waitDid(ArrayList<String> wait);
    }

    /**
     * Gets all those on waitlist
     * @author Sehej Brar
     * @param eventId event id
     * @param waitingCB a callback for entrants on waitlist that are waiting
     */
    public void getWait(String eventId, WaitingCB waitingCB) {
        ArrayList<String> wait = new ArrayList<>();
        DocumentReference waitingListDoc = db.collection("events")
                .document(eventId);

        waitingListDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    ArrayList<Map<String, String>> all_waitingList = (ArrayList<Map<String, String>>) doc.get("waitinglist");

                    if (all_waitingList != null) {
                        for (Map<String, String> user: all_waitingList) {
                            if (Objects.equals(user.get("status"), "waiting")) {
                                wait.add(user.get("status"));
                            }
                        }
                    }
                }
                waitingCB.waitDid(wait);
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
                    ArrayList<Map<String, String>> all_waitingList = (ArrayList<Map<String, String>>) doc.get("waitinglist");

                    if (all_waitingList != null) {
                        for (Map<String, String> user: all_waitingList) {
                            if (Objects.equals(user.get("status"), "cancel")) {
                                cancel.add(user.get("status"));
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
                    ArrayList<Map<String, String>> all_waitingList = (ArrayList<Map<String, String>>) doc.get("waitinglist");

                    if (all_waitingList != null) {
                        for (Map<String, String> user: all_waitingList) {
                            if (Objects.equals(user.get("status"), "chosen")) {
                                chosen.add(user.get("status"));
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
