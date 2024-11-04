package com.example.fusion0;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.zxing.WriterException;

import java.net.URI;

public class ViewEventActivity extends AppCompatActivity {

    private TextView eventNameTextView;
    private ImageView eventPosterImageView;
    private ImageView qrImageView;
    private EventInfo event;
    private ImageButton profileButton;
    private ImageButton addButton;
    private ImageButton scannerButton;
    private ImageButton favouriteButton;
    private ImageButton homeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_view); // Ensure this matches your XML layout file name

        eventNameTextView = findViewById(R.id.eventName);
        eventPosterImageView = findViewById(R.id.eventPosterImage);
        qrImageView = findViewById(R.id.qrImage);

        Intent intentReceived = getIntent();
        String eventName = intentReceived.getStringExtra("eventName");
        String eventID = intentReceived.getStringExtra("eventID");

        eventNameTextView.setText(eventName);

        EventFirebase.findEvent(eventID, new EventFirebase.EventCallback() {
            @Override
            public void onSuccess(EventInfo eventInfo) throws WriterException {
                if (eventInfo == null) {
                    Toast.makeText(ViewEventActivity.this, "Event Unavailable.", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity if no event found
                } else {
                    event = eventInfo;
                    Uri eventPosterURI = Uri.parse(event.getEventPoster());
                    if (eventPosterURI != null) {
                        Glide.with(ViewEventActivity.this)
                                .load(eventPosterURI)
                                .into(eventPosterImageView);
                    }

                    String qrcode = event.getQrCode();
                    Bitmap imageBitmap = event.generateQRCodeImage(500,500, qrcode);
                    qrImageView.setImageBitmap(imageBitmap);

                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error fetching event: " + error);
                Toast.makeText(ViewEventActivity.this, "Failed to load event details.", Toast.LENGTH_SHORT).show();
            }
        });

        favouriteButton = findViewById(R.id.toolbar_favourite);
        favouriteButton.setOnClickListener(view -> {
            Intent intent = new Intent(ViewEventActivity.this, FavouriteActivity.class);
            startActivity(intent);
        });

        scannerButton = findViewById(R.id.toolbar_qrscanner);
        scannerButton.setOnClickListener(view -> {
            Intent intent = new Intent(ViewEventActivity.this, QRActivity.class);
            startActivity(intent);
        });

        addButton = findViewById(R.id.toolbar_add);

        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(ViewEventActivity.this, EventActivity.class);
            startActivity(intent);
        });

        // Initialize profile button to navigate to ProfileActivity
        profileButton = findViewById(R.id.toolbar_person);

        profileButton.setOnClickListener(view -> {
            Intent intent = new Intent(ViewEventActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

}
