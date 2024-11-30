package com.example.fusion0.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.fusion0.R;
import com.example.fusion0.adapters.EventArrayAdapter;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.EventInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * BrowseEventsFragment allows users to browse, edit, and delete events fetched from Firebase.
 * It displays a list of events using a ListView and provides options for managing the events.
 * Users can also navigate back to the previous screen using a back arrow.
 *
 * @author Derin Karas
 */
public class BrowseEventsFragment extends Fragment {

    private EventFirebase eventFirebase = new EventFirebase();
    private ListView eventListView;
    private EventArrayAdapter eventArrayAdapter;
    private ImageButton goBackButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_browse_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        eventListView = view.findViewById(R.id.eventListView);
        goBackButton = view.findViewById(R.id.goBackButton);

        // Initialize the list of events
        ArrayList<EventInfo> events = new ArrayList<>();

        // Set up the EventArrayAdapter
        eventArrayAdapter = new EventArrayAdapter(requireContext(), events, this::onEditEvent, this::onDeleteEvent);
        eventListView.setAdapter(eventArrayAdapter);

        // Fetch events from Firebase
        fetchEvents();

        // Set up the back button
        goBackButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });
    }

    @Override
    public void onResume() {
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
                Toast.makeText(requireContext(), "Failed to load events: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles the edit event action. Navigates to the EditEventFragment with the selected event's ID.
     *
     * @param event The event to be edited.
     */
    private void onEditEvent(EventInfo event) {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", event.getEventID());
        Navigation.findNavController(requireView()).navigate(R.id.action_browseEventsFragment_to_editEventFragment, bundle);
    }

    /**
     * Handles the delete event action. Prompts the user for confirmation before deleting the event.
     *
     * @param event The event to be deleted.
     */
    private void onDeleteEvent(EventInfo event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    eventFirebase.deleteEvent(event.getEventID());
                    fetchEvents();
                    Toast.makeText(requireContext(), "Event deleted successfully.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
