package com.example.fusion0;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.zxing.WriterException;

import java.util.ArrayList;

public class ViewEventActivity extends AppCompatActivity {
    private TextView eventNameTextView;
    private TextView eventDescriptionTextView;
    private Spinner eventFacility;
    private TextView eventStartDateTextView;
    private TextView eventEndDateTextView;
    private TextView eventCapacityTextView;
    private EditText eventNameEditText;
    private EditText eventDescriptionEditText;
    private EditText eventCapacityEditText;
    private Spinner eventFacilitySpinner;
    private ImageView eventPosterImageView;
    private ImageView qrImageView;
    private ListView entrantsListView;
    private ListView chosenEntrantsListView;
    private ListView cancelledEntrantsListView;
    private Button startDateButton;
    private Button endDateButton;
    private Button editButton;
    private Button deleteButton;
    private ImageButton backButton;
    private EventInfo event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_view);

        backButton = findViewById(R.id.backButton);
        eventNameTextView = findViewById(R.id.EventName);
        eventDescriptionTextView = findViewById(R.id.Description);
        eventFacility= findViewById(R.id.spinner_facilities);
        eventStartDateTextView = findViewById(R.id.start_date_text);
        eventEndDateTextView = findViewById(R.id.end_date_text);
        eventCapacityTextView = findViewById(R.id.Capacity);
        eventNameEditText = findViewById(R.id.editEventName);
        eventDescriptionEditText = findViewById(R.id.Description);
        eventFacilitySpinner = findViewById(R.id.spinner_facilities);
        eventPosterImageView = findViewById(R.id.uploaded_image_view);
        qrImageView = findViewById(R.id.qrImage);
        entrantsListView = findViewById(R.id.entrantsListView);
        chosenEntrantsListView = findViewById(R.id.chosenEntrantsListView);
        cancelledEntrantsListView = findViewById(R.id.cancelledEntrantsListView);
        startDateButton = findViewById(R.id.start_date_button);
        endDateButton = findViewById(R.id.end_date_button);
        editButton = findViewById(R.id.edit_button);
        deleteButton = findViewById(R.id.delete_button);

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(ViewEventActivity.this, FavouriteActivity.class);
            startActivity(intent);
        });

        Intent intentReceived = getIntent();
        String eventID = intentReceived.getStringExtra("eventID");

        if (eventID != null) {
            EventFirebase.findEvent(eventID, new EventFirebase.EventCallback() {
                @Override
                public void onSuccess(EventInfo eventInfo) throws WriterException {
                    if (eventInfo == null) {
                        Toast.makeText(ViewEventActivity.this, "Event Unavailable.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        event = eventInfo;

                        eventNameTextView.setText(event.getEventName());
                        eventDescriptionTextView.setText(event.getDescription());
                        eventCapacityTextView.setText(String.valueOf(event.getCapacity()));

                        // Load event poster image
                        String eventPosterUrl = event.getEventPoster();
                        if (eventPosterUrl != null && !eventPosterUrl.isEmpty()) {
                            Uri eventPosterURI = Uri.parse(eventPosterUrl);
                            Glide.with(ViewEventActivity.this)
                                    .load(eventPosterURI)
                                    .into(eventPosterImageView);
                        }

                        // Generate and set QR code
                        String qrcode = event.getQrCode();
                        if (qrcode != null && !qrcode.isEmpty()) {
                            Bitmap qrBitmap = event.generateQRCodeImage(500, 500, qrcode);
                            qrImageView.setImageBitmap(qrBitmap);
                        }

                        eventNameEditText.setText(event.getEventName());
                        eventDescriptionEditText.setText(event.getDescription());

                        String facilityName = event.getFacilityName();
                        //ArrayList<String> facilityNames = owner.getFacilityNames();
                       // int facilityIndex = facilityNames.indexOf(facilityName);
                        //if (facilityIndex != -1) {
                        //    eventFacilitySpinner.setSelection(facilityIndex);
                        //}
                       // startDateButton.setText(event.getStartDate());
                       // endDateButton.setText(event.getEndDate());
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e("ViewEventActivity", "Error fetching event: " + error);
                    Toast.makeText(ViewEventActivity.this, "Failed to load event details.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ViewEventActivity.this, "Invalid Event ID.", Toast.LENGTH_SHORT).show();
            finish();
        }

        editButton.setOnClickListener(v -> {
        });

        deleteButton.setOnClickListener(v -> {
        });
    }
}