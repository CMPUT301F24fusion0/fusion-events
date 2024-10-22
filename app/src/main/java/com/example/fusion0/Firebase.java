package com.example.fusion0;

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
        String fullName = userInfo.getFirstName() + userInfo.getLastName();
        usersRef.document(fullName).set(user);
    }
}
