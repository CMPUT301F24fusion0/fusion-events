package com.example.fusion0;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

public class AppNotifications {
    // https://stackoverflow.com/questions/44305206/ask-permission-for-push-notification
    // https://learn.microsoft.com/en-gb/answers/questions/1181354/how-can-i-request-permission-for-push-notification
    final static int requestCode = 100;

    public static void createChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel lottery = new NotificationChannel("lottery", "lotteryAccept", NotificationManager.IMPORTANCE_HIGH);
            lottery.setDescription("Lottery acceptation or decline.");
            context.getSystemService(NotificationManager.class).createNotificationChannel(lottery);
        }
    }

    public static void permission(Activity activity, String dID) {
        // if higher than android 13
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // no permission
            if ((ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, requestCode);
            } else {
                getNotification(dID, activity);
            }
        } else {
            getNotification(dID, activity);
        }
    }

    /**
     * To send a notification to a certain user
     * @param dID device id
     * @param title title of notification
     * @param body body of notification
     */
    public static void sendNotification(String dID, String title, String body) {
        UserFirestore firebase = new UserFirestore();
        firebase.findUser(dID, new UserFirestore.Callback() {
            @Override
            public void onSuccess(UserInfo user) {
                if (user != null) {
                    user.editMode(true);
                    user.addNotifications(title, body);
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e("Error", error);
            }
        });
    }

    private static void sendAllNotifications(String dID, Context context, ArrayList<String> notifications) {
        // Here we'd call upon firebase to give us back the array of notifications and then send them to the user
        // get notification then delete them from Firebase
        createChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "lottery");

        for (int i = 0; i < notifications.size(); i += 2) {
            System.out.println(notifications.get(i));
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.setContentTitle(notifications.get(i))
                    .setSmallIcon(R.drawable.ic_grey_home)
                    .setContentText(notifications.get(i+1))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(i, builder.build());
        }
    }

    /**
     * Used during logging and the user can view their notifications
     * @param dID device ID
     * @param context context of activity or fragment
     */
    public static void getNotification(String dID, Context context) {
        new UserFirestore().findUser(dID, new UserFirestore.Callback() {
            @Override
            public void onSuccess(UserInfo user) {
                sendAllNotifications(dID, context, user.getNotifications());
                user.setNotifications(new ArrayList<String>()); // reset
            }

            @Override
            public void onFailure(String error) {
                Log.e("Error", error);
            }
        });
    }

    /*
    Use Case:
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getNotifications(String dID, Context context)
            } else {
                throw error or tell them to try again
            }
        }
     */
}
