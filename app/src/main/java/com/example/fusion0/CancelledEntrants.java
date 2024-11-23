package com.example.fusion0;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CancelledEntrants extends Fragment {
    ImageButton backButton;
    ListView cancelledEntrantsListView;
    TextView emptyTextView;
    EventFirebase firebase;
    List<UserInfo> users = new ArrayList<>();

    private int pendingRequests = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cancelled_entrants, container, false);

        backButton = view.findViewById(R.id.backButton);
        cancelledEntrantsListView = view.findViewById(R.id.cancelledEntrantsListView);
        emptyTextView = view.findViewById(R.id.emptyText);
        firebase = new EventFirebase();


        Bundle bundle = getArguments();

        if (bundle != null) {
            ArrayList<Map<String, String>> cancelledEntrants = (ArrayList<Map<String, String>>) bundle.getSerializable("cancelledEntrantsData");

            if (cancelledEntrants != null && !cancelledEntrants.isEmpty()) {
                pendingRequests = cancelledEntrants.size();

                for (Map<String, String> entry : cancelledEntrants) {
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

