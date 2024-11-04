package com.example.fusion0;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/**
 * @author Nimi Akinroye
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
    @SuppressLint("HardwareIds")
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
     * Callback interface for checking if an image was successfully deleted
     */
    public interface ImageDeleteCallback {
        void onSuccess();

        void onFailure(Exception e);
    }

    /**
     * Uploads an image to Firebase Storage for the current user.
     *
     * @param imageUri the URI of the image to be uploaded
     * @param callback the callback for handling success or failure of the upload
     */
    public void uploadImage(Uri imageUri, final ImageUploadCallback callback) {
        StorageReference userImageRef = storageReference.child("profile_images/" + deviceId + ".jpg");

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
     * Deletes an image from firebase.
     *
     * @param callback the callback for checking if the image was successfully deleted.
     */
    public void deleteImage(final ImageDeleteCallback callback) {
        StorageReference userImageRef = storageReference.child("profile_images/" + deviceId + ".jpg");

        userImageRef.delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
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


    /**
     * Creates a text drawable that can be used to deterministically generate a users profile picture
     *
     * @param context
     * @param letter
     * @param backgroundColor
     * @param textColor
     * @param width
     * @param height
     * @return
     */
    public static Drawable createTextDrawable(Context context, String letter, int backgroundColor, int textColor, int width, int height) {
        // Create a bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Create a Canvas to draw on the Bitmap
        Canvas canvas = new Canvas(bitmap);

        // Set up Paint for the background
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL);

        // Draw a background
        canvas.drawRect(0, 0, width, height, backgroundPaint);

        // Set up Paint for the text
        Paint textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(Math.min(width, height) / 2f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        // Calculate the position to the center the text
        float xPos = width / 2f;
        float yPos = (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);

        // Draw the letter
        canvas.drawText(letter, xPos, yPos, textPaint);

        return new BitmapDrawable(context.getResources(), bitmap);
    }

}

