package com.example.fusion0;

import com.google.firebase.auth.FirebaseAuth;


/**
 * LoginManagement class manages user authentication state.
 */
public class LoginManagement {

    // Firebase Authentication instance
    private FirebaseAuth auth;

    /**
     * Constructor initializes Firebase Authentication instance.
     */
  
    public LoginManagement() {
        auth = FirebaseAuth.getInstance();
    }


    /**
     * Checks if the user is currently logged in.
     *
     * @return true if the user is logged in, false otherwise
     */
    public Boolean loggedIn() {
        return auth.getCurrentUser() != null;
    }
}

