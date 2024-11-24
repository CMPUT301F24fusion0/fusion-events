package com.example.fusion0.helpers;

import com.example.fusion0.models.NotificationItem;
import com.example.fusion0.models.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class NotificationHelper {

    /**
     * Callback for firebase results
     * @author Nimi Akinroye
     */
    public interface Callback {
        void onNotificationsUpdated(List<NotificationItem> updatedNotificationList);
        void onError(String error);
    }

    /**
     * Gets the notifications in the format they were stored: title, body, flag
     * @author Nimi Akinroye
     * @param deviceId device id
     * @param callback callback as firebase is asynchronous
     */
    public static void updateNotifications(String deviceId, Callback callback) {
        UserFirestore.findUser(deviceId, new UserFirestore.Callback() {
            @Override
            public void onSuccess(UserInfo user) {
                ArrayList<String> notifications = user.getNotifications();
                List<NotificationItem> notificationList = new ArrayList<>();

                if (notifications != null && notifications.size() % 3 == 0) {
                    for (int i = 0; i < notifications.size(); i += 3) {
                        String title = notifications.get(i);
                        String body = notifications.get(i + 1);
                        String flag = notifications.get(i + 2);

                        notificationList.add(new NotificationItem(title, body, flag));
                    }
                    callback.onNotificationsUpdated(notificationList);
                } else {
                    callback.onError("No notifications found.");
                }
            }

            @Override
            public void onFailure(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Delete notifications once user swipes it away
     * @author Nimi Akinroye
     * @param deviceId device id
     * @param notificationItem notification item to delete
     * @param callback callback for updated notification list
     */
    public static void deleteNotification(String deviceId, NotificationItem notificationItem, Callback callback) {
        UserFirestore.findUser(deviceId, new UserFirestore.Callback() {
            @Override
            public void onSuccess(UserInfo user) {
                user.editMode(true);

                ArrayList<String> notifications = user.getNotifications();
                int indexToRemove = -1;

                for (int i = 0; i < notifications.size(); i += 3) {
                    String title = notifications.get(i);
                    String body = notifications.get(i + 1);
                    if (title.equals(notificationItem.getTitle()) && body.equals(notificationItem.getBody())) {
                        indexToRemove = i;
                        break;
                    }
                }

                if (indexToRemove != -1) {
                    notifications.remove(indexToRemove);
                    notifications.remove(indexToRemove);
                    notifications.remove(indexToRemove);
                    user.setNotifications(notifications);

                    List<NotificationItem> updatedNotificationList = new ArrayList<>();
                    for (int i = 0; i < notifications.size(); i += 3) {
                        String title = notifications.get(i);
                        String body = notifications.get(i + 1);
                        String flag = notifications.get(i + 2);
                        updatedNotificationList.add(new NotificationItem(title, body, flag));
                    }
                    callback.onNotificationsUpdated(updatedNotificationList);
                } else {
                    callback.onError("Notification not found.");
                }
            }

            @Override
            public void onFailure(String error) {
                callback.onError(error);
            }

        });
    }
}
