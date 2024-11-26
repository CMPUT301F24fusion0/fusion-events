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

import com.example.fusion0.activities.ViewEventActivity;
import com.example.fusion0.adapters.ProfileListAdapter;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.helpers.Waitlist;
import com.example.fusion0.models.UserInfo;
import com.example.fusion0.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CancelledEntrantsFragment extends Fragment {
    ImageButton backButton;
    ListView cancelledEntrantsListView;
    TextView emptyTextView;
    EventFirebase firebase;
    List<UserInfo> users = new ArrayList<>();
    Waitlist waitlist;

    private int pendingRequests = 0;

    /**
     * Creates the list for cancelled entrants
     * @author Simon Haile
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the View
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cancelled_entrants, container, false);

        backButton = view.findViewById(R.id.backButton);
        cancelledEntrantsListView = view.findViewById(R.id.cancelledEntrantsListView);
        emptyTextView = view.findViewById(R.id.emptyText);
        firebase = new EventFirebase();


        Bundle bundle = getArguments();

        if (bundle != null) {
            waitlist = (Waitlist) bundle.getSerializable("fragment_waitlist");

            ArrayList<Map<String, String>> cancelledList = (ArrayList<Map<String, String>>) bundle.getSerializable("cancelledEntrantsData");

            if (cancelledList != null && !cancelledList.isEmpty()) {
                pendingRequests = cancelledList.size();

                for (Map<String, String> entry : cancelledList) {
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

    /**
     * Updates the UI to allow organizers to see the cancelled entrants
     * @param bundle contains information
     */
    private void updateUI(Bundle bundle) {
        ProfileListAdapter adapter = new ProfileListAdapter(getContext(), users);
        cancelledEntrantsListView.setAdapter(adapter);

        if (users.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            cancelledEntrantsListView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            cancelledEntrantsListView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Establish the back button
     * @author Simon Haile
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
                Intent intent = new Intent(getActivity(), ViewEventActivity.class);
                intent.putExtra("eventID", bundle.getString("eventID"));
                startActivity(intent);
            }
        });
    }
}

