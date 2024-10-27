package com.example.fusion0;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class Registration extends Fragment {
    EditText firstName, lastName, email, phoneNumber;
    Firebase firebase;

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
        firebase = new Firebase();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String first = firstName.getText().toString();
        String last = lastName.getText().toString();
        String emails = email.getText().toString();
        String phone = phoneNumber.getText().toString();

        firebase.findUser(emails, new Firebase.Callback() {
            @Override
            public void onSuccess(UserInfo user) {
                if (user == null) {
                    System.out.println("This user already exists.");
                } else {
                    firebase.addUser(user);
                }
            }

            @Override
            public void onFailure(String error) {
                System.out.println(error);
            }
        });
    }
}
