package com.example.fusion0;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;


import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/**
 * ManageImageProfile class handles image upload, retrieval, and existence checks
 * in Firebase Storage for user profile images.
 */

public class ManageImageProfile {

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String deviceId;

    /**
     * Constructor initializes Firebase Authentication and Storage instances.
     */
    public ManageImageProfile(Context context) {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Callback interface for handling image upload success or failure.
     */
    public interface ImageUploadCallback {
        void onSuccess();

        void onFailure(Exception e);
    }

    /**
     * Callback interface for handling successful image retrieval or failure.
     */
    public interface ImageRetrievedCallback {
        void onImageRetrieved(Uri uri);

        void onFailure(Exception e);
    }

    /**
     * Callback interface for checking if an image exists.
     */
    public interface ImageCheckCallback {
        void onImageExists();

        void onImageDoesNotExist();
    }

    /**
     * Uploads an image to Firebase Storage for the current user.
     *
     * @param imageUri the URI of the image to be uploaded
     * @param callback the callback for handling success or failure of the upload
     */
    public void uploadImage(Uri imageUri, final ImageUploadCallback callback) {
        StorageReference userImageRef = storageReference.child("profile_images/" + deviceId + ".jpg");
        Log.d("Firebase", "Got here! ");
        userImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Checks if an image exists in Firebase Storage for the current user.
     *
     * @param callback the callback to handle whether the image exists or not
     */
    public void checkImageExists(final ImageCheckCallback callback) {
        StorageReference userImageRef = storageReference.child("profile_images/" + deviceId + ".jpg");

        userImageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> callback.onImageExists())
                .addOnFailureListener(e -> callback.onImageDoesNotExist());
    }

    /**
     * Retrieves the image URL from Firebase Storage for the current user.
     *
     * @param callback the callback for handling image retrieval success or failure
     */
    public void getImage(final ImageRetrievedCallback callback) {
        StorageReference userImageRef = storageReference.child("profile_images/" + deviceId + ".jpg");

        userImageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> callback.onImageRetrieved(uri))
                .addOnFailureListener(callback::onFailure);

    }
}

