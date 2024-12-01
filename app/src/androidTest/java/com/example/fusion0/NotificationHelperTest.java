package com.example.fusion0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.example.fusion0.helpers.AppNotifications;
import com.example.fusion0.helpers.NotificationHelper;
import com.example.fusion0.models.NotificationItem;
import com.example.fusion0.models.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Notification helper that allows for the notificaiton to be on the front page
 * @author Sehej Brar
 */
public class NotificationHelperTest {
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
        AppNotifications.sendNotification("did", "title", "body", "0", "event id");
    }

    /**
     * Check to see if notificationHelper works by returning the correct notificationItem
     * @author Sehej Brar
     */
    @Test
    public void testUpdate() {
        NotificationHelper.updateNotifications("did", new NotificationHelper.Callback() {
            @Override
            public void onNotificationsUpdated(List<NotificationItem> updatedNotificationList) {
                assertEquals(updatedNotificationList.get(0).getTitle(), "title");
                assertEquals(updatedNotificationList.get(0).getBody(), "body");
            }

            @Override
            public void onError(String error) {
                fail(error);
            }
        });
    }

}
