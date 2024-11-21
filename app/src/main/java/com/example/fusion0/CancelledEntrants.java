package com.example.fusion0;

import android.content.Intent;
import android.os.Bundle;
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

public class CancelledEntrants extends Fragment {
    ImageButton backButton;
    ListView cancelledEntrantsListView;
    EventFirebase firebase;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cancelled_entrants, container, false);

        backButton = view.findViewById(R.id.backButton);
        cancelledEntrantsListView = view.findViewById(R.id.cancelledEntrantsListView);
        firebase = new EventFirebase();

        List<UserInfo> chosenEntrants = new ArrayList<>();

        ProfileListAdapter adapter = new ProfileListAdapter(getContext(), chosenEntrants);
        cancelledEntrantsListView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });
    }
}

