package com.example.fusion0.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.fusion0.R;

public class RegistrationPromptFragment extends Fragment {

    Button acceptButton;
    Button declineButton;

    public RegistrationPromptFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration_prompt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Context context = requireContext();

        acceptButton = view.findViewById(R.id.acceptButton);
        declineButton = view.findViewById(R.id.declineButton);

        acceptButton.setOnClickListener(v -> {
            Bundle bundle = getArguments();
            if (bundle != null) {
                String destination = bundle.getString("destination");
                if ("profile".equals(destination)) {
                    Navigation.findNavController(view).navigate(R.id.action_registrationPromptFragment_to_registrationFragment, bundle);
                } else if ("addEvent".equals(destination)) {
                    Navigation.findNavController(view).navigate(R.id.action_registrationPromptFragment_to_registrationFragment, bundle);
                }
            }
        });

        declineButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_registrationPromptFragment_to_mainFragment);
        });

    }
}