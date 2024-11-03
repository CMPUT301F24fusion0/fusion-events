package com.example.fusion0;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Notifications extends FirebaseMessagingService {
    // https://www.youtube.com/watch?v=YjNZO90yVsE
    // https://github.com/bimalkaf/Android_Chat_Application/blob/main/app/src/main/java/com/example/easychat/ChatActivity.java
    // https://github.com/firebase/quickstart-android/blob/master/messaging/app/src/main/java/com/google/firebase/quickstart/fcm/java/MyFirebaseMessagingService.java
    // https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages/send?_gl=1*1myc3s4*_up*MQ..*_ga*MjE0NTc4NzM1My4xNzMwNjA0MDg2*_ga_CW55HF8NVT*MTczMDYwNDA4Ni4xLjAuMTczMDYwNDA4Ni4wLjAuMA..
    // https://firebase.google.com/codelabs/use-the-fcm-http-v1-api-with-oauth-2-access-tokens#1
    // https://square.github.io/okhttp/

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    private static String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(new FileInputStream("app/fusion-firebase-admin.json"));
        googleCredentials.refresh();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    public void createChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("all_channel", "allNotifications", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("All notifications.");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private JSONObject dataJSON(String title, String body, String fcm) throws JSONException {
        JSONObject fcmJSON = new JSONObject();
        fcmJSON.put("fcm", fcm);

        JSONObject notificationJSON = new JSONObject();
        notificationJSON.put("title", title);
        notificationJSON.put("body", body);

        JSONObject dataJSON = new JSONObject();
        dataJSON.put("to", fcmJSON);
        dataJSON.put("notification", notificationJSON);

        return dataJSON;
    }

    public void notification(String title, String body, String fcm, Context context) throws JSONException, IOException {
        String apiUrl = "https://fcm.googleapis.com/v1/projects/fusion-c01b0/messages:send";

        JSONObject dataJSON = dataJSON(title, body, fcm);

        final MediaType JSON = MediaType.get("application/json");

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(RequestBody.create(dataJSON.toString(), JSON))
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("Content-Type", "application/json")
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (e.getMessage() != null) {
                    Log.e("Failure", e.getMessage());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("Success", response.body().string());

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // bring target activity to front
                    startActivity(intent);

                } else {
                    Log.e("Failure", "Error Code: " + response.code());
                }
            }
        });
    }
}
