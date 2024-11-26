package com.example.fusion0.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fusion0.R;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.EventInfo;

import java.util.ArrayList;
import java.util.List;

public class BrowseEventsActivity extends AppCompatActivity {

    private ListView eventListView;
    private EventArrayAdapter eventArrayAdapter;
    private Button goBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events);

        eventListView = findViewById(R.id.eventListView);
        goBackButton = findViewById(R.id.goBackButton);

        ArrayList<EventInfo> events = new ArrayList<>();

        // Set up the EventArrayAdapter
        eventArrayAdapter = new EventArrayAdapter(this, events, this::onEditEvent, this::onDeleteEvent);
        eventListView.setAdapter(eventArrayAdapter);

        fetchEvents();

        // Set up the Go Back button
        goBackButton.setOnClickListener(v -> finish()); // Close this activity and return to the previous screen
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Refresh the events list when returning to this activity
        fetchEvents();
    }

    private void fetchEvents() {
        EventFirebase.getAllEvents(new EventFirebase.EventListCallback() {
            @Override
            public void onSuccess(List<EventInfo> events) {
                eventArrayAdapter.clear();
                eventArrayAdapter.addAll(events);
                eventArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(BrowseEventsActivity.this, "Failed to load events: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onEditEvent(EventInfo event) {
        // Start the EditEventActivity with the event ID as an extra
        Intent intent = new Intent(BrowseEventsActivity.this, EditEventActivity.class);
        intent.putExtra("eventId", event.getEventID()); // Pass the event ID to the activity
        startActivity(intent);
    }

    private void onDeleteEvent(EventInfo event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    EventFirebase.deleteEvent(event.getEventID());
                    fetchEvents(); // Refresh event list after deletion
                    Toast.makeText(BrowseEventsActivity.this, "Event deleted successfully.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
