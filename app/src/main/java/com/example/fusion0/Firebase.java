package com.example.fusion0;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class serves as the connection to the Firebase. It includes common CRUD operations.
 */
public class Firebase {
    /**
     * This interface is needed due to the asynchronous nature of Firestore.
     *      Adapted from <a href="https://stackoverflow.com/questions/48499310/how-to-return-a-documentsnapshot-as-a-result-of-a-method">...</a>
     */
    public interface Callback {
        void onSuccess(UserInfo user);
        void onFailure(String error);
    }

    private final CollectionReference usersRef;

    /**
     * Initializes the database as well as the users collection.
     */
    public Firebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
    }

    /**
     * This method takes in a UserInfo object and adds it to the database.
     * @param userInfo contains the UserInfo object that is to be added to the database
     */
    public void addUser(UserInfo userInfo) {
        HashMap<String, Object> user = userInfo.user();
        String dID = userInfo.getDeviceID();
        usersRef.document(dID).set(user)
                .addOnSuccessListener(documentReference -> System.out.println("Success"))
                .addOnFailureListener(error -> System.out.println("Failure" + error.getMessage()));
    }

    /**
     * This method finds the user and will return the UserInfo object through the callback
     * @param dID is the primary key for each user and each user has a unique device ID
     * @param callback is the interface needed due to the asynchronous nature of Firebase
     */
    public void findUser(String dID, Callback callback) {
        usersRef.document(dID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserInfo user = documentSnapshot.toObject(UserInfo.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(error -> {
                    System.out.println("Failure" + error.getMessage());
                    callback.onFailure(error.getMessage());
                });
    }

    /**
     * Uses the primary key to find the user then allows for the editing of any field
     * @param user represents the user to be changed
     * @param field is the field that is to be changed (i.e. first name, last name, etc.)
     * @param newField is the new attribute for the user
     */
    public void editUser(UserInfo user, String field, String newField) {
        ArrayList<String> fields = new ArrayList<>(
                Arrays.asList("first name", "last name", "phone number", "email", "did"));

        if (!fields.contains(field.toLowerCase())) {
            throw new IllegalArgumentException("The field you've tried to change is not valid");
        }

        usersRef.document(user.getDeviceID()).update(field, newField)
                .addOnSuccessListener(ref -> {
                    System.out.println("Update Successful");
                    user.editMode(true);
                    switch (field.toLowerCase()) {
                        case "first name":
                            user.setFirstName(newField);
                            break;
                        case "last name":
                            user.setLastName(newField);
                            break;
                        case "email":
                            user.setEmail(newField);
                            break;
                        case "phone number":
                            user.setPhoneNumber(newField);
                            break;
                        case "did":
                            user.setDeviceID(newField);
                            break;
                    }

                })
                .addOnFailureListener(e -> System.out.println("Failure" + e.getMessage()));
    }

    /**
     * Finds the user and then deletes them.
     * @param dID is the primary key used to find the user
     */
    public void deleteUser(String dID) {
        usersRef.document(dID).delete()
                .addOnSuccessListener(documentReference -> System.out.println("Successfully deleted"))
                .addOnFailureListener(e -> System.out.println("Failure" + e.getMessage()));
    }

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

}
