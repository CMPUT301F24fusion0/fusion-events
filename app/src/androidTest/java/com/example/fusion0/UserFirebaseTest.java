package com.example.fusion0;

import static org.junit.Assert.fail;

import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class UserFirebaseTest {
    private CollectionReference usersRef;
    private UserFirestore firebase;

    @Before
    public void firebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        firebase = new UserFirestore();
    }

    public UserInfo newUser() {
        return new UserInfo(new ArrayList<String>(Arrays.asList("title", "body")), "Mike", "Ross", "mross@psl.com", "4613217890", "1234", new ArrayList<String>());
    }

    /**
     * Ensure the collection is successfully created.
     */
    @Test
    public void collectionTest() {
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
        firebase.addUser(newUser(), () -> {});
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

    @Test
    public void editTest() {
        firebase.editUser(newUser(), "first name", new ArrayList<String>(Collections.singletonList("Harvey")));
        usersRef
                .document("1234")
                .get()
                .addOnSuccessListener(task -> {
                    if (task.exists()) {
                        if (!(Objects.equals(task.getString("first name"), "Harvey"))) {
                            fail();
                        }
                    } else {
                        fail();
                    }
                });
    }

    @Test
    public void deleteTest() {
        firebase.deleteUser("1234");
        usersRef
                .document("1234")
                .get()
                .addOnSuccessListener(task -> {
                    if (task.exists()) {
                        fail();
                    }
                });

    }
}
