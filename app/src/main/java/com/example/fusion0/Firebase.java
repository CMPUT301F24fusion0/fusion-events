package com.example.fusion0;

import static java.security.AccessController.getContext;

import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;


public class Firebase {
    private FirebaseFirestore db;
    private CollectionReference usersRef;

    public Firebase() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
    }

    public void addUser(UserInfo userInfo) {
        HashMap<String, Object> user = userInfo.user();
        String fullName = userInfo.getFirstName() + " " + userInfo.getLastName();
        usersRef.add(user)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Success");
                })
                .addOnFailureListener(documentReference -> {
                    System.out.println("Failure" + documentReference.getMessage());
                });
    }

    public void getUser() {

    }
}
