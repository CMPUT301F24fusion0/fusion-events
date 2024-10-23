package com.example.fusion0;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileManagement {

    // Get user data if a user exists
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public ProfileManagement() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Interface to handle callback
    public interface UserDataCallback {
        void onUserDataReceived(UserInfo user);

        void onDataNotAvailable();

        void onError(Exception e);
    }

    // Method to fetch user data
    public void getUserData(final UserDataCallback callback) {
        String userId = auth.getCurrentUser().getUid();
        System.out.println("Fetching data for user ID: " + userId); // Debugging line

        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            System.out.println("I got here");
            // Complete listener
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                System.out.println("Task successful"); // Debugging line

                if (document.exists()) {
                    System.out.println("Document exists: " + document.getData()); // Debugging line
                    UserInfo user = document.toObject(UserInfo.class);
                    callback.onUserDataReceived(user);
                } else {
                    System.out.println("Document does not exist for user ID: " + userId); // Debugging line
                    callback.onDataNotAvailable();
                }
            } else {
                System.out.println("Error fetching data: " + task.getException()); // Debugging line
                callback.onError(task.getException());
            }
        });
    }

}
