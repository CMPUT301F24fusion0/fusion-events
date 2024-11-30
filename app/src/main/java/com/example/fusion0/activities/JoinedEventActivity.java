package com.example.fusion0.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.UserInfo;
import com.google.zxing.WriterException;
import com.example.fusion0.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

/**
 * @author Simon Haile
 * This activity allows users to view the selected joined event and unjoin the event
 */
public class JoinedEventActivity extends AppCompatActivity {

    public UserFirestore userFirestore;
    private ImageButton backButton, facilityButton;
    private TextView eventName, facility, description, registrationDate;
    private ImageView uploadedImageView;
    private TextView startDateText;
    private TextView endDateText;
    private TextView capacity, lotteryCapacity;
    private ImageView qrImage;
    private Button unjoinButton;

    private UserInfo user;
    private EventInfo event;
    public EventFirebase eventFirebase = new EventFirebase();

    /**
     * @author Simon Haile
     * Called when the activity is created. This method sets up the user interface,
     * retrieves the event ID from the intent, fetches the event details from Firestore,
     * and binds the event data to the UI elements.
     * @param savedInstanceState The saved instance state, or null if there is no previous state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joined_events_view);

        backButton = findViewById(R.id.backButton);
        eventName = findViewById(R.id.EventName);
        description = findViewById(R.id.description);
        facility = findViewById(R.id.facilityName);
        uploadedImageView = findViewById(R.id.uploaded_image_view);
        startDateText = findViewById(R.id.start_date_text);
        endDateText = findViewById(R.id.end_date_text);
        registrationDate = findViewById(R.id.registration_date_text);
        capacity = findViewById(R.id.capacity);
        lotteryCapacity = findViewById(R.id.lotteryCapacityTextView);
        qrImage = findViewById(R.id.qrImage);
        unjoinButton = findViewById(R.id.unjoin_button);
        facilityButton = findViewById(R.id.facility_view_button);

        backButton.setOnClickListener(view -> {
            onBackPressed();
        });


        Intent intentReceived = getIntent();
        String eventID = intentReceived.getStringExtra("eventID");
        String deviceID = intentReceived.getStringExtra("deviceID");

        facilityButton.setOnClickListener(view -> {
            Intent intent = new Intent(JoinedEventActivity.this, ViewFacilityActivity.class);
            intent.putExtra("facilityID", event.getFacilityID());
            intent.putExtra("deviceID", deviceID);
            startActivity(intent);
        });

        new UserFirestore().findUser(deviceID, new UserFirestore.Callback() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                user = userInfo;
            }

            @Override
            public void onFailure(String error) {
                Log.e("JoinedEventActivity", "Error fetching user: " + error);
            }
        });


        if (eventID != null) {
            eventFirebase.findEvent(eventID, new EventFirebase.EventCallback() {
                /**
                 * Called when the event details are successfully retrieved from Firestore.
                 *
                 * @param eventInfo The object containing event details
                 * @throws WriterException If an error occurs while generating the QR code image
                 */
                @Override
                public void onSuccess(EventInfo eventInfo) throws WriterException {
                    if (eventInfo == null) {
                        Toast.makeText(JoinedEventActivity.this, "Event Unavailable.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        event = eventInfo;

                        eventName.setText(event.getEventName());
                        description.setText(event.getDescription());
                        facility.setText(event.getFacilityName());
                        capacity.setText(String.valueOf(event.getCapacity()));
                        lotteryCapacity.setText(String.valueOf(event.getLotteryCapacity()));

                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

                        startDateText.setText(dateFormat.format(event.getStartDate()));
                        endDateText.setText(dateFormat.format(event.getEndDate()));
                        registrationDate.setText(dateFormat.format(event.getRegistrationDate()));


                        String eventPoster = event.getEventPoster();
                        if (eventPoster != null && !eventPoster.isEmpty()) {
                            Glide.with(JoinedEventActivity.this)
                                    .load(eventPoster)
                                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                    .into(new SimpleTarget<Drawable>() {
                                        @Override
                                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                            // Get the original image dimensions
                                            int originalWidth = resource.getIntrinsicWidth();
                                            int originalHeight = resource.getIntrinsicHeight();


                                            int newWidth = (int) (originalWidth / 1.5);
                                            int newHeight = (int) (originalHeight / 1.5);


                                            Glide.with(JoinedEventActivity.this)
                                                    .load(eventPoster)
                                                    .override(newWidth, newHeight)
                                                    .into(uploadedImageView);

                                            uploadedImageView.setVisibility(View.VISIBLE);
                                        }
                                    });
                        }

                        String qrcode = event.getQrCode();
                        if (qrcode != null && !qrcode.isEmpty()) {
                            Bitmap qrBitmap = event.generateQRCodeImage(500, 500, qrcode);
                            qrImage.setImageBitmap(qrBitmap);
                        }

                    }
                }
                /**
                 * Called when an error occurs while fetching event details.
                 *
                 * @param error The error message
                 */
                @Override
                public void onFailure(String error) {
                    Log.e("JoinedEventActivity", "Error fetching event: " + error);
                    Toast.makeText(JoinedEventActivity.this, "Failed to load event details.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(JoinedEventActivity.this, "Invalid Event ID.", Toast.LENGTH_SHORT).show();
            finish();
        }

        unjoinButton.setOnClickListener(view ->{
            ArrayList<Map<String, String>> newWaitingList = event.removeUserFromWaitingList(deviceID, event.getWaitinglist());
            event.setWaitinglist(newWaitingList);

            eventFirebase.editEvent(event);


            ArrayList<String> userEvents =  user.getEvents();
            ArrayList<String> newEventsList = user.removeEventFromEventList(event.getEventID(), userEvents);
            user.setEvents(newEventsList);
            new UserFirestore().editUserEvents(user);

            eventFirebase.editEvent(event);
            finish();
        });

    }

}
