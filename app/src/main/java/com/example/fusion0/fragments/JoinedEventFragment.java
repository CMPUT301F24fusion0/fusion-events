package com.example.fusion0.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatImageView$InspectionCompanion;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.fusion0.R;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.UserInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.WriterException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class JoinedEventFragment extends Fragment {

    public UserFirestore userFirestore;
    private ImageButton backButton;
    private AppCompatImageView facilityButton;
    private TextView eventName, facility, description, registrationDate;
    private ImageView uploadedImageView;
    private TextView startDateText;
    private TextView endDateText;
    private TextView capacity, lotteryCapacity;
    private ImageView qrImage;
    private FloatingActionButton unjoinButton;

    private UserInfo user;
    private EventInfo event;
    public EventFirebase eventFirebase = new EventFirebase();

    public JoinedEventFragment() {
        // Required empty public constructor
    }

    /**
     * @author Simon Haile
     * Called when the activity is created. This method sets up the user interface,
     * retrieves the event ID from the intent, fetches the event details from Firestore,
     * and binds the event data to the UI elements.
     * @param savedInstanceState The saved instance state, or null if there is no previous state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_joined_event, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();

        backButton = view.findViewById(R.id.backButton);
        eventName = view.findViewById(R.id.EventName);
        description = view.findViewById(R.id.description);
        facility = view.findViewById(R.id.facilityName);
        uploadedImageView = view.findViewById(R.id.uploaded_image_view);
        startDateText = view.findViewById(R.id.start_date_text);
        endDateText = view.findViewById(R.id.end_date_text);
        registrationDate = view.findViewById(R.id.registration_date_text);
        capacity = view.findViewById(R.id.capacity);
        lotteryCapacity = view.findViewById(R.id.lotteryCapacity);
        qrImage = view.findViewById(R.id.qrImage);
        unjoinButton = view.findViewById(R.id.unjoin_button);
        facilityButton = view.findViewById(R.id.facility_view_button);

        backButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_joinedEventsFragment_to_favouriteFragment);
        });

        Bundle bundle = getArguments();
        String eventID = bundle.getString("eventID");
        String deviceID = bundle.getString("deviceID");

        facilityButton.setOnClickListener(v -> {
            Bundle joinedBundle = new Bundle();
            joinedBundle.putString("facilityID", event.getFacilityID());
            joinedBundle.putString("eventID", eventID);
            joinedBundle.putString("ID", "joinedEvent");
            Navigation.findNavController(view).navigate(R.id.action_joinedEventsFragment_to_viewFacilityFragment, joinedBundle);
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
            Log.e("JoinedEventActivity", "Error fetching user: " + eventID);

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
                        Toast.makeText(context, "Event Unavailable.", Toast.LENGTH_SHORT).show();

                    } else {
                        event = eventInfo;

                        eventName.setText(event.getEventName());
                        description.setText(event.getDescription());
                        facility.setText(event.getFacilityName());
                        capacity.setText(String.valueOf(event.getCapacity()));
                        lotteryCapacity.setText(event.getLotteryCapacity());

                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

                        startDateText.setText(dateFormat.format(event.getStartDate()));
                        endDateText.setText(dateFormat.format(event.getEndDate()));
                        registrationDate.setText(dateFormat.format(event.getRegistrationDate()));


                        String eventPoster = event.getEventPoster();
                        if (eventPoster != null && !eventPoster.isEmpty()) {
                            Glide.with(context)
                                    .load(eventPoster)
                                    .centerCrop()
                                    .into(uploadedImageView);
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
                    Toast.makeText(context, "Failed to load event details.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, "Invalid Event ID.", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigate(R.id.action_joinedEventsFragment_to_favouriteFragment);
        }

        unjoinButton.setOnClickListener(v ->{
            ArrayList<Map<String, String>> newWaitingList = event.removeUserFromWaitingList(deviceID, event.getWaitinglist());
            event.setWaitinglist(newWaitingList);

            eventFirebase.editEvent(event);


            ArrayList<String> userEvents =  user.getEvents();
            ArrayList<String> newEventsList = user.removeEventFromEventList(event.getEventID(), userEvents);
            user.setEvents(newEventsList);
            new UserFirestore().editUserEvents(user);

            eventFirebase.editEvent(event);
            Navigation.findNavController(view).navigate(R.id.action_joinedEventsFragment_to_favouriteFragment);
        });



    }
    
}