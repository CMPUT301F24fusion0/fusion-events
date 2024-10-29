package com.example.fusion0;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * ProfileManagement class manages user profile data retrieval from Firestore.
 */
public class ProfileManagement {

    // Firebase Authentication and Firestore instances
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    /**
     * Constructor initializes Firebase Authentication and Firestore instances.
     */
    public ProfileManagement() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Callback interface for handling user data retrieval.
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
     */
    public void getUserData(final UserDataCallback callback) {
        String userId = auth.getCurrentUser().getUid();

        // Access Firestore and retrieve the user's document
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {

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
}