package com.example.fusion0;

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

import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WaitlistFragment extends Fragment {
    ImageButton backButton;
    TextView waitlistCapacityRatio, emptyTextView;
    ListView waitlistListView;
    EventFirebase firebase;
    List<UserInfo> users = new ArrayList<>();

    private int pendingRequests = 0;

    Waitlist waitlist;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.waitlist, container, false);

        backButton = view.findViewById(R.id.backButton);
        waitlistCapacityRatio = view.findViewById(R.id.ratio);
        waitlistListView = view.findViewById(R.id.waitinglistListView);
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
        } else {
            emptyTextView.setVisibility(View.GONE);
            waitlistListView.setVisibility(View.VISIBLE);
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

    }
}
