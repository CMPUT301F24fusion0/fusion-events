package com.example.fusion0;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * LoginManagement class manages user authentication state using device ID.
 */
public class LoginManagement {

    private FirebaseFirestore db;
    private String deviceID;

    /**
     * Constructor initializes Firebase Firestore and sets the device ID.
     *
     * @param context The application context, used to retrieve the device ID.
     */
    public LoginManagement(Context context) {
        db = FirebaseFirestore.getInstance();
        // Retrieve the unique device ID
        deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Checks if the user is currently registered in the Firestore database using the device ID.
     *
     * @param callback Callback to handle the boolean result
     */
    public void isUserLoggedIn(LoginStateCallback callback) {
        db.collection("users").document(deviceID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // User exists
                            callback.onResult(true);
                        } else {
                            // User does not exist
                            callback.onResult(false);
                        }
                    } else {
                        Log.e("LoginManagement", "Error checking user login state: " + task.getException().getMessage());
                        callback.onResult(false); // Return false in case of an error
                    }
                });
    }

    /**
     * Retrieves the device ID.
     *
     * @return the device ID
     */
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * Callback interface for returning the login state as a boolean.
     */
    public interface LoginStateCallback {
        void onResult(boolean isLoggedIn);
    }
}