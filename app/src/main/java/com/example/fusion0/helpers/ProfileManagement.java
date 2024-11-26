package com.example.fusion0.helpers;

import com.example.fusion0.models.UserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * @author Nimi Akinroye
 * ProfileManagement class manages user profile data retrieval from Firestore.
 */
public class ProfileManagement {

    // Firebase Authentication and Firestore instances
    private FirebaseFirestore db;

    /**
     * Constructor initializes Firebase Authentication and Firestore instances.
     *
     * @author Nimi Akinroye
     */
    public ProfileManagement() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Callback interface for handling user data retrieval.
     *
     * @author Nimi Akinroye
     */
    public interface UserDataCallback {
        void onUserDataReceived(UserInfo user);
        void onDataNotAvailable();
        void onError(Exception e);
    }

    /**
     * Retrieves user data from Firestore for the current authenticated user.
     *
     * @param callback the callback to handle the result of user data retrieval
     * @author Nimi Akinroye
     */
    public void getUserData(final String deviceId, final UserDataCallback callback) {
        // Access Firestore and retrieve the user's document
        db.collection("users").document(deviceId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    UserInfo user = document.toObject(UserInfo.class);
                    callback.onUserDataReceived(user);
                } else {
                    callback.onDataNotAvailable();
                }
            } else {
                callback.onError(task.getException());
            }
        });
    }


    /**
     * Callback interface for handling device ID admin status.
     * @author Malshaan Kodithuwakku
     *
     */
    public interface IsDeviceIDAdminCallback {
        void onDeviceIDAdmin(boolean isDeviceIDAdmin);
        void onError(Exception e);
    }

    /**
     * Determines if deviceID is in admins collection
     *
     * @param deviceId the device ID to check
     * @return true if the device ID is an admin, false otherwise
     * @author Malshaan Kodithuwakku
     */
    public void isDeviceIDAdmin(String deviceId, IsDeviceIDAdminCallback callback) {
        // Access Firestore and check if the device ID is in the admins collection
        db.collection("admins").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Device ID exists in the admins collection
                        callback.onDeviceIDAdmin(true);
                    } else {
                        // Device ID does not exist in the admins collection
                        callback.onDeviceIDAdmin(false);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle Firestore error
                    callback.onError(e);
                });
    }
}

