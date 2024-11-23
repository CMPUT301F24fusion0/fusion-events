package com.example.fusion0.helpers;

import android.util.Log;

import com.example.fusion0.models.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

/**
 * @author Sehej Brar
 * The {@code Waitlist} class provides methods to manage and interact with the
 * waiting list for events. It allows CRUD operations.
 * </p>
 */

public class Waitlist implements Serializable {
    private transient FirebaseFirestore db;
    private transient CollectionReference eventsRef;
    private transient UserFirestore userFirestore;


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
     * Samples a specified number of attendees from the waiting list for a specific event.
     *
     * @param eventId The unique identifier of the event.
     * @param numToSelect The number of attendees to be randomly selected from the waiting list.
     */
    public void conductLottery(String eventId, int numToSelect) {
        // Fetch event details to get capacity and current acceptedCount
        eventsRef.document(eventId).get().addOnSuccessListener(eventDoc -> {
            if (eventDoc.exists()) {
                int lotteryCapacity;
                int acceptedCount;

                // Check if capacity is stored as a number or a string, then convert
                Object capacityField = eventDoc.get("lotteryCapacity");
                Object acceptedCountField = eventDoc.get("acceptedCount");

                // Handle capacity conversion
                if (capacityField instanceof String) {
                    try {
                        lotteryCapacity = Integer.parseInt((String) capacityField);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Capacity is not a valid number.");
                        return;
                    }
                } else {
                    System.out.println("Error: Capacity is missing or invalid.");
                    return;
                }

                if (acceptedCountField instanceof String) {
                    try {
                        acceptedCount = Integer.parseInt((String) acceptedCountField);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: AcceptedCount is not a valid number.");
                        return;
                    }
                } else {
                    System.out.println("Error: AcceptedCount is missing or invalid.");
                    return;
                }

                // Calculate the number of entrants we actually need
                int spotsRemaining = lotteryCapacity - acceptedCount;
                int finalNumToSelect = Math.min(numToSelect, spotsRemaining);

                getWait(eventId, wait -> {
                    Collections.shuffle(wait);

                    // Hashset allows for faster lookup
                    HashSet<String> winners_set = new HashSet<>(new ArrayList<>(wait.subList(0, finalNumToSelect)));

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
                                                if (winners_set.contains(user.get("did"))) {
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
     * @author Sehej Bra
     * @param eventID event's unique id
     * @param userID user's unique id
     */
    public void organizerCancel(String eventID, String userID) {
        getChosen(eventID, chosen -> {
            DocumentReference documentReference = eventsRef.document(eventID);
            if (!chosen.isEmpty()) {
                documentReference.get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                if (doc.exists()) {
                                    ArrayList<Map<String, String>> waitList = (ArrayList<Map<String, String>>) doc.get("waitinglist");

                                    if (waitList != null) {
                                        for (Map<String, String> user : waitList) {
                                            if (Objects.equals(user.get("did"), userID)) {
                                                user.put("status", "cancel");
                                            }
                                        }

                                        documentReference.update("waitinglist", waitList);

                                    }
                                }
                            }
                        });
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
