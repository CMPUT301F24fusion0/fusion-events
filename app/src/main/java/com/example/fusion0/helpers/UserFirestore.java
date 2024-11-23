package com.example.fusion0.helpers;

import com.example.fusion0.models.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class serves as the connection to the Firebase. It includes common CRUD operations.
 */
public class UserFirestore {
    /**
     * @author Sehej Brar
     * This interface is needed due to the asynchronous nature of Firestore.
     *      Adapted from <a href="https://stackoverflow.com/questions/48499310/how-to-return-a-documentsnapshot-as-a-result-of-a-method">...</a>
     */
    public interface Callback {
        void onSuccess(UserInfo user);
        void onFailure(String error);
    }

    private static final CollectionReference usersRef;

    static {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
    }

    /**
     * @author Sehej Brar
     * This method takes in a UserInfo object and adds it to the database.
     * @param userInfo contains the UserInfo object that is to be added to the database
     */
    public void addUser(UserInfo userInfo, Runnable onSuccess) {
        HashMap<String, Object> user = userInfo.user();
        String dID = userInfo.getDeviceID();

        usersRef.document(dID).set(user)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Success");
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                })
                .addOnFailureListener(error -> System.out.println("Fail" + error.getMessage())
                );
    }

    /**
     * @author Sehej Brar
     * This method finds the user and will return the UserInfo object through the callback
     * @param dID is the primary key for each user and each user has a unique device ID
     * @param callback is the interface needed due to the asynchronous nature of Firebase
     */
    public static void findUser(String dID, Callback callback) {
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
     * @author Sehej Brar
     * @throws IllegalArgumentException
     * Uses the primary key to find the user then allows for the editing of any field
     * @param user represents the user to be changed
     * @param field is the field that is to be changed (i.e. first name, last name, etc.)
     * @param newFields is a list of new attributes for the user
     */
    public static void editUser(UserInfo user, String field, ArrayList<String> newFields) {
        String newField;
        field = field.toLowerCase();

        ArrayList<String> fields = new ArrayList<>(
                Arrays.asList("first name", "last name", "phone number", "email", "did", "notifications", "events"));

        if (!fields.contains(field.toLowerCase())) {
            throw new IllegalArgumentException("The field you've tried to change is not valid");
        } else if (!(field.equalsIgnoreCase("notifications")) && !((field.equalsIgnoreCase("events")))) {
            newField = newFields.get(0);
            usersRef.document(user.getDeviceID()).update(field, newField)
                    .addOnSuccessListener(ref -> System.out.println("Update Successful"))
                    .addOnFailureListener(e -> System.out.println("Failure" + e.getMessage()));
        } else if (field.equalsIgnoreCase("notifications")) {
            System.out.println(user.getDeviceID());
            usersRef.document(user.getDeviceID()).update(field, newFields)
                    .addOnSuccessListener(ref -> System.out.println("Array value added successfully"))
                    .addOnFailureListener(e -> System.out.println("Failure" + e.getMessage()));
        } else {
            editUserEvents(user);
        }
    }

    /**
     * @author Sehej Brar
     * Finds the user and then deletes them.
     * @param dID is the primary key used to find the user
     */
    public void deleteUser(String dID) {
        usersRef.document(dID).delete()
                .addOnSuccessListener(documentReference -> System.out.println("Successfully deleted"))
                .addOnFailureListener(e -> System.out.println("Failure" + e.getMessage()));
    }

    /**
     * @author Simon Haile
     * Finds the user and then edit the events list.
     * @param user is the user instance
     */
    public static void editUserEvents(UserInfo user) {
        String userID = user.getDeviceID();
        usersRef.document(userID).set(user, SetOptions.merge())
                .addOnSuccessListener(documentReference -> System.out.println("User data updated successfully."))
                .addOnFailureListener(error -> System.err.println("Error updating user data: " + error.getMessage()));
    }
}