package com.example.fusion0;

import static junit.framework.TestCase.assertEquals;

import com.example.fusion0.helpers.AppNotifications;
import com.example.fusion0.models.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Tests if the notifications are updated
 * @author Sehej Brar
 */
public class AppNotificationsTest {
    private static UserInfo user;
    private static DocumentReference users;

    /**
     * Initialize the required variables
     * @author Sehej Brar
     */
    @BeforeClass
    public static void init() {
        user = new UserInfo(new ArrayList<>(), "first", "last", "email", "123", "did", new ArrayList<>());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");
        users = usersRef.document(user.getDeviceID());
        users.set(user.user());
    }

    /**
     * Checks to see the notifications are successfully added
     * @author Sehej Brar
     */
    @Test
    public void addNotification() {
        AppNotifications.sendNotification("did", "title", "body", "0", "event id");
        users.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserInfo user2 = documentSnapshot.toObject(UserInfo.class);
                assert user2 != null;
                assertEquals(user.getNotifications().get(0), user2.getNotifications().get(0));
            }
        });
    }
}
