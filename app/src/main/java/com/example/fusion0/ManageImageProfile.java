package com.example.fusion0;

import android.net.Uri;

import com.google.android.gms.auth.api.signin.internal.Storage;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * ManageImageProfile class handles image upload, retrieval, and existence checks
 * in Firebase Storage for user profile images.
 */

public class ManageImageProfile {

    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    /**
     * Constructor initializes Firebase Authentication and Storage instances.
     */
    public ManageImageProfile() {
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
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
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            StorageReference userImageRef = storageReference.child("profile_images/" + userId + ".jpg");

            userImageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> callback.onSuccess())
                    .addOnFailureListener(callback::onFailure);
        }
    }

    /**
     * Checks if an image exists in Firebase Storage for the current user.
     *
     * @param callback the callback to handle whether the image exists or not
     */
    public void checkImageExists(final ImageCheckCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            StorageReference userImageRef = storageReference.child("profile_images/" + userId + ".jpg");

            userImageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> callback.onImageExists())
                    .addOnFailureListener(e -> callback.onImageDoesNotExist());
        }
    }

    /**
     * Retrieves the image URL from Firebase Storage for the current user.
     *
     * @param callback the callback for handling image retrieval success or failure
     */
    public void getImage(final ImageRetrievedCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            StorageReference userImageRef = storageReference.child("profile_images/" + userId + ".jpg");

            userImageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> callback.onImageRetrieved(uri))
                    .addOnFailureListener(callback::onFailure);

        }
    }
}

