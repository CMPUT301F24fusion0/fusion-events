package com.example.fusion0.helpers;

import com.example.fusion0.models.NotificationItem;
import com.example.fusion0.models.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class NotificationHelper {

    public interface Callback {
        void onNotificationsUpdated(List<NotificationItem> updatedNotificationList);
        void onError(String error);
    }

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
