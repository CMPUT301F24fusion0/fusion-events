package com.example.fusion0.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.fusion0.BuildConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Nimi Akinroye
 * ManageImageProfile class handles image upload, retrieval, and existence checks
 * in Firebase Storage for user profile images.
 */

public class ManageImageProfile {

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String deviceId;

    private static final String OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/images/generations";
    private static final String TAG = "OPENAI";

    private static String stringOutput = "";
    private static Bitmap bitmapOutputImage;

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


    public static Drawable createTextDrawable(Context context, String initials, int backgroundColor, int textColor, int width, int height) {
        try {
            String prompt = "Generate a visually appealing image that connotes the initials: " + initials + ". There should be no letters in there";
            generateImageFromOpenAI(context, prompt);

            return downloadImageAsDrawable(context, stringOutput, width, height);
        } catch (Exception e) {
            return generateFallBackDrawable(context, initials, backgroundColor, textColor, width, height);
        }
    }

    private static void generateImageFromOpenAI(Context context, String prompt) throws Exception {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("prompt", prompt);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                OPENAI_API_URL,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            stringOutput = response
                                    .getJSONArray("data")
                                    .getJSONObject(0)
                                    .getString("url");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        throw new RuntimeException(error);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> mapHeader = new HashMap<>();
                mapHeader.put("Authorization", "Bearer " + OPENAI_API_KEY);
                mapHeader.put("Content-Type", "application/json");

                return mapHeader;
            }
        };

        int intTimeOutPeriod = 60000;
        RetryPolicy retryPolicy = new DefaultRetryPolicy(
                intTimeOutPeriod,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonObjectRequest.setRetryPolicy(retryPolicy);

        Volley.newRequestQueue(context).add(jsonObjectRequest);
    }

    private static Drawable downloadImageAsDrawable(Context context, String imageUrl, int width, int height) throws Exception {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(stringOutput);

                    bitmapOutputImage = BitmapFactory.decodeStream(url.openStream());

                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        thread.start();

        while (thread.isAlive()) {
            Log.d(TAG, "Thread is in process");
        }

        Bitmap bitmapFinalImage = Bitmap.createScaledBitmap(
                bitmapOutputImage,
                width,
                height,
                true
        );

        return new BitmapDrawable(context.getResources(), bitmapFinalImage);
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
    public static Drawable generateFallBackDrawable(Context context, String letter, int backgroundColor, int textColor, int width, int height) {
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

