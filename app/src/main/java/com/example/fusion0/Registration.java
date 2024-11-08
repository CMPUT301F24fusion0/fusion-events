package com.example.fusion0;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

/**
 * This is the registration fragment that will be displayed when a user signs up for an waiting
 * for the first time. It will only be used once per user.
 */
public class Registration extends Fragment {
    EditText firstName, lastName, email, phoneNumber;
    UserFirestore firebase;
    Button register;

    /**
     * @author Sehej Brar
     * This method initializes the input fields and an instance of the Firebase class.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return view is the view
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.registration, container, false);
        firstName = view.findViewById(R.id.firstName);
        lastName = view.findViewById(R.id.lastName);
        email = view.findViewById(R.id.email);
        phoneNumber = view.findViewById(R.id.phone);
        register = view.findViewById(R.id.confirm);
        firebase = new UserFirestore();

        return view;
    }

    /**
     * @author Sehej Brar
     * If the user is already in the database (i.e. their account exists) then they are not allowed
     * to sign up again.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        register.setOnClickListener(v-> {
            String first = firstName.getText().toString().trim();
            String last = lastName.getText().toString().trim();
            String emails = email.getText().toString().trim();
            String phone = phoneNumber.getText().toString().trim();

            String dID = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            registration(dID, first, last, emails, phone);
            Bundle bundle = getArguments();
            if (bundle != null) {
                String eventId = bundle.getString("eventID");
                Intent intent = new Intent(getActivity(), ViewEventActivity.class);
                intent.putExtra("eventID", eventId);
                startActivity(intent);
            }
        });
    }

    /**
     * @author Sehej Brar
     * Checks if a user already exists, if they don't then a new account is created
     * @param dID device id
     * @param first first name
     * @param last last name
     * @param emails email
     * @param phone phone number
     */
    private void registration(String dID, String first, String last, String emails, String phone) {
        firebase.findUser(dID, new UserFirestore.Callback() {
            /**
             * @author Sehej Brar
             * This method checks to see if the same user already exists, if it doesn't then the new
             * user is able to create their account
             * @param user is the user we get back after we look to see if the same user is in the database
             */
            @Override
            public void onSuccess(UserInfo user) {
                UserInfo newUser;
                if (user != null) {
                    System.out.println("This user already exists.");
                } else {
                    if (!phone.isEmpty()) {
                        newUser = new UserInfo(new ArrayList<String>(), first, last, emails, phone, dID);
                    } else {
                        newUser = new UserInfo(new ArrayList<String>(), first, last, emails, dID);
                    }
                    firebase.addUser(newUser);
                }
            }

            /**
             * @author Sehej Brar
             * This method controls the error received
             * @param error the error message received
             */
            @Override
            public void onFailure(String error) {
                System.out.println(error);
            }
        });
    }

}
