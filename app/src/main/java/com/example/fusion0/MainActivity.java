package com.example.fusion0;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    EditText firstName, lastName, email, phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.registration); // Set the layout for the activity

        // Initialize EditText fields
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        phoneNumber = findViewById(R.id.phone);

        Button confirm = findViewById(R.id.confirm);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRegisterButtonClick(view);
            }
        });
    }

    public void onRegisterButtonClick(View view) {
        FirebaseApp.initializeApp(this);
        // This method should be triggered by a button click
        String first = firstName.getText().toString();
        String last = lastName.getText().toString();
        String emails = email.getText().toString();
        String phone = phoneNumber.getText().toString();

        // Assuming Firebase is properly initialized
        new Firebase().addUser(new UserInfo(first, last, emails, phone));
    }
}
