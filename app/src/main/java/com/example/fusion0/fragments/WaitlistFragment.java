package com.example.fusion0.fragments;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.fusion0.adapters.ProfileListAdapter;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.helpers.Waitlist;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.UserInfo;
import com.example.fusion0.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaitlistFragment extends Fragment {
    ImageButton backButton;
    TextView waitlistCapacityRatio, emptyTextView;
    ListView waitlistListView;
    EventFirebase firebase;
    EventInfo event;
    List<UserInfo> users = new ArrayList<>();

    private int pendingRequests = 0;

    private Waitlist waitlist;

    /**
     * Find the user on the waiting list
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return
     * @author Simon Haile
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waitlist, container, false);

        backButton = view.findViewById(R.id.backButton);
        waitlistCapacityRatio = view.findViewById(R.id.ratio);
        waitlistListView = view.findViewById(R.id.waitinglistListView);
        emptyTextView = view.findViewById(R.id.emptyText);
        firebase = new EventFirebase();

        Bundle bundle = getArguments();


        if (bundle != null) {
            ArrayList<Map<String, String>> waitingList = (ArrayList<Map<String, String>>) bundle.getSerializable("waitingListData");
            Log.e(TAG, "user " + waitingList);

            if (waitingList != null && !waitingList.isEmpty()) {
                pendingRequests = waitingList.size();

                for (Map<String, String> entry : waitingList) {
                    String deviceId = entry.get("did");
                    if (deviceId != null) {

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

    /**
     * Update the waiting list fragment for organizers
     *
     * @param bundle contains the updated information
     */
    private void updateUI(Bundle bundle) {
        String eventCapacity = bundle != null ? bundle.getString("eventCapacity", "0") : "0";
        String ratio = users.size() + "/" + eventCapacity;
        waitlistCapacityRatio.setText(ratio);

        ProfileListAdapter adapter = new ProfileListAdapter(getContext(), users);
        waitlistListView.setAdapter(adapter);

        if (users.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            waitlistListView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            waitlistListView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Establish the back and lottery button
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        waitlist = (Waitlist) bundle.getSerializable("fragment_waitlist");


        backButton.setOnClickListener(v -> {
            Bundle newBundle = new Bundle();
            newBundle.putString("eventID", bundle.getString("eventID"));

            Navigation.findNavController(view).navigate(R.id.action_waitlistFragment_to_viewEventFragment,newBundle);

        });

    }
}