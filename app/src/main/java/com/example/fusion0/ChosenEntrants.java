package com.example.fusion0;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChosenEntrants extends Fragment {
    ImageButton backButton;
    TextView chosenEntrantsCapacityRatio;
    ListView chosenEntrantsListView;
    Button fillLotteryButton, removeEntrantsButton, finalRemoveButton, cancelButton;
    EventFirebase firebase;
    String lotteryCapacity;
    String eventID;
    Waitlist waitlist;
    List<UserInfo> chosenUsers = new ArrayList<>();
    private int pendingRequests = 0;
    Integer spotsRemaining;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chosen_entrants, container, false);

        backButton = view.findViewById(R.id.backButton);
        chosenEntrantsCapacityRatio = view.findViewById(R.id.ratio);
        chosenEntrantsListView = view.findViewById(R.id.chosenEntrantsListView);
        fillLotteryButton = view.findViewById(R.id.fill_lottery_button);
        removeEntrantsButton = view.findViewById(R.id.remove_button);
        finalRemoveButton = view.findViewById(R.id.remove_second_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        firebase = new EventFirebase();

        Bundle bundle = getArguments();
        if (bundle != null) {
            eventID = bundle.getString("eventID");
            waitlist = (Waitlist) bundle.getSerializable("waitlist");

            ArrayList<Map<String, String>> chosenEntrants = (ArrayList<Map<String, String>>) bundle.getSerializable("chosenEntrantsData");

            if (chosenEntrants != null && !chosenEntrants.isEmpty()) {
                pendingRequests = chosenEntrants.size();

                for (Map<String, String> entry : chosenEntrants) {
                    String deviceId = entry.get("did");
                    if (deviceId != null) {
                        Log.e(TAG, "did " + deviceId);

                        UserFirestore.findUser(deviceId, new UserFirestore.Callback() {
                            @Override
                            public void onSuccess(UserInfo user) {
                                chosenUsers.add(user);
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
        spotsRemaining = chosenUsers.size();

        return view;
    }

    private void updateUI(Bundle bundle) {
        waitlist = (Waitlist) bundle.getSerializable("waitlist");
        eventID = bundle.getString("eventID");


        lotteryCapacity = bundle.getString("lotteryCapacity");
        String ratio = chosenUsers.size() + "/" + lotteryCapacity;
        chosenEntrantsCapacityRatio.setText(ratio);

        ProfileListAdapter adapter = new ProfileListAdapter(getContext(), chosenUsers);
        chosenEntrantsListView.setAdapter(adapter);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        fillLotteryButton.setOnClickListener(v -> {
            int numToSelect = Integer.parseInt(lotteryCapacity) - spotsRemaining;
            waitlist.conductLottery(eventID, numToSelect);
        });

        removeEntrantsButton.setOnClickListener(v->{
            removeEntrantsButton.setVisibility(View.GONE);
            fillLotteryButton.setVisibility(View.GONE);

            finalRemoveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        });

        finalRemoveButton.setOnClickListener(v ->{
            SparseBooleanArray selectedItems = chosenEntrantsListView.getCheckedItemPositions();
            List<String> selectedUserIds = new ArrayList<>();

            for (int i = 0; i < selectedItems.size(); i++) {
                int position = selectedItems.keyAt(i);
                if (selectedItems.valueAt(i)) {
                    UserInfo selectedUser = (UserInfo) chosenEntrantsListView.getItemAtPosition(position);
                    selectedUserIds.add(selectedUser.getDeviceID());
                    spotsRemaining++;
                }
            }

            if (selectedUserIds.isEmpty()) {
                Toast.makeText(getContext(), "No entrants selected to remove", Toast.LENGTH_SHORT).show();
                return;
            }

            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Confirm Removal")
                    .setMessage("Are you sure you want to remove the selected entrants?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        for (String userId : selectedUserIds) {
                            waitlist.organizerCancel(eventID, userId);
                            spotsRemaining++;
                        }

                        Toast.makeText(getContext(), "Entrants removed successfully.", Toast.LENGTH_SHORT).show();

                        removeEntrantsButton.setVisibility(View.VISIBLE);
                        fillLotteryButton.setVisibility(View.VISIBLE);
                        finalRemoveButton.setVisibility(View.GONE);
                        cancelButton.setVisibility(View.GONE);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });


        cancelButton.setOnClickListener(v -> {
            chosenEntrantsListView.clearChoices();
            removeEntrantsButton.setVisibility(View.VISIBLE);
            fillLotteryButton.setVisibility(View.VISIBLE);
            finalRemoveButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
        });
    }
}

