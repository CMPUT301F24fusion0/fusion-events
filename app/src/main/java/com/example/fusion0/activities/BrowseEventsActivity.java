package com.example.fusion0.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fusion0.R;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.EventInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * BrowseEventsActivity allows users to browse, edit, and delete events fetched from Firebase.
 * It displays a list of events using a ListView and provides options for managing the events.
 * Users can also navigate back to the previous screen using a back arrow at the top left.
 *
 * @author Derin Karas
 */
public class BrowseEventsActivity extends AppCompatActivity {
    private EventFirebase eventFirebase = new EventFirebase();
    /**
     * ListView to display the list of events.
     */
    private ListView eventListView;

    /**
     * Adapter for managing and displaying event data in the ListView.
     */
    private EventArrayAdapter eventArrayAdapter;

    /**
     * Back button for navigating to the previous activity.
     */
    private ImageButton goBackButton;

    /**
     * Called when the activity is created. Initializes the layout, views, and data.
     *
     * @param savedInstanceState The saved instance state from a previous configuration change.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events);

        // Initialize views
        eventListView = findViewById(R.id.eventListView);
        goBackButton = findViewById(R.id.goBackButton);

        // Initialize the list of events
        ArrayList<EventInfo> events = new ArrayList<>();

        // Set up the EventArrayAdapter
        eventArrayAdapter = new EventArrayAdapter(this, events, this::onEditEvent, this::onDeleteEvent);
        eventListView.setAdapter(eventArrayAdapter);

        // Fetch events from Firebase
        fetchEvents();

        // Set up the back button to finish the activity
        goBackButton.setOnClickListener(v -> finish());
    }

    /**
     * Called when the activity is resumed. Refreshes the event list.
     */
    @Override
    protected void onResume() {
        super.onResume();
        fetchEvents();
    }

    /**
     * Fetches the list of events from Firebase and updates the ListView.
     */
    private void fetchEvents() {
        eventFirebase.getAllEvents(new EventFirebase.EventListCallback() {
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

    /**
     * Handles the edit event action. Starts the EditEventActivity with the selected event's ID.
     *
     * @param event The event to be edited.
     */
    private void onEditEvent(EventInfo event) {
        Intent intent = new Intent(BrowseEventsActivity.this, EditEventActivity.class);
        intent.putExtra("eventId", event.getEventID());
        startActivity(intent);
    }

    /**
     * Handles the delete event action. Prompts the user for confirmation before deleting the event.
     *
     * @param event The event to be deleted.
     */
    private void onDeleteEvent(EventInfo event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    eventFirebase.deleteEvent(event.getEventID());
                    fetchEvents();
                    Toast.makeText(BrowseEventsActivity.this, "Event deleted successfully.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
