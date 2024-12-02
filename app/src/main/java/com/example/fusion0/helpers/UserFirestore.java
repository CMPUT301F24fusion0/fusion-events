package com.example.fusion0.helpers;

import android.util.Log;

import com.example.fusion0.models.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class serves as the connection to the Firebase. It includes common CRUD operations.
 * @author Sehej Brar
 */
public class UserFirestore {

    private FirebaseFirestore db;

    /**
     * @author Sehej Brar
     * This interface is needed due to the asynchronous nature of Firestore.
     *      Adapted from <a href="https://stackoverflow.com/questions/48499310/how-to-return-a-documentsnapshot-as-a-result-of-a-method">...</a>
     */
    public interface Callback {
        void onSuccess(UserInfo user);
        void onFailure(String error);
    }

    private final CollectionReference usersRef;

    /**
     * Normal constructor
     * @author Sehej Brar
     */
    public UserFirestore() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
    }

    /**
     * This allows for the database to be mocked
     * @author Sehej Brar
     * @param db the mocked database
     */
    public UserFirestore(FirebaseFirestore db) {
        this.db = db;
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
    public void findUser(String dID, Callback callback) {
        if (dID == null || dID.isEmpty()) {
            callback.onFailure("Device ID is null or empty");
            return;
        }

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
    public void editUser(UserInfo user, String field, ArrayList<String> newFields) {
        String newField;
        field = field.toLowerCase();
        Log.d("Field to change", field);
        ArrayList<String> fields = new ArrayList<>(
                Arrays.asList("first name", "last name", "phone number", "email", "did", "notifications", "events", "homepagenotifications", "numaccepted", "numdeclined"));

        if (!fields.contains(field)) {
            throw new IllegalArgumentException("The field you've tried to change is not valid");
        } else if (!(field.equals("notifications")) && !(field.equals("events")) && !(field.equals("homepagenotifications"))) {
            newField = newFields.get(0);
            usersRef.document(user.getDeviceID()).update(field, newField)
                    .addOnSuccessListener(ref -> System.out.println("Update Successful"))
                    .addOnFailureListener(e -> System.out.println("Failure" + e.getMessage()));
        } else if (field.equals("notifications") || (field.equals("homepagenotifications"))) {
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
    public void editUserEvents(UserInfo user) {
        String userID = user.getDeviceID();
        usersRef.document(userID).set(user, SetOptions.merge())
                .addOnSuccessListener(documentReference -> System.out.println("User data updated successfully."))
                .addOnFailureListener(error -> System.err.println("Error updating user data: " + error.getMessage()));
    }

    /**
     * Callback interface for retrieving a list of users.
     * @author Ali Abouei
     */
    public interface UserListCallback {
        void onSuccess(ArrayList<UserInfo> users);
        void onFailure(String error);
    }

    /**
     * Callback interface for delete operations (user and profile picture).
     * @author Ali Abouei
     */
    public interface DeleteCallback {
        void onSuccess(); // Called when both user and profile picture are successfully deleted.
        void onFailure(Exception e); // Called when deletion fails.
    }

    /**
     * Retrieves all users from Firebase Firestore.
     *
     * @author Ali Abouei
     * @param callback The callback to handle the result of the retrieval.
     */
    public void getAllUsers(UserListCallback callback) {
        usersRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<UserInfo> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            if (document.exists()) {
                                UserInfo user = document.toObject(UserInfo.class);
                                users.add(user);
                            } else {
                                System.err.println("Document does not exist: " + document.getId());
                            }
                        } catch (Exception e) {
                            System.err.println("Error deserializing document: " + e.getMessage());
                        }
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(error -> {
                    System.err.println("Error fetching users: " + error.getMessage());
                    callback.onFailure(error.getMessage());
                });
    }

    /**
     * Deletes a user from Firestore and their profile picture from Firebase Storage.
     * @author Ali Abouei
     * @param dID      The device ID of the user to delete.
     * @param callback The callback to handle the result of the deletion.
     */
    public void deleteUserAndImage(String dID, DeleteCallback callback) {
        usersRef.document(dID).delete()
                .addOnSuccessListener(aVoid -> {
                    // Delete the user's profile image from Firebase Storage
                    deleteUserProfileImage(dID, new DeleteCallback() {
                        @Override
                        public void onSuccess() {
                            if (callback != null) callback.onSuccess();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (callback != null) callback.onFailure(e);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("UserFirestore", "Error deleting user: " + e.getMessage());
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Deletes a user's profile image from Firebase Storage.
     * @author Ali Abouei
     * @param dID      The device ID of the user whose profile image is to be deleted.
     * @param callback The callback to handle the result of the deletion.
     *
     */
    public void deleteUserProfileImage(String dID, DeleteCallback callback) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference userImageRef = storageReference.child("profile_images/" + dID + ".jpg");

        userImageRef.delete()
                .addOnSuccessListener(unused -> {
                    // Image deleted successfully
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    // Check if the error indicates that the object does not exist
                    if (e instanceof StorageException) {
                        int errorCode = ((StorageException) e).getErrorCode();
                        if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                            // Image does not exist, consider deletion successful
                            if (callback != null) callback.onSuccess();
                        } else {
                            Log.e("UserFirestore", "Error deleting profile image: " + e.getMessage());
                            if (callback != null) callback.onFailure(e);
                        }
                    } else {
                        Log.e("UserFirestore", "Error deleting profile image: " + e.getMessage());
                        if (callback != null) callback.onFailure(e);
                    }
                });
    }


}