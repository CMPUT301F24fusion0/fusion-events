package com.example.fusion0.fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.fusion0.activities.ViewEventActivity;
import com.example.fusion0.adapters.ProfileListAdapter;
import com.example.fusion0.R;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.helpers.Waitlist;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.UserInfo;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WaitlistFragment extends Fragment {
    ImageButton backButton;
    TextView waitlistCapacityRatio, emptyTextView;
    ListView waitlistListView;
    Button lotteryButton;
    EventFirebase firebase;
    EventInfo event;
    List<UserInfo> users = new ArrayList<>();

    private int pendingRequests = 0;

    private Waitlist waitlist;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.waitlist, container, false);

        backButton = view.findViewById(R.id.backButton);
        waitlistCapacityRatio = view.findViewById(R.id.ratio);
        waitlistListView = view.findViewById(R.id.waitinglistListView);
        lotteryButton = view.findViewById(R.id.generate_lottery_button);
        emptyTextView = view.findViewById(R.id.emptyText);
        firebase = new EventFirebase();

        Bundle bundle = getArguments();

        if (bundle != null) {
            ArrayList<Map<String, String>> waitingList = (ArrayList<Map<String, String>>) bundle.getSerializable("waitingListData");

            if (waitingList != null && !waitingList.isEmpty()) {
                pendingRequests = waitingList.size();

                for (Map<String, String> entry : waitingList) {
                    String deviceId = entry.get("did");
                    if (deviceId != null) {
                        Log.e(TAG, "did " + deviceId);

                        UserFirestore.findUser(deviceId, new UserFirestore.Callback() {
                            @Override
                            public void onSuccess(UserInfo user) {
                                users.add(user);
                                Log.e(TAG, "user " + user);

                                pendingRequests--;

                                if (pendingRequests == 0) {
                                    updateUI(bundle);
                                }
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e("UserFirestore", "Error: " + error);

                                pendingRequests--;

                                if (pendingRequests == 0) {
                                    updateUI(bundle);
                                }
                            }
                        });
                    }
                }
            } else {
                updateUI(bundle);
            }
        }

        return view;
    }

    private void updateUI(Bundle bundle) {
        String eventCapacity = bundle != null ? bundle.getString("eventCapacity", "0") : "0";
        String ratio = users.size() + "/" + eventCapacity;
        waitlistCapacityRatio.setText(ratio);

        ProfileListAdapter adapter = new ProfileListAdapter(getContext(), users);
        waitlistListView.setAdapter(adapter);

        if (users.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            waitlistListView.setVisibility(View.GONE);
            lotteryButton.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            waitlistListView.setVisibility(View.VISIBLE);
            lotteryButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        waitlist = (Waitlist) bundle.getSerializable("waitlist");


        backButton.setOnClickListener(v -> {
            if (bundle != null) {
                Intent intent = new Intent(getActivity(), ViewEventActivity.class);
                intent.putExtra("eventID", bundle.getString("eventID"));
                startActivity(intent);
            }
        });

        lotteryButton.setOnClickListener(v -> {
            EventFirebase.findEvent(bundle.getString("eventID"), new EventFirebase.EventCallback() {
                @Override
                public void onSuccess(EventInfo eventInfo) throws WriterException {
                    event = eventInfo;

                    AlertDialog.Builder builder = new AlertDialog.Builder(WaitlistFragment.this.getContext());
                    builder.setTitle("Enter Lottery Capacity");

                    EditText input = new EditText(WaitlistFragment.this.getContext());
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    builder.setView(input);

                    builder.setPositiveButton("OK", (dialog, which) -> {
                        String lotteryCapacity = input.getText().toString().trim();

                        if (!lotteryCapacity.isEmpty()) {
                            event.setLotteryCapacity(lotteryCapacity);
                            EventFirebase.editEvent(event);
                        } else {
                            Toast.makeText(WaitlistFragment.this.getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.setNegativeButton("Cancel", (dialog, which) -> {
                    });

                    builder.show();


                    if (!(event.getLotteryCapacity().equals("0"))) {
                        ViewEventActivity.waitlist.conductLottery(event.getEventID(), Integer.parseInt(event.getLotteryCapacity()));

                        waitlist.getChosen(event.getEventID(), chosen -> {
                            if (chosen.isEmpty()) {
                                Toast.makeText(WaitlistFragment.this.getContext(), "Chosen entrants list is empty.", Toast.LENGTH_SHORT).show();
                            }else{
                                ChosenEntrants chosenEntrants = new ChosenEntrants();

                                Bundle bundle = new Bundle();
                                bundle.putSerializable("chosenEntrantsData", chosen);
                                bundle.putString("eventID", event.getEventID());
                                bundle.putSerializable("waitlist", waitlist);
                                chosenEntrants.setArguments(bundle);

                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.event_view, chosenEntrants)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        });
                    }else {
                        Toast.makeText(WaitlistFragment.this.getContext(), "Please enter a valid lottery capacity greater than 0", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String error) {

                }
            });

        });
    }
}
