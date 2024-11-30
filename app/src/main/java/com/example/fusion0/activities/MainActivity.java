package com.example.fusion0.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.fusion0.R;
import com.example.fusion0.fragments.AddEventFragment;
import com.example.fusion0.fragments.ChosenEntrantsFragment;
import com.example.fusion0.helpers.Waitlist;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;

/**
 * The MainActivity serves as the main entry point for the app. It manages the login state
 * and directs users to the Profile page, initializing Firebase and LoginManagement to handle user sessions.
 */
public class MainActivity extends AppCompatActivity {
        private Waitlist waitlist = new Waitlist();

    /**
     * Initializes the MainActivity and manages user session and state.
     * Sets up Firebase, handles login state, and initializes the profile button to access the ProfileFragment.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EdgeToEdge.enable(this);

        FirebaseApp.initializeApp(this);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();

                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                Timestamp registrationDeadline = document.getTimestamp("registrationDate");

                                if (registrationDeadline != null) {
                                    Date now = new Date();
                                    Calendar calNow = Calendar.getInstance();
                                    calNow.setTime(now);
                                    calNow.set(Calendar.HOUR_OF_DAY, 0);
                                    calNow.set(Calendar.MINUTE, 0);
                                    calNow.set(Calendar.SECOND, 0);
                                    calNow.set(Calendar.MILLISECOND, 0);

                                    Calendar calDeadline = Calendar.getInstance();
                                    calDeadline.setTime(registrationDeadline.toDate());
                                    calDeadline.set(Calendar.HOUR_OF_DAY, 0);
                                    calDeadline.set(Calendar.MINUTE, 0);
                                    calDeadline.set(Calendar.SECOND, 0);
                                    calDeadline.set(Calendar.MILLISECOND, 0);
                                    calDeadline.add(Calendar.DAY_OF_MONTH, 1);
                                    if (calNow.after(calDeadline) && !document.getBoolean("lotteryConducted")) {
                                        String eventId = document.getId();
                                        runLottery(eventId, document);
                                        document.getReference().update("lotteryConducted", true);
                                    }
                                } else {
                                    Log.e("FirestoreError", "RegistrationFragment deadline not found for event: " + document.getId());
                                }
                            }
                        } else {
                            Log.e("FirestoreError", "QuerySnapshot is null.");
                        }
                    } else {
                        Log.e("FirestoreError", "Error getting events", task.getException());
                    }
                });
    }

    /**
     * Starting the lottery function and send notifications
     * @param eventId event id
     * @param eventDoc the document for the event
     */
    private void runLottery(String eventId, DocumentSnapshot eventDoc) {
        if (eventDoc != null) {
            if (!eventDoc.getString("lotteryCapacity").equals("0")) {
                waitlist.allNotification(eventId, "Lottery Starting",
                        "The lottery is not starting. Be on the look out for the results!", "0");
                waitlist.conductLottery(eventId, Integer.parseInt(eventDoc.getString("lotteryCapacity")));
                waitlist.chosenNotification(eventId, "Winner!",
                        "Congratulations, you have won the lottery! Please accept the invitation to confirm your spot.", "1");
                waitlist.loseNotification(eventId, "Lottery Results", "Unfortunately, you have lost the lottery. You may still receive an invite if someone declines their invitation.", "0");

                waitlist.getChosen(eventId, chosen -> {
                    if (!chosen.isEmpty()) {
                        ChosenEntrantsFragment chosenEntrants = new ChosenEntrantsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("chosenEntrantsData", chosen);
                        bundle.putString("eventID", eventId);
                        bundle.putSerializable("fragment_waitlist", waitlist);
                        chosenEntrants.setArguments(bundle);

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.event_view, chosenEntrants)
                                .addToBackStack(null)
                                .commit();
                    }
                });

            } else {
                Log.d("Lottery", "Lottery capacity is 0, skipping lottery.");
            }
        } else {
            Log.e("Lottery", "Event document is null.");
        }
    }
}