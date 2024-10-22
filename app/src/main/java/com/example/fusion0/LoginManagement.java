package com.example.fusion0;

import com.google.firebase.auth.FirebaseAuth;

public class LoginManagement {

    private FirebaseAuth auth;

    public LoginManagement() {
        auth = FirebaseAuth.getInstance();
    }

    // Return user login state
    public Boolean loggedIn() {
        return auth.getCurrentUser() != null;
    }

}
