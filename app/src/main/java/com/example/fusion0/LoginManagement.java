package com.example.fusion0;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginManagement {
    private static LoginManagement instance;
    private FirebaseAuth auth;

    // Initialize Firebase Auth
    public LoginManagement() {
        auth = FirebaseAuth.getInstance();
    }

    // Public method to provide the global instance of the class;
    public static synchronized LoginManagement getInstance() {
        if (instance == null) {
            instance = new LoginManagement();
        }
        return instance;
    }

    public void signInAnonymously(OnCompleteListener<AuthResult> listener) {
        auth.signInAnonymously().addOnCompleteListener(listener);
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void signOut() {
        auth.signOut();
    }

}
