package com.example.fusion0;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;


public class Firebase {
    // Adapted from https://stackoverflow.com/questions/48499310/how-to-return-a-documentsnapshot-as-a-result-of-a-method
    public interface Callback {
        void onCallback(UserInfo user);
    }
     private FirebaseFirestore db;
    private CollectionReference usersRef;

    public Firebase() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
    }

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

    public void getUser(String email, Callback callback) {
        usersRef.document(email).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserInfo user = documentSnapshot.toObject(UserInfo.class);
                        callback.onCallback(user);
                    } else {
                        System.out.println("not found");
                    }
                })
                .addOnFailureListener(error -> {
                    System.out.println("Failure" + error.getMessage());
                });
    }
}
