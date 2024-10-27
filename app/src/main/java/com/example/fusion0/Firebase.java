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
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
    }

    /**
     * This method takes in a UserInfo object and adds it to the database.
     * @param userInfo contains the UserInfo object that is to be added to the database
     * TODO: Use DeviceID
     */
    public void addUser(UserInfo userInfo) {
        HashMap<String, Object> user = userInfo.user();
        String email = userInfo.getEmail();
        usersRef.document(email).set(user)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Success");
                })
                .addOnFailureListener(error -> {
                    System.out.println("Failure" + error.getMessage());
                });
    }

    /**
     * This method finds the user and will return the UserInfo object through the callback
     * @param email is the primary key for each user and each user has a unique email
     * @param callback is the interface needed due to the asynchronous nature of Firebase
     * TODO: Use DeviceID
     */
    public void findUser(String email, Callback callback) {
        usersRef.document(email).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserInfo user = documentSnapshot.toObject(UserInfo.class);
                        callback.onSuccess(user);
                    } else {
                        System.out.println("not found");
                        callback.onFailure("not found");
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
     * TODO: UserID should be primary key
     */
    public void editUser(UserInfo user, String field, String newField) {
        ArrayList<String> fields = new ArrayList<String>(
                Arrays.asList("first name", "last name", "phone number", "email"));

        if (!fields.contains(field.toLowerCase())) {
            throw new IllegalArgumentException("The field you've tried to change is not valid");
        }

        usersRef.document(user.getEmail()).update(field, newField)
                .addOnSuccessListener(ref -> {
                    System.out.println("Update Successful");
                    user.editMode(true);
                    switch (field.toLowerCase()) {
                        case "first name":
                            user.setFirstName(newField);
                        case "last name":
                            user.setLastName(newField);
                        case "email":
                            user.setEmail(newField);
                        case "phone number":
                            user.setPhoneNumber(newField);
                    }

                })
                .addOnFailureListener(e -> {
                    System.out.println("Failure" + e.getMessage());
                });
    }

    /**
     * Finds the user and then deletes them.
     * @param email is the primary key used to find the user
     * TODO: DeviceID should be primary key
     */
    public void deleteUser(String email) {
        usersRef.document(email).delete()
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Successfully deleted");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failure" + e.getMessage());
                });
    }
}
