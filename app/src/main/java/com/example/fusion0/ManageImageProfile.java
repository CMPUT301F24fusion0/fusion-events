package com.example.fusion0;

import android.net.Uri;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ManageImageProfile {

    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    public ManageImageProfile() {
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    public interface ImageUploadCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface ImageRetrievedCallback {
        void onImageRetrieved(Uri uri);
        void onFailure(Exception e);
    }

    public interface ImageCheckCallback {
        void onImageExists();
        void onImageDoesNotExist();
    }

    // Upload image to Firebase Storage
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
