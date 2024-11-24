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

    /**
     * Required public constructor
     * @author Nimi Akinroye
     */
    public RegistrationPromptFragment() {
        // Required empty public constructor
    }

    /**
     * Creates the view
     * @author Nimi Akinroye
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Inflates the view
     * @author Nimi Akinroye
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration_prompt, container, false);
    }

    /**
     * If the user is not logged in and is trying to access something then they are prompted to log in
     * @author Nimi Akinroye
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
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

        declineButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_registrationPromptFragment_to_mainFragment));

    }
}