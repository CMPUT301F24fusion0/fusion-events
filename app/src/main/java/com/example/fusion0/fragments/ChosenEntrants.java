package com.example.fusion0.fragments;

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

import com.example.fusion0.activities.MainActivity;
import com.example.fusion0.adapters.ProfileListAdapter;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.R;
import com.example.fusion0.models.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class ChosenEntrants extends Fragment {
    ImageButton backButton;
    TextView chosenEntrantsCapacityRatio;
    ListView chosenEntrantsListView;
    Button lotteryButton;
    EventFirebase firebase;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chosen_entrants, container, false);

        backButton = view.findViewById(R.id.backButton);
        chosenEntrantsCapacityRatio = view.findViewById(R.id.ratio);
        chosenEntrantsListView = view.findViewById(R.id.chosenEntrantsListView);
        lotteryButton = view.findViewById(R.id.fill_lottery_button);
        firebase = new EventFirebase();

        List<UserInfo> chosenEntrants = new ArrayList<>();

        ProfileListAdapter adapter = new ProfileListAdapter(getContext(), chosenEntrants);
        chosenEntrantsListView.setAdapter(adapter);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        lotteryButton.setOnClickListener(v -> {
        });
    }
}

