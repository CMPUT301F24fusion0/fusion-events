package com.example.fusion0.helpers;

import android.util.Log;

import com.example.fusion0.models.OrganizerInfo;
import com.example.fusion0.models.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Sehej Brar
 * The {@code Waitlist} class provides methods to manage and interact with the
 * waiting list for events. It allows CRUD operations.
 * </p>
 */

public class Waitlist implements Serializable {
    private transient final FirebaseFirestore db;
    private transient CollectionReference eventsRef, usersRef;


    /**
     * Constructor for Waitlist
     * @author Sehej Brar
     */
    public Waitlist() {
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        usersRef = db.collection("users");
    }


    public interface LotteryCallback {
        void onComplete();
    }
    /**
     * @author Sehej Brar
     * Samples a specified number of entrants from the waiting list for a specific event. Also
     * can be used to re-sample entrants.
     *
     * @param eventId The unique identifier of the event.
     * @param numToSelect The number of attendees to be randomly selected from the waiting list.
     */
    public void conductLottery(String eventId, int numToSelect, LotteryCallback callback) {
        // Fetch event details to get capacity and current acceptedCount
        eventsRef.document(eventId).get().addOnSuccessListener(eventDoc -> {
            if (eventDoc.exists()) {
                int acceptedCount;
                int capacity;
                // Check if capacity is stored as a number or a string, then convert
                Object capacityField = eventDoc.get("capacity");
                Object acceptedCountField = eventDoc.get("acceptedCount");
                Object waitField  = eventDoc.get("waitinglist");

                if (capacityField instanceof String && acceptedCountField instanceof String) {
                    try {
                        capacity = Integer.parseInt((String) capacityField);
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

                if (waitField instanceof ArrayList<?>) {
                    ArrayList<Map<String, String>> waitList = (ArrayList<Map<String, String>>) waitField;

                    // Check if the wait list is empty
                    if (waitList.isEmpty()) {
                        Log.w("Lottery", "Wait list is empty. No entrants to select.");
                        return;
                    }

                    Collections.shuffle(waitList);

                    // Select the winners
                    List<String> winners_set = new ArrayList<>();
                    for (int i = 0; i < finalNumToSelect; i++) {
                        Map<String, String> winner = waitList.get(i);
                        winners_set.add(winner.get("did"));
                    }

                    DocumentReference documentReference = eventsRef.document(eventId);
                    documentReference.get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot doc = task.getResult();
                                    if (doc.exists()) {
                                        if (waitList != null) {
                                            Log.d("Shuffled", "winners set"+winners_set);
                                            // If the winners are in the waiting list then their new status is chosen
                                            for (Map<String, String> user : waitList) {
                                                // Can't select chosen entrants nor cancelled entrants
                                                if (winners_set.contains(user.get("did")) && !Objects.equals(user.get("status"), "chosen") && !Objects.equals(user.get("status"), "cancel")) {
                                                    user.put("status", "chosen");
                                                    Log.d("Shuffled", "Updated status for user: " + user.get("did"));
                                                }
                                            }
                                            // Update the waiting list
                                            documentReference.update("waitinglist", waitList).addOnSuccessListener(aVoid -> {
                                                chosenNotification(eventId, "Winner!",
                                                        "Congratulations, you have won the lottery! Please accept the " +
                                                                "invitation to confirm your spot.", "1");
                                                loseNotification(eventId, "Lottery Results", "Unfortunately, " +
                                                        "you have lost the lottery. You may still receive an invite if someone declines their invitation.", "0");
                                            });
                                            callback.onComplete();

                                        }
                                    }
                                }
                    });
                }
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
        changeStatus(eventID, userID, "cancel");
    }

    /**
     * Changes the user's waiting list status
     * @author Sehej Brar
     * @param eventID event id
     * @param userID user's unique id
     * @param newStatus the status to change the user to
     */
    public void changeStatus(String eventID, String userID, String newStatus) {
        ArrayList<String> allStatus = new ArrayList<>(Arrays.asList("accept", "waiting", "cancel", "chosen"));

        if (userID == null || eventID == null|| !allStatus.contains(newStatus.toLowerCase())) {
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
     * Add user to their fragment_waitlist
     * @param entrantId entrant id
     * @param eventId event id
     */
    public void addToUserWL(String entrantId, String eventId, UserInfo user) {
        usersRef.document(entrantId)
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
     * Remove event from user's fragment_waitlist
     * @author Sehej Brar
     * @param entrantId entrant id
     * @param eventId event id
     */
    public void removeFromUserWL(String entrantId, String eventId, UserInfo user) {
        usersRef.document(entrantId)
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
     * Interface for all fragment_waitlist entrants
     * @author Sehej Brar
     */
    public interface AllCB {
        void allDid(ArrayList<String> all);
    }

    /**
     * Gets all those on fragment_waitlist
     * @author Sehej Brar
     * @param eventId event id
     * @param allCB a callback for all entrants on fragment_waitlist
     */
    public void getAll(String eventId, AllCB allCB) {
        ArrayList<String> all = new ArrayList<>();
        DocumentReference waitingListDoc = eventsRef.document(eventId);

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
                allCB.allDid(all);
            } else {
                Log.e("Error", "Error");
            }
        });
    }

    /**
     * Interface for all fragment_waitlist entrants
     * @author Sehej Brar
     */
    public interface WaitingCB {
        void waitDid(ArrayList<String> wait);
    }

    /**
     * Gets all those on waitlist
     * @author Sehej Brar
     * @param eventId event id
     * @param waitingCB a callback for entrants on fragment_waitlist that are waiting
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
                                wait.add(user.get("did"));
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

    public interface AcceptCB {
        void acceptDid(ArrayList<String> accept);
    }

    public void getAccepted(String eventId, AcceptCB acceptCB) {
        ArrayList<String> accept = new ArrayList<>();
        DocumentReference waitingListDoc = db.collection("events")
                .document(eventId);

        waitingListDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    ArrayList<Map<String, String>> all_waitingList = (ArrayList<Map<String, String>>) doc.get("waitinglist");

                    if (all_waitingList != null) {
                        for (Map<String, String> user: all_waitingList) {
                            if (Objects.equals(user.get("status"), "accept")) {
                                accept.add(user.get("did"));
                            }
                        }
                    }
                }
                acceptCB.acceptDid(accept);
            } else {
                Log.e("Error", "Error");
            }
        });
    }

    /**
     * Callback to get accepted people
     * @author Sehej Brar
     */
    public interface AcceptCB {
        void acceptDid(ArrayList<String> accept);
    }

    /**
     * Get accepted people
     * @author Sehej Brar
     * @param eventId event id
     * @param acceptCB accept callback people
     */
    public void getAccepted(String eventId, AcceptCB acceptCB) {
        ArrayList<String> accept = new ArrayList<>();
        DocumentReference waitingListDoc = db.collection("events")
                .document(eventId);

        waitingListDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    ArrayList<Map<String, String>> all_waitingList = (ArrayList<Map<String, String>>) doc.get("waitinglist");

                    if (all_waitingList != null) {
                        for (Map<String, String> user: all_waitingList) {
                            if (Objects.equals(user.get("status"), "accept")) {
                                accept.add(user.get("did"));
                            }
                        }
                    }
                }
                acceptCB.acceptDid(accept);
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
        DocumentReference waitingListDoc = eventsRef.document(eventId);

        waitingListDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    ArrayList<Map<String, String>> all_waitingList = (ArrayList<Map<String, String>>) doc.get("waitinglist");

                    if (all_waitingList != null) {
                        for (Map<String, String> user: all_waitingList) {
                            if (Objects.equals(user.get("status"), "cancel")) {
                                cancel.add(user.get("did"));
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
        DocumentReference waitingListDoc = eventsRef.document(eventId);

        waitingListDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    ArrayList<Map<String, String>> all_waitingList = (ArrayList<Map<String, String>>) doc.get("waitinglist");
                    Log.d("all_waitingList", "s " + all_waitingList);
                    Log.d("Task to get chosen", "is there");
                    if (all_waitingList != null) {
                        Log.d("list to get chosen", "is there");
                        for (Map<String, String> user: all_waitingList) {
                            if (Objects.equals(user.get("status"), "chosen")) {
                                chosen.add(user.get("did"));
                                Log.d("chosen: ", user.get("did"));
                            }
                        }
                    }
                }
                Log.d("Chosen: ", "s: " + chosen);
                chosenCB.ChosenDid(chosen);
            } else {
                Log.e("Error", "Error");
            }
        });
    }

    /**
     * Sends a notification to everyone on the fragment_waitlist
     * @author Sehej Brar
     * @param eventId event id
     * @param title title of notification
     * @param message message of notification
     */
    public void allNotification(String eventId, String title, String message, String flag) {
        getAll(eventId, all -> {
            for (String dID: all) {
                AppNotifications.sendNotification(dID, title, message, flag, eventId);
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
                AppNotifications.sendNotification(dID, title, message, flag, eventId);
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
        getWait(eventId, wait -> {
            for (String dID: wait) {
                AppNotifications.sendNotification(dID, title, message, flag, eventId);
            }
        });
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
                AppNotifications.sendNotification(dID, title, message, flag, eventId);
            }
        });
    }
}