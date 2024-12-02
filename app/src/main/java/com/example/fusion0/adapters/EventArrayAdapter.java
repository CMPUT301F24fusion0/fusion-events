package com.example.fusion0.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fusion0.R;
import com.example.fusion0.models.EventInfo;

import java.util.ArrayList;

/**
 * To show all events
 */
public class EventArrayAdapter extends ArrayAdapter<EventInfo> {

    private final EventEditCallback editCallback;
    private final EventDeleteCallback deleteCallback;

    /**
     * Event Initializer
     * @param context context
     * @param events event array
     * @param editCallback callback for editing
     * @param deleteCallback callback for deleting
     */
    public EventArrayAdapter(Context context, ArrayList<EventInfo> events, EventEditCallback editCallback, EventDeleteCallback deleteCallback) {
        super(context, 0, events);
        this.editCallback = editCallback;
        this.deleteCallback = deleteCallback;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.browse_event_item, parent, false);
        }

        EventInfo event = getItem(position);

        TextView eventName = view.findViewById(R.id.browseEventName);
        ImageButton editButton = view.findViewById(R.id.editEventButton);
        ImageButton deleteButton = view.findViewById(R.id.deleteEventButton);

        if (event != null) {
            eventName.setText(event.getEventName());
            editButton.setOnClickListener(v -> editCallback.onEdit(event));
            deleteButton.setOnClickListener(v -> deleteCallback.onDelete(event));
        }

        return view;
    }

    /**
     * Edit callback
     */
    public interface EventEditCallback {
        void onEdit(EventInfo event);
    }

    /**
     * Delete callback
     */
    public interface EventDeleteCallback {
        void onDelete(EventInfo event);
    }
}
