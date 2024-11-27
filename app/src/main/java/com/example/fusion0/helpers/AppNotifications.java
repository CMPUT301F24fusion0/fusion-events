package com.example.fusion0.helpers;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.fusion0.R;
import com.example.fusion0.activities.MainActivity;
import com.example.fusion0.models.UserInfo;

import java.util.ArrayList;

/**
 * This class provides the methods needed to send/receive notifications
 * Sources:
 *      <a href="https://stackoverflow.com/questions/44305206/ask-permission-for-push-notification">...</a>
 *      <a href="https://learn.microsoft.com/en-gb/answers/questions/1181354/how-can-i-request-permission-for-push-notification">...</a>
 */
public class AppNotifications {

    private final static int REQUEST_CODE = 100;

    /**
     * @author Sehej Brar
     * Creates the notification channels required to send the notification but this is only needed for
     * android versions above 13
     * @param context app's context
     */
    public static void createChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel lottery = new NotificationChannel("lottery", "lotteryAccept", NotificationManager.IMPORTANCE_HIGH);
            lottery.setDescription("Lottery acceptation or decline.");
            context.getSystemService(NotificationManager.class).createNotificationChannel(lottery);
        }
    }

    /**
     * Changes permission if need be
     * @author Sehej Brar
     * Adapted from: <a href="https://stackoverflow.com/questions/58047177/how-to-turn-off-app-notification-from-inside-the-app">...</a>
     * @param context context
     * @param perm true for notifications can be sent, false otherwise
     */
    public static void setNotificationPermission(Context context, boolean perm) {
        SharedPreferences sp = context.getSharedPreferences("notifications", Context.MODE_PRIVATE);
        sp.edit().putBoolean("permission", perm).apply();
    }

    /**
     * Returns true if permission has been given or if nothing explicit has been given.
     * @author Sehej Brar
     * @param context context
     * @return a boolean
     */
    public static boolean checkNotificationPermission(Context context) {
        SharedPreferences sp = context.getSharedPreferences("notifications", Context.MODE_PRIVATE);
        return sp.getBoolean("permission", true);
    }

    /**
     * @author Sehej Brar
     * Ask for permission to send notifications for Android versions 13
     * @param activity the activity to ask for permission in
     * @param dID device ID
     */
    public static void permission(Activity activity, String dID) {
        // if higher than android 13
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // no permission then ask
            if ((ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE);
            } else {
                getNotification(dID, activity);
            }
        } else {
            getNotification(dID, activity);
        }
    }

    /**
     * @author Sehej Brar
     * Adds the notification to that user to be sent out in sendAllNotifications.
     * @param dID device id
     * @param title title of notification
     * @param body body of notification
     */
    public static void sendNotification(String dID, String title, String body, String flag, String eventId) {
        new UserFirestore().findUser(dID, new UserFirestore.Callback() {
            @Override
            public void onSuccess(UserInfo user) {
                if (user != null) {
                    user.editMode(true);
                    user.addNotifications(title, body, flag, eventId);
                    user.editMode(false);
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("Error", error);
            }
        });
    }

    /**
     * @author Sehej Brar
     * The user has logged in so they are ready to receive the notifications. This is the method
     * that actually sends out the notifications
     * @param context context for the notifications to be sent to
     * @param notifications an array of notifications
     */
    private static void sendAllNotifications(Context context, ArrayList<String> notifications) {
        // Here we'd call upon firebase to give us back the array of notifications and then send them to the user
        // get notification then delete them from Firebase
        createChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "lottery");
        if (notifications != null) {
            for (int i = 0; i < notifications.size(); i += 4) {
                Intent intent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                builder.setContentTitle(notifications.get(i))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(notifications.get(i + 1))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(i, builder.build());
            }
        }
    }

    /**
     * @author Sehej Brar
     * Used during logging in. Send all notifications if the user exists.
     * @param dID device ID
     * @param context context of activity or fragment
     */
    public static void getNotification(String dID, Context context) {
        new UserFirestore().findUser(dID, new UserFirestore.Callback() {
            @Override
            public void onSuccess(UserInfo user) {
                if (AppNotifications.checkNotificationPermission(context)) {
                    sendAllNotifications(context, user.getNotifications());
                }
                user.editMode(true);
                user.setNotifications(new ArrayList<>());
                System.out.println("Notifications removed successfully");
                user.editMode(false);
            }

            @Override
            public void onFailure(String error) {
                Log.e("Error", error);
            }
        });
    }
}