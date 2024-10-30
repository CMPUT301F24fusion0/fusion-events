package com.example.fusion0;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

public class FirebaseTest {
    private CollectionReference usersRef;
    private UserFirestore firebase;

    public void initDb() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
    }

    @Before
    public void firebase() {
        firebase = new UserFirestore();
    }

    public UserInfo newUser() {
        return new UserInfo("Mike", "Ross", "mross@psl.com", "4613217890", "1234");
    }

    /**
     * Ensure the collection is successfully completed.
     */
    @Test
    public void collectionTest() {
        initDb();
        usersRef
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        throw new IllegalArgumentException("This collection doesn't exist");
                    }
                });
    }

    /**
     * This checks if the user is successfully added.
     */
    @Test
    public void addTest() {
        firebase.addUser(newUser());
        initDb();
        usersRef
                .document("1234")
                .get()
                .addOnSuccessListener(task -> {
                    if (!task.exists()) {
                        fail();
                    }
                });
    }

    /**
     * Tests to see if the user we just added can be found.
     */
    @Test
    public void findTest() {
        firebase.findUser("1234", new UserFirestore.Callback() {
            @Override
            public void onSuccess(UserInfo user) {
                if (!(Objects.equals(user.getFirstName(), "Mike")) || !(Objects.equals(user.getDeviceID(), "1234"))) {
                    fail();
                }
            }

            @Override
            public void onFailure(String error) {
                fail();
            }
        });
    }






}
