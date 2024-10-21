package com.example.fusion0;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private TextView textField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize firebase in app
        FirebaseApp.initializeApp(this);

        LoginManagement loginManagement = LoginManagement.getInstance();

        if (loginManagement.getCurrentUser() == null) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        } else {
            TextView textField = findViewById(R.id.textField);
            textField.setText("Hello World");
        }


    }
}