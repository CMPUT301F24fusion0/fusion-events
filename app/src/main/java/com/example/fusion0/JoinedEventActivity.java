package com.example.fusion0;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.zxing.WriterException;

import java.util.ArrayList;

public class JoinedEventActivity extends AppCompatActivity {

    private ImageButton backButton;
    private ScrollView scrollContainer;
    private TextView eventName;
    private TextView description;
    private Spinner spinnerFacilities;
    private ImageView uploadedImageView;
    private TextView startDateText;
    private TextView endDateText;
    private TextView capacity;
    private ImageView qrImage;
    private Button unjoinButton;

    private EventInfo event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joined_events_view);

        backButton = findViewById(R.id.backButton);
        scrollContainer = findViewById(R.id.scroll_container);
        eventName = findViewById(R.id.EventName);
        description = findViewById(R.id.description);
        spinnerFacilities = findViewById(R.id.spinner_facilities);
        uploadedImageView = findViewById(R.id.uploaded_image_view);
        startDateText = findViewById(R.id.start_date_text);
        endDateText = findViewById(R.id.end_date_text);
        capacity = findViewById(R.id.capacity);
        qrImage = findViewById(R.id.qrImage);
        unjoinButton = findViewById(R.id.unjoin_button);

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(JoinedEventActivity.this, FavouriteActivity.class);
            startActivity(intent);
        });

        Intent intentReceived = getIntent();
        String eventID = intentReceived.getStringExtra("eventID");

        if (eventID != null) {
            EventFirebase.findEvent(eventID, new EventFirebase.EventCallback() {
                @Override
                public void onSuccess(EventInfo eventInfo) throws WriterException {
                    if (eventInfo == null) {
                        Toast.makeText(JoinedEventActivity.this, "Event Unavailable.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        event = eventInfo;

                        eventName.setText(event.getEventName());
                        description.setText(event.getDescription());
                        capacity.setText(String.valueOf(event.getCapacity()));
                        startDateText.setText(String.valueOf(event.getStartDate()));
                        endDateText.setText(String.valueOf(event.getEndTime()));


                        String eventPoster = event.getEventPoster();
                        if (eventPoster != null && !eventPoster.isEmpty()) {
                            Glide.with(JoinedEventActivity.this)
                                    .load(eventPoster)
                                    .into(uploadedImageView);
                            uploadedImageView.setVisibility(View.VISIBLE);
                        }

                        String qrcode = event.getQrCode();
                        if (qrcode != null && !qrcode.isEmpty()) {
                            Bitmap qrBitmap = event.generateQRCodeImage(500, 500, qrcode);
                            qrImage.setImageBitmap(qrBitmap);
                        }

                    }
                }

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

    }
}
