package com.example.fusion0;

public class UserInfo {
    String firstName, lastName, email, phoneNumber;

    public UserInfo(String first, String last, String email, String phoneNumber) {
        this.firstName = first;
        this.lastName = last;
        this.email = email;
        this.phoneNumber = parsed(phoneNumber);
    }

    public UserInfo(String first, String last, String email) {
        this.firstName = first;
        this.lastName = last;
        this.email = email;
    }
}
