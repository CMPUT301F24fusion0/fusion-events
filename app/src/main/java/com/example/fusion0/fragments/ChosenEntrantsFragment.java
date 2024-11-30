package com.example.fusion0.fragments;

import static android.content.ContentValues.TAG;

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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.fusion0.R;
import com.example.fusion0.adapters.ProfileListAdapter;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.helpers.Waitlist;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.UserInfo;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChosenEntrantsFragment extends Fragment {
    ImageButton backButton;
    TextView chosenEntrantsCapacityRatio, fullCapacityTextView, emptyTextView;
    ListView chosenEntrantsListView;
    Button fillLotteryButton, removeEntrantsButton, secondRemoveButton, cancelButton;
    EventFirebase firebase;
    EventInfo event = null;
    List<UserInfo> users = new ArrayList<>();

    private int pendingRequests = 0;

    private Waitlist waitlist;

    private ProfileListAdapter adapter;
    ArrayList<Map<String, String>> chosenList;

    private boolean isSelectionMode = false;

    EventFirebase eventFirebase = new EventFirebase();




    /**
     * Sets up the variables required in this class and uses the adapter to show selected entrants
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chosen_entrants, container, false);

        backButton = view.findViewById(R.id.backButton);
        chosenEntrantsCapacityRatio = view.findViewById(R.id.ratio);
        chosenEntrantsListView = view.findViewById(R.id.chosenEntrantsListView);
        fillLotteryButton = view.findViewById(R.id.fill_lottery_button);
        removeEntrantsButton = view.findViewById(R.id.remove_button);
        secondRemoveButton = view.findViewById(R.id.remove_second_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        fullCapacityTextView = view.findViewById(R.id.full_capacity_text_view);
        emptyTextView = view.findViewById(R.id.emptyText);

        Bundle bundle = getArguments();

        if (bundle != null) {
            waitlist = (Waitlist) bundle.getSerializable("fragment_waitlist");

            chosenList = (ArrayList<Map<String, String>>) bundle.getSerializable("chosenEntrantsData");

            if (chosenList != null && !chosenList.isEmpty()) {
                pendingRequests = chosenList.size();

                for (Map<String, String> entry : chosenList) {
                    String deviceId = entry.get("did");
                    if (deviceId != null) {
                        Log.e(TAG, "did " + deviceId);

                        new UserFirestore().findUser(deviceId, new UserFirestore.Callback() {
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
        String lotteryCapacity = bundle != null ? bundle.getString("lotteryCapacity", "0") : "0";
        String ratio = users.size() + "/" + lotteryCapacity;
        chosenEntrantsCapacityRatio.setText(ratio);

        if (users.size() == Integer.parseInt(lotteryCapacity)){
            fullCapacityTextView.setVisibility(View.VISIBLE);
            fillLotteryButton.setVisibility(View.GONE);
        }

        if (users.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            chosenEntrantsListView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            chosenEntrantsListView.setVisibility(View.VISIBLE);
        }

        adapter = new ProfileListAdapter(getContext(), users);
        chosenEntrantsListView.setAdapter(adapter);
    }

    /**
     * Establishes the back button
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();

        backButton.setOnClickListener(v -> {
            if (bundle != null) {
                bundle.putString("eventID", bundle.getString("eventID"));
                Navigation.findNavController(view).navigate(R.id.action_chosenEntrantsFragment_to_viewEventFragment, bundle);
            }
        });

        fillLotteryButton.setOnClickListener(v -> {
            int numToSelect = Integer.parseInt(bundle.getString("lotteryCapacity")) - users.size()  ;
            waitlist.conductLottery(bundle.getString("eventID"),numToSelect);

            waitlist.getChosen(bundle.getString("eventID"), chosen -> {
                ArrayList<Map<String, String>> fullChosenEntrants = new ArrayList<>();

                if(event == null){
                    eventFirebase.findEvent(bundle.getString("eventID"), new EventFirebase.EventCallback() {
                        @Override
                        public void onSuccess(EventInfo eventInfo) throws WriterException {
                            event = eventInfo;

                            if (event.getWaitinglist() != null && !event.getWaitinglist().isEmpty()) {
                                for (Map<String, String> user :  event.getWaitinglist()) {
                                    if (chosen.contains(user.get("did")) && "chosen".equals(user.get("status"))) {
                                        fullChosenEntrants.add(user);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(String error) {

                        }
                    });
                }

                chosenList = fullChosenEntrants;
            });
        });

        removeEntrantsButton.setOnClickListener(v -> {
            chosenEntrantsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            fillLotteryButton.setVisibility(View.GONE);
            fullCapacityTextView.setVisibility(View.GONE);
            removeEntrantsButton.setVisibility(View.GONE);
            secondRemoveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);

            isSelectionMode = true;
            adapter.setSelectionMode(isSelectionMode);
        });

        secondRemoveButton.setOnClickListener(v -> {
            SparseBooleanArray checkedItems = adapter.getSelectedItems();
            List<UserInfo> usersToRemove = new ArrayList<>();

            for (int i = 0; i < checkedItems.size(); i++) {
                int position = checkedItems.keyAt(i);
                if (checkedItems.valueAt(i)) {
                    waitlist.organizerCancel(bundle.getString("eventID"), users.get(position).getDeviceID());
                    usersToRemove.add(users.get(position));
                }
            }

            users.removeAll(usersToRemove);
            adapter.notifyDataSetChanged();

            chosenEntrantsListView.clearChoices();
            adapter.clearSelections();

            // Update UI visibility
            if (users.size() == Integer.parseInt(bundle.getString("lotteryCapacity"))) {
                fullCapacityTextView.setVisibility(View.VISIBLE);
                fillLotteryButton.setVisibility(View.GONE);
            } else{
                fullCapacityTextView.setVisibility(View.GONE);
                fillLotteryButton.setVisibility(View.VISIBLE);
            }
            cancelButton.setVisibility(View.GONE);
            secondRemoveButton.setVisibility(View.GONE);
            removeEntrantsButton.setVisibility(View.VISIBLE);

            isSelectionMode = false;  // Disable selection mode
            adapter.setSelectionMode(isSelectionMode);  // Notify adapter
        });



        cancelButton.setOnClickListener(v->{
            isSelectionMode = false;
            adapter.setSelectionMode(isSelectionMode);
            if (users.size() == Integer.parseInt(bundle.getString("lotteryCapacity"))){
                fullCapacityTextView.setVisibility(View.VISIBLE);
                fillLotteryButton.setVisibility(View.GONE);
            } else{
                fullCapacityTextView.setVisibility(View.GONE);
                fillLotteryButton.setVisibility(View.VISIBLE);
            }

            chosenEntrantsListView.clearChoices();
            adapter.clearSelections();
            adapter.notifyDataSetChanged();
            cancelButton.setVisibility(View.GONE);
            secondRemoveButton.setVisibility(View.GONE);
            removeEntrantsButton.setVisibility(View.VISIBLE);
        });
    }
}

