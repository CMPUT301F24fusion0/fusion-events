package com.example.fusion0;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class Firebase {
    // Adapted from https://stackoverflow.com/questions/48499310/how-to-return-a-documentsnapshot-as-a-result-of-a-method
    public interface Callback {
        void onCallback(UserInfo user);
    }

    private final CollectionReference usersRef;

    public Firebase() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
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

    public void findUser(String email, Callback callback) {
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

    public void editUser(String email, String field, String newField) {
        ArrayList<String> fields = new ArrayList<String>(
                Arrays.asList("first name", "last name", "phone number", "email"));

        if (!fields.contains(field)) {
            throw new IllegalArgumentException("The field you've tried to change is not valid");
        }

        usersRef.document(email).update(field, newField)
                .addOnSuccessListener(ref -> {
                    System.out.println("Update Successful");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failure" + e.getMessage());
                });
    }

    public void deleteUser(String email, String field, String newField) {
        usersRef.document(email).delete()
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Successfully deleted");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failure" + e.getMessage());
                });
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
